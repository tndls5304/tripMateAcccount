package com.tripmate.account.user.service;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.InvalidErrorException;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private final UserManageRepository dao;

    public void userJoin(UserJoinDto userJoinDto) {
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(userJoinDto.getUserId())
                .userPwd(userJoinDto.getUserPwd())
                .nickname(userJoinDto.getNickname())
                .phoneNo(userJoinDto.getPhoneNo())
                .emailId(userJoinDto.getEmailId())
                .emailDomain(userJoinDto.getEmailDomain())
                .regUser(userJoinDto.getUserId())
              //  .regDtm(LocalDateTime.now())
               // .pwdUpdDt(LocalDate.now())
                .build();
        UserEntity savedEntity = dao.save(userJoinEntity);

        if (savedEntity == null) {
            throw new InvalidErrorException(CommonErrorCode.INTERNAL_SERVER_ERROR);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new CommonResponse<>(CommonErrorCode.INTERNER_SERVER_ERROR));
        }
    }

    public void userDuplicateTest(String userId) {

        boolean isDuplicate = dao.findById(userId).isPresent();
        if (isDuplicate) {
            throw new InvalidErrorException(CommonErrorCode.USER_ALREADY_EXISTS);
        }
    }
}
