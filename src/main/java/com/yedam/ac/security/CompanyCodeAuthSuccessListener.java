package com.yedam.ac.security;

import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yedam.ac.util.CompanyContext;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCodeAuthSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final JdbcTemplate jt;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest req = attrs.getRequest();
        if (req == null) return;

        String userId = event.getAuthentication().getName();
        try {
            // TODO: 실제 사용자/회사코드 스키마에 맞게 쿼리 수정
            String cc = jt.query(
                "SELECT company_code FROM system_user " +
                " WHERE user_id = ? AND (usage_status IS NULL OR usage_status='Y') AND ROWNUM = 1",
                ps -> ps.setString(1, userId),
                rs -> rs.next() ? rs.getString(1) : null
            );
            if (cc != null && !cc.isBlank()) {
                req.getSession(true).setAttribute(CompanyContext.ATTR, cc.trim());
                log.info("[LOGIN] COMPANY_CODE 세션 주입: user={}, cc={}", userId, cc);
            } else {
                log.warn("[LOGIN] COMPANY_CODE 없음: user={}", userId);
            }
        } catch (Exception e) {
            log.warn("[LOGIN] COMPANY_CODE 조회 실패: user={}, err={}", userId, e.toString());
        }
    }
}
