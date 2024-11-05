package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTbRepository extends JpaRepository<UserEntity, String> {
}
