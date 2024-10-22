package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Schema(description = "(선택적)마케팅 약관 동의 DTO ")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserMarketingAgreeDto {

    @NotNull(message = "1010")
     String templateSq;

    @NotBlank(message = "1011")
     String agreeFl;

}
