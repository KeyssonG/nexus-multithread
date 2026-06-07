package keysson.nexus.common.exception;

import lombok.Getter;

@Getter
public class BaseBusinessException extends RuntimeException {
    private final IErrorCode errorCode;

    public BaseBusinessException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
