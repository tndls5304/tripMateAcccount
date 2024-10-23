package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Schema(description = "숙박이용자의 비밀번호 번경 요청 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdatePwdReqDto {
    String userId;//TODO 화면에서 받을건 아님 추후 세션에서 받기
    @NotBlank(message = "1004")
    String currentPwd;
    @NotBlank(message = "1004")
    String newPwd;
}
