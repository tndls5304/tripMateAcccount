package com.tripmate.account.guest.repository;

import com.tripmate.account.common.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTbRepository extends JpaRepository<UserEntity, String> {
}



