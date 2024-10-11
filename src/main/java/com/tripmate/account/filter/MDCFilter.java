package com.tripmate.account.filter;
import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        final UUID uuid = UUID.randomUUID();
        MDC.put("request_id", uuid.toString());
        System.out.print("****************test~~~~~~~");
        chain.doFilter(req, res);
        MDC.clear();
    }
}
