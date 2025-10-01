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

    /**
     * 현재 사용자로부터 COMPANY_CODE 도출:
     * 1) 사용자명 패턴 파싱: "회사코드:사용자ID:사번" → 회사코드 즉시 반환
     * 2) (실패 시) DB에서 조회 (테이블/컬럼에 맞게 수정)
     */
    public String resolveForCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.info("[CCP] SecurityContext Authentication=null (아직 비로그인 또는 permitAll)");
                return null;
            }
            String rawUser = auth.getName(); // 예: "C001:admin01:E001"
            log.info("[CCP] authenticated userId={}", rawUser);

            // 1) 포맷 파싱 우선
            String parsed = parseCompanyCodeFromPrincipal(rawUser);
            if (parsed != null) {
                log.info("[CCP] principal parsed companyCode={}", parsed);
                return parsed;
            }

            // 2) 파싱 실패 시 DB 조회
            String byDb = resolveForUserId(rawUser);
            log.info("[CCP] DB fallback result userId={} -> companyCode={}", rawUser, byDb);
            return byDb;
        } catch (Exception e) {
            log.warn("[CCP] 현재 사용자 회사코드 조회 실패: {}", e.toString());
            return null;
        }
    }

    /**
     * 사용자명에 "회사코드:사용자ID:사번" 형식이 들어오면 회사코드를 반환.
     * 예: "C001:admin01:E001" -> "C001"
     * 그 외엔 null.
     */
    private String parseCompanyCodeFromPrincipal(String principal) {
        if (principal == null) return null;
        int i = principal.indexOf(':');
        if (i <= 0) return null;
        // 첫 토큰을 회사코드로 본다
        String first = principal.substring(0, i).trim();
        return first.isEmpty() ? null : first;
    }

    /**
     * (보조) 특정 사용자ID로 DB에서 회사코드 조회.
     * TODO: 실제 스키마에 맞게 SQL을 수정하세요.
     * 만약 DB의 user_id가 principal의 "중간 토큰(예: admin01)"이라면,
     * parseUserIdForDb(...)로 변환해서 쓰세요.
     */
    public String resolveForUserId(String userIdFromAuth) {
        if (userIdFromAuth == null || userIdFromAuth.isBlank()) return null;
        String dbUserId = parseUserIdForDb(userIdFromAuth); // "C001:admin01:E001" -> "admin01"
        try {
            String cc = jt.query(
                "SELECT company_code FROM system_user " +
                " WHERE user_id = ? AND (usage_status IS NULL OR usage_status='Y') AND ROWNUM = 1",
                ps -> ps.setString(1, dbUserId),
                rs -> rs.next() ? rs.getString(1) : null
            );
            return (cc == null || cc.isBlank()) ? null : cc.trim();
        } catch (Exception e) {
            log.warn("[CCP] DB 조회 실패 userId(db)={}: {}", dbUserId, e.getMessage());
            return null;
        }
    }

    /**
     * principal이 "C001:admin01:E001" 형식일 때 DB상의 user_id가 "admin01"이라면
     * 가운데 토큰을 반환하도록 분리.
     * 형식이 아니면 원본을 그대로 반환.
     */
    private String parseUserIdForDb(String principal) {
        String[] parts = principal.split(":");
        if (parts.length >= 2) {
            // 0: 회사코드, 1: 사용자ID, 2: 사번(옵션)
            return parts[1].trim();
        }
        return principal;
    }
}
