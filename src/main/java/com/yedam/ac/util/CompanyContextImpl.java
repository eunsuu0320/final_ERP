// src/main/java/com/yedam/ac/util/CompanyContextImpl.java
package com.yedam.ac.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyContextImpl implements CompanyContext {

    private final CompanyCodeProvider codeProvider;

    @Value("${ac.company.default-code:}")
    private String defaultCompanyCode;

    @Override
    public String getCompanyCode() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) { log.debug("[CCX] RequestAttributes=null"); return null; }
        HttpServletRequest req = attrs.getRequest();
        if (req == null) { log.debug("[CCX] request=null"); return null; }

        // 1) 세션
        HttpSession ses = req.getSession(false);
        if (ses != null) {
            Object v = ses.getAttribute(ATTR);
            log.debug("[CCX] session.{} = {}", ATTR, v);
            if (v != null) return String.valueOf(v);
        } else {
            log.debug("[CCX] session = null");
        }

        // 2) 헤더
        String h = trimOrNull(req.getHeader("X-Company-Code"));
        log.debug("[CCX] header.X-Company-Code = {}", h);
        if (h != null) { cache(req, h, "HEADER"); return h; }

        // 3) 파라미터
        String p = trimOrNull(req.getParameter("cc"));
        log.debug("[CCX] param.cc = {}", p);
        if (p != null) { cache(req, p, "PARAM"); return p; }

        // 4) 현재 로그인 사용자 기준 DB 조회 (Provider)
        String db = trimOrNull(codeProvider.resolveForCurrentUser());
        log.debug("[CCX] provider(DB) = {}", db);
        if (db != null) { cache(req, db, "PROVIDER"); return db; }

        // 5) 기본값 (개발용)
        String def = trimOrNull(defaultCompanyCode);
        log.debug("[CCX] default-code(yml) = {}", def);
        if (def != null) { cache(req, def, "DEFAULT"); return def; }

        log.debug("[CCX] companyCode = <null>");
        return null;
    }

    private static String trimOrNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static void cache(HttpServletRequest req, String cc, String src){
        req.getSession(true).setAttribute(CompanyContext.ATTR, cc);
        // 최종 한 줄 요약
        req.setAttribute("__cc_src", src);
    }
}
