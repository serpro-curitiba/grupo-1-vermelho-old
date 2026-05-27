package br.gov.sifap.beneficiarios.domain;

import br.gov.sifap.shared.PrefixosDocumentosEspeciais;

/**
 * Domain service de {@code VALDOCS.NSN}.
 * Valida CPF + RG; prefixos especiais (000/001/002/010/011/099/100/999) bypassam.
 */
public final class ValidadorDocumentos {

    private ValidadorDocumentos() {}

    public static boolean documentosOk(String cpf, String rg) {
        if (cpf == null || cpf.length() != 11) return false;
        if (PrefixosDocumentosEspeciais.isEspecial(cpf)) return true;
        if (rg == null || rg.trim().length() < 5) return false;
        return true;
    }
}
