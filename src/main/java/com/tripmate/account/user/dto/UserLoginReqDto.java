package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Schema(description = "숙박회원 로그인 요청DTO")
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginReqDto {
    @NotBlank(message = "1001")
    String userId;

    @NotBlank(message = "1004")
    String userPwd;
}
