package com.tripmate.account.user.service;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinRequestDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.tripmate.account.common.errorcode.CommonErrorCode.SUCCESS;
import static com.tripmate.account.common.errorcode.CommonErrorCode.USER_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class UserManageService {
    private final UserManageRepository repository;

    public CommonResponse<Void> checkUserIdDuplicate(String userId) {
        if (repository.existsById(userId)) {
            return new CommonResponse<Void>(USER_ALREADY_EXISTS);
        }
        return new CommonResponse<Void>(SUCCESS);
    }

    public void userJoin(UserJoinRequestDto userJoinRequestDto) {
        /*existsById(): If userId already exists, the value is true

        JPA의 save() 는 entity의 ID가 존재할경우 기존 entity로 간주하고 update를 수행하는 특성이 있기떄문에
        save()가 호출되기 전에, 입력된 ID가 이미 존재하는지 확인해야 함
         */
        if (repository.existsById(userJoinRequestDto.getUserId())) {
            throw new InvalidRequestException(USER_ALREADY_EXISTS);
        }

        UserEntity userJoinEntity = UserEntity.builder()
                .userId(userJoinRequestDto.getUserId())
                .userPwd(userJoinRequestDto.getUserPwd())
                .nickname(userJoinRequestDto.getNickname())
                .phoneNo(userJoinRequestDto.getPhoneNo())
                .emailId(userJoinRequestDto.getEmailId())
                .emailDomain(userJoinRequestDto.getEmailDomain())
                .regUser(userJoinRequestDto.getUserId())
                // .regDtm(LocalDateTime.now())
                //.pwdUpdDt(LocalDate.now())
                .build();
        UserEntity savedEntity = repository.save(userJoinEntity);
        if (savedEntity == null) {
            throw new ServerErrorException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


//    public ResponseEntity<CommonResponse<Void>> registerAgree() {
//    }
}
