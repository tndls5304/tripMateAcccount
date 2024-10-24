package com.tripmate.account.user.service;

import com.tripmate.account.common.entity.*;
import com.tripmate.account.common.entity.Compositekey.BasicAgreeId;
import com.tripmate.account.common.entity.chose.AgreeFl;
import com.tripmate.account.common.exception.DataConflictException;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.*;
import com.tripmate.account.user.repository.UserAccountInfoRepository;
import com.tripmate.account.user.repository.UserBasicAgreeRepository;
import com.tripmate.account.user.repository.UserMarketingAgreeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.tripmate.account.common.errorcode.CommonErrorCode.*;
import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManageService {

    @Value("${server.name}")
    private String serverName;
    private final UserAccountInfoRepository accountInfoRepository;
    private final UserBasicAgreeRepository basicAgreeRepository;
    private final UserMarketingAgreeRepository marketingAgreeRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 숙박회원의 아이디 중복 검사
     *
     * @param userId 입력한 아이디가 이미 존재하는지 여부를 확인
     * @return 중복된 아이디가 존재할 경우 예외발생-> 에러응답코드와 메세지를 담은 ResponseEntity 전달
     */
    public ResponseEntity<CommonResponse<Void>> checkUserIdDuplicate(String userId) {
        if (accountInfoRepository.existsById(userId)) {
            throw new DataConflictException(CONFLICT_ACCOUNT_ALREADY_EXISTS);
        }
        return new CommonResponse<Void>().toRespEntity(SUCCESS);
    }

    /**
     * (숙박회원) 가입 요청
     * JPA 특성때문에 가입 전 중복검사 한 후에 회원가입함.
     * 필수약관은 모두 동의를 했을때만 가입이 되게 하고
     * 마케팅약관은 선택이라 동의 여부 상관없이 가입은 할 수 있음
     * 그러나 마케팅이력 테이블에는 동의했을때만 저장함.
     *
     * @param reqUserJoin 개인정보와 (필수)약관동의리스트,마케팅약관동의리스트(선택적)를 받음
     */
    public void userJoin(UserJoinReqDto reqUserJoin) {

        String reqUserId = reqUserJoin.getUserId();

        // JPA의 save() 는 entity의 ID가 존재할경우 기존 entity로 간주하고 update를 수행하는 특성이 있기떄문에 save()가 호출되기 전에, 입력된 ID가 이미 존재하는지 확인해야 함
        if (accountInfoRepository.existsById(reqUserId)) {
            throw new DataConflictException(CONFLICT_ACCOUNT_ALREADY_EXISTS);
        }

        //개인정보 저장
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(reqUserId)
                .userPwd(
                        BCrypt.hashpw(reqUserJoin.getUserPwd(), BCrypt.gensalt())
                )//BCrypt.hashpw 메서드는 주어진 비밀번호를 BCrypt 해시로 변환하고, BCrypt.gensalt()는 랜덤 솔트 값을 생성하여 비밀번호에 추가
                .nickname(reqUserJoin.getNickname())
                .phoneNo(reqUserJoin.getPhoneNo())
                .emailId(reqUserJoin.getEmailId())
                .emailDomain(reqUserJoin.getEmailDomain())
                .regUser(reqUserId)
                .build();
        accountInfoRepository.save(userJoinEntity);

        // 필수 약관 동의 리스트 저장하기
        List<UserBasicAgreeReqDto> reqRequireAgreeList = reqUserJoin.getBasicAgreeDtoList();
        if (reqRequireAgreeList == null || reqRequireAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_BASIC_AGREE_BLANK);
        }
        for (UserBasicAgreeReqDto reqRequireAgree : reqRequireAgreeList) {
            BasicAgreeEntity requireAgreeEntity = BasicAgreeEntity.builder()
                    .id(
                            BasicAgreeId.builder()
                                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                                    .accountId(reqUserId)
                                    .templateSq(parseInt(reqRequireAgree.getTemplateSq()))
                                    .build()
                    )
                    .regUser(reqUserId)
                    .agreeFl(reqRequireAgree.getAgreeFl())
                    .build();
            basicAgreeRepository.save(requireAgreeEntity);
        }

        // 마케팅 약관 동의 리스트 저장하기
        List<UserCreateMarketingAgreeDto> reqMarketingAgreeList = reqUserJoin.getMarketingAgreeDtoList();
        if (reqMarketingAgreeList == null || reqMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }

        //마케팅 약관에 동의를 안하면 아예 저장이 안됨. 동의를 했다면 추후에 비동의로 수정할 경우 철회시간을 기록하기.
        int count = 1;
        for (UserCreateMarketingAgreeDto reqMarketingAgree : reqMarketingAgreeList) {
            if (reqMarketingAgree.getAgreeFl() == AgreeFl.N) {
                continue;
            }
            MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                    .agreeSq(
                            getMarketingPk(reqUserId, 'U', count)
                    )                  //TODO 'U'는 JWT에서 가져올 예정
                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                    .accountId(reqUserId)
                    .agreeFl(reqMarketingAgree.getAgreeFl())
                    .templateSq(parseInt((reqMarketingAgree.getTemplateSq())))
                    .regUser(reqUserId)
                    .agreeDtm(LocalDateTime.now())
                    .build();
            marketingAgreeRepository.save(marketingAgreeEntity);
            count++;
        }
    }

    /**
     * 마케팅 동의 테이블 PK를 만드는 메서드다.
     * 마케팅동의는 조회를 많이 할것이다 그래서 의미 없는 시퀀스를 pk로 잡는것보다
     * 가공해서 쓰는게 효율적일것이다.
     * 날짜와 계정정보를 넣으면 조회 조건에 맞는 인덱스 활용이 가능해져, 빠른 검색이 가능할거라 생각했고
     * 다중서버에서 중복을 막기 위해 서버이름을 활용하였다.
     *
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
        return dateTime + 'U' + accountIdPadded + serverName + sqFormatted;
        //바꾸기 ❣️ 타입+ id+날짜시간분초까지
    }

    /**
     * (숙박회원) 비밀번호 변경 요청
     * 접속한 회원의 비밀번호와 입력한 현재 비밀번호 비교후 일치한다면? 요청한 새비밀번호로 변경함.
     * @param modifyUserPwdDto
     */
    public void modifyPwd(ModifyUserPwdDto modifyUserPwdDto) {
        //해당 아이디에 해당하는 비밀번호와 입력한 현재 비밀번호가 일치하는지 확인하기
        /*findById 메서드는 Optional<UserEntity> 객체를 반환
        해당 userId가 accountInfoRepository에 존재한다면, Optional 객체 안에 UserEntity가 담겨서 반환돼.
         만약 해당 userId가 데이터베이스에 없다면, 빈 Optional 객체가 반환돼. 즉, Optional.empty()가 반환
         */
        Optional<UserEntity> userOptional = accountInfoRepository.findById(modifyUserPwdDto.getUserId());

        // 접속 id에 해당하는 계정이 존재하는지 확인하기 TODO id는 세션이나 jwt에 들고오는걸로 바꾸기
        if (userOptional.isEmpty()) {
            throw new InvalidRequestException(INVALID_USER_ID_MISMATCH);
        }

        // 사용자 정보 가져오기
        UserEntity existingUser = userOptional.get();

        //접속된 계정의 비밀번호와 클라이언트가 입력한 자신의 현재 비밀번호 일치여부 체크
        if (passwordEncoder.matches(modifyUserPwdDto.getCurrentPwd(), existingUser.getUserPwd())) {
            //새 비밀번호로 update
            UserEntity updateUserEntity = UserEntity.builder()
                    .userId(existingUser.getUserId())
                    //encode newPwd
                    .userPwd(passwordEncoder.encode(modifyUserPwdDto.getNewPwd()))
                    .nickname(existingUser.getNickname())
                    .phoneNo(existingUser.getPhoneNo())
                    .emailId(existingUser.getEmailId())
                    .emailDomain(existingUser.getEmailDomain())
                    .regUser(existingUser.getRegUser())
                    .regDtm(existingUser.getRegDtm())
                    .updtUser(existingUser.getUpdtUser())
                    .updtDtm(LocalDateTime.now())
                    .lastLoginDt(existingUser.getLastLoginDt())
                    .build();
            accountInfoRepository.save(updateUserEntity);
        } else {
            //클라이언트가 자신의 비밀번호를 잘못입력 했을경우
            throw new InvalidRequestException(INVALID_USER_PWD_MISMATCH);
        }
    }

    /**
     *1. 처음 마케팅약관에 동의할경우 :
     * 기존 마케팅약관이력을 찾아 동의한 이력이 있는지 조회후 없다면 등록
     *
     * 2. 이전에 동의를 했다가 철회하는경우:
     * 기존에 등록된 동의건 있는 지 조회후 동의한 이력이 있으면 동의 상태는 N으로 변경하고 철회일자 update
     * @param ModifyMarketingAgreeList 여러 마케팅동의 수정한 리스트
     */
    public void modifyMarketingAgree(List<UserModifyMarketingAgreeDto> ModifyMarketingAgreeList) {
        if (ModifyMarketingAgreeList == null || ModifyMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }
        int count = 1;
        for (UserModifyMarketingAgreeDto reqMarketingAgree : ModifyMarketingAgreeList) {
            String templateSq=reqMarketingAgree.getTemplateSq();

            if (reqMarketingAgree.getAgreeFl() == AgreeFl.N) {

            }
            //TODO 세션있으면 타입, 아이디 바꾸기
            MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                    .agreeSq(getMarketingPk("4test", 'U', count))
                    .accountType('U')
                    .accountId("4test")
                    .agreeFl(reqMarketingAgree.getAgreeFl())
                    .agreeDtm(reqMarketingAgree.getAgreeFl() == 'Y' ? LocalDateTime.now() : null)
                    .dAgreeDtm(reqMarketingAgree.getAgreeFl() == 'N' ? LocalDateTime.now() : null)
                    .templateSq(Integer.parseInt(reqMarketingAgree.getTemplateSq()))
                    .regUser("4test")
                    .build();
            marketingAgreeRepository.save(marketingAgreeEntity);
            count++;
        }
    }
}

