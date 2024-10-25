package com.tripmate.account.user.service;

import com.tripmate.account.common.entity.*;
import com.tripmate.account.common.entity.Compositekey.BasicAgreeId;
import com.tripmate.account.common.exception.DataConflictException;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.common.custom.validation.AgreeFl;
import com.tripmate.account.user.dto.*;
import com.tripmate.account.user.repository.UserTbRepository;
import com.tripmate.account.user.repository.UserBasicAgreeThRepository;
import com.tripmate.account.user.repository.UserMarketingAgreeThRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.tripmate.account.common.errorcode.CommonErrorCode.*;
import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor

public class UserManageService {

    private final UserTbRepository userTbRepository;
    private final UserBasicAgreeThRepository basicAgreeThRepository;
    private final UserMarketingAgreeThRepository marketingAgreeThRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 숙박회원의 아이디 중복 검사
     *
     * @param userId 입력한 아이디가 이미 존재하는지 여부를 확인
     * @return 중복된 아이디가 존재할 경우 예외발생-> 에러응답코드와 메세지를 담은 ResponseEntity 전달
     */
    public ResponseEntity<CommonResponse<Void>> checkUserIdDuplicate(String userId) {
        if (userTbRepository.existsById(userId)) {
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
    @Transactional
    public void userJoin(UserJoinReqDto reqUserJoin) {

        String reqUserId = reqUserJoin.getUserId();

        // JPA의 save() 는 entity의 ID가 존재할경우 기존 entity로 간주하고 update를 수행하는 특성이 있기떄문에 save()가 호출되기 전에, 입력된 ID가 이미 존재하는지 확인해야 함
        if (userTbRepository.existsById(reqUserId)) {
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
        userTbRepository.save(userJoinEntity);

        // 필수 약관 동의 리스트 저장하기
        List<UserBasicAgreeReqDto> reqBasicAgreeList = reqUserJoin.getBasicAgreeDtoList();
        if (reqBasicAgreeList == null || reqBasicAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_BASIC_AGREE_BLANK);
        }
        for (UserBasicAgreeReqDto reqBasicAgree : reqBasicAgreeList) {
            BasicAgreeEntity requireAgreeEntity = BasicAgreeEntity.builder()
                    .id(
                            BasicAgreeId.builder()
                                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                                    .accountId(reqUserId)
                                    .templateSq(parseInt(reqBasicAgree.getTemplateSq()))
                                    .build()
                    )
                    .regUser(reqUserId)
                    .agreeFl((reqBasicAgree.getAgreeFlEnum()))
                    .build();
            basicAgreeThRepository.save(requireAgreeEntity);
        }

        // 마케팅 약관 동의 리스트 저장하기
        List<UserCreateMarketingAgreeDto> reqMarketingAgreeList = reqUserJoin.getMarketingAgreeDtoList();
        if (reqMarketingAgreeList == null || reqMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }
        System.out.println("❣️❣️reqMarketingAgreeList"+reqMarketingAgreeList);
        //마케팅 약관에 동의를 안하면 아예 저장이 안됨. 동의를 했다면 추후에 비동의로 수정할 경우 철회시간을 기록하기.
        List<MarketingAgreeEntity> marketingAgreeEntities = new ArrayList<>();
        int pkSq=1;
        for (UserCreateMarketingAgreeDto reqMarketingAgree : reqMarketingAgreeList) {
            if (reqMarketingAgree.getAgreeFlEnum() == AgreeFl.N) {
                continue;
            }
            MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                    .agreeSq(
                            getMarketingPk(reqUserId,pkSq)
                    )
                    .accountType('U')//TODO 'U'는 JWT에서 가져올 예정
                    .accountId(reqUserId)
                    .agreeFl(reqMarketingAgree.getAgreeFlEnum())
                    .templateSq(parseInt((reqMarketingAgree.getTemplateSq())))
                    .regUser(reqUserId)
                    .agreeDtm(LocalDateTime.now())
                    .build();
            marketingAgreeEntities.add(marketingAgreeEntity);
            pkSq++;
        }
        System.out.println("marketingAgreeEntities❣️❣️"+marketingAgreeEntities);
        marketingAgreeThRepository.saveAll(marketingAgreeEntities);
    }

    /**
     * (숙박회원) 비밀번호 변경 요청
     * 접속한 회원의 비밀번호와 입력한 현재 비밀번호 비교후 일치한다면? 요청한 새비밀번호로 변경함.
     *
     * @param modifyUserPwdDto
     */
    public void modifyPwd(UserModifyPwdReqDto modifyUserPwdDto) {
        //해당 아이디에 해당하는 비밀번호와 입력한 현재 비밀번호가 일치하는지 확인하기
        /*findById 메서드는 Optional<UserEntity> 객체를 반환
        해당 userId가 userTbRepository에 존재한다면, Optional 객체 안에 UserEntity가 담겨서 반환돼.
         만약 해당 userId가 데이터베이스에 없다면, 빈 Optional 객체가 반환돼. 즉, Optional.empty()가 반환
         */
        Optional<UserEntity> userOptional = userTbRepository.findById(modifyUserPwdDto.getUserId());

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
            userTbRepository.save(updateUserEntity);
        } else {
            //클라이언트가 자신의 비밀번호를 잘못입력 했을경우
            throw new InvalidRequestException(INVALID_USER_PWD_MISMATCH);
        }
    }

    /**
     * 사용자가 마케팅 약관에 대한 동의를 수정하는 메서드입니다.
     * 1.사용자가 마케팅약관에 동의할경우 :
     * 우선 기존 마케팅 약관 이력 테이블에서 "동의"한 데이터가 존재하는지 조회합니다.
     * "동의"한 데이터는 한 개 또는 없어야 합니다.
     * ✔️"동의"한 데이터가 없을때는 ? 이력 테이블을 새로 만들고 상태값을 동의로 설정합니다.
     * ✔️"동의"한 데이터가 한개일때 ? 이미 동의를 했다고 알려줍니다 (마케팅 동의를 하면 혜택을 주는 경우가 많기 때문에)
     * ✔️"동의"한 데이터가 여러개면 ? 서버 오류를 발생시킵니다.
     *
     * 2.사용자가 마케팅동의 거절을 한경우:
     * 우선 기존 마케팅약관이력 table 에 "동의" 한 데이터 조회합니다.
     * "동의"한 데이터는 한 개 또는 없어야 합니다.
     * ✔️"동의"한 데이터가 없을때는 ? 과거에 마케팅동의를 거절한 상태에서 또 거절하는것. 사용자에게 굳이 알릴 필요가 없어서 continue
     * ✔️"동의"한 데이터가 한개일때 ? 비동의 AgreeFl=N 상태로 바꿔주고 철회시간 등록하기
     * ✔️"동의"한 데이터가 여러개면 ? 서버 오류를 발생시킵니다.
     *
     * @param ModifyMarketingAgreeList 사용자가 변경 요청한 여러 개의 마케팅 동의 리스트. 각 항목은 마케팅 약관의 템플릿과 동의 상태를 포함합니다.
     * @throws InvalidRequestException 마케팅 동의 요청 목록이 비어있거나 null인 경우, 유효하지 않은 요청으로 간주됩니다.
     * @throws DataConflictException   동의 또는 비동의 처리 중 다음과 같은 충돌이 발생한 경우:
     *                                 - 사용자가 이미 동의한 상태에서 다시 동의 요청을 하는 경우.
     *
     * @throws ServerErrorException    데이터베이스에서 여러 개의 동의 상태가 발견되었을 때 발생합니다. 서버 오류가 발생한 경우다.
     */
    public void modifyMarketingAgree(List<UserModifyMarketingAgreeDto> ModifyMarketingAgreeList) {
        if (ModifyMarketingAgreeList == null || ModifyMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }
        int pkSq=1;
        for (UserModifyMarketingAgreeDto reqMarketingAgree : ModifyMarketingAgreeList) {
            String userId = "1test";//TODO
            char userType = 'U';

            String partOfMarketingPk = getPartOfMarketingPk(userId);
            List<MarketingAgreeEntity> agreeEntityList = marketingAgreeThRepository.findByAccountInfo(partOfMarketingPk, reqMarketingAgree.getTemplateSq(), AgreeFl.Y);

            //⭐동의를 한경우
            AgreeFl reqAgreeFl = reqMarketingAgree.getAgreeFl();
            if (reqAgreeFl == AgreeFl.Y) {
                switch (agreeEntityList.size()) {
                    case 0:
                        MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                                .agreeSq(getMarketingPk(userId,pkSq))
                                .accountType(userType)
                                .accountId(userId)
                                .agreeFl(reqAgreeFl)
                                .agreeDtm(LocalDateTime.now())
                                .dAgreeDtm(null)
                                .templateSq(Integer.parseInt(reqMarketingAgree.getTemplateSq()))
                                .regUser(userId)
                                .build();
                        marketingAgreeThRepository.save(marketingAgreeEntity);
                        pkSq++;
                        break;
                    case 1:
                        throw new DataConflictException(CONFLICT_MARKETING_AGREE_FL_Y_DUPLICATE);
                    default:
                        throw new ServerErrorException(SERVER_ERROR_AGREE_FL_MANY_ERROR);
                }
            } else if (reqAgreeFl == AgreeFl.N) {
                //⭐마케팅동의 거절을 한경우
                switch (agreeEntityList.size()) {
                    case 0:
                       continue;
                    case 1:
                        MarketingAgreeEntity existingEntity = agreeEntityList.get(0);
                        MarketingAgreeEntity updatedEntity = MarketingAgreeEntity.builder()
                                .agreeSq(existingEntity.getAgreeSq()) // 기존의 agreeSq를 사용
                                .accountType(existingEntity.getAccountType())
                                .accountId(existingEntity.getAccountId())
                                .agreeFl(AgreeFl.N)
                                .agreeDtm(existingEntity.getAgreeDtm())
                                .templateSq(existingEntity.getTemplateSq())
                                .regUser(existingEntity.getRegUser())
                                .dAgreeDtm(LocalDateTime.now())
                                .updtDtm(LocalDateTime.now())//이게 맞나?
                                .updtUser(userId)//이게 맞나???
                                .build();
                        marketingAgreeThRepository.save(updatedEntity);
                        break;
                    default:
                        throw new ServerErrorException(SERVER_ERROR_AGREE_FL_MANY_ERROR);
                }
            }
        }
    }

    /**
     * 마케팅 약관에 동의하는 테이블의  PK를 만드는 메서드다.
     * 마케팅동의는 조회를 많이 할것이다 그래서 의미 없는 시퀀스를 pk로 잡는것보다 가공해서 쓰는게 효율적일것이라고 생각했다.
     * PK를 만드는 방법은 = 계정 타입 + 계정 id + pkSq(충돌피하기위함) + 년월일시 날짜 ( 충돌피하기위함)
     * 계정정보를 넣으면 조회 조건에 맞는 인덱스 활용이 가능해져, 빠른 검색이 가능할거라 생각했다.
     * PK를 유일하게 만들기 위해 날짜도 저장했는데 리스트를 받아서 저장해보니 충돌이 생겨서 같은 값으로 취급해버렸다
     * 그래서 PK를 구분하는 시퀀스를 만들었다.
     * 같은 날짜에 여러번 마케팅 동의를 하더라도 충돌을 피하고 각 기 다른 PK를 생성할 수 있기 떄문이다.
     * @param userId: 숙박회원 id
     *  @param pkSq : 한 서버에 여러 마케팅이력이 저장될때 pkSq가 없으니까 같은 pk로 취급되길래 구분하기 위해서
     * @return 마케팅동의 pk       :(동의 날짜 +계정타입 + id + 서버이름 +마케팅리스트 순번)
     */
    public String getMarketingPk(String userId,int pkSq) {
        char userType = 'U';//TODO 바꾸기

        //사용자 id 최대 길이는 20자. 20자 미만일때는 '*'로 대체하기
        StringBuilder accountIdBuilder = new StringBuilder(userId);
        while (accountIdBuilder.length() < 20) {
            accountIdBuilder.append('0');
        }
        // 최종적으로 accountIdBuilder를 String으로 변환
        String userIdIdPadded = accountIdBuilder.toString();

        // pkSq 값을 두 자리로 표현하기
        String pkSqFormatted = String.format("%02d", pkSq);
        //14자리 현재 시간 (yyyyMMddHHmmss)
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // AgreeSq 조합: account_type(1글자)  accountIdPadded(20자) +날짜 시간(14자리)
        return userType + userIdIdPadded +pkSqFormatted+ dateTime;
        // 키 생성 수정함 ️ 타입+ id+날짜시간분초까지
    }

    /**
     * 마케팅 동의 이력 데이터는 조회 및 수정이 빈번히 이루어지는 데이터입니다.
     * 따라서 빠른 조회 성능을 위해 PK의 일부를 특정 형식으로 구성하여 인덱싱을 최적화하고자 했습니다.
     * 이 마케팅 동의 PK는 '계정 타입 + 계정 ID + pkSq(중복 방지용 값) + 년월일시(중복 방지용 값)' 형식으로 구성됩니다.
     *  PK의 일부를 LIKE 조건으로 검색해 해당 계정의 마케팅 동의 이력을 빠르게 찾는 방식입니다.
     * @param userId 숙박회원 id
     * @return 마케팅 동의 이력을 조회하기 위한 '계정 타입 + 계정 ID' 문자열
     */
    public String getPartOfMarketingPk(String userId) {
        char userType = 'U';//TODO  추후에 세션에서받기
        userId = "1test";// TODO  추후에 세션에서받기
        //사용자 id 최대 길이는 20자. 20자 미만일때는 '0'로 대체하기
        StringBuilder accountIdBuilder = new StringBuilder(userId);
        while (accountIdBuilder.length() < 20) {
            accountIdBuilder.append('0');
        }
        // 최종적으로 accountIdBuilder를 String으로 변환
        String userIdPadded = accountIdBuilder.toString();

        return userType + userIdPadded;
    }
}

