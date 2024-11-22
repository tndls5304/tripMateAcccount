package com.tripmate.account.security;

import com.tripmate.account.common.enums.AccountType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class AllAccountDetails implements UserDetails {
    String id;
    AccountType accountType;
    String pwd;
    private Set<GrantedAuthority> roleEntitySet;

    @Override
    public String getUsername() {
        return id;
    }

    @Override                   //TODO 비밀번호는 여기서 가져오면 안되는데 그냥 두는게 나을까?
    public String getPassword() {
        return pwd;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
