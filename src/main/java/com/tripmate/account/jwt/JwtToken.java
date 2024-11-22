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
    private String grantType;      // 인증 타입 (Bearer)

    public JwtToken(String accessToken, String refreshToken) {
    }
}
