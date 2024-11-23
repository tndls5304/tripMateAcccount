package com.tripmate.account.jwt;

import com.tripmate.account.common.entity.UserEntity;
import com.tripmate.account.security.guestSecurity.GuestUserDetails;
import com.tripmate.account.user.repository.RoleThRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;


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
    private final RoleThRepository roleThRepository;

    public GuestJwtTokenProvider(RefreshTokenRepository refreshTokenRepository, RoleThRepository roleThRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleThRepository = roleThRepository;
    }
    // JWT 토큰 생성과 검증할떄  .signWith() signWith 메서드는 secretKey를 입력받아 HMAC-SHA256 서명을 계산합니다. 이때 secretKey가 Base64로 인코딩되어 있어야 정상적으로 동작하기때문
    @PostConstruct
    public void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }


    //저장된 리프레쉬토큰이 시간이 유효한지 검사함.
    public Boolean isValidSavedRefreshToken(RefreshTokenInfo savedRefreshToken) {
        return savedRefreshToken.getExpireTime().isAfter(LocalDateTime.now());
    }
    //만료시간 계산
    /*
    public Date calculateTokenExpiry(boolean isRefreshToken) {
        Date now = new Date();
        long validity = isRefreshToken ? refreshTokenValidTime : accessTokenValidTime;
        return new Date(now.getTime() + validity);  // 밀리초의 현재시간+ 유효시간 설정 후 서명 메서드 호출
    }
     */

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
        String userId = claims.getSubject();
        List<GrantedAuthority> userRoles = claims.get("roles", List.class);

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                // 헤더 설정
                .setSubject(userId)
                .claim("id", userId)
                .claim("roles", userRoles)               // 역할 등을 클레임으로 추가
                .setIssuedAt(now)                                             // 발급 시간(issuedAt) 설정
                .setExpiration(calculateTokenExpiry(JwtTokenType.ACCESS))       // 만료 시간(expiration) 설정
                                                                                 //   .claim("aud", 특정서비스) //특정 서비스에서만 사용되는 토큰이라면 aud를 설정하는 것이 유용
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 설정 (HS256 알고리즘 사용)
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
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                // 헤더 설정
                //         .setSubject()
                .claim("id", guestUserDetails.getUsername())
                .claim("roles", getRolesList(guestUserDetails))
                .setIssuedAt(now)                                             // 발급 시간(issuedAt) 설정
//                .setExpiration(calculateTokenExpiry(false))       // 만료 시간(expiration) 설정
                .setExpiration(calculateTokenExpiry(JwtTokenType.ACCESS))       // 만료 시간(expiration) 설정
                //   .claim("aud", 특정서비스) //특정 서비스에서만 사용되는 토큰이라면 aud를 설정하는 것이 유용
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 설정 (HS256 알고리즘 사용)
                .compact();  // 최종적으로 JWT 생성

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                        // 헤더 설정
                .setIssuedAt(now)                                                   // 토큰 발급 시각 설정
//                .setExpiration(calculateTokenExpiry(true))
                .setExpiration(calculateTokenExpiry(JwtTokenType.REFRESH))
                .claim("id", guestUserDetails.getUsername())
                .claim("roles", getRolesList(guestUserDetails))  // 사용자 권한을 roles로 설정
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        refreshTokenRepository.save(new RefreshTokenInfo(guestUserDetails.getUsername(), refreshToken));
        return new JwtToken(accessToken, refreshToken);
    }

    enum JwtTokenType {
        ACCESS, REFRESH;
    }

}
