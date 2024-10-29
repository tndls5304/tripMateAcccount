package com.tripmate.account.user.controller;

import com.tripmate.account.common.reponse.CommonResponse;
import com.tripmate.account.user.dto.*;
import com.tripmate.account.user.service.UserManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
     * (숙박회원) 아이디 중복 검사
     *
     * @param userId 중복 검사 요청 id
     * @return 중복된 아이디가 존재시 에러 코드와 메시지를 포함한 ResponseEntity 를 반환,중복되지 않을 경우 성공 응답을 반환
     */
    @GetMapping("api/account/user/join/duplicate")
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
    @PostMapping("api/account/user/join")
    @Operation(summary = "투숙객 회원가입 등록 ", description = "투숙객 회원가입 요청 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 유효성 검사 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 필수약관이 없음", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 마케팅 약관이 없음", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 id로 id 중복검사 실패", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CommonResponse<Void>> userJoin( @Valid @RequestBody UserJoinReqDto userJoinReqDto) {
        service.userJoin(userJoinReqDto);
        return new CommonResponse<>().toRespEntity(SUCCESS);
    }


    //로그인
    public ResponseEntity<CommonResponse<UserLoginRespDto>> login(@Valid @RequestBody UserLoginReqDto userLoginReqDto) {
        UserLoginRespDto userLoginRespDto = service.login(userLoginReqDto);
        return new CommonResponse<UserLoginRespDto>().toRespEntity(userLoginRespDto, SUCCESS);
    }


    /**
     * (숙박회원)비밀번호 변경 요청
     *
     * @param modifyUserPwdDto 현재 비밀번호와 바꿀 비밀번호
     * @return 현재 비밀번호 유효성검사를 한 후 성공이면 비밀번호 성공 응답코드,메세지 전달
     */
    @PutMapping("api/account/user/pwd")
    @Operation(summary = "투숙객 비밀번호 변경 ", description = "현재 비밀번호와 바꿀 비밀번호를 입력후 변경 요청 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 유효성 검사 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 필수약관이 없음", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 : 마케팅 약관이 없음", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 id로 id 중복검사 실패", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CommonResponse<Void>> updatePwd(@Valid @RequestBody UserModifyPwdReqDto modifyUserPwdDto) {
        service.modifyPwd(modifyUserPwdDto);
        return new CommonResponse<>().toRespEntity(SUCCESS);
    }


    /**
     * 마케팅 동의 수정 요청
     * 이전 마케팅약관에 동의를 한적 있다면 동의이력을 수정하거나 동의한적 없으면 동의이력 생성
     * @param ModifyMarketingAgreeDtoList 마케팅 동의 리스트
     * @return
     */
    @PostMapping("api/account/user/marketing")
    public ResponseEntity<CommonResponse<Void>> modifyMarketingAgree(@Valid @RequestBody List<UserModifyMarketingAgreeReqDto> ModifyMarketingAgreeDtoList){
        service.modifyMarketingAgree(ModifyMarketingAgreeDtoList);
        return new CommonResponse<>().toRespEntity(SUCCESS);
    }
}



