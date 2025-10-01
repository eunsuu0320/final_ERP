package com.yedam.ac.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.yedam.ac.util.CompanyCodeProvider;
import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCodeTraceInterceptor implements HandlerInterceptor {

    private final CompanyCodeProvider provider;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {

        Object src = req.getAttribute("__cc_src");
        String cc = (String) (req.getSession(false) == null ? null :
                req.getSession(false).getAttribute(CompanyContext.ATTR));

        // 🔧 안전망: 세션에 없으면 Provider로 채워 넣기(로그인 사용자 기준)
        if (cc == null || cc.isBlank()) {
            try {
                String candidate = provider.resolveForCurrentUser(); // ← Provider 메소드명 통일 필수
                if (candidate != null && !candidate.isBlank()) {
                    req.getSession(true).setAttribute(CompanyContext.ATTR, candidate.trim());
                    req.setAttribute("__cc_src", "TRACE_PROVIDER");
                    cc = candidate.trim();
                    src = "TRACE_PROVIDER";
                    log.info("[CC][SAFE] {} {} -> injected cc={}", req.getMethod(), req.getRequestURI(), cc);
                } else {
                    log.info("[CC][SAFE] {} {} -> provider returned null", req.getMethod(), req.getRequestURI());
                }
            } catch (Exception e) {
                log.warn("[CC][SAFE] exception during fallback: {}", e.toString());
            }
        }

        log.info("[CC] {} {} cc={} (src={})", req.getMethod(), req.getRequestURI(), cc, src);
        return true;
    }
}
