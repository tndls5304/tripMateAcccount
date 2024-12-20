package com.tripmate.account.security.guest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import java.io.IOException;
import java.util.Set;

/**
 * 커스텀필터입니다
 * 일반적으로 스프링 시큐리티는 폼 데이터로 요청을 받는데, 이 필터는 JSON 데이터를 받아 로그인 인증을 진행하도록 설계되었습니다.
 * 시큐리티에서 기본으로 제공해주는 UsernamePasswordAuthenticationFilter는 폼 기반 요청만(application/x-www-form-urlencoded)을 처리하도록 설계되어 있고 JSON을 읽지 못합니다.
 * 그러나 저는 API로 JSON 형식으로 모든 요청을 보내야 합니다.
 * 기본 필터로는 JSON 바디를 읽을 수 없기 때문에, 이같이 커스텀 필터를 통해 인증 과정을 수동으로 처리하는걸 추가 했습니다.
 * 동작하는 방식은 jSON 형식의 요청 바디에서 username과 password를 추출해 UsernamePasswordAuthenticationToken을 생성하고, AuthenticationManager로 전달하여 인증을 진행하게 합니다.
 */
public class GuestJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final Validator validator; // Validator 선언
    private static final String DEFAULT_LOGIN_REQUEST_URL = "/api/account/guest/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private final ObjectMapper objectMapper;
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER = new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);

    public GuestJsonUsernamePasswordAuthenticationFilter(Validator validator, ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.validator = validator;
        setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        if (!CONTENT_TYPE.equals(request.getContentType())) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: " + request.getContentType());
        }
        // JSON 데이터를 DTO로 매핑
        GuestLoginReqDto loginReqDto = objectMapper.readValue(request.getInputStream(), GuestLoginReqDto.class);

        // DTO의 유효성 검사 (javax.validation 활용)//TODO 다시보기
        Set<ConstraintViolation<GuestLoginReqDto>> violations = validator.validate(loginReqDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);//TODO 예외만들기
        }
        // UsernamePasswordAuthenticationToken 생성
        UsernamePasswordAuthenticationToken authRequestToken =
                new UsernamePasswordAuthenticationToken(loginReqDto.getGuestId(), loginReqDto.getGuestPwd());

        // 세부 정보를 설정 (예: HTTP 요청 관련 세부 정보)필요하면 추가하기 authRequestToken.setDetails(new WebAuthenticationDetails(request));

        //AuthenticationManager에 이 토큰을 전달함.

        AuthenticationManager ProviderManager = this.getAuthenticationManager();
        //내부에서 인증을 처리하고 검증이 성공하면 인증된 토큰을 생성함.인증된 토큰은 SecurityContext에 저장
        return ProviderManager.authenticate(authRequestToken);
    }

//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        System.out.println("**************성공핸들러 호출해볼까?*****");
//    this.successHandler.onAuthenticationSuccess(request, response, authResult);
//    }
}

/*
인증 과정:

DaoAuthenticationProvider가 authRequestToken을 받아서 UserDetailsService를 호출하여 사용자 정보를 데이터베이스에서 조회합니다.
사용자가 존재하면, 입력한 비밀번호와 데이터베이스에서 조회된 비밀번호를 비교하여 일치하는지 확인합니다.
비밀번호가 일치하면, 인증된 Authentication 객체를 반환합니다.


 ProviderManager.authenticate(authRequestToken); 통해 인증되면 인증된 Authentication 객체가 반환된다
 이름, 비밀번호, 권한이 포함됩니다.
인증된 Authentication 객체는 이후 필터나 서비스에서 사용할 수 있으며, 보통 SecurityContextHolder에 저장되어 현재 요청의 인증 정보를 제공합니다.

 */

