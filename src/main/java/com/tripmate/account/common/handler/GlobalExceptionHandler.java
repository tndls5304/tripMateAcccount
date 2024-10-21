package com.tripmate.account.common.handler;

import com.tripmate.account.common.exception.DataConflictException;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
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

        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
    }

    //Incorrect request from user(유효성검사 제외 나머지 사용자로 부터 입력이 잘못된거)
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidRequestException(InvalidRequestException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        //return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
    }

    /**
     * 클라이언트측 입력이나 요청이 서버의 현재 상태 또는 비즈니스 규칙과 충돌하는 경우
     * 1.특정 작업이 현재 리소스의 상태와 충돌하는 경우. 예를 들어, "결제 완료" 상태의 주문에 대해 다시 결제를 시도할 때.
     * 2.중복 리소스:사용자가 이미 존재하는 사용자 ID, 이메일 주소, 또는 다른 고유 식별자를 사용하여 리소스를 생성하려고 할 때.
     * 3.시간 기반 제약:특정 기간이 지나거나 마감일이 지난 요청에 대해 작업을 시도할 때.
     * "신청 마감 시간이 지났습니다."
     * "이 이벤트는 이미 종료되었습니다."
     * 4.리소스의 사용 불가:특정 리소스가 다른 사용자의 요청으로 인해 현재 사용할 수 없는 경우.
     * "이 시간대는 이미 예약되었습니다."
     * "해당 아이템은 다른 사용자에게 할당되었습니다."
     * 5. 비즈니스 규칙 위반: 특정 비즈니스 로직에 의해 요청이 거부될 때
     * "회원 등급이 낮아 이 서비스를 이용할 수 없습니다."
     * "사용자의 계정 상태가 활성화되어 있지 않습니다."
     *
     * @since 2024.10.18
     */
    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<CommonResponse<Void>> handleDataConflictException(DataConflictException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
    }

    //
    //서버에러
    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<CommonResponse<Void>> handleServerErrorException(ServerErrorException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
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


