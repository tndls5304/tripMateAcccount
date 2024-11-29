package com.tripmate.account.jwt;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.filter.exception.JwtValidateException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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


    //로그인할때 jwt 생성하기 클라이언트에 토큰을 유무와 상관없이 서버에서 access, refresh토큰을 새로 만든다.-> db에는 리프레시토큰만 저장한다. (userId+권한,리프레시토큰값)

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

    public JwtToken reCreateJwtToken(String clientAccessToken, String clientRefreshToken) {
        /*클라이언트가 access토큰만료로 새로운 토큰이 필요하다고 요청이 왔을때 호출되는 메서드다. 그럼 먼저 클라이언트가 보낸 access토큰과 refresh토큰정보를 파싱해 변조하진 않았는지 확인해봐야 한다.
         클라이언트가 보낸 refresh,access토큰과 서버의 access,refresh토큰을 각각 비교해보면 된다.
         첫번쨰로는 클라이언트측 refresh토큰  VS 2번서버의 refresh토큰을 비교할것이다.*/

        //1)클라이언트가 보낸 refresh 토큰을 파싱해서 id와 roles를 조회한다 (만약 refresh토큰이 만료되어 파싱중 오류가 발생한다면 재로그인하라는 메세지 전달한다)
        Claims reqRefreshClaims = parseRefreshToken(clientRefreshToken);
        String clientId = reqRefreshClaims.getSubject();
        List<String> clientRoles = reqRefreshClaims.get("roles", List.class);

        //2)클라이언트측 refresh토큰을 파싱해 알아낸 사용자 정보(id,roles)로 서버측 DB에 저장된 refresh토큰을 조회할때 키로 쓴다
        String clientInForKey = createKeyOfDb(clientId, clientRoles);

        //3) 클라이언트 refresh토큰에서 알아낸 사용자 정보(id,roles)로 서버의 refresh토큰을 조회해본다. 조회할 수 없다면 클라이언트와 서버측 refresh토큰은 다르다는것을 알수 있다. 재로그인 요청 메세지를 보낸다
        RefreshTokenInfo savedRefreshTokenInfo = refreshTokenRepository.findById(clientInForKey)
                .orElseThrow(() -> new JwtValidateException(CommonErrorCode.JWT_SAVED_REFRESH_TOKEN_NOT_FOUND));

        String serverRefreshToken = savedRefreshTokenInfo.getRefreshValue();

        //4) 클라이언트와 서버의 refresh토큰 값을 비교하고 다르다면 기록해두고 에러메세지를 보낸다
        if (!StringUtils.equals(clientRefreshToken, serverRefreshToken)) {
            log.warn("Refresh token mismatch for clientAccessId : {},rolesOfClientAccess:{}. clientAccessRoles : {}, Server token: {}",
                    clientId, clientRoles, clientRefreshToken, serverRefreshToken);
            throw new JwtValidateException(CommonErrorCode.JWT_REFRESH_TOKEN_MISMATCH);
        }

        /*.클라이언트와 서버의 refresh토큰이 일치하는걸 확인했으니 이제 클라이언트의 access토큰  VS 서버의 access토큰 비교 하기!
           비교하기에 앞서 서버의 access토큰이 필요한데 서버에는 access토큰을 저장하지 않는다. 그래서 만들어야한다.
           서버의 access토큰을 만들기위해 refresh토큰에서 알아낸 정보를 가져와 만들것이다. 그런데 클라이언트와 똑같은조건으로 비교해야하니 클라이언트의 access 토큰에서 발급일,만료일 정보를 넣어 만들것이다*/

        //1)서버의 refresh토큰에서 사용자 정보를 가져온다
        Claims serverRefreshClaims=parseRefreshToken(serverRefreshToken);

        //2)클라이언트의 access토큰에서도 사용자 정보를 가져온다. access토큰끼리 비교할때 똑같은 조건을 위해 발급일,만료일이 필요하기 떄문 (*파싱할때 만료된 상태로 요청이 올 수있으니 만료상태로 파싱할떄 오류를 내뱉지 않고 claim을 가져와야 한다.
        Claims clientAccessClaims = parseAccessTokenWhenExpiry(clientAccessToken);

        //이제 서버의 access토큰을 만들자 (서버의 refresh토큰에서 알아낸 정보를 바탕으로 만들건데 비교해야할 클라이언트의 access토큰 발급일,만료일을 입력한다)
        String serverAccessToken = createServerAccessToken(serverRefreshClaims, clientAccessClaims);

        //서버의 access토큰이 만들어졌으니 클라이언트의 access토큰과 비교한다.
        if (!StringUtils.equals(clientAccessToken, serverAccessToken)) {
            //서버에 저장된 access토큰과 클라이언트측 access토큰이 다르면 오류내뱉기
        }
        //이로써 클라이언트측 서버측 jwt는 동일하다 이제 클라이언트가 요청한 새로운 jwt토큰을 만들어준다
        String newAccess = createAccessToken(clientId, clientRoles);
        String newRefresh = createRefreshToken(clientId, clientRoles);

        //새로 생성된 jwt를 보내기전에 서버에도 저장한다.
        refreshTokenRepository.save(new RefreshTokenInfo(clientInForKey, newRefresh));
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
