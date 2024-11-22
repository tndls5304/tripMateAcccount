package com.tripmate.account.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//요청마다 토큰을 검증하여 사용자 인증 처리. JWT 토큰 필터를 Security Filter Chain에 추가.
public class JwtTokenAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate redisTemplate;

    public JwtTokenAuthFilter(JwtTokenProvider jwtTokenProvider, RedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 요청 헤더에서 Authorization 키의 값을 가져옴
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. JWT 토큰 추출
        // (예: "Bearer <TOKEN>" 형식의 문자열에서 "<TOKEN>" 부분만 가져옴)
        String token = jwtTokenProvider.getToken(authorizationHeader);

        // 3. 토큰이 존재하고 유효한지 확인
        if (StringUtils.hasText(token) && jwtTokenProvider.validToken(token)) {
            // 4. Redis에서 해당 토큰이 로그아웃된 상태인지 확인
            String isLogout = (String) redisTemplate.opsForValue().get(token);

            // 5. 로그아웃되지 않은 경우 처리
            if (ObjectUtils.isEmpty(isLogout)) {
                // 6. 토큰에서 사용자 인증 정보를 가져옴
                Authentication authentication = jwtTokenProvider.getAuthentication(token);

                // 7. SecurityContext에 인증 정보를 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. 다음 필터로 요청 전달 (다른 필터나 컨트롤러가 요청을 처리)
        filterChain.doFilter(request, response);
    }
}
