package com.tripmate.account.jwt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity

public class RefreshTokenInfo {
    @Id
    private String userIdRole;
    private String refreshValue;

    public RefreshTokenInfo() {
    }

    public RefreshTokenInfo(String userIdRole, String refreshValue) {
        this.userIdRole = userIdRole;
        this.refreshValue = refreshValue;
    }

    //getter setter
    public String getUserIdRole() {
        return userIdRole;
    }

    public void setUserIdRole(String userIdRole) {
        this.userIdRole = userIdRole;
    }

    public String getRefreshValue() {
        return refreshValue;
    }

    public void setRefreshValue(String refreshValue) {
        this.refreshValue = refreshValue;
    }

}
