package br.gov.sifap.pagamentos.api;

import br.gov.sifap.pagamentos.application.CicloDuplicadoException;
import br.gov.sifap.pagamentos.application.CicloNaoEncontradoException;
import br.gov.sifap.pagamentos.application.CompetenciaFuturaException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler global RFC 7807 — alinhado ao plano §4 (códigos de erro padronizados).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CicloDuplicadoException.class)
    public ProblemDetail handleDuplicado(CicloDuplicadoException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("urn:sifap:erro:ciclo.duplicado"));
        pd.setTitle("Ciclo duplicado para a competência");
        return pd;
    }

    @ExceptionHandler(CompetenciaFuturaException.class)
    public ProblemDetail handleFutura(CompetenciaFuturaException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("urn:sifap:erro:competencia.futura"));
        pd.setTitle("Competência futura não permitida");
        return pd;
    }

    @ExceptionHandler(CicloNaoEncontradoException.class)
    public ProblemDetail handleNaoEncontrado(CicloNaoEncontradoException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("urn:sifap:erro:ciclo.nao-encontrado"));
        pd.setTitle("Ciclo não encontrado");
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("urn:sifap:erro:competencia.formato-invalido"));
        pd.setTitle("Entrada inválida");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("validation failed");
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        pd.setType(URI.create("urn:sifap:erro:validacao"));
        pd.setTitle("Falha de validação");
        return pd;
    }
}
