package com.tripmate.account.user.service;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private final UserManageRepository repository;

    public void userJoin(UserJoinDto userJoinDto) {
        /*Due to the characteristics of jpa, ID duplication check is required.
         existsById(): If userId already exists, the value is true */
        if (repository.existsById(userJoinDto.getUserId())) {
            throw new InvalidRequestException(CommonErrorCode.USER_ALREADY_EXISTS);
        }

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

//    public ResponseEntity<CommonResponse<Void>> registerAgree() {
//    }
}
