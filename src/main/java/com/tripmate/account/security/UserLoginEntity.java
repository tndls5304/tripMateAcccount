package com.tripmate.account.security;

import com.tripmate.account.common.entity.RoleEntity;
import com.tripmate.account.common.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserLoginEntity implements UserDetails {
    private String userId;
    private String userPwd;
    private Set<RoleEntity> roleEntities;

    public UserLoginEntity(UserEntity userEntity){
        this.userId=userEntity.getUserId();
        this.userPwd=userEntity.getUserPwd();
        this.roleEntities =userEntity.getRoleEntities();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    //    return roleEntities;
        // Role을 SimpleGrantedAuthority로 변환
        return roleEntities.stream()
                .map(roleEntity -> new SimpleGrantedAuthority(roleEntity.getAuthority()))
                .collect(Collectors.toSet());
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
