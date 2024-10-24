package com.tripmate.account.user.dto;

import com.tripmate.account.common.entity.chose.AgreeFl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Schema(description = "숙박회원의 마케팅 약관 동의 등록 요청 DTO ")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateMarketingAgreeDto {

    @NotNull(message = "1020")
     String templateSq;

    @NotNull(message = "1021")
    AgreeFl agreeFl;

}
