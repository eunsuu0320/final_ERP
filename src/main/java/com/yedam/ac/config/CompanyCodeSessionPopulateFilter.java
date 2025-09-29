// src/main/java/com/yedam/ac/config/CompanyCodeSessionPopulateFilter.java
package com.yedam.ac.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yedam.ac.util.CompanyCodeProvider;
import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 100) // ★ Security 필터들 이후에 실행되도록 ‘뒤’로 보냄
public class CompanyCodeSessionPopulateFilter extends OncePerRequestFilter {

    private final CompanyCodeProvider codeProvider;

    @Value("${ac.company.default-code:}")   // 개발 기본값(선택)
    private String defaultCompanyCode;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Object cur = (session == null) ? null : session.getAttribute(CompanyContext.ATTR);

        if (cur == null) {
            // 1) SecurityContext(principal) → DB fallback 순서로 회사코드 조회
            String cc = trimOrNull(codeProvider.currentCompanyCode());

            // 2) 헤더/파라미터(개발/테스트 편의)
            if (cc == null) cc = trimOrNull(req.getHeader("X-Company-Code"));
            if (cc == null) cc = trimOrNull(req.getParameter("cc"));

            // 3) yml 기본값(선택)
            if (cc == null && defaultCompanyCode != null && !defaultCompanyCode.isBlank()) {
                cc = defaultCompanyCode.trim();
                log.debug("[COMPANY_CODE] 기본값 사용: {}", cc);
            }

            if (cc != null) {
                req.getSession(true).setAttribute(CompanyContext.ATTR, cc);
                log.info("[COMPANY_CODE] 세션 캐싱 완료: {}", cc);
            } else {
                log.debug("[COMPANY_CODE] 세션/프린시펄/헤더/파라미터/기본값 모두 없음");
            }
        }

        chain.doFilter(req, res);
    }

    private static String trimOrNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
