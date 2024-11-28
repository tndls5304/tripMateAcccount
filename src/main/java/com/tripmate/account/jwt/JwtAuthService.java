package com.tripmate.account.jwt;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.filter.exception.JwtValidateException;
import com.tripmate.account.jwt.dto.JwtTokenReqDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class JwtAuthService {

    @Value("${jwt.access-token-valid-time}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-valid-time}")
    private long refreshTokenValidTime;

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtAuthService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    //처음 로그인할때 jwt생성하기
    // 클라이언트가 토큰을 가지고 있던 말던 서버에서 access, refresh토큰을 새로 만든다. db에 리프레시토큰만 저장하고 (userId+권한,리프레시토큰값)

    public JwtToken processJwtWhenLogin(String userId, List<String> roles) {
        String accessToken = createAccessToken(userId, roles);
        String refreshToken = createRefreshToken(userId, roles);

        String keyOfDb = createKeyOfDb(userId, roles);
        refreshTokenRepository.save(new RefreshTokenInfo(keyOfDb, refreshToken));

        return new JwtToken(accessToken, refreshToken);
        //TODO 순서대로 매칭이 되겠나?
    }

    private String createKeyOfDb(String userId, List<String> roles) {
        if (roles.contains("RG00")) {
            return userId + "RG00";
        } else if (roles.contains("RH00")) {
            return userId + "RH00";
        } else if (roles.contains("RA00")) {
            return userId + "RA00";
        }
        return userId + "DEFAULT";
    }

    private Claims parseAccessTokenWhenExpiry(String reqAccessToken) {
        //클라이언트 access토큰 파싱하면 만료됐으니  ExpiredJwtException 가 발생하면
        //	   	     access토큰의 클레임에서 발급일, 만료일을 뺀다 만료돼지 않아도 괜찮음
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(reqAccessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (IllegalArgumentException | UnsupportedJwtException | MalformedJwtException e) {
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_INVALID_FORMAT);
        } catch (SignatureException e) {
            //토큰의 서명이 올바르지 않은 경우 발생합니다. 예를 들어, 서명 키가 잘못되었거나, 토큰이 클라이언트에서 변조된 경우.
            //토큰이 변조되었거나 위조된 가능성을 판단하고 요청을 차단해야 합니다.
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_UNKNOWN_ERROR); // 기타 알 수 없는 오류
        }
    }

    public JwtToken reCreateJwtToken(String reqAccessToken, String reqRefreshToken) {
        //-----클라이언트측  refresh토큰  VS 2번서버측 refresh토큰을 비교하기-----

        // 클라이언트측의 refresh토큰을 파싱하고	 id와 role 뺀다 (만약 ExpiredJwtException오류 발생한다면 재로그인 오류 코드 전달)
        Claims reqRefreshClaims = parseRefreshToken(reqRefreshToken);
        String id = reqRefreshClaims.getSubject();
        List<String> roles = reqRefreshClaims.get("roles", List.class);
        String key = createKeyOfDb(id, roles);

        RefreshTokenInfo savedRefreshTokenInfo = refreshTokenRepository.findById(key)
                .orElseThrow(() -> new JwtValidateException(CommonErrorCode.JWT_SAVED_REFRESH_TOKEN_NOT_FOUND));

        String serverRefreshToken = savedRefreshTokenInfo.getRefreshValue();

        if (!StringUtils.equals(reqRefreshToken, serverRefreshToken)) {
            log.warn("Refresh token mismatch for clientAccessId : {},rolesOfClientAccess:{}. clientAccessRoles : {}, Server token: {}",
                    id, roles, reqRefreshToken, serverRefreshToken);
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_MISMATCH);
        }


        //클라이언트측  요청 access 토큰  VS 서버측 access 토큰을 비교하기
        Claims reqAccessClaims = parseAccessTokenWhenExpiry(reqAccessToken);
        //토큰은 만료됐으니 ExpiredJwtException 가 발생하면 access토큰의 클레임에서 발급일, 만료일을 뺀다

        /*
        서버측 access토큰 준비하기
                1)클라이언트측 access토큰의 발급일,만료일은 그대로 넣어준다. + 클라이언트측 refresh토큰에서 얻은 id,role을 넣는다

         */
        String serverAccessToken = createServerAccessToken(reqAccessClaims, reqRefreshClaims);

        if (!StringUtils.equals(reqAccessToken, serverAccessToken)) {
            //서버에 저장된 access토큰과 클라이언트측 access토큰이 다르면 오류내뱉기
        }
        //동일하다면  억세스토큰, 리프레쉬토큰을 새로 생성한다
        String newAccess = createAccessToken(id, roles);
        String newRefresh = createRefreshToken(id, roles);
        //리프레쉬 토큰을 DB에 업데이트하기

        refreshTokenRepository.save(new RefreshTokenInfo(key, newRefresh));
        return new JwtToken(newAccess, newRefresh);

        String clientAccessId = reqAccessClaims.getId();
        List<String> clientAccessRoles = reqAccessClaims.get("role", List.class);


        key = createKeyOfDb(clientAccessId, clientAccessRoles);

        RefreshTokenInfo savedRefreshTokenInfo = refreshTokenRepository.findById(key)
                .orElseThrow(() -> new JwtValidateException(CommonErrorCode.JWT_SAVED_REFRESH_TOKEN_NOT_FOUND));

        String serverRefreshToken = savedRefreshTokenInfo.getRefreshValue();
        //서버에 저장된 토큰과 비교후 다르면 오류
        if (!StringUtils.equalsIgnoreCase(reqRefreshToken, serverRefreshToken)) {
            log.warn("Refresh token mismatch for clientAccessId : {},rolesOfClientAccess:{}. clientAccessRoles : {}, Server token: {}",
                    clientAccessId, clientAccessRoles, reqAccessToken, serverRefreshToken);
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_MISMATCH);
        }
        Claims reqRefreshClaims = parseRefreshToken(reqRefreshToken);
        String serverAccessToken = createServerAccessToken(reqAccessClaims, reqRefreshClaims);

        if (!StringUtils.equals(reqAccessToken, serverAccessToken)) {
            //서버에 저장된 access토큰과 클라이언트측 access토큰이 다르면 오류내뱉기
        }
        //클라이언트와 서버측이 완전히 토큰이 일치하는거니까 토큰 두개를 새걸로 생성해준다
        String newAccess = createAccessToken(clientAccessId, clientAccessRoles);
        String newRefresh = createRefreshToken(clientAccessId, clientAccessRoles);

        key = createKeyOfDb(clientAccessId, clientAccessRoles);
        refreshTokenRepository.save(new RefreshTokenInfo(key, newRefresh));
        return new JwtToken(newAccess, newRefresh);
    }


    private String createServerAccessToken(Claims clientAccessClaims, Claims clientRefreshTokenClaims) {
        //(reqAccessClaims, reqRefreshClaims);
        Date issuedAtOfClientAccess = clientAccessClaims.getIssuedAt();
        Date expirateOfClientAccess = clientAccessClaims.getExpiration();

        String idOfClientRefreshToken = clientRefreshTokenClaims.getSubject();
        List<String> rolesOfClientRefresh = clientRefreshTokenClaims.get("roles", List.class);


        return Jwts.builder()
                .setSubject(idOfClientRefreshToken)
                .claim("roles", rolesOfClientRefresh)
                .setIssuedAt(issuedAtOfClientAccess)
                .setExpiration(expirateOfClientAccess)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }


    private String createAccessToken(String userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    private String createRefreshToken(String userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidTime); // 7일 유효

        return Jwts.builder()
                .setSubject(userId)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // acceess 토큰 파싱 및 Claims 반환 반환중에 access토큰이 만료되면 오류를 내뱉으면서 클라이언트에게 access, refresh 모두 서버로 보내게 해야함
    public Claims parseAccessToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_EXPIRED);     //access토큰이 만료될시에 자동으로 새 Access 토큰을 발급받아 사용자가 끊김 없이 서비스를 이용하게끔 해야함. 클라이언트는 서버로 access토큰 발급 요청을 보내야함
        } catch (IllegalArgumentException | UnsupportedJwtException | MalformedJwtException e) {
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_INVALID_FORMAT);
        } catch (SignatureException e) {
            //토큰의 서명이 올바르지 않은 경우 발생합니다. 예를 들어, 서명 키가 잘못되었거나, 토큰이 클라이언트에서 변조된 경우.
            //토큰이 변조되었거나 위조된 가능성을 판단하고 요청을 차단해야 합니다.
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new JwtValidateException(CommonErrorCode.JWT_ACCESS_TOKEN_UNKNOWN_ERROR); // 기타 알 수 없는 오류
        }
    }

    // refresh 토큰 파싱 및 Claims 반환
    /*
    IllegalArgumentException:토큰 없음 (토큰 값이 null, 비어 있는 문자열, 또는 공백 문자열일 때 발생)
    UnsupportedJwtException :토큰자체가 지원되지 않는 형식인 경우 발생 예를 들어, JWS(서명된 토큰)가 아닌 JWE(암호화된 토큰)를 사용하는 경우.
    MalformedJwtException :JWT의 형식이 잘못된 경우 발생합니다예를 들어, 토큰이 헤더.페이로드.서명 형식을 따르지 않는 경우.
     */
    public Claims parseRefreshToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_EXPIRED);  //토큰만료.클레임에 포함된 exp 필드 값이 현재 시간보다 이전인 경우 에러발생 refresh토큰 만료로 다시 로그인 요청 메세지보내기
        } catch (IllegalArgumentException | UnsupportedJwtException | MalformedJwtException e) {
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_INVALID_FORMAT);
        } catch (SignatureException e) {
            //토큰의 서명이 올바르지 않은 경우 발생합니다. 예를 들어, 서명 키가 잘못되었거나, 토큰이 클라이언트에서 변조된 경우.
            //토큰이 변조되었거나 위조된 가능성을 판단하고 요청을 차단해야 합니다.
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_UNKNOWN_ERROR); // 기타 알 수 없는 오류
        }
    }
}
/*
access토큰을 갱신할때
refresh토큰도 시간이 유효하더라도
갱신하는데 이유는?

갱신하지 않는 경우의 문제점----------------------------------------------
(1) Refresh 토큰 유출 위험 증가
유효 기간 동안 Refresh 토큰이 변하지 않으면, 유출된 Refresh 토큰으로 Access 토큰을 반복 생성할 수 있습니다.
갱신 과정을 통해 Refresh 토큰이 주기적으로 바뀌면, 이전 토큰이 유출되어도 곧 무효화됩니다.
(2) 서버의 제어력 감소
Refresh 토큰 갱신이 없으면, 서버가 세션을 제어할 기회가 줄어듭니다.
예를 들어, 비밀번호 변경, 로그아웃, 혹은 보안 정책 변경 시 영향을 주기 어렵습니다.
 */
