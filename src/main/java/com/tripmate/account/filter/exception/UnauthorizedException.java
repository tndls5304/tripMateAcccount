package com.tripmate.account.filter.exception;

import com.tripmate.account.common.errorcode.CommonErrorCode;

/**
 * 이 예외는 인증되지 않은 사용자가 접근하려 할 때 발생합니다.
 * 인증이 필요한 리소스에 인증되지 않은 사용자가 접근하려 할 때 발생하는 예외로,
 * 401 Unauthorized HTTP 상태 코드를 반환합니다.
 * 사용 상황:
 * 인증되지 않은 사용자가 인증이 필요한 리소스에 접근하려 할 때.
 * 인증을 요구하는 API에서 유효한 인증 자격을 제공하지 않았을 때 발생.
 * JWT 토큰이 없는 경우나, 유효한 토큰을 제공하지 않았을 때.
 */
public class UnauthorizedException extends RuntimeException {
    private final CommonErrorCode commonErrorCode;

    public UnauthorizedException(CommonErrorCode commonErrorCode) {
        this.commonErrorCode = commonErrorCode;
    }
    public CommonErrorCode getCommonErrorCode() {
        return commonErrorCode;
    }
}
