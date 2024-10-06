package com.tripmate.account.user.service;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private final UserManageRepository repository;

    public void userJoin(UserJoinDto userJoinDto) {
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(userJoinDto.getUserId())
                .userPwd(userJoinDto.getUserPwd())
                .nickname(userJoinDto.getNickname())
                .phoneNo(userJoinDto.getPhoneNo())
                .emailId(userJoinDto.getEmailId())
                .emailDomain(userJoinDto.getEmailDomain())
                .regUser(userJoinDto.getUserId())
                // .regDtm(LocalDateTime.now())
                //.pwdUpdDt(LocalDate.now())
                .build();
        UserEntity savedEntity = repository.save(userJoinEntity);
        if (savedEntity == null) {
            throw new ServerErrorException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Boolean userDuplicateTest(String userId) {
        return repository.existsById(userId);
    }
}
