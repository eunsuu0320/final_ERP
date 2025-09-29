// src/main/java/com/yedam/ac/util/CompanyCodeProvider.java
package com.yedam.ac.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCodeProvider {

    private final JdbcTemplate jt;

    public String currentCompanyCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        // 1) principal.getCompanyCode() 먼저
        try {
            Object v = auth.getPrincipal().getClass().getMethod("getCompanyCode").invoke(auth.getPrincipal());
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v).trim();
        } catch (Exception ignore) {}

        // 2) auth.getName()이 "cc:userId:..."면 cc 바로 반환
        String name = auth.getName();
        if (name != null && name.contains(":")) {
            String[] parts = name.split(":");
            if (parts.length >= 1 && parts[0] != null && !parts[0].isBlank()) {
                return parts[0].trim();
            }
        }

        // 3) 아니면 DB fallback
        String userId = (name == null || name.isBlank()) ? null : name.trim();
        if (userId == null) return null;
        try {
            String cc = jt.query(
                "SELECT company_code FROM system_user " +
                " WHERE user_id = ? AND (usage_status IS NULL OR usage_status='Y') AND ROWNUM = 1",
                ps -> ps.setString(1, userId),
                rs -> rs.next() ? rs.getString(1) : null
            );
            return (cc == null || cc.isBlank()) ? null : cc.trim();
        } catch (Exception e) {
            log.warn("[CCP] DB 조회 실패 userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}
