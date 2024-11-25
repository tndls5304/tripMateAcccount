package com.tripmate.account.security.guest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GuestJwtTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final String jwtHeader = "Authorization";
    private final String jwtPrefix = "Bearer ";


    private String getTokenValueFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtHeader);
        if (bearerToken != null && bearerToken.startsWith(jwtPrefix)) {
            return bearerToken.substring(jwtPrefix.length());
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = getTokenValueFromHeader(request);
        // JWT 유효성 검증
        if (tokenValue != null && isValidAccessToken(tokenValue)) {
            // JWT가 유효하면 JWT를 디코딩(파싱)하여 사용자 정보를 추출합니다.
            Authentication authentication = getAuthentication(tokenValue);
            //Spring Security의 SecurityContext에 인증 객체를 설정하여 요청의 인증 상태를 관리합니다:
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    public boolean isValidAccessToken(String tokenValue) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)                    //서명키 설정
                .parseClaimsJws(tokenValue)       //서명확인
                .getBody();                     //클레임 정보 추출

        // 2. 토큰 만료 시간 확인
        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            new Exception("");
        }
        // 3. 추가 검증 (선택)
        String userId = claims.get("id", String.class);
        if (userId == null || userId.isEmpty()) {
            throw new AccessDeniedException("No roles assigned to the user.");
        }
        // 2. 권한 확인 (roles가 클레임에 포함된 경우)
        List<String> roles = claims.get("roles", List.class);
        if (roles == null || roles.isEmpty()){
            throw new AccessDeniedException("Invalid JWT token.");
        }

        return true;
        //access토큰을 서버로 보내고
        // access토큰 검증하고 만료하면
        // 만료됐을때 서버에서 에러를 내뱉는다
        // 특정코드값으로 내려준다 클라이언트는
        // 그 코드값을 받았을떄 access토큰이 만료됐다 판단하고 access토큰을 재요청한다.
        //그때 클라이언트가 자기가 가지고 있는 리프레쉬토큰을 보내준다.

        //서버가 클라이언트가 보낸 리프레쉬 토큰을 받고
        //서버에서 디비에 있는 리프레쉬 토큰과 맞는지 비교를 한다
        //리프레시 토큰이 만료됐는지 확인을 한다.
        //이상이 없으면 access 토큰을 재발행해서 사용자한테 보낸다 (토큰 두개 다 보내기)/.
        //레프레시 토큰이 만료되면 서버에서 에러를 내뱉는다
        //리프레시 토큰이 만료되면 로그인하라고 해야함. 에러코드는 다시 로그인하라고 하는 내용

    }

    private Authentication getAuthentication(String tokenValue) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(tokenValue)
                .getBody();
        String guestId = claims.get("id", String.class);
        List<String> rolesStringList = claims.get("roles", List.class);

        Set<GrantedAuthority> guestRoles = new HashSet<>();
        for (String role : rolesStringList) {
            guestRoles.add(new SimpleGrantedAuthority(role));
        }

        // JWT에서 사용자 정보 추출 후 인증된 Authentication 반환
        GuestUserDetails userDetails = new GuestUserDetails(guestId, "", guestRoles); // JWT에서 사용자 정보 로드
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
