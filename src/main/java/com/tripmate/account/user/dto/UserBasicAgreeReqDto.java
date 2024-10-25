package com.tripmate.account.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripmate.account.common.custom.validation.AgreeFl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@ToString
@Schema(description = "(필수) 약관동의서 기록지 등록 요청 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBasicAgreeReqDto {

    @NotBlank(message = "1016")     //"templateSq": "" 빈문자열일때  ["templateSq": ""]자체가 없을때도 유효성검사에 걸림 @NotNull은 ["templateSq": ""]자체가 없을때만 유효성검사에서 걸림
    String templateSq;

    @NotBlank(message = "1017")
    @JsonProperty("agreeFl")
    @Pattern(regexp = "^[Yy]$", message = "1018")
    String originAgreeFl;

    // 일단 agreeFl을 String으로 받아서 유효성 검사를 통과한다면? AgreeFl enum으로 변환하게 함.
    public AgreeFl getAgreeFlEnum() {
        return AgreeFl.valueOf(originAgreeFl.toUpperCase()); //  대문자로 바꿔서 반환
    }


/**
 * 이렇게 했더니 Y,N일때만 유효성검사를 한다
 * 내가원하는건 Y,N일때도 검사를 하는거지만 다른문자열이나 공백이 왔을때도 유효성검사를 하길 바란거다.
 AgreeFl agreeFl;
 @AssertTrue(message = "1017") //
 public boolean isAgreeFlValid() {
 // agreeFl이 null일 경우 false 반환
 if (agreeFl == null) {
 return false;
 }
 // agreeFl이 Y 또는 N이 아닐 경우 false 반환
 return agreeFl == AgreeFl.Y;
 }
 **/

}
