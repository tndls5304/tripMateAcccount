package com.tripmate.account.guest.repository;

import com.tripmate.account.common.entity.BasicAgreeEntity;
import com.tripmate.account.common.entity.compositekey.BasicAgreeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestBasicAgreeThRepository extends JpaRepository<BasicAgreeEntity, BasicAgreeId> {
}
