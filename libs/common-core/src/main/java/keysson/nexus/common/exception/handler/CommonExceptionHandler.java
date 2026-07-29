package keysson.nexus.common.exception.handler;

import keysson.nexus.common.exception.BaseBusinessException;
import keysson.nexus.common.exception.IErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CommonExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<SimpleErrorResponse> handleBusinessRuleException(BaseBusinessException ex) {
        IErrorCode code = ex.getErrorCode();

        log.warn("business_exception | code={} | http={} | msg={}",
                ex.getClass().getSimpleName(),
                code.getStatus().value(),
                code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(new SimpleErrorResponse(
                        code.getStatus().value(),
                        code.getMessage(),
                        LocalDateTime.now()
                ));
    }
}
