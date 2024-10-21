package com.tripmate.account.user.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;


@Schema(description = "일반고객 회원가입 요청DTO")
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJoinReqDto {

    //user테이블
    @JsonProperty("userId")
    @NotBlank(message = "1001")
    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$", message = "1002")           //하나 이상의 소문자 또는 대문자 영문자가 포함되어야 하며, 숫자와 언더스코어도 허용
    @Size(min = 1, max = 20, message = "1003")
    String userId;

    @JsonProperty("userPwd")
    @NotBlank(message = "1004")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "1005")                //  "영문자 혹은 숫자만 입력 가능"
    String userPwd;

    @JsonProperty("nickname")
    @NotBlank(message = "1006")
    @Pattern(regexp = "^[가-힣]*$", message = "1007")                     // "한글만 입력 가능"
    @Size(max = 10, message = "1008")    //*db 최대 데이터 길이는 30바이트로 설정됐기에 한글 최대 길이 10까지
    String nickname;

    @JsonProperty("phoneNo")
    @NotBlank(message = "1009")
    @Pattern(regexp = "^\\d{11}$", message = "1010")                     // "숫자로만 구성된 11자리"
    String phoneNo;

    @JsonProperty("emailId")
    @NotBlank(message = "1011")
    @Size(max = 30, message = "1012")
    String emailId;

    @JsonProperty("emailDomain")
    @NotBlank(message = "1013")
    @Size(max = 30, message = "1014")
    String emailDomain;

    //필수 약관 동의
    @JsonProperty("requireAgreeList")
    @NotEmpty(message = "1015")
    List<RequireAgreeReqDto> requireAgreeList;

    //(선택)마케팅 약관동의
    @JsonProperty("marketingAgreeList")
    @NotEmpty(message = "1019")
    List<MarketingAgreeReqDto> marketingAgreeList;

    // 기본 생성자 필요
    public UserJoinReqDto() {
    }


    public UserJoinReqDto(
            @JsonProperty("userId") String userId,
            @JsonProperty("userPwd") String userPwd,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("phoneNo") String phoneNo,
            @JsonProperty("emailId") String emailId,
            @JsonProperty("emailDomain") String emailDomain,
            @JsonProperty("requireAgreeList") List<RequireAgreeReqDto> requireAgreeList,
            @JsonProperty("marketingAgreeList") List<MarketingAgreeReqDto> marketingAgreeList
    ) {
        this.userId = userId;
        this.userPwd = userPwd;
        this.nickname = nickname;
        this.phoneNo = phoneNo;
        this.emailId = emailId;
        this.emailDomain = emailDomain;
        this.requireAgreeList = requireAgreeList;
        this.marketingAgreeList = marketingAgreeList;
    }
}
