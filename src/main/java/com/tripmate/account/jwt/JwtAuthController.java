package com.tripmate.account.jwt;

import com.tripmate.account.jwt.dto.JwtTokenReqDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/*

- 억세스토큰 만료시 재발행 요청
클라이언트로부터 억세스토큰, 리프레쉬토큰 정보로 토큰 갱신을 요청 받는다 ((DTO에 두토큰을 담아서 보냄))

 */

@RestController
public class JwtAuthController {
    private final JwtAuthService service;

    public JwtAuthController(JwtAuthService service) {
        this.service = service;
    }

    @PostMapping("api/jwt/refresh")
    public void refreshToken(@RequestBody JwtTokenReqDto jwtTokenReqDto) {
        //클라이언트에서 준 두토큰이 맞는지 검증한 후 새로운 토큰을 만든다
        //디비에 업데이트 한다
        String accessToken=jwtTokenReqDto.getAccessToken();
        String userRefresh=jwtTokenReqDto.getRefreshToken();
        service.reCreateJwtToken(accessToken,userRefresh);
        //액세스토큰, 리프레쉬토큰을 응답값으로 전달한다
    }

}
