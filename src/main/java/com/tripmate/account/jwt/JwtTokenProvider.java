package com.tripmate.account.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTokenProvider {
    // JWT 생성 및 검증에 사용할 비밀키
    private final String secretKey = "your-secret-key";
    // Access Token 유효 시간 (1시간, 밀리초 단위)
    private final long accessTokenValidTime = 3600000;
    // Refresh Token 유효 시간 (7일, 밀리초 단위)
    private final long refreshTokenValidTime = 604800000;

    /**
     * Access Token 생성 메서드
     * @param username 사용자 ID
     * @param roles 사용자의 권한 목록
     * @return 생성된 Access Token (JWT 형식)
     */
    public String createAccessToken(String username, List<String> roles) {
        return createToken(username, roles, accessTokenValidTime);
    }

    /**
     * Refresh Token 생성 메서드
     * Refresh Token은 권한 정보 없이 생성됩니다.
     * @param username 사용자 이름 또는 ID
     * @return 생성된 Refresh Token (JWT 형식)
     */
    public String createRefreshToken(String username) {
        return createToken(username, null, refreshTokenValidTime);
    }

    /**
     * JWT Token 생성 메서드 (공통 로직)
     * @param username 사용자 이름 또는 ID
     * @param roles 사용자의 권한 목록 (null 가능)
     * @param validTime 토큰의 유효 시간
     * @return 생성된 JWT Token
     */
    private String createToken(String username, List<String> roles, long validTime) {
        Claims claims = Jwts.claims().setSubject(username);
        if (roles != null) {
            claims.put("roles", roles); // 권한 정보 추가
        }
        // 토큰의 생성 시간 및 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + validTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명에 비밀키 사용
                .compact();
    }
}
