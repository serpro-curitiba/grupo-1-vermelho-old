package br.gov.sifap.shared;

/**
 * Utilitário de mascaramento de CPF para logs e respostas REST.
 * Atende REQ-PAY-051 + LGPD.
 */
public final class CpfMask {

    private CpfMask() {}

    public static String mask(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***********";
        }
        return "***" + cpf.substring(3, 9) + "**";
    }
}
