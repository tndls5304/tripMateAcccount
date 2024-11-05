package com.tripmate.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * 커스텀필터입니다
 * 시큐리티에서 기본으로 제공해주는 UsernamePasswordAuthenticationFilter는 폼 기반 요청만(application/x-www-form-urlencoded)을 처리하도록 설계되어 있고 JSON을 읽지 못합니다.
 * 그러나 저는 API로 JSON 형식으로 모든 요청을 보내야 합니다.
 * 기본 필터로는 JSON 바디를 읽을 수 없기 때문에, 이같이 커스텀 필터를 통해 인증 과정을 수동으로 처리하는걸 추가 했습니다.
 * 동작하는 방식은 jSON 형식의 요청 바디에서 username과 password를 추출해 UsernamePasswordAuthenticationToken을 생성하고, AuthenticationManager로 전달하여 인증을 진행하게 합니다.
 */
public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_REQUEST_URL = "/api/account/user/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private final ObjectMapper objectMapper;

    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);

    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (!CONTENT_TYPE.equals(request.getContentType())) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }
        //request.getInputStream()에서 바로 JSON 데이터를 읽고, Map<String, String> 타입으로 변환합니다. 그런 다음 UsernamePasswordAuthenticationToken에 사용자 이름과 비밀번호를 전달하고 인증을 시도합니다.
        Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(credentials.get("userId"), credentials.get("userPwd"));
        return this.getAuthenticationManager().authenticate(authRequest);
    }


}



