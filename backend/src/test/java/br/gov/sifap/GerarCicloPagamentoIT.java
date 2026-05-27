package br.gov.sifap;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integração end-to-end (HTTP → DB real) — Testcontainers PostgreSQL 16.
 *
 * <p>Cobre US-001 (REQ-PAY-001) e US-002 (REQ-PAY-002).</p>
 */
@SpringBootTest
@Testcontainers
class GerarCicloPagamentoIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sifap").withUsername("sifap").withPassword("sifap_test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private WebApplicationContext ctx;
    @Autowired private ObjectMapper mapper;

    @Test
    void US001_geraCiclo_eUS002_bloqueiaDuplicado() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        String body = """
                {"competencia":"202504"}
                """;

        // US-001 — geração inicial
        MvcResult first = mvc.perform(post("/api/v1/ciclos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.cicloId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CALCULADO"))
                .andExpect(jsonPath("$.totalPagamentos").value(4))
                .andReturn();

        JsonNode node = mapper.readTree(first.getResponse().getContentAsString());
        assertThat(node.get("valorTotal").decimalValue().signum()).isPositive();

        // US-002 — duplicidade bloqueada (REQ-PAY-002)
        mvc.perform(post("/api/v1/ciclos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Ciclo duplicado para a competência"));
    }
}
