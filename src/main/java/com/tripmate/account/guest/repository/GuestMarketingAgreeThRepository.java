package com.tripmate.account.guest.repository;

import com.tripmate.account.common.enums.AgreeFl;
import com.tripmate.account.common.entity.MarketingAgreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface GuestMarketingAgreeThRepository extends JpaRepository<MarketingAgreeEntity, String> {
    @Query("""
            SELECT m
            FROM  MarketingAgreeEntity m
            WHERE
            m.agreeSq LIKE CONCAT(:partPk, '%')
            AND m.templateSq=:templateSq
            AND m.agreeFl= :agreeFl
            """)
    List<MarketingAgreeEntity> findByAccountInfo(
            @Param("partPk") String partPk,
            @Param("templateSq") String templateSq,
            @Param("agreeFl") AgreeFl agreeFl);
}
