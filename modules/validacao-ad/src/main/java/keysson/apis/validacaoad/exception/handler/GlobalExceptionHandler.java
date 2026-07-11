package keysson.apis.validacaoad.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import keysson.apis.validacaoad.exception.BusinessRuleException;
import keysson.apis.validacaoad.exception.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "keysson.apis.validacaoad.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<SimpleErrorResponse> handleBusinessRuleException(
            BusinessRuleException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();

        log.warn("business_rule_exception | uri={} | method={} | error={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                code.name(),
                code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(new SimpleErrorResponse(
                        code.getStatus().value(),
                        code.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SimpleErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("illegal_argument | uri={} | method={} | message={}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<SimpleErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.debug("method_not_supported | uri={} | method={}",
                request.getRequestURI(),
                request.getMethod());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new SimpleErrorResponse(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Método " + request.getMethod() + " não suportado para este endpoint",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<SimpleErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("missing_parameter | uri={} | param={}",
                request.getRequestURI(),
                ex.getParameterName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new SimpleErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetro obrigatório ausente: " + ex.getParameterName(),
                        LocalDateTime.now()
                ));
    }
}