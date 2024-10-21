package com.tripmate.account.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@NoArgsConstructor
@Getter
@Schema(description = "(선택적)마케팅 약관 동의 DTO ")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MarketingAgreeReqDto {
    @JsonProperty("templateSq")
    @NotNull(message = "1010")
     String templateSq;

    @JsonProperty("agreeFl")
    @NotBlank(message = "1011")
     String agreeFl;


    public MarketingAgreeReqDto(
            @JsonProperty("templateSq") String templateSq,
            @JsonProperty("agreeFl") String agreeFl
    ) {
        this.templateSq = templateSq;
        this.agreeFl = agreeFl;
    }
}
