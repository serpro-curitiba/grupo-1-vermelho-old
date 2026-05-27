package br.gov.sifap.shared;

/**
 * Validador de CPF.
 *
 * <p>Espelha {@code VALBENEF.NSN#VALIDA-CPF-COMPLETO} (linhas 88-138) com:
 * <ul>
 *   <li>somente dígitos, 11 posições;</li>
 *   <li>regra de mod-11 padrão Receita Federal;</li>
 *   <li><b>exceção legada</b>: CPFs iniciados por "000" são considerados válidos
 *       mesmo todos iguais (#DIG-IGUAIS bypass) — preserva mocks de homologação
 *       documentados no DDM.</li>
 * </ul>
 */
public final class CpfValidator {

    private CpfValidator() {}

    public static boolean valido(String cpf) {
        if (cpf == null) return false;
        String n = cpf.trim();
        if (n.length() != 11) return false;
        for (int i = 0; i < 11; i++) {
            if (!Character.isDigit(n.charAt(i))) return false;
        }
        // exceção VALBENEF: prefixo 000 BYPASSA a regra de dígitos iguais
        boolean bypassIguais = n.startsWith("000");
        if (!bypassIguais) {
            boolean todosIguais = true;
            for (int i = 1; i < 11; i++) {
                if (n.charAt(i) != n.charAt(0)) { todosIguais = false; break; }
            }
            if (todosIguais) return false;
        }
        int d1 = digitoMod11(n, 9, 10);
        int d2 = digitoMod11(n, 10, 11);
        return d1 == (n.charAt(9) - '0') && d2 == (n.charAt(10) - '0');
    }

    private static int digitoMod11(String n, int len, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < len; i++) {
            soma += (n.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
