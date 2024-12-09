package com.tripmate.account.filter.handler;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.filter.exception.JwtValidateException;
import com.tripmate.account.filter.exception.MissingRoleException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Set;

@ControllerAdvice
public class FilterGlobalExceptionHandler {
    //클라이언트측에서 access토큰이 없어서 access+refresh토큰을 주면서 새로운 토큰을 생성해달라 요청할때 ReqDto에서 유효성 유효성 검사할때 토큰 각각 비었을때
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handle(ConstraintViolationException ex) {
        // ConstraintViolation 목록을 가져옴
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        // 첫 번째 ConstraintViolation을 가져옴
        ConstraintViolation<?> violation = violations.iterator().next();

        // 오류 메시지를 기반으로 errorCode를 가져옴
        String errorCode = violation.getMessage(); // 오류 메시지가 errorCode로 설정되어 있다고 가정

        // errorCode를 통해 CommonErrorCode를 찾음
        CommonErrorCode commonErrorCode = CommonErrorCode.fromCode(errorCode);

        // CommonResponse 객체 생성
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);

        // ResponseEntity에서 HttpStatus를 직접 설정
        return ResponseEntity.status(commonErrorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(JwtValidateException.class)
    public ResponseEntity<CommonResponse<Void>> handle(JwtValidateException ex) {
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
    }

    @ExceptionHandler(MissingRoleException.class)
    public ResponseEntity<CommonResponse<Void>>handle(MissingRoleException ex){
        CommonErrorCode commonErrorCode = ex.getCommonErrorCode();
        CommonResponse<Void> response = new CommonResponse<>(commonErrorCode);
        return new ResponseEntity<>(response, commonErrorCode.getHttpStatus());
    }
}
