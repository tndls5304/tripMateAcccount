package com.tripmate.account.swagger;


import com.tripmate.account.common.errorcode.CommonErrorCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {

            // 메서드에 ApiErrorCodeExample 애너테이션이 적용된 경우 해당 오류 코드를 설정
            ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
            if (apiErrorCodeExample != null) {
                generateErrorCodeResponseExample(operation, apiErrorCodeExample.value());
            } else {
                // 모든 메서드에 기본 응답을 추가 (200 성공, 500 서버 에러 등)
                addDefaultResponses(operation);
            }

            return operation;
        };
    }

    // 기본 응답을 추가하는 메서드
    private void addDefaultResponses(Operation operation) {
        Map<String, ApiResponse> defaultResponses = new HashMap<>();

        // 200 응답 추가
        ApiResponse successResponse = new ApiResponse().description("성공");
        defaultResponses.put("200", successResponse);

        // 400 응답 추가
        ApiResponse badRequestResponse = new ApiResponse().description("잘못된 요청");
        defaultResponses.put("400", badRequestResponse);

        // 404 응답 추가
        ApiResponse notFoundResponse = new ApiResponse().description("찾을 수 없음");
        defaultResponses.put("404", notFoundResponse);

        // 500 응답 추가
        ApiResponse serverErrorResponse = new ApiResponse().description("서버 에러");
        defaultResponses.put("500", serverErrorResponse);

        // Operation 객체에 기본 응답 추가
        defaultResponses.forEach((code, resp) -> operation.getResponses().addApiResponse(code, resp));
    }

    // 특정 에러 코드에 대한 응답을 추가하는 메서드
    private void generateErrorCodeResponseExample(Operation operation, Class<? extends Enum<CommonErrorCode>> type) {
        CommonErrorCode[] errorCodes = (CommonErrorCode[]) type.getEnumConstants();
        Map<String, ApiResponse> responsesMap = new HashMap<>();

        for (CommonErrorCode errorCode : errorCodes) {
            String httpStatusCode = String.valueOf(errorCode.getHttpStatus().value());
            String description = errorCode.getMessage();

            // ApiResponse 객체 생성
            ApiResponse response = new ApiResponse().description(description);
            responsesMap.put(httpStatusCode, response);
        }

        // Operation 객체에 에러 응답 추가
        responsesMap.forEach((code, resp) -> operation.getResponses().addApiResponse(code, resp));
    }
}
/*
handlerMethod란?
HandlerMethod는 Spring MVC에서 HTTP 요청을 처리할 메소드를 나타내는 클래스

 */