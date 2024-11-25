package com.tripmate.account.security.guestSecurity.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.jwt.JwtToken;
import com.tripmate.account.jwt.GuestJwtTokenProvider;
import com.tripmate.account.jwt.RefreshTokenInfo;
import com.tripmate.account.jwt.RefreshTokenRepository;
import com.tripmate.account.security.guestSecurity.GuestUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import java.io.IOException;
import java.util.Optional;

/**
 * 로그인 인증 성공시 호출되는 핸들러
 * 게스트가 성공적으로 인증되면, 리디렉션 대신 인증 성공 메시지를 JSON 형식으로 클라이언트에게 반환합니다.
 * 인증 성공 시 응답에는 기본적으로 HTTP 상태 코드 200과 함께 성공 메시지가 포함됩니다.
 */
@Slf4j
public class GuestAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final GuestJwtTokenProvider guestJwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public GuestAuthSuccessHandler(ObjectMapper objectMapper, GuestJwtTokenProvider guestJwtTokenProvider, RefreshTokenRepository refreshTokenRepository) {
        this.objectMapper = objectMapper;
        this.guestJwtTokenProvider = guestJwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    //1.리프레쉬토큰이 디비에 존재하는지 검사부터 하기.
    //2.존재한다면 시간이 휴요한지 검사 유효하다면 Access토큰만발행.   유효하지 않는다면 토큰두개 발행
    //존재하지 않는다면 토큰두개 발행.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("************성공핸들러 호출됐슴다~~~~~~~~~~~~~~~~~");
        GuestUserDetails guestUserDetails= (GuestUserDetails) authentication.getPrincipal();
        Optional<RefreshTokenInfo> optionalRefreshToken = refreshTokenRepository.findById(guestUserDetails.getUsername());
        JwtToken newToken;

        if (optionalRefreshToken.isPresent()) {
            RefreshTokenInfo savedRefreshToken = optionalRefreshToken.get();
            newToken = guestJwtTokenProvider.isValidSavedRefreshToken(savedRefreshToken)
                    ? guestJwtTokenProvider.createAccessToken(savedRefreshToken)
                    : guestJwtTokenProvider.createAllTokenAndSaveRefreshToken(guestUserDetails);
        } else {
            newToken=   guestJwtTokenProvider.createAllTokenAndSaveRefreshToken(guestUserDetails);
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