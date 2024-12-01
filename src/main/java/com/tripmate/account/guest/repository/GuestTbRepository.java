package com.tripmate.account.guest.repository;

import com.tripmate.account.common.entity.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestTbRepository extends JpaRepository<GuestEntity, String> {
}



