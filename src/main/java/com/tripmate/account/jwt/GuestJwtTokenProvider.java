package com.tripmate.account.jwt;

import com.tripmate.account.common.enums.JwtTokenType;
import com.tripmate.account.security.guest.GuestUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GuestJwtTokenProvider {
    // JWT 생성 및 검증에 사용할 비밀키
    @Value("${jwt.secret-key}")
    private String secretKey;

    // 프로퍼티 파일에서 유효 시간 값을 주입받기
    @Value("${jwt.access-token-valid-time}")
    private long accessTokenValidTime;

    @Value("${jwt.refresh-token-valid-time}")
    private long refreshTokenValidTime;

    private final RefreshTokenRepository refreshTokenRepository;
;

    public GuestJwtTokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;

    }

    // JWT 토큰 생성과 검증할떄  .signWith() signWith 메서드는 secretKey를 입력받아 HMAC-SHA256 서명을 계산합니다. 이때 secretKey가 Base64로 인코딩되어 있어야 정상적으로 동작하기때문
    @PostConstruct
    public void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    //저장된 리프레쉬토큰이 시간이 유효한지 검사함.
    public Boolean isValidSavedRefreshToken(RefreshTokenInfo savedRefreshToken) {
        return savedRefreshToken.getExpireTime().isAfter(new Date());
    }

    //만료시간 계산
    public Date calculateTokenExpiry(JwtTokenType jwtTokenType) {
        Date now = new Date();
        long validity = JwtTokenType.REFRESH.equals(jwtTokenType) ? refreshTokenValidTime : accessTokenValidTime;
        return new Date(now.getTime() + validity);  // 밀리초의 현재시간+ 유효시간 설정 후 서명 메서드 호출
    }

    // RefreshToken을 이용하여 AccessToken을 재발급하는 메서드
    public JwtToken createAccessToken(RefreshTokenInfo refreshTokenInfo) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey) // 서명 키 설정
                .parseClaimsJws(refreshTokenInfo.getRefreshToken())// 토큰 파싱
                .getBody();

        String guestId = claims.get("id",String.class);
        List<GrantedAuthority> guestRoles = claims.get("roles", List.class);

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                    // 헤더 설정
                .setSubject(guestId)
                .claim("id", guestId)
                .claim("roles", guestRoles)
                .setIssuedAt(now)                                             // 발급 시간(issuedAt) 설정
                .setExpiration(calculateTokenExpiry(JwtTokenType.ACCESS))
                .signWith(SignatureAlgorithm.HS256, secretKey)                  // 서명 설정 (HS256 알고리즘 사용)
                .compact();  // 최종적으로 JWT 생성
        return new JwtToken(accessToken, refreshTokenInfo.getRefreshToken());
    }

    //권한가져오기
    public List<String> getRolesList(GuestUserDetails guestUserDetails) {
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : guestUserDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }

    //access토큰+ refresh토큰 모두 생성하고 생성된 refresh토큰은 디비에 저장
    public JwtToken createAllTokenAndSaveRefreshToken(GuestUserDetails guestUserDetails) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .claim("id", guestUserDetails.getUsername())
                .claim("roles", getRolesList(guestUserDetails))
                .setIssuedAt(now)
                .setExpiration(calculateTokenExpiry(JwtTokenType.ACCESS))
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 설정 (HS256 알고리즘 사용)
                .compact();

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(calculateTokenExpiry(JwtTokenType.REFRESH))
                .claim("id", guestUserDetails.getUsername())
                .claim("roles", getRolesList(guestUserDetails))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        refreshTokenRepository.save(new JwtToken(accessToken, refreshToken)); //순서를 어떻게알고 들어가지???
        return new JwtToken(accessToken, refreshToken);
    }
}
