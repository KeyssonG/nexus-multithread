package keysson.nexus.common.exception;

import org.springframework.http.HttpStatus;

public interface IErrorCode {
    String getMessage();
    HttpStatus getStatus();
}
