package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.StatusBeneficiario;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AlteracaoBeneficiarioCommand(
        String nome,
        Integer regiao,
        BigDecimal rendaMensal,
        LocalDate dtNascimento,
        String email,
        String telCelular,
        String uf,
        StatusBeneficiario status,
        String motSituacao
) {}
