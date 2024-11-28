package com.tripmate.account.filter.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class JwtValidateException extends RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public JwtValidateException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }
    public CommonErrorCode getCommonErrorCode() {
        return commonErrorCode;
    }
}
