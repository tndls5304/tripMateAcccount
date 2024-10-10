package com.tripmate.account.common.reponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommonResponse<T> {

    private String codeNo;        // 응답 상태 코드 (예: 0000)
    private String message;  // 응답 메시지 (예: "성공")
    @JsonIgnore
    private HttpStatus httpStatus;
    private T data;          // 응답에 포함된 데이터, 제네릭 타입 (T)

    //이건안씀 응답을 안내려주는경우는 없음 public CommonResponse(){}
    public CommonResponse(CommonErrorCode commonErrorCode, T data) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.httpStatus=commonErrorCode.getHttpStatus();
        this.data = data;
    }

    public CommonResponse(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.httpStatus=commonErrorCode.getHttpStatus();
    }

    //컨트롤러에서 내려줄 데이터가 없을때
//    public ResponseEntity<CommonResponse<Void>> getNoDataResp(HttpStatus httpStatus){
//     CommonResponse<Void>=new CommonResponse<>(new CommonErrorCode(httpStatus status));
//        return ResponseEntity.status(httpStatus).build();
//    }
    //컽르롤러에서 내려줄 데이터가 있을때


    public String getCode(){
        return codeNo;
    }

    public String getMessage(){
        return message;
    }

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

    public T getData(){
        return data;
    }
//    public void setCode(int code){
//        this.code=code;
//    }

}
