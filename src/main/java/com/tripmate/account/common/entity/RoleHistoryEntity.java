package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import com.tripmate.account.common.entity.compositekey.RoleHistoryId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Table(name = "ROLE_TH")
@NoArgsConstructor
public class RoleHistoryEntity extends BaseEntity {
    //복합키 권한대상의 타입+ 대상id+권한 코드 RG00, RP00, RA00 등
    @EmbeddedId
    RoleHistoryId id;
}

    //권한가져오기-------------------------------------------------------
    // 중복된 권한을 허용하지 않기 때문에, 같은 권한이 여러 번 추가되는 상황을 방지할 수 있습니다.

//    public Set<RoleEntity> getRoleEntities() {
//        return roleEntities;
//    }

