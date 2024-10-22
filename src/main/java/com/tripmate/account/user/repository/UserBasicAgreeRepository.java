package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.BasicAgreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface UserBasicAgreeRepository  extends JpaRepository<BasicAgreeEntity, String> {
}
