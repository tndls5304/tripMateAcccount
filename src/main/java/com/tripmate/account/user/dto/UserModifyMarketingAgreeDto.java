package com.tripmate.account.user.dto;

import com.tripmate.account.common.custom.validation.AgreeFl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@Schema(description = "마케팅 동의서 수정 요청 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserModifyMarketingAgreeDto {
 //   @NotNull(message = "1020")
    String templateSq;
   // @NotNull(message = "1021")
    AgreeFl agreeFl;
}
