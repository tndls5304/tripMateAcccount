package com.tripmate.account.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripmate.account.common.enums.AgreeFl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@Schema(description = "마케팅 동의서 수정 요청 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserModifyMarketingAgreeReqDto {

    @NotBlank(message = "1020")
    String templateSq;

    @JsonProperty("agreeFl")
    @NotBlank(message = "1021")
    @Pattern(regexp = "^[YyNn]$", message = "1022") //대소문 yn만 허용
    String originAgreeFl;

    public AgreeFl getAgreeFlEnum() {
        return AgreeFl.valueOf(originAgreeFl.toUpperCase()); //  대문자로 바꿔서 반환
    }
}
