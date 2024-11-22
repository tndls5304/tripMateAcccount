package com.tripmate.account.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenInfo,String> {
    // 사용자의 리프레시 토큰 조회
   // Optional<RefreshTokenInfo> findByUserId(String userId);

    // 사용자에 대한 리프레시 토큰 저장
  //  RefreshTokenInfo save(RefreshTokenInfo refreshTokenInfo);

}
