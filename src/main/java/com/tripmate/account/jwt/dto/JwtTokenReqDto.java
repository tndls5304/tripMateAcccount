package com.tripmate.account.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JwtTokenReqDto {
    @NotBlank(message = "2108")
    private String accessToken;
    @NotBlank(message = "2109")
    private String refreshToken;
}
