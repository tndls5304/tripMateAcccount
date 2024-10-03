package com.tripmate.account.common.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class InvalidErrorException extends RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public InvalidErrorException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }
    public CommonErrorCode getCommonErrorCode(){
        return commonErrorCode;
    }
}
