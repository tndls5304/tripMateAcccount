package com.tripmate.account.jwt;

import com.tripmate.account.filter.exception.UnauthorizedException;
import com.tripmate.account.security.guest.GuestUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.tripmate.account.common.errorcode.CommonErrorCode.JWT_REQ_ACCESS_MISSING;

/**
 * 1.역할:
 * 서버는 클라이언트의 상태(Session)를 유지하지 않으므로, 요청마다 클라이언트를 식별하기 위해 Access Token을 확인해야 한다.
 * 클라이언트로부터 전달받은 JWT access 토큰을 검증하고, 유효한 경우 인증 정보를 생성하여 Spring Security의 컨텍스트에 설정.

 * 2.동작 방식:
 * 1.Access Token 만료 여부를 확인.
 * 2.만료되지 않았다면 토큰을 파싱해서 사용자 정보를 추출. (만료되었다면 파싱하다가 오류를 내뱉고 JWT 재발급 요청 api를 보내야함)
 * 3.추출한 사용자 정보로 Authentication 객체를 생성해 SecurityContext에 저장.

 * 개인 study : "시큐리티 컨텍스트에 저장하는 것 자체가 목적은 아님":
 * 시큐리티 컨텍스트에 인증 객체를 저장하는 건 Spring Security가 요청을 처리하는 데 필요한 기본 메커니즘일 뿐이다.
 */

public class GuestJwtTokenFilter extends OncePerRequestFilter {
    private final JwtAuthService jwtAuthService;
    @Value("${jwt.secret-key}")
    private String secretKey;
    private final String jwtHeader = "Authorization";
    private final String jwtPrefix = "Bearer ";

    public GuestJwtTokenFilter(JwtAuthService jwtAuthService) {
        this.jwtAuthService = jwtAuthService;
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Access Token을 추출하는 메서드.
     * Authorization 헤더에서 "Bearer"로 시작하는 JWT 토큰을 찾아 반환.
     * 만약 토큰이 없거나 형식이 맞지 않으면 null을 반환합니다.
     *
     * @param request request HTTP 요청 객체.
     * @return JWT Access Token 또는 null
     */
    private String getAccessTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtHeader);
        if (bearerToken != null && bearerToken.startsWith(jwtPrefix)) {
            return bearerToken.substring(jwtPrefix.length());
        }
        return null;
    }

    /*
    -api통신할때는 access토큰만 서버로 받는다
    이때 access토큰을 검증한다. 파싱해서 시간확인만 함.   -----> 통과 ---->인증된 토큰을 만들어서 컨텍스트  홀더에 저장함.
                                         ----->만료되면 ----> 오류를 뱉음 (억세스토큰만료됨 리프레시토큰 달라고함)


     */

    /**
     * 요청이 들어올 때마다 호출되는 필터 메서드.
     * 이 메서드는 HTTP 요청에서 JWT Access Token을 추출하고, 해당 토큰이 유효한지 확인합니다.
     * 유효한 토큰일 경우 Spring Security의 SecurityContext에 인증 정보를 설정하여 인증된 상태로 이후 요청을 처리할 수 있도록 합니다.
     * 유효한 토큰일 경우 Spring Security의 SecurityContext에 인증 정보를 설정하여
     * 인증된 상태로 이후 요청을 처리할 수 있도록 합니다.
     *
     * @param request     HTTP 요청 객체.
     * @param response    HTTP 응답 객체.
     * @param filterChain filterChain 요청 필터 체인.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = getAccessTokenFromHeader(request);
        if (accessToken == null) {
            throw new UnauthorizedException(JWT_REQ_ACCESS_MISSING);
        }

        //파싱해서 시간 확인 하던중에 유효기간이 만료되면 오류를 내뱉을거임
        Claims claims = jwtAuthService.parseAccessToken(accessToken);
        // JWT가 유효하면 JWT를 디코딩(파싱)하여 사용자 정보를 추출해 인증객체 만들기
        Authentication authentication = createAuthentication(claims);
        //Spring Security의 SecurityContext에 인증 객체를 설정하여 요청의 인증 상태를 관리하기
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 필터 체인 진행 . 다음 필터나 서블릿으로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * JWT에서 추출한 Claims 정보로부터 인증 객체(Authentication)를 생성
     *
     * @param claims JWT에서 추출한 Claims 정보
     * @return Authentication 객체 (사용자 정보 및 권한 정보 포함)
     */
    private Authentication createAuthentication(Claims claims) {
        String guestId = claims.get("id", String.class);
        List<String> rolesList = claims.get("roles", List.class);
        //리스트를 꺼내서 set으로 바꾸고 , 타입은 String으로 바꾸기
        Set<GrantedAuthority> guestRoles = new HashSet<>();
        for (String role : rolesList) {
            guestRoles.add(new SimpleGrantedAuthority(role));
        }

        // JWT에서 사용자 정보 추출 후 인증된 Authentication 반환
        GuestUserDetails userDetails = new GuestUserDetails(guestId, "", guestRoles); // JWT에서 사용자 정보 로드
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}


