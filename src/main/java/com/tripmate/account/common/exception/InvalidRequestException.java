package com.tripmate.account.common.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class InvalidRequestException extends RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public InvalidRequestException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }

    public CommonErrorCode getCommonErrorCode() {
        return commonErrorCode;
    }
}
