package com.tripmate.account.common.errorcode;

import com.tripmate.account.common.exception.InvalidErrorException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import java.util.HashMap;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CommonErrorCode {
    //0000번 : 성공
    SUCCESS("0000", "성공"),
    // 1000~1099: 클라이언트 유효성 검사 관련 에러 코드
    USER_ID_BLANK("1001", "아이디는 빈 값이 될 수 없습니다."),
    USER_ID_INVALID("1002", "아이디는 영문자, 숫자, '.', '_', '%', '+', '-'만 사용할 수 있습니다."),
    USER_ID_TOO_LONG("1003", "아이디는 최대 30자까지 가능합니다."),
    USER_PWD_BLANK("1004", "비밀번호는 빈 값이 될 수 없습니다."),
    USER_PWD_INVALID("1005", "비밀번호는 영문자와 숫자로만 구성되어야 합니다."),
    NICKNAME_BLANK("1006", "닉네임은 빈 값이 될 수 없습니다."),
    NICKNAME_ONLY_KOREAN("1007", "닉네임은 한글로만 작성해야 합니다."),
    NICKNAME_TOO_LONG("1008", "닉네임은 최대 10자 (30바이트)까지 가능합니다."),
    PHONE_NUMBER_BLANK("1009", "휴대폰 번호는 빈 값이 될 수 없습니다."),
    PHONE_NUMBER_INVALID("1010", "휴대폰 번호는 숫자 11자리로 입력해야 합니다."),
    EMAIL_ID_BLANK("1011", "이메일 아이디는 빈 값이 될 수 없습니다."),
    EMAIL_ID_TOO_LONG("1012", "이메일 아이디는 최대 30자까지 가능합니다."),
    EMAIL_DOMAIN_BLANK("1013", "이메일 도메인은 빈 값이 될 수 없습니다."),
    EMAIL_DOMAIN_TOO_LONG("1014", "이메일 도메인은 최대 30자까지 가능합니다."),

    //1100~1199:회원가입 관련 서비스단에서 문제
    USER_ALREADY_EXISTS("1101", "이미 존재하는 아이디입니다."),

    //2000번대:db연결 문제
    DATABASE_CONNENCTION_ERROR("2000", "데이터베이스 연결 오류"),
    //3000번대:일반고객 상대

    // 5000번대: 일반 서버 오류
    INTERNAL_SERVER_ERROR("5000", "서버오류"),

    //매칭되는 에러가 없을때
    NO_MATCHING_ERROR_CODE("9999", "매칭되는 에러코드가 없습니다.");

    final String code;
    final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private static final Map<String, CommonErrorCode> errorCodeMap = new HashMap<>();

    static {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            errorCodeMap.put(errorCode.getCode(), errorCode); //0000 ,  SUCCESS("0000", "성공")
        }
    }

    public static CommonErrorCode fromCode(String code) {
        CommonErrorCode errorCode = errorCodeMap.get(code);

        if (errorCode == null) {
            throw new InvalidErrorException(NO_MATCHING_ERROR_CODE);
        }
        return errorCode;
    }
}


/*
public class CommonErrorCode {
    public static final CommonErrorCode SUCCESS = new CommonErrorCode(0000, "성공");
    public static final CommonErrorCode USER_ID_BLANK = new CommonErrorCode(1001, "아이디는 빈 값이 될 수 없습니다.");

    private final int code;
    private final String message;

    public CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
 */