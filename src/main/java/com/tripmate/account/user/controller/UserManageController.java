package com.tripmate.account.user.controller;

import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.UserJoinReqDto;
import com.tripmate.account.user.dto.UserUpdatePwdReqDto;
import com.tripmate.account.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.tripmate.account.common.errorcode.CommonErrorCode.SUCCESS;

/**
 * user(숙박회원) 계정 관련 처리
 *
 * @author 이수인
 * @since 2024.10.02
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "투숙객 계정 관련 ", description = "투숙객 계정관련 API")
public class UserManageController {
    private final UserManageService service;

    /**
     * 숙박회원의 아이디 중복 검사
     *
     * @param userId 입력한 아이디
     * @return 중복된 아이디가 존재시 에러 코드와 메시지를 포함한 ResponseEntity를 반환,중복되지 않을 경우 성공 응답을 반환
     */
    @GetMapping("api/user/join/duplicate")
    @Operation(summary = "투숙객 회원가입시 id 중복 검사", description = "userId를 이용해 투숙객의 id 중복 검사")
    public ResponseEntity<CommonResponse<Void>> checkUserIdDuplicate(@Valid @RequestParam String userId) {
        return service.checkUserIdDuplicate(userId);
    }

    /**
     * (숙박회원) 가입 요청
     *
     * @param userJoinReqDto 개인정보와 (필수)약관동의리스트,마케팅약관동의리스트(선택적)를 받음
     * @return 회원가입 성공시 성공 응답코드와 메세지 전달, 실패시 예외 발생--> 에러코드,메세지 전달
     */
    @PostMapping("/api/user/join")
    @Operation(summary = "투숙객 회원가입 등록 ", description = "투숙객 회원가입 요청 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 유효성 검사 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 id로 id 중복검사 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버오류", content = @Content(mediaType = "application/json"))
    })

    public ResponseEntity<CommonResponse<Void>> userJoin(@Valid @RequestBody UserJoinReqDto userJoinReqDto) {
        service.userJoin(userJoinReqDto);
        return new CommonResponse<>().toRespNoDataEntity(SUCCESS);
    }

  //숙박회원 비밀번호 변경
    @PostMapping("api/user/change")
    public ResponseEntity<CommonResponse<Void>> updatePwd(@RequestBody UserUpdatePwdReqDto userUpdatePwdReqDto){
         service.updatePwd(userUpdatePwdReqDto);
        return new CommonResponse<>().toRespNoDataEntity(SUCCESS);
    }
}



