package com.tripmate.account.filter.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

/**
 * JwtValidateException
 * 목적: 이 예외는 JWT 토큰의 유효성 검사에서 발생합니다.
 * 토큰의 서명이 잘못되었거나, 만료되었거나, 형식이 잘못된 경우에 발생할 수 있습니다.
 * 즉, JWT 자체에 문제가 있을 때 발생합니다.
 * 사용 상황:
 * JWT 토큰이 올바르지 않거나, 파싱에 실패했을 때.
 * 서명이 잘못된 경우.
 * 토큰이 만료된 경우.
 * JWT 형식이 잘못된 경우 (예: 잘못된 토큰 구문).
 */
public class JwtValidateException extends RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public JwtValidateException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }

    public CommonErrorCode getCommonErrorCode() {
        return commonErrorCode;
    }
}
