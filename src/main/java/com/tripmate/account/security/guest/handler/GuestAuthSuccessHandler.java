package com.tripmate.account.security.guest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.jwt.*;
import com.tripmate.account.security.guest.GuestUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import java.io.IOException;
import java.util.*;

/**
 * 로그인 인증 성공시 호출되는 핸들러
 * 게스트가 성공적으로 인증되면, 리디렉션 대신 인증 성공 메시지를 JSON 형식으로 클라이언트에게 반환합니다.
 * 인증 성공 시 응답에는 기본적으로 HTTP 상태 코드 200과 함께 성공 메시지가 포함됩니다.
 */
@Slf4j
public class GuestAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtAuthService jwtAuthService;
    private final ObjectMapper objectMapper;

    public GuestAuthSuccessHandler(JwtAuthService jwtAuthService, ObjectMapper objectMapper) {
        this.jwtAuthService = jwtAuthService;
        this.objectMapper = objectMapper;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        GuestUserDetails guestUserDetails= (GuestUserDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = guestUserDetails.getAuthorities();

        //Set을 List로 변환하기 위해 Collection타입을 Object타입의 배열로 복사해서 ArrayList의 멤버변수로 넣어줌
        List<GrantedAuthority> authoritiesList =  new ArrayList<>(authorities);

        // GrantedAuthority에서 실질적인 권한을 가져오기
        List<String> roleList = new ArrayList<>();
        for(GrantedAuthority authority:authoritiesList){
            String guestRole= authority.getAuthority();
            roleList.add(guestRole);
        }
        String guestId=guestUserDetails.getUsername();
        //jwt 두 토큰을 만들고 리프레쉬토큰은 저장하는 서비스 호출
        JwtToken newJwtToken=jwtAuthService.processJwtWhenLogin(guestId,roleList);

        // 응답 헤더에 토큰 포함 (예: Authorization 헤더 사용)
        response.setHeader("Authorization", "Bearer " + newJwtToken.getAccessToken());
        response.setHeader("Refresh-Token", newJwtToken.getRefreshToken());

        // 로그에 기록
        log.info("Authentication successful for user: " + authentication.getName());

        CommonResponse<Void> commonResponse = new CommonResponse<>(CommonErrorCode.SUCCESS);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        objectMapper.writeValue(response.getWriter(), commonResponse);
    }
}
/*
--------------------------------------------------------------------------------study---
Collection<? extends GrantedAuthority> authorities = guestUserDetails.getAuthorities();
List<? extends GrantedAuthority> authoritiesSet =  new ArrayList<>(authorities);
-----------------------------------------------------------------------------------------
new ArrayList<>(authorities); 내부적으로 동작하는것을 살펴봤더니
1.Object[] a = authorities.toArray();
    :toArray() 메서드는 Collection의 모든 요소를 배열로 변환한다.  이때, 결과는 Object[] 배열로 변환된다
    :예를 들어, authorities에 GrantedAuthority 타입 객체 3개가 들어있었다면
     이과정으로 authorities는 배열이 authorities = [GrantedAuthority 객체1, GrantedAuthority 객체2, GrantedAuthority객체3]  된다
     그러면 원래의 컬렉션 형태는 사라지고, 모든 요소가 Object타입의 요소를 받는 배열에 저장된다.

2. System.arraycopy(authorities, 0, elementData, 0, authorities.length);
    :ArrayList 생성자에서 배열의 요소들을 복사
     ArrayList는 전달받은 컬렉션의 요소를 내부 배열로 복사한다.

3.private transient Object[] elementData;
    :내부 배열로 복사하면 ArrayList의 멤버 변수 elementData가 생성되며, 이는 Object[] 타입의 배열이다.
     복사가 완료되면, ArrayList 내부에 Object[] 배열 형태로 데이터가 저장된다.
 */