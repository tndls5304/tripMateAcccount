package com.tripmate.account.common.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class ServerErrorException extends RuntimeException {

    private final CommonErrorCode commonErrorCode;

    public ServerErrorException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }

    public CommonErrorCode getCommonErrorCode() {
        return commonErrorCode;
    }
}
