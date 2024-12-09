package com.tripmate.account.security.guest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class GuestUserDetails implements UserDetails {
    private String guestId;
    private String guestPwd;
    private Set<GrantedAuthority> roleEntitySet;


    public GuestUserDetails(String username, String password
            , Set<GrantedAuthority> authoritySet) {
        this.guestId = username;
        this.guestPwd = password;
        this.roleEntitySet = authoritySet;

    }
//⭐⭐ Collection<String> collection = set;  :  우항기준으로 생각하기! Set은 Collection으로 업캐스팅 가능

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleEntitySet;
    }

    @Override
    public String getPassword() {
        return guestPwd;
    }

    @Override
    public String getUsername() {
        return guestId;
    }


    // 기본적으로 계정이 만료되지 않음
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠기지 않음
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명이 만료되지 않음
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화되어 있음
    @Override
    public boolean isEnabled() {
        return true;
    }
}
