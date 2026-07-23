package keysson.apis.estoque.exception;

import keysson.apis.estoque.exception.enums.ErrorCode;
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
