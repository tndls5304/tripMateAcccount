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


    public CommonResponse() {
    }

    // 데이터가 없는 경우에 대한 ResponseEntity 반환
    public ResponseEntity<CommonResponse<Void>> toRespEntity(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return ResponseEntity
                .status(commonErrorCode.getHttpStatus())
                .body(response);
        //  return new ResponseEntity<>(new CommonResponse<Void>(commonErrorCode), commonErrorCode.getHttpStatus());
    }

    //데이터가 없는 경우 생성자
    public CommonResponse(CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
    }

    // 데이터가 있는 경우에 대한 ResponseEntity 반환
    public ResponseEntity<CommonResponse<T>> toRespEntity(T data, CommonErrorCode commonErrorCode) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.data = data;
        // ResponseEntity로 반환
        return ResponseEntity
                .status(commonErrorCode.getHttpStatus())
                .body(this);
        //return new ResponseEntity<>(this, commonErrorCode.getHttpStatus());
    }

    //데이터가 있는 경우 생성자
    public CommonResponse(CommonErrorCode commonErrorCode, T data) {
        this.codeNo = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.data = data;
    }

    public String getCode() {
        return codeNo;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

}
