package com.tripmate.account.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Component
public class HttpLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        /*
        나는 @RequestBody 을 써서
        Spring이 요청 본문을 읽어서 지정된 DTO로 매핑하게끔 했다
        그리고 나서 필터를 추가하고 요청 본문을 읽으려 하니 문제다⭐
        필터가 여기서 HTTP 본문 (body)을 먼저 읽어버리면,
        컨트롤러에서 DTO 매핑 시 데이터가 없기 때문에 DTO 필드가 null로 설정되거나 매핑이 이루어지지 않는 문제가 발생.
        그래서
        ContentCachingRequestWrapper를 사용했다. 요청 본문을 읽고, 한 번 읽은 본문 데이터를 내부적으로 캐시한다.

        요청 본문이 스트림 형태로 전달되는데,
        new ContentCachingRequestWrapper((HttpServletRequest) request); 생성자를 사용하면
        HttpServletRequest 객체를 넘겨주고,
        이 과정에서 요청 본문의 내용을 스트림으로 읽어서 내부 버퍼에 저장하고 캐싱하는 기능을 장착한
        wrappedRequest가 생성됨.
         */

        request.getInputStream().read();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        //서블릿리퀘스트안의 스트림이
        ServletInputStream servletInputStream=request.getInputStream();

        //헤더 정보는 스트림 없이 간편하게 볼 수 있다
        Enumeration<String> headerNames = ((HttpServletRequest) request).getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = ((HttpServletRequest) request).getHeader(headerName);
            System.out.println("⭐"+headerName + ": 🐳" + headerValue);
        }

        /*
             헤더 정보는 body 본문데이터와 다르게 서버에서 스트림 없이 헤더 정보를 읽어들인다.⭐ 왜???
             헤더 정보는 서버에 요청이 도착한 순간 이미 읽혀서 메모리에 저장되기 때문이다.
             그래서 스트림 없이 필터나 인터셉터 같은 서블릿 이전 단계에서 자유롭게 사용할 수 있다
             이걸 확인해보고 싶어서 콘솔에 찍음
        */

        chain.doFilter(wrappedRequest, wrappedResponse);

        /*필터 체인의 범위는 어디 까진가?
         서블릿으로 전달한 후, 서블릿이 비즈니스 로직을 처리하고
         HttpServletResponse를 통해 스트림을 생성하여 응답 데이터를 작성한 뒤,
         다시 필터로 돌아오는 과정까지를 포함*/


        /*여기서부터는 다시 필터로 돌아옴 */

        String uri = wrappedRequest.getRequestURI();

        // 이 코드는 요청 본문을 직접 읽는 것이 아니라, 서블릿에서 이미 읽은 요청 본문 데이터를 cachedContent 메모리에 저장한 후 그 데이터를 읽는것
        String reqContent = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.info("URI: {}, Request: {}", uri, reqContent);

        // 응답 상태와 본문 로깅
        int httpStatus = wrappedResponse.getStatus();
        String resContent = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        log.info("Status: {}, Response: {}", httpStatus, resContent);

        // 응답 복사해서 클라이언트로 보냄
        wrappedResponse.copyBodyToResponse();
        }

    }


/*
Spring MVC는 요청 본문을 DTO로 매핑할 때 내부적으로 요청 본문을 읽어야 한다.
필터가 이 본문 (body)을 먼저 읽어버리면,
컨트롤러에서 DTO 매핑 시 데이터가 없기 때문에 DTO 필드가 null로 설정되거나 매핑이 이루어지지 않는 문제가 발생한다.

 */