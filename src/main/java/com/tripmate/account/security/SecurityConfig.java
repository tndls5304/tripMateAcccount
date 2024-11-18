package com.tripmate.account.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.security.handler.CustomAccessDeniedHandler;
import com.tripmate.account.security.handler.CustomAuthFailureHandler;
import com.tripmate.account.security.handler.CustomAuthSuccessHandler;
import com.tripmate.account.security.handler.CustomAuthenticationEntryPoint;
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
//=생성자메서드를 호출하고 자신을 스프링이관리하는 컨테이너안에 등록한다는것
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ObjectMapper objectMapper;                                                                                       //모든 컴포넌트들을 빈으로 등록시킨다음에 ->그 컴포넌트에  @Autowired가 달려있으면 이것부터 연결 다 시키고 -->@Bean달린걸 본다
    @Autowired
    private GeneralUserDetailsService generalUserDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ProviderManager customProviderManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(generalUserDetailsService);
        provider.setHideUserNotFoundExceptions(false);//⭐ID틀린걸 찾아내지못해서
        return new ProviderManager(provider);
    }

    @Bean
    public CustomAuthSuccessHandler authSuccessHandler() {
        return new CustomAuthSuccessHandler(objectMapper);
    }

    @Bean
    public CustomAuthFailureHandler authFailureHandler() {
        return new CustomAuthFailureHandler(objectMapper);
    }

    /**
     * 일반적으로 스프링 시큐리티는 폼 데이터로 요청을 받는데,JSON 데이터를 받아 로그인 인증을 진행하는 필터가 필요해서 이 필터를 빈으로 등록합니다.
     * 빈을 등록하면서 위 메서드에서 커스텀한 ProviderManager를 설정해줍니다
     * 설정하게 되면 로그인 요청이 들어올때 이 필터안에서 커스텀한 ProviderManager를 통해 이증로직을 처리하게 됩니다
     * 만약 커스텀한 ProviderManager을 설정해주지 않으면 기본 AuthenticationManager를 사용해 요청을 처리하게 됩니다.
     *
     * @return
     */
    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() {
        JsonUsernamePasswordAuthenticationFilter jsonFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonFilter.setAuthenticationManager(customProviderManager()); // AuthenticationManager 설정
        jsonFilter.setAuthenticationSuccessHandler(authSuccessHandler());
        jsonFilter.setAuthenticationFailureHandler(authFailureHandler());
        return jsonFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain generalUserSecurityFilterChain(HttpSecurity http, GeneralUserDetailsService service, AuthenticationManager authenticationManager, AuthenticationManagerBuilder authManageBuilder) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)//CSRF 보호를 비활성화(API 서버나 세션이 사용되지 않는 경우)
                .addFilterBefore(jsonUsernamePasswordLoginFilter(), UsernamePasswordAuthenticationFilter.class) // JSON 인증 필터 추가

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
