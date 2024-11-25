package com.tripmate.account.guest.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;


@Schema(description = "숙박회원 가입 요청DTO")
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJoinReqDto {

    //user테이블
    @NotBlank(message = "1001")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$", message = "1002")           //하나 이상의 소문자 또는 대문자 영문자가 포함되어야 하며, 숫자와 언더스코어도 허용
    @Size(min = 1, max = 20, message = "1003")
    String userId;

    @NotBlank(message = "1004")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "1005")                //  "영문자 혹은 숫자만 입력 가능"
    String userPwd;

    @NotBlank(message = "1006")
    @Pattern(regexp = "^[가-힣]*$", message = "1007")                     // "한글만 입력 가능"
    @Size(max = 10, message = "1008")    //*db 최대 데이터 길이는 30바이트로 설정됐기에 한글 최대 길이 10까지
    String nickname;

    @NotBlank(message = "1009")
    @Pattern(regexp = "^\\d{11}$", message = "1010")                     // "숫자로만 구성된 11자리"
    String phoneNo;

    @NotBlank(message = "1011")
    @Size(max = 30, message = "1012")
    String emailId;

    @NotBlank(message = "1013")
    @Size(max = 30, message = "1014")
    String emailDomain;

    //필수 약관 동의
    @NotEmpty(message = "1015")
    List<@Valid UserBasicAgreeReqDto> basicAgreeDtoList;

    //(선택)마케팅 약관동의
    @NotEmpty(message = "1019")
    List<@Valid UserCreateMarketingAgreeDto> marketingAgreeDtoList;
}
