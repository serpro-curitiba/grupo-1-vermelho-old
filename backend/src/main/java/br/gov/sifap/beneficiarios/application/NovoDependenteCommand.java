package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.Parentesco;
import br.gov.sifap.beneficiarios.domain.Sexo;
import java.time.LocalDate;

public record NovoDependenteCommand(
        String nome,
        int idade,
        String cpf,
        LocalDate dtNascimento,
        Parentesco parentesco,
        String documento,
        Sexo sexo,
        String indDeficiencia
) {}
