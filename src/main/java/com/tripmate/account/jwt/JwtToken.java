package com.tripmate.account.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class JwtToken {
    private String accessTokenValidity;
    private String refreshTokenValidity;
}
