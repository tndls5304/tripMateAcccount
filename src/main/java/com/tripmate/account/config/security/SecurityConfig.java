package com.tripmate.account.config.security;
import com.tripmate.account.user.service.UserManageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(adminAuthenticationProvider(service))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/login", "/home").permitAll()  // 인증 없이 접근 가능
                        .anyRequest().authenticated()                        // 나머지 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/user/login")                                 // 커스텀 로그인 페이지 설정
                        .permitAll()                                         //: 로그인 페이지는 모든 사용자에게 열려 있습니다.
                )
                .logout(LogoutConfigurer::permitAll                           // 로그아웃 허용
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()                 //  세션 고정 공격에 대한 보호 설정
                        .maximumSessions(1)                                 // 최대 세션 수 제한
                        .maxSessionsPreventsLogin(true)                     // 최대 세션 수가 초과되면 새로운 로그인 시도를 방지합니다.
                );
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider userAuthenticationProvider(GeneralUserDetailsService service) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(service);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
