package com.tripmate.account.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokenRespDto {
    private String accessToken;
    private String refreshToken;
}
