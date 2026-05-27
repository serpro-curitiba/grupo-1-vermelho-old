package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.Endereco;
import br.gov.sifap.beneficiarios.domain.EstadoCivil;
import br.gov.sifap.beneficiarios.domain.Sexo;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO de entrada do CadastrarBeneficiarioUseCase. */
public record NovoBeneficiarioCommand(
        String cpf,
        String nome,
        int regiao,
        BigDecimal rendaMensal,
        int idade,
        String programaCodigo,
        String nomeMae,
        String nomePai,
        LocalDate dtNascimento,
        Sexo sexo,
        EstadoCivil estCivil,
        String rgNumero,
        String rgOrgao,
        String rgUf,
        Endereco endereco,
        String telFixo,
        String telCelular,
        String email,
        String nis,
        LocalDate dtInicioBenef,
        String biometria,        // S/N/P
        String codElegibilidade  // ex: "RDIA"
) {}
