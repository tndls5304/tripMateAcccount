package com.tripmate.account.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "(선택적)마케팅 약관 동의 DTO ")
public class MarketingAgreeReqDto {
    int templateSq;
    char agreeFl;
}
