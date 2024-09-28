package com.tripmate.account.user.controller;

import com.tripmate.account.user.dto.UserJoinDto;
import com.tripmate.account.user.service.UserManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserManageController {
    private UserManageService service;

    @PostMapping("user/join")
    public UserJoinDto userJoin(@Valid @RequestBody UserJoinDto userDto){
     //   UserEntity userEntity =service.uerJoin();
        return userDto;
    }
}
