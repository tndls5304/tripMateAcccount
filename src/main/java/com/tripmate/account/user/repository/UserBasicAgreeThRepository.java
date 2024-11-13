package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.BasicAgreeEntity;
import com.tripmate.account.common.entity.id.BasicAgreeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBasicAgreeThRepository extends JpaRepository<BasicAgreeEntity, BasicAgreeId> {
}
