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
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class CompanyCodeSessionPopulateFilter extends OncePerRequestFilter {

    private final CompanyCodeProvider provider;

    @Value("${ac.company.default-code:}")
    private String defaultCompanyCode; // 운영에선 비추천(가능하면 비워두기)

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        Object v = (ses == null) ? null : ses.getAttribute(CompanyContext.ATTR);

        if (v == null) {
            // 1) 헤더
            String cc = trimOrNull(req.getHeader("X-Company-Code"));
            if (cc != null) {
                cache(req, cc, "HEADER");
            } else {
                // 2) 파라미터
                cc = trimOrNull(req.getParameter("cc"));
                if (cc != null) {
                    cache(req, cc, "PARAM");
                } else {
                    // 3) 로그인 사용자 → DB 조회
                    cc = trimOrNull(provider.resolveForCurrentUser());
                    if (cc != null) {
                        cache(req, cc, "USER_DB");
                    } else {
                        // 4) 기본값 (개발용)
                        String def = trimOrNull(defaultCompanyCode);
                        if (def != null) {
                            cache(req, def, "DEFAULT");
                        } else {
                            log.debug("[COMPANY_CODE] 세션/프린시펄/헤더/파라미터/기본값 모두 없음");
                        }
                    }
                }
            }
        }
        chain.doFilter(req, res);
    }

    private static String trimOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static void cache(HttpServletRequest req, String cc, String src) {
        req.getSession(true).setAttribute(CompanyContext.ATTR, cc);
        req.setAttribute("__cc_src", src); // 디버깅용
        // INFO 로그는 너무 시끄러우면 DEBUG로 낮춰도 됨
        log.info("[COMPANY_CODE] 세션 캐싱 완료: {} (src={})", cc, src);
    }
}
