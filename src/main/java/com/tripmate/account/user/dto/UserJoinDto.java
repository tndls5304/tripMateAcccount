package com.tripmate.account.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJoinDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+$", message = "아이디틀림")// "영문자, 숫자, '.', '_', '%', '+', '-'만 허용
    @Size(max = 30)
    String userId;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]*$")   //  "영문자 혹은 숫자만 입력 가능"
    String userPwd;

    @NotBlank
    @Pattern(regexp = "^[가-힣]*$")  // "한글만 입력 가능"
    @Size(max = 10)
    String nickname;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")   // "숫자로만 구성된 11자리"
    String phoneNo;

    @NotBlank
    @Size(max = 30)
    String emailId;

    @NotBlank
    @Size(max = 30)
    String emailDomain;

    String lastLoginDt;
    String pwdUpdDt;

}
