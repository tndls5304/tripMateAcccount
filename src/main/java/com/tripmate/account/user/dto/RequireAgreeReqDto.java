package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "선택/필수 약관 동의 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequireAgreeReqDto {
    int templateSq;
    char agreeFl;
}
