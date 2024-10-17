package com.tripmate.account.user.service;

import com.tripmate.account.common.entity.MarketingAgreeEntity;
import com.tripmate.account.common.entity.RequireAgreeEntity;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.RequireAgreeReqDto;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinReqDto;
import com.tripmate.account.common.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public void userJoin(UserJoinReqDto userJoinRequestDto) {
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

        // 필수 약관 동의 리스트 가져오기
        List<RequireAgreeReqDto> requireAgreeList = userJoinRequestDto.getRequireAgreeList();

        // 변환된 엔티티 리스트를 저장할 곳
        List<RequireAgreeEntity> requireAgreeEntities = new ArrayList<>();

        // for문을 사용하여 각각의 DTO를 엔티티로 변환
        for (RequireAgreeReqDto requireAgreeDto : requireAgreeList) {
            RequireAgreeEntity requireAgreeEntity = RequireAgreeEntity.builder()
                    .agreeFl(requireAgreeDto.getAgreeFl())
                    .templateSq(requireAgreeDto.getTemplateSq())
                    .agreeDt(requireAgreeDto.getAgreeDt())
                    .build();

            // 변환된 엔티티를 리스트에 추가
            requireAgreeEntities.add(requireAgreeEntity);
        }

        // 변환된 엔티티 리스트를 저장 또는 다른 로직 처리
        saveRequiredAgreements(requireAgreeEntities);

        if (savedEntity == null) {
            throw new ServerErrorException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


//    public ResponseEntity<CommonResponse<Void>> registerAgree() {
//    }
}
