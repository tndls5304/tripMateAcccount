package com.tripmate.account.user.controller;

import com.tripmate.account.common.errorCode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.common.entity.UserEntity;
import com.tripmate.account.user.service.UserManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserManageController {
    private final UserManageService service;

    @PostMapping("api/user/join")
    public ResponseEntity<CommonResponse<Object>> userJoin(@Valid @RequestBody UserJoinDto userJoinDto){

        UserEntity savedEntity = service.uerJoin(userJoinDto);
        if (savedEntity == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonResponse<>(CommonErrorCode.INTERNER_SERVER_ERROR));
        } else {
            return ResponseEntity.ok(new CommonResponse<>(CommonErrorCode.SUCCESS));
        }
    }
}

/*
CommonErrorCode.SUCCESS는 어떻게 동작?
CommonErrorCode.SUCCESS는 Enum의 한 값을 의미
Enum 호출: CommonErrorCode.SUCCESS라고 하면 SUCCESS(0000, "성공")이라는 Enum 값이 호출됩니다.
 Getter 메서드 사용: CommonErrorCode.SUCCESS.getCode()를 호출하면 **0000**을 반환하고, CommonErrorCode.SUCCESS.getMessage()를 호출하면 **성공**이라는 메시지를 반환합니다.
 */