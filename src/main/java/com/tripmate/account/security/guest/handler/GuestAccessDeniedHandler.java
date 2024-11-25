package com.tripmate.account.security.guest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.reponse.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static com.tripmate.account.common.errorcode.CommonErrorCode.FORBIDDEN_ACCESS;

/**
 * 사용자가 인증되었으나 특정 자원에 접근할 권한이 없을 때 발생하는 예외를 처리하는 핸들러입니다.
 * AccessDeniedHandler 인터페이스를 구현하여, 권한이 없는 요청에 대해 JSON 형태로 API 응답을 반환하도록 합니다.
 */
@Slf4j
public class GuestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    /**
     * 응답 내용을 json문자열로 바꾸기 위해 스프링빈에 등록된 ObjectMapper를 이용
     *
     * @param objectMapper JSON 직렬화를 위한 ObjectMapper 객체
     */
    public GuestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 권한 없는 사용자가 접근하려 할때 호출되는 메서드
     * 403 Forbidden 상태코드와 함꼐 커스텀 에러메세지를 JSON 형식으로 응답합니다
     * @param request 클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param accessDeniedException 권한 예외 정보
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("No Authorities", accessDeniedException);
        log.error("Request Uri:{}", request.getRequestURI());

        ResponseEntity<CommonResponse<Void>> commonResponse = new CommonResponse<>().toRespEntity(FORBIDDEN_ACCESS);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        String jsonResponse = objectMapper.writeValueAsString(commonResponse.getBody());
        response.getWriter().write(jsonResponse);
    }
}
