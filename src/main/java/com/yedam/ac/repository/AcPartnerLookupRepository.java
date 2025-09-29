// src/main/java/com/yedam/ac/repository/AcPartnerLookupRepository.java  (파일명/클래스 정리)
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yedam.ac.web.dto.PartnerLookupDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AcPartnerLookupRepository {

    private final EntityManager em;

    public List<PartnerLookupDto> top200(String cc) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery("""
                SELECT * FROM (
                    SELECT
                        p.PARTNER_CODE,
                        p.PARTNER_NAME,
                        NVL(p.PARTNER_PHONE, '') AS TEL,
                        NVL(p.MANAGER,      '')  AS PIC_NAME
                    FROM PARTNER p
                    WHERE p.COMPANY_CODE = :cc                  -- ★ 회사코드
                    ORDER BY p.PARTNER_NAME
                ) WHERE ROWNUM <= 200
            """).setParameter("cc", cc).getResultList();

            return rows.stream()
                .map(r -> new PartnerLookupDto(nvl(r[0]), nvl(r[1]), nvl(r[2]), nvl(r[3])))
                .toList();
        } catch (PersistenceException e) {
            log.error("top200() query failed", e);
            throw e;
        }
    }

    public List<PartnerLookupDto> search(String cc, String q) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery("""
                SELECT * FROM (
                    SELECT
                        p.PARTNER_CODE,
                        p.PARTNER_NAME,
                        NVL(p.PARTNER_PHONE, '') AS TEL,
                        NVL(p.MANAGER,      '')  AS PIC_NAME
                    FROM PARTNER p
                    WHERE p.COMPANY_CODE = :cc                  -- ★ 회사코드
                      AND (:q IS NOT NULL AND :q <> '')
                      AND (
                           p.PARTNER_NAME  LIKE '%' || :q || '%'
                        OR p.PARTNER_CODE  LIKE '%' || :q || '%'
                        OR p.PARTNER_PHONE LIKE '%' || :q || '%'
                        OR p.MANAGER       LIKE '%' || :q || '%'
                      )
                    ORDER BY p.PARTNER_NAME
                ) WHERE ROWNUM <= 200
            """).setParameter("cc", cc)
              .setParameter("q", q)
              .getResultList();

            return rows.stream()
                .map(r -> new PartnerLookupDto(nvl(r[0]), nvl(r[1]), nvl(r[2]), nvl(r[3])))
                .toList();
        } catch (PersistenceException e) {
            log.error("search('{}') query failed", q, e);
            throw e;
        }
    }

    private static String nvl(Object o){ return o == null ? "" : String.valueOf(o); }
}
