package com.tripmate.account.common.handler;

import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    //Validation failure in DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        FieldError firstError = fieldErrorList.get(0);

        String errorCode = firstError.getDefaultMessage();
        CommonErrorCode commonErrorCode = CommonErrorCode.fromCode(errorCode);

        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    //Incorrect request from user
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidRequestException(InvalidRequestException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<CommonResponse<Void>> handleServerErrorException(ServerErrorException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}


/*  원래는 Handler에서 map으로 응답했었음
    근데 이렇게 하고 보니 클라이언트에 일관되게 응답해줘야 하지 않을까 생각해서 지워버렸다...
        for (FieldError error : fieldErrorList) {
            // error.getDefaultMessage()는 DTO에서 설정한 message 값 (에러 코드)
            String errorCode = error.getDefaultMessage();
            CommonErrorCode commonErrorCode = CommonErrorCode.fromCode(errorCode);
            int resultCd = commonErrorCode.getCode();
            String resultMsg = commonErrorCode.getMessage();
            errors.put("resultCd", String.valueOf(resultCd));
            errors.put("resultMsg", resultMsg);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

 */




/*
------------MethodArgumentNotValidException 과정 공부

1. 사용자가 잘못된 데이터로 가입 요청:
    예시로
    userId: ""                         아이디가 빈 값이라 문제됨  @NotBlank(message = "1001")
                                            오류정보는
                                            Field: "userId"
                                            Default Message: "1001" (DTO에서 설정한 메시지)
    userPwd: "a*b*c*d*"                특수 문자가 포함되어 있어 유효성 검사에 실패
                                            오류정보는
                                            Field: "userPwd"
                                            Default Message: "1005"

    nickname: "123"                    숫자만 포함되어 있어 유효성 검사에 실패
                                             오류 정보:
                                             Field: "nickname"
                                             Default Message: "1007"

2.유효성 검사 실패하면 MethodArgumentNotValidException 발생:
스프링 MVC는 유효성 검사가 실패하면 MethodArgumentNotValidException을 발생시킨다.

3.유효성 검사가 실패하면, 각 필드에 대해 FieldError 객체가 생성된다.
FieldError error = new FieldError("user", "userId", "1001");
  이 FieldError 객체들은 BindingResult 클래스의 FieldErrors 리스트에 저장된다.

[
    FieldError(field="userId", defaultMessage="1001"),
    FieldError(field="userPwd", defaultMessage="1005"),
    FieldError(field="nickname", defaultMessage="1007")
]

FieldError 객체는 오류 정보를 담고 있으며, 여러 개의 FieldError는 BindingResult에 담겨져 있다.
    BindingResult.add(new FieldError("userId", "1001"));
    BindingResult.add(new FieldError("userPwd", "1005"));
    BindingResult.add(new FieldError("nickname", "1007"));

 List<FieldError> fieldErrors = bindingResult.getFieldErrors(); // 오류 리스트 가져오기
     for (FieldError fieldError : fieldErrors) {
        String field = fieldError.getField(); // 필드 이름 가져오기
        String errorCode = fieldError.getDefaultMessage(); // 오류 메시지 코드 가져오기
      }

4.그러면 에러가 발생했으니 @ControllerAdvice가 에러를 잡아채서 가져옴

5.BindingResult 접근:
    @ControllerAdvice에서 ex.getBindingResult() 호출하면
    BindingResult 객체가 반환되며,
    이 객체는 내부적으로 다음과 같은 FieldError 객체들을 포함한다.
[
    FieldError(field="userId", defaultMessage="1001"),
    FieldError(field="userPwd", defaultMessage="1005"),
    FieldError(field="nickname", defaultMessage="1007")
]


 */


