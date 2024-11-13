package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.RoleHistoryEntity;
import com.tripmate.account.common.entity.id.RoleHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleThRepository extends JpaRepository<RoleHistoryEntity, RoleHistoryId> {
}
