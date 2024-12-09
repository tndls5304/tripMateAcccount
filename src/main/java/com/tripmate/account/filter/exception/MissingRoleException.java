package com.tripmate.account.filter.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class MissingRoleException extends  RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public MissingRoleException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }
    public CommonErrorCode getCommonErrorCode(){
        return commonErrorCode;
    }
}
