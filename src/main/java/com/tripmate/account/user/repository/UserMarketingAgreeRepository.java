package com.tripmate.account.user.repository;

import com.tripmate.account.common.entity.MarketingAgreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface UserMarketingAgreeRepository  extends JpaRepository<MarketingAgreeEntity, String> {
}
