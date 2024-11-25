package com.tripmate.account.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;

/**
 * GrantedAuthority 인터페이스를 구현하며, 사용자 권한(Authority)을 제공
 */
@Entity
@Table(name = "ROLE_TB")
public class RoleEntity implements GrantedAuthority {
    @Id
    @Column(name = "ROLE_CD", nullable = false)
    private String code; // 예: RG00, RP00, RA00 등
    @Column(name = "ROLE_NM", nullable = false)
    private String description; // 예: 일반 회원, 일반 비즈니스 회원 등


    // 생성자
    public RoleEntity(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 권한 코드를 반환하는 메서드 (스프링 시큐리티에서 권한 이름으로 사용됨)
    @Override
    public String getAuthority() {
        return "ROLE_" + code;  // ROLE_RG00, ROLE_RA01 등으로 변환
    }

    // Getter 메서드
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
