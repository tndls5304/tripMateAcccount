package com.tripmate.account.config.security;

import org.springframework.security.core.GrantedAuthority;

public class Role  implements GrantedAuthority {
    private String code; // 예: RU00, RP00, RA00 등
    private String description; // 예: 일반 회원, 일반 비즈니스 회원 등


    // 생성자
    public Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 권한 코드를 반환하는 메서드 (스프링 시큐리티에서 권한 이름으로 사용됨)
    @Override
    public String getAuthority() {
        return "ROLE_" + code;  // ROLE_RU00, ROLE_RA01 등으로 변환
    }

    // Getter 메서드
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
