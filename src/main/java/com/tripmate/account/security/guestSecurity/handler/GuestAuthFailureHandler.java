package com.tripmate.account.security.guestSecurity.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
 * 인증실패시 발생하는 예외에 따라 적절한 오류 코드와 메시지를 포함한 JSON 응답을 반환하는 커스텀 인증 실패 핸들러 클래스.
 * 오류 발생 시 {@link ObjectMapper}를 사용하여 {@link CommonResponse} 객체를 JSON 형식으로 변환하고, 이를 {@link HttpServletResponse}에 작성하여 클라이언트로 전송합니다.
 */

public class GuestAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    public GuestAuthFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패 후 호출되는 메서드
     * {@link AuthenticationException}의 구체적인 종류에 따라 적합한 {@link CommonErrorCode}를 설정하고,
     * 그에 해당하는 HTTP 상태 코드와 메시지를 포함한 JSON 응답을 반환합니다.
     *
     * @param request   인증 요청을 담은 객체
     * @param response  인증 실패 응답을 담은 객체
     * @param exception 발생한 {@link AuthenticationException} 예외객체
     * @throws IOException      입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        CommonErrorCode errorCode;
        if (exception instanceof AuthenticationServiceException) {
            logger.error("AuthenticationServiceException: " + exception.getMessage());
            errorCode = CommonErrorCode.CONTENT_TYPE_NOT_SUPPORTED;
        } else if (exception instanceof UsernameNotFoundException) {
            errorCode = CommonErrorCode.USERNAME_NOT_FOUND;
        } else if (exception instanceof BadCredentialsException) {
            errorCode = CommonErrorCode.BAD_CREDENTIALS;
        } else if (exception instanceof LockedException) {
            errorCode = CommonErrorCode.ACCOUNT_LOCKED;
        } else if (exception instanceof CredentialsExpiredException) {
            errorCode = CommonErrorCode.CREDENTIALS_EXPIRED;//비밀번호 오래 써서  만료
        } else if (exception instanceof DisabledException) {
            errorCode = CommonErrorCode.ACCOUNT_DISABLED;//계정 비활성화 상태 (관리자가 계정비활성화함)
        } else if (exception instanceof AccountExpiredException) {
            errorCode = CommonErrorCode.ACCOUNT_EXPIRED;
        } else {
            errorCode = CommonErrorCode.UNEXPECTED_AUTHENTICATION_FAILED;
        }

        CommonResponse<Void> commonResponse = new CommonResponse<>(errorCode);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getHttpStatus().value());
        /*      String jsonResponse = objectMapper.writeValueAsString(commonResponse);
           response.getWriter().write(jsonResponse); 이 2줄을 한줄로 */
        objectMapper.writeValue(response.getWriter(), commonResponse); // JSON 형식으로 응답을 반환
    }
}
