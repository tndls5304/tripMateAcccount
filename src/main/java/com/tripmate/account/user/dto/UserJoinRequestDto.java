package com.tripmate.account.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "일반고객 회원가입 요청DTO")
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserJoinRequestDto {

    @NotBlank(message = "1001")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+$", message = "1002")           // "영문자, 숫자, '.', '_', '%', '+', '-'만 허용
    @Size(max = 30, message = "1003")
    String userId;

    @NotBlank(message = "1004")
    @Pattern(regexp = "^[a-zA-Z0-9]*$" ,message = "1005")                //  "영문자 혹은 숫자만 입력 가능"
    String userPwd;

    @NotBlank(message = "1006")
    @Pattern(regexp = "^[가-힣]*$", message = "1007")                     // "한글만 입력 가능"
    @Size(max = 10, message = "1008")    //*db 최대 데이터 길이는 30바이트로 설정됐기에 한글 최대 길이 10까지
    String nickname;

    @NotBlank(message = "1009")
    @Pattern(regexp = "^\\d{11}$" ,message = "1010")                     // "숫자로만 구성된 11자리"
    String phoneNo;

    @NotBlank(message = "1011")
    @Size(max = 30,message = "1012")
    String emailId;

    @NotBlank(message = "1013")
    @Size(max = 30,message = "1014")
    String emailDomain;

}
