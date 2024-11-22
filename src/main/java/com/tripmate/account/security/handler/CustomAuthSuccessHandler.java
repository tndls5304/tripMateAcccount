package com.tripmate.account.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.jwt.JwtToken;
import com.tripmate.account.jwt.JwtTokenProvider;
import com.tripmate.account.jwt.RefreshTokenInfo;
import com.tripmate.account.jwt.RefreshTokenRepository;
import com.tripmate.account.security.AllAccountDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import java.io.IOException;
import java.util.Optional;

/**
 * 로그인 인증 성공시 호출되는 핸들러
 * 사용자가 성공적으로 인증되면, 리디렉션 대신 인증 성공 메시지를 JSON 형식으로 클라이언트에게 반환합니다.
 * 인증 성공 시 응답에는 기본적으로 HTTP 상태 코드 200과 함께 성공 메시지가 포함됩니다.
 */
@Slf4j
public class CustomAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public CustomAuthSuccessHandler(ObjectMapper objectMapper, JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository) {
        this.objectMapper = objectMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //디비에 리프레쉬토큰이 존재해요 그러면 먼저 만료됐는지 확인하기 -> 만료 됐으면 액세스+리프레쉬토큰 모두 생성  -> 만료안됐으면 액세스토큰만 발급하기

      //TODO AllAccountDetail 타입으로 업캐스팅한 후에도 자식 클래스에 정의된 메서드나 필드를 사용할 수 있습니다. 다만, 업캐스팅한 후에는 기본적으로 부모 클래스(AllAccountDetail)에서 정의된 메서드만 접근할 수 있습니다. 자식 클래스에 정의된 메서드를 사용하려면, 다시 다운캐스팅을 해야 합니다.

        AllAccountDetails allAccountDetails = (AllAccountDetails) authentication.getPrincipal(); //자식인host,user인데 업캐스팅하기
        Optional<RefreshTokenInfo> optionalRefreshToken = refreshTokenRepository.findById(authentication.getName());
        JwtToken newToken;
        //1.리프레쉬토큰이 디비에 존재하는지 검사부터 하기.
        //2.존재한다면 시간이 휴요한지 검사 유효하다면 Access토큰만발행.   유효하지 않는다면 토큰두개 발행
        //존재하지 않는다면 토큰두개 발행.
        if (optionalRefreshToken.isPresent()) {
            RefreshTokenInfo savedRefreshToken = optionalRefreshToken.get();
            newToken = jwtTokenProvider.isValidSavedRefreshToken(savedRefreshToken)
                    ? jwtTokenProvider.createAccessToken(savedRefreshToken)
                    : jwtTokenProvider.createAllTokenAndSaveRefreshToken(allAccountDetails);
        } else {
            newToken=   jwtTokenProvider.createAllTokenAndSaveRefreshToken(allAccountDetails);
        }


        // 응답 헤더에 토큰 포함 (예: Authorization 헤더 사용)
        response.setHeader("Authorization", "Bearer " + newToken.getAccessToken());
        response.setHeader("Refresh-Token", newToken.getRefreshToken());

        // 로그에 기록
        log.info("Authentication successful for user: " + authentication.getName());

        CommonResponse<Void> commonResponse = new CommonResponse<>(CommonErrorCode.SUCCESS);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        objectMapper.writeValue(response.getWriter(), commonResponse);
    }
}

/**
 * 리프레쉬 토큰을 쿠키에 포함하라고 하는데 맞나?
 * // Refresh Token은 HttpOnly 쿠키로 포함
 * Cookie refreshTokenCookie = new Cookie("refreshToken", newToken.getRefreshToken());
 * refreshTokenCookie.setHttpOnly(true);
 * refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
 * refreshTokenCookie.setPath("/");
 * refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
 * response.addCookie(refreshTokenCookie);
 */