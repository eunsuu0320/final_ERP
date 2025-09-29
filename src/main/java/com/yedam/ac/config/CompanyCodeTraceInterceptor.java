// src/main/java/com/yedam/ac/config/CompanyCodeTraceInterceptor.java
package com.yedam.ac.config;

import com.yedam.ac.util.CompanyContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCodeTraceInterceptor implements HandlerInterceptor {
    private final CompanyContext ctx;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String cc = ctx.getCompanyCode(); // 여기서 위 로깅이 전부 수행됨
        String src = (String) req.getAttribute("__cc_src");
        log.info("[CC] {} {} cc={} (src={})", req.getMethod(), req.getRequestURI(), cc, src);
        return true;
    }
}
