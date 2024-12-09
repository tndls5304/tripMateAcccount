package com.tripmate.account.guest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Schema(description = "숙박이용자의 비밀번호 번경 요청 DTO")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuestModifyPwdReqDto {

    @NotBlank(message = "1004")
    String oldPwd;

    @NotBlank(message = "1004")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "1005")                //  "영문자 혹은 숫자만 입력 가능"
    String newPwd;

    @AssertFalse(message = "1023")
    public boolean isNewPwdAndOldPwdDuplicate(){
        return oldPwd.equals(newPwd);
    }
}
