// src/main/java/com/yedam/ac/security/CompanyCodeAuthSuccessListener.java
package com.yedam.ac.security;

import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CompanyCodeAuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final JdbcTemplate jt;

    public CompanyCodeAuthSuccessListener(JdbcTemplate jt) {
        this.jt = jt;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null || attrs.getRequest() == null) {
            log.warn("[CC-LISTENER] RequestAttributes/Request 없음");
            return;
        }
        HttpServletRequest req = attrs.getRequest();

        String name = (event.getAuthentication() == null) ? null : event.getAuthentication().getName();
        if (name == null || name.isBlank()) {
            log.warn("[CC-LISTENER] auth.getName() 비어있음");
            return;
        }

        // 1) "C001:admin01:E001" 처럼 들어오면 앞 파트가 회사코드
        final String cc;
        String userId = null;
        if (name.contains(":")) {
            String[] parts = name.split(":");
            // 방어적으로 길이 체크
            if (parts.length >= 1) cc = safeTrim(parts[0]);
            else cc = null;
            if (parts.length >= 2) userId = safeTrim(parts[1]);
        } else {
            cc = null;
            userId = safeTrim(name);
        }

        // 2) 콜론 형식이면 DB조회 없이 바로 세션 캐싱
        if (cc != null && !cc.isBlank()) {
            HttpSession ses = req.getSession(true);
            ses.setAttribute(CompanyContext.ATTR, cc);
            log.info("[CC-LISTENER] 로그인 성공(복합ID): name={} → COMPANY_CODE={} 세션 캐싱", name, cc);
            return;
        }

        // 3) 형식이 아니면 DB fallback (SYSTEM_USER)
        if (userId == null || userId.isBlank()) {
            log.warn("[CC-LISTENER] userId 파싱 실패 name={}", name);
            return;
        }

        // 새로운 userId를 변수로 전달
        String finalUserId = userId;
        try {
            String dbCc = jt.query(
                "SELECT company_code FROM system_user " +
                " WHERE user_id = ? AND (usage_status IS NULL OR usage_status='Y') AND ROWNUM = 1",
                ps -> ps.setString(1, finalUserId),
                rs -> rs.next() ? rs.getString(1) : null
            );
            if (dbCc != null && !dbCc.isBlank()) {
                req.getSession(true).setAttribute(CompanyContext.ATTR, dbCc.trim());
                log.info("[CC-LISTENER] 로그인 성공(DB): userId={} → COMPANY_CODE={} 세션 캐싱", finalUserId, dbCc);
            } else {
                log.warn("[CC-LISTENER] DB에 company_code 없음 userId={}", finalUserId);
            }
        } catch (Exception e) {
            log.error("[CC-LISTENER] DB 조회 실패 userId={}: {}", finalUserId, e.getMessage());
        }
    }




    private static String safeTrim(String s) { return (s == null) ? null : s.trim(); }
}
