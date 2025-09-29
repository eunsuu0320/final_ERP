// src/main/java/com/yedam/ac/config/DevCompanyCodeFilter.java
package com.yedam.ac.config;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Profile({"local","dev"})
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // ★ 가장 먼저
public class DevCompanyCodeFilter implements Filter {
    private static final String DEFAULT_CC = "C001";
    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpSession s = r.getSession();
        if (s.getAttribute(CompanyContext.ATTR) == null) {
            s.setAttribute(CompanyContext.ATTR, DEFAULT_CC);
        }
        chain.doFilter(req, res);
    }
}
