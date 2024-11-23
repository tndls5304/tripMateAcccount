package com.tripmate.account.security.guestSecurity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class GuestUserDetails implements UserDetails{
    private String userId;
    private String userPwd;
    private Set<GrantedAuthority> roleEntitySet;



    public GuestUserDetails(String username, String password, String nickname
                                    , Set<GrantedAuthority> authoritySet) {
        this.userId = username;
        this.userPwd = password;
        this.roleEntitySet = authoritySet;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleEntitySet;
    }

    @Override
    public String getPassword() {
        return userPwd;
    }

    @Override
    public String getUsername() {
        return userId;
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
