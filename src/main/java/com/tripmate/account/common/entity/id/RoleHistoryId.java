package com.tripmate.account.common.entity.id;

import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.enums.RoleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Embeddable
@Getter
public class RoleHistoryId {


    @Column(name ="ROLE_TARGET_TYPE")
    @Enumerated(EnumType.STRING)
    AccountType roleTargetType;

    @Column(name ="ROLE_TARGET")
    String roleTarget;

    @Column(name ="ROLE_CD")
    @Enumerated(EnumType.STRING)
    RoleCode roleCode;

public RoleHistoryId(){
}
    // 생성자, equals(), hashCode()는 기존처럼 구현해두세요.
    public RoleHistoryId(AccountType roleTargetType, String roleTarget, RoleCode roleCode) {
        this.roleTargetType = roleTargetType;
        this.roleTarget = roleTarget;
        this.roleCode = roleCode;
    }


    // 오류생김...equals()와 hashCode() 메서드를 구현해야 합니다.⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐공부하기

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleHistoryId that = (RoleHistoryId) o;
        return roleTargetType == that.roleTargetType &&
                Objects.equals(roleTarget, that.roleTarget) &&
                roleCode == that.roleCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleTargetType, roleTarget, roleCode);
    }
}
