package com.tripmate.account.common.errorcode;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;

//@Getter
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CommonErrorCode {
    /*이늄의 각 상수는 이늄클래스가 구현한 인스턴스다.
      에러메세지는 클라이언트에게 보여지는것으로 자주 수정할 수있으므로 여기서 관리하는게 유지보수에 좋을것이다. */

    //0000번 : 성공
    SUCCESS("0000", "성공",HttpStatus.OK),

    // 1000~1099: 클라이언트 유효성 검사 관련 에러 코드-DTO에서함
    ID_BLANK("1001", "아이디는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    ID_INVALID("1002", "아이디는 대,소문자의 알파벳과 숫자, 언더스코어(_)가 허용됩니다 단,하나 이상의 영문자를 기입하셔야 합니다",HttpStatus.BAD_REQUEST),
    ID_TOO_LONG("1003", "아이디는 1~20자까지 가능합니다.",HttpStatus.BAD_REQUEST),
    PWD_BLANK("1004", "비밀번호는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    PWD_INVALID("1005", "비밀번호는 영어 대소문자와 숫자로만 구성되어야 합니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_BLANK("1006", "닉네임은 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_ONLY_KOREAN("1007", "닉네임은 한글로만 작성해야 합니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_TOO_LONG("1008", "닉네임은 최대 10자 (30바이트)까지 가능합니다.",HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_BLANK("1009", "휴대폰 번호는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_INVALID("1010", "휴대폰 번호는 숫자 11자리로 입력해야 합니다.",HttpStatus.BAD_REQUEST),
    EMAIL_ID_BLANK("1011", "이메일 아이디는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    EMAIL_ID_TOO_LONG("1012", "이메일 아이디는 최대 30자까지 가능합니다.",HttpStatus.BAD_REQUEST),
    EMAIL_DOMAIN_BLANK("1013", "이메일 도메인은 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    EMAIL_DOMAIN_TOO_LONG("1014", "이메일 도메인은 최대 30자까지 가능합니다.",HttpStatus.BAD_REQUEST),

    BASIC_AGREE_BLANK("1015","필수 약관자체가!!! 누락되었습니다",HttpStatus.BAD_REQUEST),
    BASIC_AGREE_TEMPLATE_BLANK("1016","필수 약관 템플릿이 전달되지 않았거나 약관 템플릿 번호가 비어 있습니다.",HttpStatus.BAD_REQUEST),
    BASIC_AGREE_FL_BLANK("1017","필수약관의 동의 여부가 누락되었습니다",HttpStatus.BAD_REQUEST),
    BASIC_AGREE_FL_Y_BLANK("1018","필수약관은 꼭 동의하셔야 합니다",HttpStatus.BAD_REQUEST),

    MARKETING_AGREE_BLANK("1019","마케팅 약관자체가!!! 누락되었습니다",HttpStatus.BAD_REQUEST),
    MARKETING_AGREE_TEMPLATE_BLANK("1020","마케팅 동의서 템플릿이 선택되지 않았습니다. 다시 시도해주세요",HttpStatus.BAD_REQUEST),
    MARKETING_AGREE_FL_BLANK("1021","마케팅동의 여부가 누락되었습니다",HttpStatus.BAD_REQUEST),
    MARKETING_AGREE_FL_YN_BLANK("1022","마케팅약관에 동의, 비동의를 했는지 확인해주세요",HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATION_ERROR("1023","현재 비밀번호,새 비밀번호 입력값이 같습니다 다르게 입력해주세요",HttpStatus.BAD_REQUEST),

    //1100~1199: 클라이언트측 요청,입력이 서버의 현재 상태 또는 비즈니스 규칙과 충돌하는 경우-서비스단에서함
    CONFLICT_ACCOUNT_ALREADY_EXISTS("1101", "이미 존재하는 아이디입니다.다른 id로 재 요청해주세요",HttpStatus.CONFLICT),
    CONFLICT_MARKETING_AGREE_FL_N_DUPLICATE("1103","이미 비동의된 상태입니다",HttpStatus.CONFLICT),

    //1200~:(유효성 검사 제외 하고) 사용자 요청, 입력이 잘못된 경우-서비스단에서함 InvalidRequestException
    INVALID_BASIC_AGREE_BLANK("1200","필수 약관 동의를 기입해주세요",HttpStatus.BAD_REQUEST),
    INVALID_MARKETING_AGREE_BLANK("1201","마케팅 동의 요청 항목이 비어 있습니다. 적어도 하나의 동의 항목을 선택해서 요청해주세요",HttpStatus.BAD_REQUEST),
    INVALID_USER_ID_MISMATCH("1202","아이디에 해당하는 계정은 존재하지 않습니다",HttpStatus.BAD_REQUEST),
    INVALID_USER_PWD_MISMATCH("1203","비밀번호가 틀렸습니다",HttpStatus.BAD_REQUEST),



    //2000번대 : 인증 및 권한 오류 시큐리티----------바꾸기 TODO
    UNAUTHORIZED_ACCESS("2000", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED), //HttpStatus 401
    FORBIDDEN_ACCESS("2001", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),//403 Forbidden

    USERNAME_NOT_FOUND("2002", "입력하신 id는 가입내역이 없습니다", HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS("2003", "입력하신 비밀번호는 틀렸습니다", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("2004", "너무 많은 로그인 실패로 계정이 잠겼습니다 ", HttpStatus.FORBIDDEN),
    CREDENTIALS_EXPIRED("2005", "오래된 비밀번호로 인증정보가 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("2006", "관리자에게 문의해주세요 계정이 비활성화되었습니다", HttpStatus.FORBIDDEN),
    ACCOUNT_EXPIRED("2007", "계정사용기간이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    CONTENT_TYPE_NOT_SUPPORTED("2008","요청 데이터 형식이 지원되지 않습니다. ",HttpStatus.BAD_REQUEST),

    UNEXPECTED_AUTHENTICATION_FAILED("2099", "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    //2100번대 : jwt 토큰 권한 인증
    JWT_REFRESH_TOKEN_EXPIRED("2100","로그인 세션이 만료되었습니다. 다시 로그인해 주세요.",HttpStatus.UNAUTHORIZED),
    JWT_REFRESH_TOKEN_INVALID_FORMAT("2101","토큰형식이 잘못되었습니다",HttpStatus.BAD_REQUEST),
    JWT_REFRESH_TOKEN_INVALID_SIGNATURE("2102","토큰이 위조 되었습니다",HttpStatus.UNAUTHORIZED),
    JWT_REFRESH_TOKEN_UNKNOWN_ERROR("2103","알수없는 토큰오류입니다",HttpStatus.INTERNAL_SERVER_ERROR),

    JWT_ACCESS_TOKEN_EXPIRED("2104","세션이 만료되었습니다. 새로고침 중입니다..",HttpStatus.UNAUTHORIZED),
    JWT_ACCESS_TOKEN_INVALID_FORMAT("2105","토큰형식이 잘못되었습니다",HttpStatus.BAD_REQUEST),
    JWT_ACCESS_TOKEN_INVALID_SIGNATURE("2106","토큰이 위조 되었습니다",HttpStatus.UNAUTHORIZED),
    JWT_ACCESS_TOKEN_UNKNOWN_ERROR("2107","알수없는 토큰오류입니다",HttpStatus.INTERNAL_SERVER_ERROR),

    JWT_REQ_VALIDATION_ACCESS_BLANK("2108","요청하신 토큰이 누락됐습니다",HttpStatus.BAD_REQUEST),
    JWT_REQ_VALIDATION_REFRESH_BLANK("2109","요청하신 토큰이 누락됐습니다",HttpStatus.BAD_REQUEST),

    JWT_SAVED_REFRESH_TOKEN_NOT_FOUND("2110","토큰을 찾을 수없습니다 다시 로그인 해주세요", HttpStatus.NOT_FOUND),
    JWT_REFRESH_TOKEN_MISMATCH("2111","토큰이 일치하지 않습니다 다시 로그인해주세요 ",HttpStatus.BAD_REQUEST),

    //3000번대:일반고객 상대

    //4000번대:db연결 문제
    DATABASE_CONNENCTION_ERROR("4000", "데이터베이스 연결 오류",HttpStatus.INTERNAL_SERVER_ERROR),



    // 5000번대: 일반 서버 오류
    INTERNAL_SERVER_ERROR("5000", "서버오류",HttpStatus.INTERNAL_SERVER_ERROR),

    //9999: 예게치 못한 서버 오류 매칭되는 에러가 없을때
    NO_MATCHING_ERROR_CODE("9999", "매칭되는 에러코드가 없습니다.",HttpStatus.INTERNAL_SERVER_ERROR);

    final String code;
    final HttpStatus httpStatus;
    final String message;

    // Getter methods
    public String getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }

    public HttpStatusCode getHttpStatus(){
        return httpStatus;
    }

    CommonErrorCode(String code, String message,HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus=httpStatus;
    }

    /*조회할때마다 반복문을 타는 걸로 구현했었는데 빈번하게 호출하는 경우에는 Map에 캐시해두는게 성능측면과 속도면에서 좋아서 수정했다.
     해시맵은 평균적으로 O(1) 시간 복잡도를 가지고 있기 때문에 빠르게 데이터를 찾을 수 있고
      반복문을 줄이면 CPU 사용량이 감소하고, 그에 따라 애플리케이션의 전반적인 성능도 개선된다.
      static 블록으로 반복문은 클래스가 로드될 때 한 번만 실행된다. */

    private static final Map<String, CommonErrorCode> errorCodeMap = new HashMap<>();

    static {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            errorCodeMap.put(errorCode.getCode(), errorCode); //key: 0000 ,  value: SUCCESS("0000", "성공")
        }
    }

    //코드번호로 CommonErrorCode.SUCCESS 인스턴스를 반환
    public static CommonErrorCode fromCode(String code) {
        CommonErrorCode errorCode = errorCodeMap.get(code);

        if (errorCode == null) {
            //TODO 예외이름이 적절하지 못해서 바꿔야 함. 혹시 이렇게 예기치못한 에러가 있으면 새로운 예외를 만들자.
            // throw new ServerErrorException(NO_MATCHING_ERROR_CODE);
            log.error("NO_MATCHING_ERROR_CODE : {}", code);
            return NO_MATCHING_ERROR_CODE;
        }
        return errorCode;
    }
}


/*
-----클래스가 로드 될떄 실제 동작하는것 !!--------
public class CommonErrorCode {
    public static final CommonErrorCode SUCCESS = new CommonErrorCode(0000, "성공");
    public static final CommonErrorCode USER_ID_BLANK = new CommonErrorCode(1001, "아이디는 빈 값이 될 수 없습니다.");
}
 */