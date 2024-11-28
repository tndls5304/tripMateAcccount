package com.tripmate.account.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenInfo,String> {
    // AccessToken을 기준으로 조회하는 메서드
 //   JwtToken findByAccessToken(String accessToken);


    // 사용자에 대한 리프레시 토큰 저장
  //  RefreshTokenInfo save(RefreshTokenInfo refreshTokenInfo);

}
