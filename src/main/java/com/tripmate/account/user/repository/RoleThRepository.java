package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.RoleHistoryEntity;
import com.tripmate.account.common.entity.id.RoleHistoryId;
import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.enums.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleThRepository extends JpaRepository<RoleHistoryEntity, RoleHistoryId> {
@Query("""
    SELECT rh.id.roleCode
    FROM RoleHistoryEntity rh
    WHERE rh.id.roleTargetType = :roleTargetType
    AND   rh.id.roleTarget= :roleTarget
    """)
    List<RoleCode> findRoleCodeByUserTypeAndId(@Param("roleTargetType") AccountType roleTargetType
                                             , @Param("roleTarget")String roleTarget);


}
