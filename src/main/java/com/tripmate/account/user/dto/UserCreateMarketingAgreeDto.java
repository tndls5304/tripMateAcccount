package com.tripmate.account.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripmate.account.common.enums.AgreeFl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Getter
@Schema(description = "숙박회원의 마케팅 약관 동의 등록 요청 DTO ")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateMarketingAgreeDto {

    @NotBlank(message = "1020")
    String templateSq;

    @JsonProperty("agreeFl")
    @NotBlank(message = "1021")
    @Pattern(regexp = "^[YyNn]$", message = "1022") //대소문YN을제외하고 오류뱉어야는데 안먹힘
    String originAgreeFl;

    public AgreeFl getAgreeFlEnum() {
        return AgreeFl.valueOf(originAgreeFl.toUpperCase()); //  대문자로 바꿔서 반환
    }
}

/*
------공부------
이렇게 유효성검사 할 수도 있음. 이건 필드에 공통적으로 검사해야 될떄 유용할거 같다.
@AssertFalse(message = "1022")
public boolean isAgreeFlInvalid() {
    return
            (originAgreeFl.equals("Y") ||
                    originAgreeFl.equals("y") ||
                    originAgreeFl.equals("N") ||
                    originAgreeFl.equals("n"));
}
*/
