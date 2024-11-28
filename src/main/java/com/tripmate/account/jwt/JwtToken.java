package com.tripmate.account.jwt;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {

    private String accessToken;    // Access Token
    private String refreshToken;   // Refresh Token
}
