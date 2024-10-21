package com.tripmate.account.user.service;

import com.tripmate.account.common.entity.*;
import com.tripmate.account.common.exception.DataConflictException;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.MarketingAgreeReqDto;
import com.tripmate.account.user.dto.RequireAgreeReqDto;
import com.tripmate.account.user.repository.UserManageRepository;
import com.tripmate.account.user.dto.UserJoinReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tripmate.account.common.errorcode.CommonErrorCode.*;
import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
public class UserManageService {

    @Value("${server.name}")
    private String serverName;
    private final UserManageRepository repository;

    /**
     * 숙박회원의 아이디 중복 검사
     *
     * @param userId 입력한 아이디가 이미 존재하는지 여부를 확인
     * @return 중복된 아이디가 존재할 경우 예외발생-> 에러응답코드와 메세지를 담은 ResponseEntity 전달
     */
    public ResponseEntity<CommonResponse<Void>> checkUserIdDuplicate(String userId) {
        if (repository.existsById(userId)) {
            throw new DataConflictException(USER_ALREADY_EXISTS_CONFLICT);
        }
        return new CommonResponse<Void>().toRespNoDataEntity(SUCCESS);
    }

    /**
     * (숙박회원) 가입 요청: JPA 특성때문에 가입 전 중복검사 한 후에 회원가입함
     *
     * @param reqUserJoin 개인정보와 (필수)약관동의리스트,마케팅약관동의리스트(선택적)를 받음
     */
    public void userJoin(UserJoinReqDto reqUserJoin) {

        // JPA의 save() 는 entity의 ID가 존재할경우 기존 entity로 간주하고 update를 수행하는 특성이 있기떄문에 save()가 호출되기 전에, 입력된 ID가 이미 존재하는지 확인해야 함
        if (repository.existsById(reqUserJoin.getUserId())) {
            throw new DataConflictException(USER_ALREADY_EXISTS_CONFLICT);
        }

        //개인정보 저장
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(reqUserJoin.getUserId())
                .userPwd(reqUserJoin.getUserPwd())
                .nickname(reqUserJoin.getNickname())
                .phoneNo(reqUserJoin.getPhoneNo())
                .emailId(reqUserJoin.getEmailId())
                .emailDomain(reqUserJoin.getEmailDomain())
                .regUser(reqUserJoin.getUserId())
                .build();
        repository.save(userJoinEntity);

        // 필수 약관 동의 리스트 저장하기
        List<RequireAgreeReqDto> reqRequireAgreeList = reqUserJoin.getRequireAgreeList();
        if (reqRequireAgreeList == null || reqRequireAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_REQUIRE_AGREE_BLANK);
        }
        for (RequireAgreeReqDto reqRequireAgree : reqRequireAgreeList) {
            RequireAgreeEntity.builder()
                    .id(
                            RequireAgreeId.builder()
                                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                                    .accountId(reqUserJoin.getUserId())
                                    .templateSq(parseInt(reqRequireAgree.getTemplateSq()))
                                    .build()
                    )
                    .regUser(reqUserJoin.getUserId())
                    .agreeFl(reqRequireAgree.getAgreeFl().charAt(0))
                    .build();
        }
        // 마케팅 약관 동의 리스트 저장하기
        List<MarketingAgreeReqDto> reqMarketingAgreeList = reqUserJoin.getMarketingAgreeList();
        if (reqMarketingAgreeList == null || reqMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }
        for (int count = 0; count < reqMarketingAgreeList.size(); count++) {
            MarketingAgreeReqDto reqMarketingAgree = reqMarketingAgreeList.get(count);
            MarketingAgreeEntity.builder()
                    .agreeSq(
                            getMarketingPk(reqUserJoin.getUserId(), 'U', count)
                    )                  //TODO 'U'는 JWT에서 가져올 예정
                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                    .accountId(reqUserJoin.getUserId())
                    .agreeFl(reqMarketingAgree.getAgreeFl().charAt(0))
                    .templateSq(parseInt((reqMarketingAgree.getTemplateSq())))
                    .build();
        }
    }

    /**
     * @param id                 : 계정 id
     * @param U                  : 계정 타입 (H:호스트 ,U:숙박회원 )
     * @param marketingCounterSq 마케팅동의 리스트 순번
     * @return 마케팅동의 pk       :(동의 날짜 +계정타입 + id + 서버이름 +마케팅리스트 순번)
     */
    public String getMarketingPk(String id, char U, int marketingCounterSq) {
        //14자리 현재 시간 (yyyyMMddHHmmss)
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        //사용자 id 최대 길이는 20자. 20자 미만일때는 '*'로 대체하기
        StringBuilder accountIdBuilder = new StringBuilder(id);
        while (accountIdBuilder.length() < 20) {
            accountIdBuilder.append('*');
        }
        // 최종적으로 accountIdBuilder를 String으로 변환
        String accountIdPadded = accountIdBuilder.toString();

        // 임의로 4자리 숫자로 포맷
        String sqFormatted = String.format("%02d", marketingCounterSq);

        // AgreeSq 조합: 날짜 시간(14자리) + account_type(1글자) + accountIdPadded(20자) +serverName(2글자)+ marketingCounterSq(2자리)
        return dateTime + 'U' + accountIdPadded + serverName + marketingCounterSq;
    }


//    public ResponseEntity<CommonResponse<Void>> registerAgree() {
//    }
}
