package com.tripmate.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.jwt.GuestJwtTokenFilter;
import com.tripmate.account.jwt.JwtAuthService;
import com.tripmate.account.security.guest.GuestJsonUsernamePasswordAuthenticationFilter;
import com.tripmate.account.security.guest.GuestUserDetailsService;
import com.tripmate.account.security.guest.handler.GuestAccessDeniedHandler;
import com.tripmate.account.security.guest.handler.GuestAuthFailureHandler;
import com.tripmate.account.security.guest.handler.GuestAuthSuccessHandler;
import com.tripmate.account.security.guest.handler.GuestAuthenticationEntryPoint;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//=생성자메서드를 호출하고 자신을 스프링이관리하는 컨테이너안에 등록한다는것 생성자 안만들어도 되나...???
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ObjectMapper objectMapper;                                                                                       //모든 컴포넌트들을 빈으로 등록시킨다음에 ->그 컴포넌트에  @Autowired가 달려있으면 이것부터 연결 다 시키고 -->@Bean달린걸 본다
    @Autowired
    private GuestUserDetailsService guestUserDetailsService;
    @Autowired
    private JwtAuthService jwtAuthService;
    @Autowired
    private Validator validator;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ProviderManager 빈을 등록하면서 커스텀 AuthenticationProvider를 설정합니다.
     * DaoAuthenticationProvider를 사용하여 사용자 인증을 처리하고,
     * 비밀번호 인코딩 방식을 BCryptPasswordEncoder로 설정합니다.
     * 또한, 사용자 정보 조회를 위해 커스텀 UserDetailsService인 generalUserDetailsService를 사용합니다.
     * setHideUserNotFoundExceptions(false)는 인증 실패 시 예외를 숨기지 않도록 설정하여
     * 정확한 인증 실패 원인을 알 수 있게 합니다.(사용자 ID 틀림 여부를 명확히 드러내도록 설정)
     */
    @Bean
    public ProviderManager customProviderManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(guestUserDetailsService);
        provider.setHideUserNotFoundExceptions(false);
        return new ProviderManager(provider);
    }


    /**
     * 인증 성공 후 호출되는 핸들러를 설정합니다.
     * 인증이 성공하면 JSON 형식으로 성공 응답을 반환하는 CustomAuthSuccessHandler를 사용합니다.
     */
    @Bean
    public GuestAuthSuccessHandler authSuccessHandler() {
        return new GuestAuthSuccessHandler(jwtAuthService, objectMapper);
    }

    /**
     * 인증 실패 시 호출되는 핸들러를 설정합니다.
     * 인증에 실패하면 JSON 형식으로 실패 응답을 반환하는 CustomAuthFailureHandler를 사용합니다.
     */
    @Bean
    public GuestAuthFailureHandler authFailureHandler() {
        return new GuestAuthFailureHandler(objectMapper);
    }

    /**
     * 일반적으로 폼 데이터로 요청을 받기 떄문에 JSON 형식으로 사용자 로그인 인증을 처리하기 위한 커스텀 필터입니다.
     * 빈을 등록하면서 위 메서드에서 커스텀한 ProviderManager를 설정해줍니다
     * 커스텀 ProviderManager를 사용하여 사용자 인증 로직을 처리하고,
     * 인증 성공 및 실패 후에 각각 커스텀 핸들러를 호출합니다.
     *
     * @return 필터
     */
    @Bean
    public GuestJsonUsernamePasswordAuthenticationFilter guestJsonUsernamePasswordLoginFilter() {
        GuestJsonUsernamePasswordAuthenticationFilter jsonFilter = new GuestJsonUsernamePasswordAuthenticationFilter(validator, objectMapper);
        jsonFilter.setAuthenticationManager(customProviderManager()); // AuthenticationManager 설정
        jsonFilter.setAuthenticationSuccessHandler(authSuccessHandler());
        jsonFilter.setAuthenticationFailureHandler(authFailureHandler());
        return jsonFilter;
    }

    /**
     * 인증되지 않은 사용자의 요청이 보안 제약에 위배되었을 때 호출되는 엔트리 포인트입니다.
     * 인증이 필요한 리소스에 접근하려 할 때 인증되지 않은 사용자가 접근하면,
     * 이 엔트리 포인트가 작동하여 401 Unauthorized 응답을 반환합니다.
     */
    @Bean
    public GuestAuthenticationEntryPoint authenticationEntryPoint() {
        return new GuestAuthenticationEntryPoint(objectMapper);
    }

    /**
     * 인가되지 않은 사용자의 요청이 보안 제약에 위배되었을 때 호출되는 핸들러입니다.
     * 권한이 부족한 사용자에게 접근을 차단하고, 403 Forbidden 응답을 반환합니다.
     */
    @Bean
    public GuestAccessDeniedHandler accessDeniedHandler() {
        return new GuestAccessDeniedHandler(objectMapper);
    }

    @Bean
    public GuestJwtTokenFilter guestJwtTokenFilter(JwtAuthService jwtAuthService) {
        return new GuestJwtTokenFilter(jwtAuthService);
    }

    /**
     * HTTP 요청에 대한 보안 필터 체인을 설정하는 메서드입니다.
     * 이 설정은 Spring Security의 기본 보안 설정을 사용자 정의로 구성합니다.
     * 주요 설정 사항:
     * - CSRF 보호 비활성화
     * - JSON 인증 필터 추가
     * - URL별 접근 권한 설정
     * - 로그인 및 로그아웃 관련 설정
     * - 세션 관리 설정 (세션 고정 공격 방지, 최대 세션 수 설정)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain guestSecurityFilterChain(HttpSecurity http, GuestUserDetailsService service, AuthenticationManager authenticationManager, AuthenticationManagerBuilder authManageBuilder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)//CSRF 보호를 비활성화(API 서버나 세션이 사용되지 않는 경우)
                .addFilterBefore(guestJsonUsernamePasswordLoginFilter(), UsernamePasswordAuthenticationFilter.class) // JSON 인증 필터 추가
                .addFilterAfter(guestJwtTokenFilter(jwtAuthService), UsernamePasswordAuthenticationFilter.class)
                // 필터 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/account/guest/join", "/api/account/guest/login", "/home")
                        .permitAll()  // 회원가입, 로그인 및 /home은 인증 없이 접근 가능
                        .requestMatchers("/api/guest/**")
                        .hasRole("RG00")// "RG00" 권한을 가진 사용자만 /api/guest/** 경로 접근 가능
                        .anyRequest()
                        .authenticated()  // 나머지 요청은 인증 필요
                )
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
                // 로그아웃 설정
                .logout((logout) -> logout
                        .logoutSuccessUrl("/api/account/guest/login")// 로그아웃 후 리다이렉트 경로 설정
                        .invalidateHttpSession(true)) // 세션 무효화

                //ExceptionTranslationFilter가 예외를 처리하던중에 인증이 안되어있으면 AuthenticationEntryPoint, 권한이 없으면 AccessDeniedHandler가 호출
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint()) // 인증 실패 시 처리
                        .accessDeniedHandler(accessDeniedHandler()) // 인가 실패 시 처리
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()                 //  세션 고정 공격에 대한 보호 설정
                        .maximumSessions(1)                                 // 최대 세션 수 제한
                        .maxSessionsPreventsLogin(true)                     // 최대 세션 수가 초과되면 새로운 로그인 시도를 방지합니다.(동시 세션을 하나로 제한하여 새로운 로그인이 기존 세션을 덮어쓰지 못하게 )
                );
        return http.build();
    }
}

/*
study 필터 순서
1.인증 관련 필터
    로그인 요청은 UsernamePasswordAuthenticationFilter(커스텀)통과한다 : id, pwd 일치하면 SecurityContextHolder에 Authentication 객체가 저장된다.
    로그인 요청 이외의 api 요청은 JWT 토큰을 통해 인증을 처리하는 필터인 JwtTokenFilter(커스텀)를 통과한다. JWT 토큰을 검증하고, 유효한 토큰이 있다면  SecurityContextHolder에 Authentication 객체가 저장된다.
2.권한 검사 필터
    FilterSecurityInterceptor
    :인증이 완료된 후에, FilterSecurityInterceptor가 요청을 가로채어 권한 검사를 수행한다.
    이 필터는 SecurityContextHolder에 있는 Authentication 객체를 사용하여 사용자가 요청한 리소스를 접근할 권한이 있는지 검사한다.
    예를 들어, hasRole("RG00")처럼 설정된 권한을 검사하고, 해당 권한이 없는 사용자가 요청하면 403 Forbidden을 반환한다.

    ExceptionTranslationFilter
    :권한이 없다면 FilterSecurityInterceptor에서 AccessDeniedException이 발생하고,
    이를 ExceptionTranslationFilter가 처리하여 403 Forbidden을 반환합니다.
    인증이 되지 않았다면, **AuthenticationEntryPoint**를 통해 **401 Unauthorized**를 반환합니다.
 */