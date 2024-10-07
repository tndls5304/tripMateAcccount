package com.tripmate.account.user.controller;

import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.swagger.ApiErrorCodeExample;
import com.tripmate.account.user.dto.UserJoinRequestDto;
import com.tripmate.account.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "투숙객 계정 관련 ", description = "투숙객 계정관련 API")
public class UserManageController {
    private final UserManageService service;

    @GetMapping("api/user/join/duplicate")
    @ApiErrorCodeExample(CommonErrorCode.class)// CommonErrorCode를 사용하여 애너테이션 적용
    @Operation(summary = "투숙객 id 중복 검사", description = "userId를 이용해 투숙객의 id 중복 검사")
    public ResponseEntity<CommonResponse<Void>> userDuplicateTest(@Valid @RequestParam String userId) {
        if (service.userDuplicateTest(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new CommonResponse<>(CommonErrorCode.USER_ALREADY_EXISTS));
        }
        return ResponseEntity.ok(new CommonResponse<>(CommonErrorCode.SUCCESS));
    }

    @PostMapping("api/user/join")
    @ApiErrorCodeExample(CommonErrorCode.class)
    @Operation(summary = "투숙객 회원가입", description = "투숙객 회원가입 요청 API")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
//            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 유효성 검사 실패", content = @Content(mediaType = "application/json")),
//            @ApiResponse(responseCode = "409", description = "이미 존재하는 id로 id 중복검사 실패", content = @Content(mediaType = "application/json")),
//            @ApiResponse(responseCode = "500", description = "서버오류",content = @Content(mediaType = "application/json"))
//    })
    public ResponseEntity<CommonResponse<Void>> userJoin(@Valid @RequestBody UserJoinRequestDto userJoinRequestDto) {
        service.userJoin(userJoinRequestDto);
        return ResponseEntity.ok(new CommonResponse<>(CommonErrorCode.SUCCESS));
    }

    /*
    HttpStatus 공부
        id 중복 테스트에서 이미 존재하는 id일때 HttpStatus.BAD_REQUEST 인줄 알았는데 아니다.
        어떤 HttpStatus인지 찾아봤다. 답은 409 Conflick!
        분명한 차이가 있다. 기억하기!

            **HttpStatus.CONFLICT (409)
            요청은 유효하고 잘 작성되었지만, 요청한 데이터가 서버의 리소스와 충돌하여 요청을 처리할 수 없는 상태
            **HttpStatus.BAD_REQUEST (400)
            요청의 데이터가 잘못되었기에 요청을 고쳐야 함. ex) 필수항목누락 혹은 잘못된 형식으로 입력

     */

//    @PostMapping("api/user/join/agree")
//    public ResponseEntity<CommonResponse<Void>> registerAgree(){
//      return   service.registerAgree();
//    }
}



/*
CommonErrorCode.SUCCESS는 어떻게 동작?
CommonErrorCode.SUCCESS는 Enum의 한 값을 의미
Enum 호출: CommonErrorCode.SUCCESS라고 하면 SUCCESS(0000, "성공")이라는 Enum 값이 호출됩니다.
 Getter 메서드 사용: CommonErrorCode.SUCCESS.getCode()를 호출하면 **0000**을 반환하고, CommonErrorCode.SUCCESS.getMessage()를 호출하면 **성공**이라는 메시지를 반환합니다.
 */