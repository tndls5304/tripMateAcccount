package com.tripmate.account.filter;
import com.tripmate.account.common.errorcode.CommonErrorCode;
import com.tripmate.account.common.exception.ServerErrorException;
import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
//@WebFilter(urlPatterns = "/*")
public class MDCFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain){
        final UUID uuid = UUID.randomUUID();
        MDC.put("request_id", uuid.toString()); // 각 요청마다 UUID 생성 후 MDC에 저장
        try{
            System.out.print("****************test~~~~~~~");
            chain.doFilter(req, res);  // 다음 필터로 요청을 전달
        }catch (Exception e){
            log.error("An error occurred in filter processing", e);  // 오류 로그
            throw new ServerErrorException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        finally {
            MDC.clear();// 요청이 끝나면 MDC에서 정보 삭제
        }
    }
}

/*

***
MDC
로그를 찍기 위해서
모든 요청, 응답, 에러, 메서드 시작 전 등등 filter, Interceptor, aop 중
어떤 방식을 통해 구현할지를 선택할 수 있다.
그 중에 필터를 고른 이유!
모든 요청에 대한 로그가 필요하기 떄문. 모든 HTTP 요청과 응답을 다룰 수 있는 방법이라
filter를 사용하여 구현함.
필터는 Spring의 DispatcherServlet보다 먼저 실행되기 때문에,
요청이 애플리케이션의 다른 부분으로 전달되기 전에 UUID를 설정하고 로그를 남길 수 있다.

****
MDC와 스레드 로컬 변수: 필터를 사용하여 각 요청에 대한 UUID를 MDC에 저장하면,
이 UUID는 요청을 처리하는 스레드에만 유효하게 된다.
 다른 스레드에서 이 값을 참조할 수 없으므로, 요청 간의 혼란을 방지할 수 있다.

 */