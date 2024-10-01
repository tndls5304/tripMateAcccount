package com.tripmate.account.common.handler;

import com.tripmate.account.common.errorCode.CommonErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
       //빈값 확인
        Map<String, String> errors = new HashMap<>();
        BindingResult bindingResult  =ex.getBindingResult();

        List<FieldError>fieldErrorList=   bindingResult.getFieldErrors();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // error.getDefaultMessage()는 DTO에서 설정한 message 값 (에러 코드)
            String errorCode =error.getDefaultMessage();
            CommonErrorCode commonErrorCode = CommonErrorCode.fromCode(errorCode);
            int resultCd=commonErrorCode.getCode();
            String resultMsg=commonErrorCode.getMessage();
            errors.put("resultCd",String.valueOf(resultCd));
            errors.put("resultMsg",resultMsg);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}


        /*ex는 MethodArgumentNotValidException 클래스의 인스턴스입니다. 이 예외는 유효성 검사가 실패했을 때 발생합니다.
        BindingResult는 검증된 객체의 결과를 포함하고 있으며, 유효성 검사에서 발생한 에러 정보와 관련된 다양한 메서드를 제공
        getFieldErrors() 메서드는 BindingResult에서 유효성 검사가 실패한 모든 필드의 에러를 포함하는 List<FieldError>를 반환
        FieldError 클래스는 특정 필드에 대한 유효성 검사 오류를 나타내는 객체라서
            필드 이름: 유효성 검사가 실패한 필드의 이름.
            에러 코드: 어떤 이유로 유효성 검사가 실패했는지를 나타내는 코드.
            메시지: 해당 필드의 오류에 대한 설명.

        for (FieldError error : ex.getBindingResult().getFieldErrors()):
          이 부분은 FieldError 객체들을 반복(iterate)하면서 각 필드에 대한 오류를 처리하는 루프입니다.
          error 변수는 현재 반복에서 처리하고 있는 FieldError 객체를 나타냅니다.
     */



/*
1. BindingResult
BindingResult는 스프링 MVC에서 사용되는 인터페이스로, 주로 데이터 바인딩과 유효성 검사 결과를 포함합니다.
이는 컨트롤러 메서드에서 폼 데이터를 처리할 때 사용되며, 다음과 같은 정보들을 담고 있습니다:

바인딩 오류: 클라이언트에서 전달된 데이터와 DTO 객체 간의 불일치로 인해 발생한 오류
유효성 검사 결과: @Valid 또는 @Validated 어노테이션에 의해 발생한 유효성 검사 오류

2. getBindingResult()
getBindingResult() 메서드는 MethodArgumentNotValidException 객체의 메서드로,
예외가 발생한 후 해당 요청의 바인딩 결과를 반환합니다.
즉, 이 메서드는 유효성 검사 과정에서 발생한 모든 오류 정보를 포함하는 BindingResult 객체를 제공합니다.

3. getFieldErrors()
getFieldErrors()는 BindingResult 인터페이스의 메서드입니다. 이 메서드는 바인딩 과정에서 발생한 필드 수준의 오류 리스트를 반환합니다. 각 필드 오류는 FieldError 객체로 표현되며, 일반적으로 다음과 같은 정보를 포함합니다:

필드 이름: 오류가 발생한 DTO의 필드 이름
오류 코드: 오류를 구분하기 위한 코드
기본 메시지: DTO에서 설정한 메시지 값


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

2.유효성 검사 실패하면 MethodArgumentNotValidException 발생: 이 경우 스프링 MVC는 유효성 검사 실패로 인해
 MethodArgumentNotValidException을 발생시킵니다.
3.검증에 실패한 필드에 대해 각각 FieldError객체가 생성되고
  오류 목록은 BindingResult 객체 내에 FieldErrors 라는 리스트 변수가 있고
   이 리스트 변수는 이런식으로 데이터가 저장이 됩니다:

[
    FieldError(field="userId", defaultMessage="1001"),
    FieldError(field="userPwd", defaultMessage="1005"),
    FieldError(field="nickname", defaultMessage="1007")
]
    위 데이터를 리스트처럼 생긴 객체다. 배
    BindingResult.add(new FieldError("userId", "1001"));
    BindingResult.add(new FieldError("userPwd", "1005"));
    BindingResult.add(new FieldError("nickname", "1007"));

4.그러면 에러가 발생했으니 @ControllerAdvice가 에러를 잡아채서 가져옴
5. @ControllerAdvice에서 ex.getBindingResult() 호출하면
    BindingResult 객체가 반환되며,
    이 객체는 내부적으로 다음과 같은 FieldError 객체들을 포함합니다
[
    FieldError(field="userId", defaultMessage="1001"),
    FieldError(field="userPwd", defaultMessage="1005"),
    FieldError(field="nickname", defaultMessage="1007")
]

6.        for (FieldError error : ex.getBindingResult().getFieldErrors()) {}~
    FieldError(field="userId", defaultMessage="1001") 이 데이터 각각 FieldError 를  error 변수에 담음





    class BindingResult {
    List<FieldError> fieldErrors = new ArrayList<>();

    public void add(FieldError error) {
        fieldErrors.add(error);
    }
}
 */


