package br.gov.sifap.pagamentos.infrastructure;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PagamentosConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
