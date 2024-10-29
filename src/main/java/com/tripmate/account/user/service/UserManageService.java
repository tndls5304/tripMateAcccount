package com.tripmate.account.user.service;

import com.tripmate.account.common.entity.*;
import com.tripmate.account.common.entity.id.BasicAgreeId;
import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.exception.DataConflictException;
import com.tripmate.account.common.exception.InvalidRequestException;
import com.tripmate.account.common.exception.ServerErrorException;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.common.enums.AgreeFl;
import com.tripmate.account.user.dto.*;
import com.tripmate.account.user.repository.UserTbRepository;
import com.tripmate.account.user.repository.UserBasicAgreeThRepository;
import com.tripmate.account.user.repository.UserMarketingAgreeThRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.tripmate.account.common.errorcode.CommonErrorCode.*;
import static java.lang.Integer.parseInt;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManageService {

    private final UserTbRepository userTbRepository;
    private final UserBasicAgreeThRepository basicAgreeThRepository;
    private final UserMarketingAgreeThRepository marketingAgreeThRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * (숙박회원)  아이디 중복 검사
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
     * (숙박회원) 가입 요청 처리 메서드
     * 가입 절차:
     * 1) ID 중복 검사: JPA의 특성상 가입 전 중복 여부를 검사 ( JPA는 entity의 ID가 존재할경우 기존 entity로 간주하고 update를 수행하는 특성이 있기떄문)
     * 2) 필수약관 동의서(basicAgree)저장 :필수약관에 모두 동의한것만 이력에 저장
     * 3) 마케팅 약관 동의 처리: 마케팅약관에 대한 동의는 동의,비동의 상태값을 가지나 동의한 데이터만 이력에 저장 (추후에 클라이언트가 마케팅약관을 철회할때는 상태값을 비동의로 바꿀 예정)
     * <p>
     * 위 세 단계가 모두 통과되면 @Transactional을 통해 가입 완료
     *
     * @param reqUserJoin 개인정보, 필수 약관 동의 리스트, 마케팅 약관 동의 리스트를 포함한 가입 요청 정보
     */
    @Transactional
    public void userJoin(UserJoinReqDto reqUserJoin) {
        if (userTbRepository.existsById(reqUserJoin.getUserId())) {
            throw new DataConflictException(CONFLICT_ACCOUNT_ALREADY_EXISTS);
        }
        insertAccountInfo(reqUserJoin);
        insertBasicAgree(reqUserJoin);
        insertMarketingAgree(reqUserJoin);
    }

    /**
     * (숙박회원) 비밀번호 변경 요청
     * 1)접속한 회원(existingUser)의 비밀번호와 클라이언트측(modifyPwdDto)에서 보낸 현재 비밀번호를 조회해서 일치하는지 확인
     * 2)일치하면 새 비밀번호로 수정해주고 불일치는 예외 발생
     *
     * @param modifyPwdDto 클라이언트의 현재 비밀번호(oldPwd)와 바꾸고 싶은 새 비밀번호(newPwd)
     */
    @Transactional//더티체킹: 엔티티가 영속성 컨텍스트에 속한 상태가 되고 트랜잭션이 끝나는 시점에 변경된 엔티티가 자동으로 감지되어 UPDATE 쿼리가 실행
    public void modifyPwd(UserModifyPwdReqDto modifyPwdDto) {
        Optional<UserEntity> userOptional = userTbRepository.findById(modifyPwdDto.getUserId());//이부분 세션할때 바꾸기

        // 접속 id에 해당하는 계정이 존재하는지 확인하기 TODO id는 세션이나 jwt에 들고오는걸로 바꾸기
        if (userOptional.isEmpty()) {
            throw new InvalidRequestException(INVALID_USER_ID_MISMATCH);
        }

        UserEntity existingUserEntity = userOptional.get();

        if (passwordEncoder.matches(modifyPwdDto.getOldPwd(), existingUserEntity.getUserPwd())) {
            existingUserEntity.setUserPwd(passwordEncoder.encode(modifyPwdDto.getNewPwd()));
            existingUserEntity.setPwdUpdDt(LocalDate.now());
            existingUserEntity.setUpdtDtm(LocalDateTime.now());
            // existingUserEntity.setUpdtUser(); TODO 서버이름 넣기
            return;
        }
        throw new InvalidRequestException(INVALID_USER_PWD_MISMATCH);
    }

    /**
     * ---개인 생각
     * 사실 마케팅 동의 테이블은 수정할때마다 동의, 비동의 여부만 저장해도 된다.
     * 허나 마케팅동의를 한사람 대상으로 딱 짧은 기간에만 이벤트를 한다거나 해서 "저는 동의했는데 왜 혜택이 없나요?"
     * 라고 고객이 생각할 수 있다. 이처럼 기간이 중요할 서비스가 있을것이다.
     * 약관동의 이력이 중요하지 않을 수 있지만  나중에 이력이 중요한 서비스에 적용하고 싶어서 짜보기로 했다.
     * ---메서드 설명
     * 사용자가 마케팅 리스트 (앱푸쉬알람 동의하시겠습니까? ,이메일 수신에 동의하시겠습니까? )에 대해 각 각 동의/비동의 이력을 수정하는 메서드입니다
     * 리스트 중에 우선 하나를 꺼내서 다음과 같은 절차를 진행하고 수정합니다.
     * 1.사용자가 마케팅약관에 동의할경우 :
     * 우선 기존 마케팅 약관 이력 테이블에서 "동의"한 데이터가 존재하는지 조회합니다.
     * "동의"한 데이터는 한 개 또는 없어야 합니다.
     * ✔️"동의"한 데이터가 없을때는 ? 이력 테이블을 새로 만들고 상태값을 동의로 설정합니다.
     * ✔️"동의"한 데이터가 한개일때 ? 이미 동의를 했는데 또 동의를 요청하는거라 그냥 넘어간다!!!!!!!!!!!!⭐
     * ✔️"동의"한 데이터가 여러개면 ? 앱푸쉬 알람에 동의했던 이력을 조회하면 '동의'상태는 딱 하나만 존재해야한다.
     * 그런데 여러개면
     * 서버에러가 발생하고 있단거. 과거 이력의 상태값을 모두 '비동의'상태로 바꾸고 수정자는 철회한 서버명을 기록 철회시간을 기록한다
     * 개발자가 이를 알아야 하니까 로그로 기록해둔다.
     *
     * <p>
     * 2.사용자가 마케팅동의 거절을 한경우:
     * 우선 기존 마케팅약관이력 table 에 "동의" 한 데이터 조회합니다.
     * "동의"한 데이터는 한 개 또는 없어야 합니다.
     * ✔️"동의"한 데이터가 없을때는 ? 과거에 마케팅동의를 거절한 상태에서 또 거절하는것. 사용자에게 굳이 알릴 필요가 없어서 continue
     * ✔️"동의"한 데이터가 한개일때 ? 비동의 AgreeFl=N 상태로 바꿔주고 철회시간 등록하기
     * ✔️"동의"한 데이터가 여러개면 ? 서버 오류를 발생시킵니다.
     *
     * @param reqModifyMarketingList 사용자가 변경 요청한 여러 개의 마케팅 동의 리스트. 각 항목은 마케팅 약관의 템플릿과 동의 상태를 포함합니다.
     * @throws InvalidRequestException 마케팅 동의 요청 목록이 비어있거나 null인 경우, 유효하지 않은 요청으로 간주됩니다.
     * @throws DataConflictException   동의 또는 비동의 처리 중 다음과 같은 충돌이 발생한 경우:
     *                                 - 사용자가 이미 동의한 상태에서 다시 동의 요청을 하는 경우.
     * @throws ServerErrorException    데이터베이스에서 여러 개의 동의 상태가 발견되었을 때 발생합니다. 서버 오류가 발생한 경우다.
     */

    public void modifyMarketingAgree(List<UserModifyMarketingAgreeReqDto> reqModifyMarketingList) {
        if (reqModifyMarketingList == null || reqModifyMarketingList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }
        int marketingPkSq = 1;

        for (UserModifyMarketingAgreeReqDto reqModifyOneMarketing : reqModifyMarketingList) {
            String userId = "1test";//TODO

            String partPkOfMarketing = getPartPkOfMarketing(userId);
            String reqTemplateSq = reqModifyOneMarketing.getTemplateSq();
            AgreeFl reqAgreeFlForOneMarketing = reqModifyOneMarketing.getAgreeFlEnum();
            List<MarketingAgreeEntity> agreedHistoryListForOneMarketing = marketingAgreeThRepository.findByAccountInfo(partPkOfMarketing, reqTemplateSq, AgreeFl.Y);
            //동의를 한경우
            if (AgreeFl.Y.equals(reqAgreeFlForOneMarketing)) {
                if (agreedHistoryListForOneMarketing.isEmpty()) {
                    insertNewMarketingAgree(userId, marketingPkSq, reqAgreeFlForOneMarketing, reqTemplateSq);                     //동의한적 없으니 새로운 동의테이블 만들어주기
                    marketingPkSq++;
                }
                if (agreedHistoryListForOneMarketing.size() > 1) {
                    ModifyErrorMarketingHistory(agreedHistoryListForOneMarketing, reqModifyOneMarketing);
                }
            } else if (AgreeFl.N.equals(reqAgreeFlForOneMarketing)) {                                            //마케팅동의 거절을 한경우
                if (agreedHistoryListForOneMarketing.size() == 1) {
                    UpdateDisAgreeForMarketing(userId, agreedHistoryListForOneMarketing);
                }
                if (agreedHistoryListForOneMarketing.size() > 1) {
                    ModifyErrorMarketingHistory(agreedHistoryListForOneMarketing, reqModifyOneMarketing);
                }
            }
        }
    }

    //새 마케팅 동의 이력 저장하기
    public void insertNewMarketingAgree(String userId, int marketingPkSq, AgreeFl reqAgreeFlForOneMarketing, String reqTemplateSq) {
        MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                .agreeSq(getMarketingPk(userId, marketingPkSq))
                .accountType(AccountType.U)
                .accountId(userId)
                .agreeFl(reqAgreeFlForOneMarketing)
                .agreeDtm(LocalDateTime.now())
                .dAgreeDtm(null)
                .templateSq(Integer.parseInt(reqTemplateSq))
                .regUser(userId)
                .build();
        marketingAgreeThRepository.save(marketingAgreeEntity);//이전에 동의한적 없으면 동의이력테이블에 데이터 저장
    }

    /*하나의 마케팅동의 이력테이블을 조회 했을때 '동의'한 이력은 딱 한번이어야 하는데 여러개일떄 오류다.
     */

    /**
     * 하나의 마케팅동의 이력을 조회했을때 '동의'한 이력은 딱 한번이어야 한다.
     * '동의'한 데이터가 여러개일떄 중복으로 저장되고 있었던거라 이때까지 마케팅 이력테이블이 잘못 관리되고있었던거니 오류가 발생한것이다.
     * 그떄 이메서드가 호출된다.
     * 단순히 오류를 던지면 이걸로 모든 서비스가 멈추면 안될것이다.
     * 돈과 관련되거나 누가 피해보는 서비스가 아니므로 정상으로 돌려놓고 오류를 남기는것이 좋다고 생각했다
     * 현재 클라이언트가 바꾸고 싶어하는 동의 상태값은 저장해두고 이전 기록들은 정상으로 돌려놔야 한다.
     * 1.현재 클라이언트가 바꾸고 싶어하는 동의 상태값이 '동의'라면     (AgreeFl='Y')
     * 이전 이력에 중복으로 저장되었던 '동의'데이터를 (AgreeFl=Y이였던 것들) 모두 '비동의'인 ('N')으로 돌려놓는다. 다 비동의로 돌려놓으면 안되니까 하나만  AgreeFl=Y 동의로 남겨둔다.
     * 2.현재 클라이언트가 바꾸고 싶어하는 동의 상태값이 '비동의'라면   (AgreeFl='N')
     * 이전 이력에 중복으로 저장되었던 '동의'데이터를 (AgreeFl=Y이였던 것들) 모두 '비동의'인 ('N')으로 돌려놓는다. 다 비동의로 돌려놓으면 된다.
     *
     * @param agreedHistoryListForOneMarketing
     */
    //이력에 있던 것들은 모두 '비동의'상태로 바꿔주고 수정자는 '서버이름'을 쓰고 에러는 어떤에러인지 로그로 알려주기.
    public void ModifyErrorMarketingHistory(List<MarketingAgreeEntity> agreedHistoryListForOneMarketing, UserModifyMarketingAgreeReqDto reqModifyOneMarketing) {
        if (agreedHistoryListForOneMarketing.isEmpty()) {
            //예외뱉기 TODO 어떤에러지???????????리스트마다 이 짓을 해야하나
        }
        //현재 클라이언트 요청이 '동의'라면 이력에서 '동의'인 값들 중 하나는(firstErrorEntity) 상태값을 바꾸지 않고 그대로 두기 위함
        MarketingAgreeEntity firstErrorEntity = agreedHistoryListForOneMarketing.get(0);

        //현재 클라이언트 요청이'비동의'라면 이력에서 '동의'인 값들 중 하나는 '비동의'로 바꿔줌
        if (reqModifyOneMarketing.getAgreeFlEnum() == AgreeFl.N) {
            firstErrorEntity.setAgreeFl(AgreeFl.N);
            firstErrorEntity.setDAgreeDtm(LocalDateTime.now());
            firstErrorEntity.setUpdtDtm(LocalDateTime.now());
            firstErrorEntity.setUpdtUser("serverName");//        TODO 서버이름을 바꾸기
        }

        //나머지 이력에서 '동의'인 값들을 모두 '비동의'로 저장함
        for (MarketingAgreeEntity errorMarketingEntity : agreedHistoryListForOneMarketing) {
            if (errorMarketingEntity != firstErrorEntity) {
                errorMarketingEntity.setAgreeFl(AgreeFl.N);
                errorMarketingEntity.setDAgreeDtm(LocalDateTime.now());
                errorMarketingEntity.setUpdtDtm(LocalDateTime.now());
                errorMarketingEntity.setUpdtUser("serverName");//        TODO 서버이름을 바꾸기
                // 로그 기록: 어떤 동의 이력을 비동의로 변경했는지에 대한 정보 추가
                log.error("Changed marketing agreement status from 'Y' to 'N' for user: {} due to multiple agreements.", errorMarketingEntity.getUpdtUser());
            }

        }
    }
    //마케팅 동의여부에 '동의'였던 데이터를 '비동의'로 바꿈
    public void UpdateDisAgreeForMarketing(String userId, List<MarketingAgreeEntity> agreedHistoryListForOneMarketing) {
        MarketingAgreeEntity existingEntity = agreedHistoryListForOneMarketing.get(0);
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
    }

    /**
     * 마케팅 약관에 동의하는 테이블의  PK를 만드는 메서드다.
     * 마케팅동의는 조회를 많이 할것이다 그래서 의미 없는 시퀀스를 pk로 잡는것보다 가공해서 쓰는게 효율적일것이라고 생각했다.
     * PK를 만드는 방법은 = 계정 타입 + 계정 id + marketingPkSq(충돌피하기위함) + 년월일시 날짜 ( 충돌피하기위함)
     * 계정정보를 넣으면 조회 조건에 맞는 인덱스 활용이 가능해져, 빠른 검색이 가능할거라 생각했다.
     * PK를 유일하게 만들기 위해 날짜도 저장했는데 리스트를 받아서 저장해보니 충돌이 생겨서 같은 값으로 취급해버렸다
     * 그래서 PK를 구분하는 시퀀스를 만들었다.
     * 같은 날짜에 여러번 마케팅 동의를 하더라도 충돌을 피하고 각 기 다른 PK를 생성할 수 있기 떄문이다.
     *
     * @param userId:       숙박회원 id
     * @param marketingPkSq : 한 서버에 여러 마케팅이력이 저장될때 marketingPkSq가 없으니까 같은 pk로 취급되길래 구분하기 위해서
     * @return 마케팅동의 pk       :(동의 날짜 +계정타입 + id + 서버이름 +마케팅리스트 순번)
     */
    public String getMarketingPk(String userId, int marketingPkSq) {
        char userType = 'U';//TODO 바꾸기

        //사용자 id 최대 길이는 20자. 20자 미만일때는 '0'로 대체하기
        String userIdPadded = StringUtils.rightPad(userId, 20, '0');

        // marketingPkSq 값을 두 자리로 표현하기
        String marketingPkSqFormatted = String.format("%02d", marketingPkSq);

        //14자리 현재 시간 (yyyyMMddHHmmss)
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // AgreeSq 조합: account_type(1글자)  accountIdPadded(20자) +날짜 시간(14자리)+ 시퀀스
        return userType + userIdPadded + marketingPkSqFormatted + dateTime;
        // 키 생성 수정함 ️ 타입+ id+날짜시간분초까지
    }

    /**
     * 마케팅 동의 이력 데이터는 조회 및 수정이 빈번히 이루어지는 데이터입니다.
     * 따라서 빠른 조회 성능을 위해 PK의 일부를 특정 형식으로 구성하여 인덱싱을 최적화하고자 했습니다.
     * 이 마케팅 동의 PK는 '계정 타입 + 계정 ID + marketingPkSq(중복 방지용 값) + 년월일시(중복 방지용 값)' 형식으로 구성됩니다.
     * PK의 일부를 LIKE 조건으로 검색해 해당 계정의 마케팅 동의 이력을 빠르게 찾는 방식입니다.
     *
     * @param userId 숙박회원 id
     * @return 마케팅 동의 이력을 조회하기 위한 '계정 타입 + 계정 ID' 문자열
     */
    public String getPartPkOfMarketing(String userId) {
        char userType = 'U';//TODO  추후에 세션에서받기
        userId = "1test";// TODO  추후에 세션에서받기
        //사용자 id 최대 길이는 20자. 20자 미만일때는 '0'로 대체하기
        String userIdPadded = StringUtils.rightPad(userId, 20, '0');
        return userType + userIdPadded;
    }

    //회원가입할때 계정정보 저장하기
    public void insertAccountInfo(UserJoinReqDto reqUserJoin) {
        UserEntity userJoinEntity = UserEntity.builder()
                .userId(reqUserJoin.getUserId())
                .userPwd(
                        passwordEncoder.encode(reqUserJoin.getUserPwd()) //수동생성방법:BCrypt.hashpw(reqUserJoin.getUserPwd(), BCrypt.gensalt())   :BCrypt.hashpw 메서드는 주어진 비밀번호를 BCrypt 해시로 변환하고, BCrypt.gensalt()는 랜덤 솔트 값을 생성하여 비밀번호에 추가
                )
                .nickname(reqUserJoin.getNickname())
                .phoneNo(reqUserJoin.getPhoneNo())
                .emailId(reqUserJoin.getEmailId())
                .emailDomain(reqUserJoin.getEmailDomain())
                .regUser(reqUserJoin.getUserId())//서버이름으로 바꾸기⭐
                .pwdUpdDt(LocalDate.now())//가입하면 비번업데이트 날짜도 기록함
                .build();
        userTbRepository.save(userJoinEntity);
    }


    //회원가입할때  필수 약관 동의 리스트 저장하기
    public void insertBasicAgree(UserJoinReqDto reqUserJoin) {
        List<UserBasicAgreeReqDto> reqBasicAgreeList = reqUserJoin.getBasicAgreeDtoList();
        if (reqBasicAgreeList == null || reqBasicAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_BASIC_AGREE_BLANK);
        }
        for (UserBasicAgreeReqDto reqBasicAgree : reqBasicAgreeList) {
            BasicAgreeEntity requireAgreeEntity = BasicAgreeEntity.builder()
                    .id(
                            BasicAgreeId.builder()
                                    .accountType(AccountType.U)//TODO 'U'는 JWT에서 가져올 예정
                                    .accountId(reqUserJoin.getUserId())
                                    .templateSq(parseInt(reqBasicAgree.getTemplateSq()))
                                    .build()
                    )
                    .agreeFl((reqBasicAgree.getAgreeFlEnum()))
                    .agreeDt(LocalDate.now())
                    .regUser(reqUserJoin.getUserId())//TODO 서버이름 넣기
                    .build();
            basicAgreeThRepository.save(requireAgreeEntity);
        }
    }

    // 마케팅 약관 동의 리스트 저장하기
    public void insertMarketingAgree(UserJoinReqDto reqUserJoin) {
        List<UserCreateMarketingAgreeDto> reqMarketingAgreeList = reqUserJoin.getMarketingAgreeDtoList();
        if (reqMarketingAgreeList == null || reqMarketingAgreeList.isEmpty()) {
            throw new InvalidRequestException(INVALID_MARKETING_AGREE_BLANK);
        }

        List<MarketingAgreeEntity> marketingOkEntityList = new ArrayList<>();

        int marketingmarketingPkSq = 1;

        for (UserCreateMarketingAgreeDto reqMarketingAgree : reqMarketingAgreeList) {
            //마케팅 약관에 거절한 데이터는 저장 안함.
            if (reqMarketingAgree.getAgreeFlEnum() == AgreeFl.N) {
                continue;//⭐
            }
            //마케팅 약관에 동의를 해야지만 마케팅동의 이력테이블에 저장됨.추후에 비동의로 수정할 경우 철회시간을 기록하기.
            MarketingAgreeEntity marketingAgreeEntity = MarketingAgreeEntity.builder()
                    .agreeSq(
                            getMarketingPk(reqUserJoin.getUserId(), marketingmarketingPkSq)
                    )
                    .accountType(AccountType.U)         //TODO 'U'는 JWT에서 가져올 예정
                    .accountId(reqUserJoin.getUserId())
                    .agreeFl(reqMarketingAgree.getAgreeFlEnum())
                    .agreeDtm(LocalDateTime.now())
                    .regUser(reqUserJoin.getUserId())//TODO 서버이름넣기
                    .templateSq(parseInt((reqMarketingAgree.getTemplateSq())))
                    .build();
            marketingOkEntityList.add(marketingAgreeEntity);
            marketingmarketingPkSq++;
        }
        marketingAgreeThRepository.saveAll(marketingOkEntityList);
    }


    public UserLoginRespDto login(UserLoginReqDto reqLoginDto) {
        //아이디로 일단 조회하고 계정이 없으면 예외던지기
        String reqUserId = reqLoginDto.getUserId();
        UserEntity userEntity = userTbRepository.findById(reqUserId).orElseThrow(()
                -> new InvalidRequestException(INVALID_USER_ID_MISMATCH));

        //아이디가 일치하니까 ,비번을 비교
        if (!passwordEncoder.matches(reqLoginDto.getUserPwd(), userEntity.getUserPwd())) {
            throw new InvalidRequestException(INVALID_USER_PWD_MISMATCH);
        }

        // 비밀번호가 일치하니까 lastLoginDt 업데이트
        userEntity.setLastLoginDt(LocalDate.now());
        userTbRepository.save(userEntity); // 사용자 엔티티 저장

        // 로그인 성공 시 UserLoginRespDto를 생성하여 반환
        return UserLoginRespDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .phoneNo(user.getPhoneNo())
                .emailId(user.getEmailId())
                .emailDomain(user.getEmailDomain())
                .lastLoginDt(user.getLastLoginDt()) // 로그인 시간
                .build();
    }


}


/* 개인공부
findById 메서드는 Optional<UserEntity> 객체를 반환
해당 userId가 userTbRepository에 존재한다면, Optional 객체 안에 UserEntity가 담겨서 반환돼.
만약 해당 userId가 데이터베이스에 없다면, 빈 Optional 객체가 반환돼. 즉, Optional.empty()가 반환
 */
