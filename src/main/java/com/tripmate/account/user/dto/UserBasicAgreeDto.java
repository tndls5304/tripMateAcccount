package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Schema(description = "선택/필수 약관 동의 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBasicAgreeDto {

    @NotNull(message = "1016")
    String templateSq;

    @NotBlank(message = "1017")
    @Pattern(regexp = "Y", message = "1018")
    String agreeFl;

}
