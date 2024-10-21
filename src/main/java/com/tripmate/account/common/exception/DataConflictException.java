package com.tripmate.account.common.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

public class DataConflictException extends RuntimeException{
    private final CommonErrorCode commonErrorCode;
    //예외 던질떄 에러코드를 전달하게 하고 핸들러에서는 예외에서 에러코드를 바로 꺼내게 하기
    public DataConflictException(CommonErrorCode commonErrorCode){
        this.commonErrorCode=commonErrorCode;
    }
    public CommonErrorCode getCommonErrorCode(){
        return commonErrorCode;
    }
}
