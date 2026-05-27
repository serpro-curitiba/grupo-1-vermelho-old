package br.gov.sifap.conciliacao.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser do arquivo CNAB 240 BB — apenas registros tipo "3" (detalhe).
 * Header (tipo 0/1), trailer (tipo 5/9) e segmentos não-pagamento são ignorados.
 */
public final class ParserCnab240 {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("ddMMyyyy");

    private ParserCnab240() {}

    public static List<RegistroCnab240> ler(InputStream in) throws IOException {
        var out = new ArrayList<RegistroCnab240>();
        try (var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.length() < 240) continue;
                char tipoReg = linha.charAt(7);
                if (tipoReg != '3') continue;
                out.add(parse(linha));
            }
        }
        return out;
    }

    static RegistroCnab240 parse(String l) {
        String banco = l.substring(0, 3);
        String lote  = l.substring(3, 7);
        String numDoc = l.substring(73, 83).trim();
        String cpf = l.substring(43, 54).trim();
        String vlr = l.substring(119, 134).trim();
        String dt  = l.substring(139, 147).trim();
        String cod = l.substring(230, 232).trim();

        BigDecimal valor = new BigDecimal(vlr.isEmpty() ? "0" : vlr)
                .movePointLeft(2);
        LocalDate data = null;
        try { data = LocalDate.parse(dt, DTF); } catch (Exception ignored) {}

        Long numPagto = null;
        try { numPagto = Long.parseLong(numDoc); } catch (Exception ignored) {}

        return new RegistroCnab240(banco, lote, cpf, numPagto, valor, data, cod);
    }
}
