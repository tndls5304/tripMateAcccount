package com.tripmate.account.jwt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;

import java.time.LocalDateTime;


@Entity
@Builder
public class RefreshTokenInfo {
    @Id
    private String userId;
    private String refreshToken;
    private LocalDateTime expireTime;

    public RefreshTokenInfo() {
    }

    public RefreshTokenInfo(String userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
    }

    //getter setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    public LocalDateTime getExpireTime(){
        return expireTime;
    }
}
