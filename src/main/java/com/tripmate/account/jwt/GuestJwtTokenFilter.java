package com.tripmate.account.jwt;

import com.tripmate.account.security.guest.GuestUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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


public class GuestJwtTokenFilter extends OncePerRequestFilter {
    private final JwtAuthService jwtAuthService;
    @Value("${jwt.secret-key}")
    private String secretKey;
    private final String jwtHeader = "Authorization";
    private final String jwtPrefix = "Bearer ";

    public GuestJwtTokenFilter(JwtAuthService jwtAuthService) {
        this.jwtAuthService = jwtAuthService;
    }


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
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = getAccessTokenFromHeader(request);
        // JWT 유효성 검증
        if (accessToken != null) {
            //파싱해서 시간 확인 하던중에 유효기간이 만료되면 오류를 내뱉을거임
            Claims claims = jwtAuthService.parseAccessToken(accessToken);
            // JWT가 유효하면 JWT를 디코딩(파싱)하여 사용자 정보를 추출해 인증객체 만들기
            Authentication authentication = createAuthentication(claims);
            //Spring Security의 SecurityContext에 인증 객체를 설정하여 요청의 인증 상태를 관리하기
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }


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
