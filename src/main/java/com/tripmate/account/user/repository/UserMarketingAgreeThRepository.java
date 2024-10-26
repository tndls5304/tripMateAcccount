package com.tripmate.account.user.repository;

import com.tripmate.account.common.custom.validation.AgreeFl;
import com.tripmate.account.common.entity.MarketingAgreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserMarketingAgreeThRepository extends JpaRepository<MarketingAgreeEntity, String> {
    @Query("""
            SELECT m
            FROM  MarketingAgreeEntity m
            WHERE
            m.agreeSq LIKE CONCAT(:partOfPk, '%')
            AND m.templateSq=:templateSq
            AND m.agreeFl= :agreeFl
            """)
    List<MarketingAgreeEntity> findByAccountInfo(
            @Param("partOfPk") String partOfPk,
            @Param("templateSq") String templateSq,
            @Param("agreeFl") AgreeFl agreeFl);
}
/*
쿼리 스캔 할때 순서???? 어떻게 해야하지..?
 쿼리 최적화: PK 우선 검색
agreeSq가 findKey + 날짜로 구성되어 있으므로, 만약 LIKE 조건을 통해 findKey 부분만 알고 있을 때, PK의 일부로도 효율적인 검색이 가능합니다. agreeSq 컬럼을 LIKE로 검색할 경우, 데이터베이스는 PK 인덱스를 활용하여 빠르게 검색 범위를 줄일 수 있
 */