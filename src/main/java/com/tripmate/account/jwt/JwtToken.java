package com.tripmate.account.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {
    private String accessToken;    // Access Token
    private String refreshToken;   // Refresh Token


}
