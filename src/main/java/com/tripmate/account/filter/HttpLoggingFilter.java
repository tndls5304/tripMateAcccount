package com.tripmate.account.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 요청과 응답을 캐싱 가능하도록 래핑
        ContentCachingRequestWrapper httpServletRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper httpServletResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        // 필터 체인을 통해 요청 진행
        chain.doFilter(httpServletRequest, httpServletResponse);

        // 요청 URI와 본문 로깅
        String uri = httpServletRequest.getRequestURI();
        String reqContent = new String(httpServletRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
        log.info("URI: {}, Request: {}", uri, reqContent);

        // 응답 상태와 본문 로깅
        int httpStatus = httpServletResponse.getStatus();
        String resContent = new String(httpServletResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        log.info("Status: {}, Response: {}", httpStatus, resContent);

        // 응답 복사해서 클라이언트로 보냄
        httpServletResponse.copyBodyToResponse();
        }

    }

