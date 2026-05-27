package br.gov.sifap.shared;

import java.util.Set;

/**
 * Conjunto de prefixos especiais de CPF/Documento que <b>bypassam</b> a validação
 * em {@code VALDOCS.NSN#CHECK-DOC-ESPECIAL} (linhas 71-90).
 *
 * <p>São usados pelo legado para CPFs de homologação, diplomatas e CPFs de teste.
 * Mantidos para paridade de comportamento.</p>
 */
public final class PrefixosDocumentosEspeciais {

    public static final Set<String> PREFIXOS = Set.of(
            "000", "001", "002", "010", "011", "099", "100", "999");

    private PrefixosDocumentosEspeciais() {}

    public static boolean isEspecial(String cpf) {
        if (cpf == null || cpf.length() < 3) return false;
        return PREFIXOS.contains(cpf.substring(0, 3));
    }
}
