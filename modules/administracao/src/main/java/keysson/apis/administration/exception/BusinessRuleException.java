package keysson.apis.administration.exception;

import keysson.apis.administration.exception.enums.ErrorCode;
import keysson.nexus.common.exception.BaseBusinessException;

public class BusinessRuleException extends BaseBusinessException {
    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }

    @Override
    public ErrorCode getErrorCode() {
        return (ErrorCode) super.getErrorCode();
    }
}
