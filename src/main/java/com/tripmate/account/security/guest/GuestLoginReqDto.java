package com.tripmate.account.security.guest;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuestLoginReqDto {
    @NotBlank(message = "1001")
    String guestId;

    @NotBlank(message = "1004")
    String guestPwd;
}
