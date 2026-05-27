package br.gov.sifap.beneficiarios.domain;

import java.time.LocalDate;
import java.time.Period;

/**
 * Domain service que aplica regras de validação de {@code VALBENEF.NSN}
 * (linhas 50-220) — nome obrigatório com sobrenome, datas plausíveis,
 * UF na tabela de 27 e regra de idade.
 *
 * <p>Não persiste — usado por {@code CadastrarBeneficiarioUseCase} e
 * {@code AlterarBeneficiarioUseCase} antes do save.</p>
 */
public final class ValidadorBeneficiario {

    private static final java.util.Set<String> UFS = java.util.Set.of(
        "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG",
        "PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO");

    private ValidadorBeneficiario() {}

    public static void validarNome(String nome) {
        if (nome == null || nome.isBlank() || !nome.trim().contains(" ")) {
            throw new IllegalArgumentException("nome.invalido");
        }
    }

    public static void validarDataNascimento(LocalDate dt) {
        if (dt == null) return;
        int ano = dt.getYear();
        if (ano < 1900 || ano > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("dt-nascimento.invalida");
        }
    }

    public static void validarUf(String uf) {
        if (uf == null) return;
        if (!UFS.contains(uf.toUpperCase())) {
            throw new IllegalArgumentException("uf.invalida");
        }
    }

    /**
     * Regra CADBENEF: beneficiário com idade &gt; 75 anos é cadastrado/atualizado
     * com status SUSPENSO automaticamente (linhas 142-150).
     */
    public static StatusBeneficiario aplicarRegraIdade(StatusBeneficiario solicitado, LocalDate dtNasc) {
        if (dtNasc == null) return solicitado;
        int idade = Period.between(dtNasc, LocalDate.now()).getYears();
        if (idade > 75 && solicitado == StatusBeneficiario.ATIVO) {
            return StatusBeneficiario.SUSPENSO;
        }
        return solicitado;
    }
}
