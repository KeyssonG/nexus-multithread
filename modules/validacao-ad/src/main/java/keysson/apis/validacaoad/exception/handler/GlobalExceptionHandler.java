package keysson.apis.validacaoad.exception.handler;

import keysson.apis.validacaoad.exception.BusinessRuleException;
import keysson.apis.validacaoad.exception.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice("validacaoADGlobalExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<SimpleErrorResponse> handleBusinessRuleException(BusinessRuleException ex) {
        ErrorCode code = ex.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(new SimpleErrorResponse(
                        code.getStatus().value(),
                        code.getMessage(),
                        LocalDateTime.now()
                ));
    }
}