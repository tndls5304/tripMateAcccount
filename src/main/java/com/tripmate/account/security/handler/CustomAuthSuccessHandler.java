package com.tripmate.account.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * 로그인 인증 성공시 호출되는 핸들러
 * 사용자가 성공적으로 인증되면, 리디렉션 대신 인증 성공 메시지를 JSON 형식으로 클라이언트에게 반환합니다.
 * 인증 성공 시 응답에는 기본적으로 HTTP 상태 코드 200과 함께 성공 메시지가 포함됩니다.
 */
public class CustomAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;

    public CustomAuthSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 성공 후 호출되는 메서드
     * 인증이 성공적으로 이루어졌을 때 클라이언트에게 성공 메시지를 JSON 형식으로 반환합니다.
     * 기존의 리디렉션 방식 대신, {@link CommonResponse}를 JSON 형식으로 응답에 포함하여
     * 클라이언트에게 전달합니다.
     * @param request 인증 요청 정보
     * @param response 인증 성공 응답 정보
     * @param authentication 인증 정보를 포함하는 객체
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CommonResponse<Void> commonResponse = new CommonResponse<>(CommonErrorCode.SUCCESS);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        objectMapper.writeValue(response.getWriter(), commonResponse);
    }
}
