package com.tripmate.account.user.service;

import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private final UserManageRepository dao;

    public UserEntity uerJoin(UserJoinDto userJoinDto) {
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(userJoinDto.getUserId())
                .userPwd(userJoinDto.getUserPwd())
                .nickname(userJoinDto.getNickname())
                .phoneNo(userJoinDto.getPhoneNo())
                .emailId(userJoinDto.getEmailId())
                .emailDomain(userJoinDto.getEmailDomain())
                .regUser(userJoinDto.getUserId())
                .regDtm(LocalDateTime.now())
                .pwdUpdDt(LocalDate.now())
                .build();
        return dao.save(userJoinEntity);
    }
}
