// src/main/java/com/yedam/ac/config/CompanyContextFilter.java
package com.yedam.ac.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(10)
public class CompanyContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // 이미 세션에 있으면 그대로
        Object cur = req.getSession(true).getAttribute(CompanyContext.ATTR);

        // 없으면 헤더/파라미터에서 주입 (개발/테스트 편의)
        if (cur == null) {
            String cc = req.getHeader("X-Company-Code");
            if (cc == null || cc.isBlank()) cc = req.getParameter("cc");
            if (cc != null && !cc.isBlank()) {
                req.getSession(true).setAttribute(CompanyContext.ATTR, cc.trim());
            }
        }

        chain.doFilter(req, res);
    }
}
