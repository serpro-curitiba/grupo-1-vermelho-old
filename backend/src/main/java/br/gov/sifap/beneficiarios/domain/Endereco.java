package br.gov.sifap.beneficiarios.domain;

/** Endereço de correspondência — embeddable na entidade JPA. */
public record Endereco(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String uf,
        String cep) {}
