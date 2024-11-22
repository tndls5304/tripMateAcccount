package com.tripmate.account.jwt;

import com.tripmate.account.common.entity.UserEntity;
import com.tripmate.account.security.AllAccountDetails;
import com.tripmate.account.security.GeneralUserDetailsEntity;
import com.tripmate.account.user.repository.RoleThRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class JwtTokenProvider {
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

    public JwtTokenProvider(RefreshTokenRepository refreshTokenRepository, RoleThRepository roleThRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.roleThRepository = roleThRepository;
    }

    //저장된 리프레쉬토큰이 시간이 유효한지 검사함.
    public Boolean isValidSavedRefreshToken(RefreshTokenInfo savedRefreshToken) {
        return savedRefreshToken.getExpireTime().isAfter(LocalDateTime.now());
    }

    public Date calculateTokenExpiry(boolean isRefreshToken) {
        Date now = new Date();
        long validity = isRefreshToken ? refreshTokenValidTime : accessTokenValidTime;
        return new Date(now.getTime() + validity);  // 밀리초의 현재시간+ 유효시간 설정 후 서명 메서드 호출
    }

    // RefreshToken을 이용하여 AccessToken을 재발급하는 메서드
    public JwtToken createAccessToken( RefreshTokenInfo validRefreshToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(validRefreshToken.getRefreshToken())
                .getBody();
        String userId = claims.getSubject();

        //사용자 권한 가져오기 가져오기 전에 사용자 타입을 알아내기.
        Optional<UserEntity> optionalUser = roleThRepository.findRoleCodeByUserTypeAndId(, );
        UserEntity user = optionalUser.orElseThrow(() -> new RuntimeException("사용자를 찾을수 없음"));


        Date now = new Date();
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                // 헤더 설정
                .setSubject(userId)
                .claim("id", userId)
                .claim("roles", user.get)               // 역할 등을 클레임으로 추가
                .claim("iss", "serverName")                              //TODO 발급자(issuer) 설정 서버명으로 하기
                .setIssuedAt(now)                                             // 발급 시간(issuedAt) 설정
                .setExpiration(calculateTokenExpiry(false))       // 만료 시간(expiration) 설정
                //   .claim("aud", 특정서비스) //특정 서비스에서만 사용되는 토큰이라면 aud를 설정하는 것이 유용
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 설정 (HS256 알고리즘 사용)
                .compact();  // 최종적으로 JWT 생성

    }

    public List<String> getRolesList(GeneralUserDetailsEntity userDetails) {
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
    }

    public JwtToken createAllTokenAndSaveRefreshToken(AllAccountDetails allAccountDetails) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                // 헤더 설정
       //         .setSubject()
                .claim("id", allAccountDetails.getId())
                .claim("iss", "serverName")                              //TODO 발급자(issuer) 설정 서버명으로 하기
                .claim("clientType", allAccountDetails.getAccountType())
                .setIssuedAt(now)                                             // 발급 시간(issuedAt) 설정
                .setExpiration(calculateTokenExpiry(false))       // 만료 시간(expiration) 설정
                //   .claim("aud", 특정서비스) //특정 서비스에서만 사용되는 토큰이라면 aud를 설정하는 것이 유용
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 설정 (HS256 알고리즘 사용)
                .compact();  // 최종적으로 JWT 생성

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)                        // 헤더 설정
                .setIssuedAt(now)                                                   // 토큰 발급 시각 설정
                .setExpiration(calculateTokenExpiry(true))
                .claim("id", allAccountDetails.getId())
                .claim("roles", getRolesList(userDetails))  // 사용자 권한을 roles로 설정
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        refreshTokenRepository.save(new RefreshTokenInfo(userDetails.getUsername(), refreshToken));
        return new JwtToken(accessToken, refreshToken);
    }


}
