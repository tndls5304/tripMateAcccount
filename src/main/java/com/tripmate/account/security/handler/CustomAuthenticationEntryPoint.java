package com.tripmate.account.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripmate.account.common.reponse.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.tripmate.account.common.errorcode.CommonErrorCode.UNAUTHORIZED_ACCESS;

/**
 * 로그인할때 요청한 id가 db에서 조회했을떄 없을때  UsernameNotFoundException 발생하며 이 핸들러가 예외를 처리하게 됩니다
 * 이같이 인증 안된 익명의 사용자가 인증이 필요한 엔드포인트로 접근하게 된다면 Spring Security의 기본 설정으로는 HttpStatus 401과 함께 스프링의 기본 오류페이지를 보여줍니다
 * JSON 데이터 등으로 응답해야 하기때문에 커스텀하였습니다.
 * 예외 정보와 요청url을 로그로 기록합니다.
 * (DispatcherServlet 이후에 발생하는 예외를 관리하는 GlobalExceptionHandler와 다릅니다.
 * GlobalExceptionHandler는 Spring Security 관련 설정에서 발생하는 예외를 잡지 못합니다 )
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    /**
     * 응답 내용을 json문자열로 바꾸기 위해 스프링빈에 등록된 ObjectMapper를 이용
     *
     * @param objectMapper JSON 직렬화를 위한 ObjectMapper 객체
     */
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증되지 않은 사용자가 보호된 엔드포인트에 접근하려 할 때 호출되는 메서드
     * UNAUTHORIZED_ACCESS 에러 코드와 커스텀 에러메세지를 Json형태로 응답하기 위함
     *
     * @param request 클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param authException 인증 예외 정보
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿예외
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Not Authenticated Request", authException);
        log.error("Request Uri : {}", request.getRequestURI());
        ResponseEntity<CommonResponse<Void>> commonResponse = new CommonResponse<>().toRespEntity(UNAUTHORIZED_ACCESS);

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());      //401
        // response.setStatus(commonResponse.getStatusCodeValue()); ->  ResponseEntity의 상태 코드를 정수형으로 가져옴

        String jsonResponse = objectMapper.writeValueAsString(commonResponse.getBody());
        response.getWriter().write(jsonResponse);
    }
}


/*
공부
commonResponse.getBody()를 하면?
ResponseEntity<CommonResponse<Void>> 객체에서 CommonResponse<Void> 본문을 가져오는 메소드입니다.
이 경우 CommonResponse 객체는 다음과 같은 내용을 포함하고 있습니다:

codeNo: 응답 상태 코드 (예: "4000", "0000" 등).
message: 응답 메시지 (예: "인증이 필요합니다.", "성공" 등).
data: 응답에 포함된 데이터. 여기서는 Void 타입이기 때문에, 일반적으로 null이 될 수 있습니다. 즉, 데이터가 없음을 나타냅니다.

{
  "codeNo": "4000",
  "message": "인증이 필요합니다.",
  "data": null
}

◽Body부분만 왜 줘야 하나 ResponseEntity<CommonResponse<Void>> 객체 그대로 전달하는게 낫지 않을까 생각했었는데

String jsonResponse = objectMapper.writeValueAsString(commonResponse);와 같이
전체 commonResponse 객체를 JSON으로 변환하는 것도 가능하다.
만약 commonResponse 객체 자체를 직렬화하면, 구조는 :
{
  "statusCode": 401,        // HTTP 상태 코드
  "headers": { ... },       // 헤더 정보
  "body": {
    "codeNo": "4000",
    "message": "인증이 필요합니다.",
    "data": null
  }

이런형태로 전달한다.
이제까지 개발하면서 에러나 성공일떄 클라이언트에게 보내는 응답객체는 code번호와 메세지 그리고 데이터만 보내고 있다
통일성을 위해 객체의 body부분을 JSON으로 변환해서 내려주고 http상태코드는  response의 status에 입력하기로 했다.

}
 */