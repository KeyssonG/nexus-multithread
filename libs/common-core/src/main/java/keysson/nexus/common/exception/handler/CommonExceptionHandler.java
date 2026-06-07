package keysson.nexus.common.exception.handler;

import keysson.nexus.common.exception.BaseBusinessException;
import keysson.nexus.common.exception.IErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<SimpleErrorResponse> handleBusinessRuleException(BaseBusinessException ex) {
        IErrorCode code = ex.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(new SimpleErrorResponse(
                        code.getStatus().value(),
                        code.getMessage(),
                        LocalDateTime.now()
                ));
    }
}
