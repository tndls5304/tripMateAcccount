package com.tripmate.account.common.entity.id;

import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.enums.RoleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

@Builder
@Embeddable
public class RoleHistoryId {

    @Column(name ="ROLE_TARGET_TYPE")
    @Enumerated(EnumType.STRING)
    AccountType roleTargetType;

    @Column(name ="ROLE_TARGET")
    String roleTarget;

    @Column(name ="ROLE_CD")
    @Enumerated(EnumType.STRING)
    RoleCode roleCode;
}
