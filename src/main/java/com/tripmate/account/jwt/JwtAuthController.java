package com.tripmate.account.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.jwt.dto.JwtTokenReqDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

/*
- 억세스토큰 만료시 재발행 요청
클라이언트로부터 억세스토큰, 리프레쉬토큰 정보로 토큰 갱신을 요청 받는다 ((DTO에 두토큰을 담아서 보냄))
 */

@RestController
public class JwtAuthController {
    private final JwtAuthService service;
    private final ObjectMapper objectMapper;

    public JwtAuthController(JwtAuthService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @PostMapping("api/jwt/reissue")
    public void reissueJwtWhenAccessExpiration(@RequestBody JwtTokenReqDto jwtTokenReqDto,  HttpServletResponse response) throws IOException {
        String accessToken=jwtTokenReqDto.getAccessToken();
        String userRefresh=jwtTokenReqDto.getRefreshToken();

        //jwt토큰 재발행하기
        JwtToken newJwtToken=service.reissueJwtToken(accessToken,userRefresh);

        // 응답 헤더에 토큰 포함 (예: Authorization 헤더 사용)
        response.setHeader("Authorization", "Bearer " + newJwtToken.getAccessToken());
        response.setHeader("Refresh-Token", newJwtToken.getRefreshToken());

        CommonResponse<Void> commonResponse = new CommonResponse<>(CommonErrorCode.SUCCESS);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        // JSON 형태로 응답
        objectMapper.writeValue(response.getWriter(), commonResponse);
    }
}


/*
개인공부
response.getWriter()는 왜 IOException을 발생하는지?
response.getWriter()를 통해 얻은 출력 스트림은 네트워크 연결을 통해 클라이언트로 데이터를 전송한다. 이 연결에서 다음과 같은 문제가 발생할 수 있다.

클라이언트 연결 끊김: 클라이언트가 요청 후 응답을 받기 전에 연결을 종료하면 IOException이 발생할 수 있음
스트림이 이미 닫힘: 출력 스트림이 이미 닫힌 상태에서 데이터를 쓰려고 시도하면 예외가 발생함.

 */