package com.tripmate.account.common.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommonResponse<T> {

    private String codeNo;        // 응답 상태 코드 (예: 0000)
    private String message;  // 응답 메시지 (예: "성공")
    private T data;          // 응답에 포함된 데이터, 제네릭 타입 (T)

    //기본 생성자가 필요하겠지..? 필요하나? 모르겠다 ㅠㅠ
    public CommonResponse() {
    }

    // 데이터가 없는 경우에 대한 ResponseEntity 반환
    public ResponseEntity<CommonResponse<Void>> toRespNoDataEntity(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        // 데이터가 없으므로 CommonResponse<Void>로 설정
        return new ResponseEntity<>(new CommonResponse<Void>(commonErrorCode), commonErrorCode.getHttpStatus());
    }

    //데이터가 없는 경우 생성자
    public CommonResponse(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
    }

    // 데이터가 있는 경우에 대한 ResponseEntity 반환
    public ResponseEntity<CommonResponse<T>> toRespWithDataEntity(T data, CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.data = data;
        // ResponseEntity로 반환
        return new ResponseEntity<>(this, commonErrorCode.getHttpStatus());
    }

    //데이터가 있는 경우 생성자
    public CommonResponse(CommonErrorCode commonErrorCode, T data) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.data = data;
    }


    //컨트롤러에서 내려줄 데이터가 없을때
//    public ResponseEntity<CommonResponse<Void>> getNoDataResp(HttpStatus httpStatus){
//     CommonResponse<Void>=new CommonResponse<>(new CommonErrorCode(httpStatus status));
//        return ResponseEntity.status(httpStatus).build();
//    }
    //컽르롤러에서 내려줄 데이터가 있을때


    public String getCode() {
        return codeNo;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
//    public void setCode(int code){
//        this.code=code;
//    }

}
