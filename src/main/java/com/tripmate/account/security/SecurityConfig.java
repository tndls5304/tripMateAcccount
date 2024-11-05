package com.tripmate.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.security.handler.CustomAccessDeniedHandler;
import com.tripmate.account.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final ObjectMapper objectMapper;

    private final GeneralUserDetailsService generalUserDetailsService;

    public SecurityConfig(ObjectMapper objectMapper, GeneralUserDetailsService generalUserDetailsService, AuthenticationManager authenticationManager) {
        this.objectMapper = objectMapper;
        this.generalUserDetailsService = generalUserDetailsService;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain generalUserSecurityFilterChain(HttpSecurity http, GeneralUserDetailsService service) throws Exception {


        http
                .csrf(AbstractHttpConfigurer::disable)//CSRF 보호를 비활성화(API 서버나 세션이 사용되지 않는 경우)
                .addFilter(jsonUsernamePasswordLoginFilter(authManager(http))) // JSON 로그인 필터 추가
//                .addFilterBefore(jsonUsernamePasswordLoginFilter(), UsernamePasswordAuthenticationFilter.class) // JSON 인증 필터 추가
                //   .authenticationProvider(generalUserAuthenticationProvider(service)) 매니저한테 프로바이더 등록
                .authorizeHttpRequests(auth -> auth                                             //URL 패턴별 접근 권한을 정의
                        .requestMatchers("/", "/api/account/user/join", "/api/account/user/login", "/home")
                        .permitAll()  // 인증 없이 접근 가능
                        .anyRequest().authenticated()                                        // 나머지 요청은 인증 필요
                )
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화formLogin(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 설정 (Postman 용)
                /*
                 .formLogin(form -> form
                           .loginPage("/user/login")
                    .loginProcessingUrl("/user/login_proc")
                    .defaultSuccessUrl("/home", true)
                )
                */

                .logout((logout) -> logout
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true))
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

    @Bean
    public DaoAuthenticationProvider generalUserAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(generalUserDetailsService);        //--->두개만 셋팅하면 서비스를호출함---> 사용자가 입력한 id로 db조회한 userDetails를 리턴
        provider.setPasswordEncoder(passwordEncoder()); //암호화된 비밀번호를 비교할려고
        return provider;
    }



//    @Bean  이 메서드를 제거하여 Spring이 AuthenticationManager를 자동으로 구성하게 합니다.
//    public AuthenticationManager authenticationManager() {
//        return new ProviderManager(generalUserAuthenticationProvider());
//    }



    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(generalUserAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }
    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter(AuthenticationManager authenticationManager) {
        JsonUsernamePasswordAuthenticationFilter jsonFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonFilter.setAuthenticationManager(authenticationManager); // AuthenticationManager 설정
        return jsonFilter;
    }


    // 인증되지 않은 사용자의 요청이 보안 제약에 위배되었을 때 호출되는 엔트리 포인트 정의
    // 401 Unauthorized 응답을 생성하는 데 사용
    @Bean
    public CustomAuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }
    // 인가되지 않은 사용자의 요청이 보안 제약에 위배되었을 때 호출되는 핸들러 정의
    // 403 Forbidden 응답을 생성하는 데 사용
    @Bean
    public CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper);
    }

}
