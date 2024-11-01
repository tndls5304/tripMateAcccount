package com.tripmate.account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain generalUserSecurityFilterChain(HttpSecurity http, GeneralUserDetailsService service) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)//CSRF 보호를 비활성화(API 서버나 세션이 사용되지 않는 경우)
                .authenticationProvider(generalUserAuthenticationProvider(service))
                .authorizeHttpRequests(auth -> auth                                             //URL 패턴별 접근 권한을 정의
                        .requestMatchers("/","/api/account/user/join", "user/login", "/home").permitAll()  // 인증 없이 접근 가능
                        .anyRequest().authenticated()                                        // 나머지 요청은 인증 필요
                )
                .httpBasic(withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                                                         // HTTP Basic 인증 설정 (Postman 용)
                /*
                 .formLogin(form -> form
                           .loginPage("/user/login")
                    .loginProcessingUrl("/user/login_proc")
                    .defaultSuccessUrl("/home", true)
                )
                */
                .logout(LogoutConfigurer::permitAll                           // 로그아웃 허용
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()                 //  세션 고정 공격에 대한 보호 설정
                        .maximumSessions(1)                                 // 최대 세션 수 제한
                        .maxSessionsPreventsLogin(true)                     // 최대 세션 수가 초과되면 새로운 로그인 시도를 방지합니다.(동시 세션을 하나로 제한하여 새로운 로그인이 기존 세션을 덮어쓰지 못하게 )
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider generalUserAuthenticationProvider(GeneralUserDetailsService service) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);        //--->두개만 셋팅하면 서비스를호출함---> 사용자가 입력한 id로 db조회한 userDetails를 리턴
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

}
