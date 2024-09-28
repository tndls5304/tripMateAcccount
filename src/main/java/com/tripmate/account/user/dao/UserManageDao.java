package com.tripmate.account.user.dao;

import com.tripmate.account.user.mapper.UserManageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserManageDao {
    private UserManageMapper mapper;
}
