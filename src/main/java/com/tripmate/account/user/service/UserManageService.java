package com.tripmate.account.user.service;

import com.tripmate.account.user.dao.UserManageDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private UserManageDao dao;
}
