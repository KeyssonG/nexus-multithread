package keysson.apis.estoque.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import keysson.apis.estoque.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice(basePackages = "keysson.apis.estoque.controller")
public class EstoqueExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(EstoqueExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRuleException(
            BusinessRuleException ex, HttpServletRequest request) {

        var code = ex.getErrorCode();

        log.warn("estoque.regra_negocio | uri={} | method={} | code={} | http={} | msg={}",
                request.getRequestURI(),
                request.getMethod(),
                code.name(),
                code.getStatus().value(),
                code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(Map.of(
                        "status", code.getStatus().value(),
                        "error", code.name(),
                        "message", code.getMessage(),
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("estoque.parametro_invalido | uri={} | method={} | msg={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "PARAMETRO_INVALIDO",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Parâmetro inválido",
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("estoque.parametro_ausente | uri={} | param={}",
                request.getRequestURI(),
                ex.getParameterName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "PARAMETRO_AUSENTE",
                        "message", "Parâmetro obrigatório ausente: " + ex.getParameterName(),
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.debug("estoque.metodo_nao_suportado | uri={} | method={}",
                request.getRequestURI(),
                request.getMethod());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of(
                        "status", 405,
                        "error", "METODO_NAO_SUPORTADO",
                        "message", "Método " + request.getMethod() + " não suportado para este endpoint",
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("estoque.erro_inesperado | uri={} | method={} | exception={} | msg={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", 500,
                        "error", "ERRO_INTERNO",
                        "message", "Erro interno do servidor. Tente novamente ou entre em contato com o suporte.",
                        "path", request.getRequestURI(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
