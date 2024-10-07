package com.tripmate.account.common.errorcode;

import com.tripmate.account.common.exception.ServerErrorException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

//@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CommonErrorCode {
    /*이늄의 각 상수는 이늄클래스가 구현한 인스턴스다.
      에러메세지는 클라이언트에게 보여지는것으로 자주 수정할 수있으므로 여기서 관리하는게 유지보수에 좋을것이다. */

    //0000번 : 성공
    SUCCESS("0000", "성공",HttpStatus.OK),
    // 1000~1099: 클라이언트 유효성 검사 관련 에러 코드
    USER_ID_BLANK("1001", "아이디는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    USER_ID_INVALID("1002", "아이디는 영문자, 숫자, '.', '_', '%', '+', '-'만 사용할 수 있습니다.",HttpStatus.BAD_REQUEST),
    USER_ID_TOO_LONG("1003", "아이디는 최대 30자까지 가능합니다.",HttpStatus.BAD_REQUEST),
    USER_PWD_BLANK("1004", "비밀번호는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    USER_PWD_INVALID("1005", "비밀번호는 영문자와 숫자로만 구성되어야 합니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_BLANK("1006", "닉네임은 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_ONLY_KOREAN("1007", "닉네임은 한글로만 작성해야 합니다.",HttpStatus.BAD_REQUEST),
    NICKNAME_TOO_LONG("1008", "닉네임은 최대 10자 (30바이트)까지 가능합니다.",HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_BLANK("1009", "휴대폰 번호는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_INVALID("1010", "휴대폰 번호는 숫자 11자리로 입력해야 합니다.",HttpStatus.BAD_REQUEST),
    EMAIL_ID_BLANK("1011", "이메일 아이디는 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    EMAIL_ID_TOO_LONG("1012", "이메일 아이디는 최대 30자까지 가능합니다.",HttpStatus.BAD_REQUEST),
    EMAIL_DOMAIN_BLANK("1013", "이메일 도메인은 빈 값이 될 수 없습니다.",HttpStatus.BAD_REQUEST),
    EMAIL_DOMAIN_TOO_LONG("1014", "이메일 도메인은 최대 30자까지 가능합니다.",HttpStatus.BAD_REQUEST),

    //1100~1199:회원가입 관련 서비스단에서 문제
    USER_ALREADY_EXISTS("1101", "이미 존재하는 아이디입니다.다른 id로 재 요청해주세요",HttpStatus.CONFLICT),

    //2000번대:db연결 문제
    DATABASE_CONNENCTION_ERROR("2000", "데이터베이스 연결 오류",HttpStatus.INTERNAL_SERVER_ERROR),
    //3000번대:일반고객 상대

    // 5000번대: 일반 서버 오류
    INTERNAL_SERVER_ERROR("5000", "서버오류",HttpStatus.INTERNAL_SERVER_ERROR ),

    //매칭되는 에러가 없을때
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

    public HttpStatus getHttpStatus(){
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
            throw new ServerErrorException(NO_MATCHING_ERROR_CODE);
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