package com.tripmate.account.common.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommonResponse<T> {

    private String codeNo;        // 응답 상태 코드 (예: 0000)
    private String message;  // 응답 메시지 (예: "성공")
    private T data;          // 응답에 포함된 데이터, 제네릭 타입 (T)

    //이건안씀 응답을 안내려주는경우는 없음 public CommonResponse(){}
    public CommonResponse(CommonErrorCode commonErrorCode, T data) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.data = data;
    }

    public CommonResponse(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
    }

    public String getCode(){
        return codeNo;
    }

    public String getMessage(){
        return message;
    }

    public T getData(){
        return data;
    }
//    public void setCode(int code){
//        this.code=code;
//    }

}
