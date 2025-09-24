// com.yedam.ac.repository/PartnerLookupRepository.java
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
public class PartnerLookupRepository {

    private final EntityManager em;

    /** 상위 200건 (이름순) */
    public List<PartnerLookupDto> top200() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery("""
                SELECT *
                  FROM (
                        SELECT p.PARTNER_CODE,
                               p.PARTNER_NAME,
                               COALESCE(p.PARTNER_PHONE, p.TEL)        AS TEL,
                               COALESCE(p.MANAGER,      p.PIC_NAME)    AS PIC_NAME
                          FROM PARTNER p
                         ORDER BY p.PARTNER_NAME
                       )
                 WHERE ROWNUM <= 200
            """).getResultList();

            return rows.stream()
                       .map(r -> new PartnerLookupDto(
                           nvl(r[0]), nvl(r[1]), nvl(r[2]), nvl(r[3])
                       ))
                       .toList();
        } catch (PersistenceException e) {
            log.error("top200() query failed", e);
            throw e;
        }
    }

    /** 키워드 검색 (최대 200건) */
    public List<PartnerLookupDto> search(String q) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery("""
                SELECT *
                  FROM (
                        SELECT p.PARTNER_CODE,
                               p.PARTNER_NAME,
                               COALESCE(p.PARTNER_PHONE, p.TEL)        AS TEL,
                               COALESCE(p.MANAGER,      p.PIC_NAME)    AS PIC_NAME
                          FROM PARTNER p
                         WHERE (:q IS NOT NULL AND :q <> '')
                           AND (
                                p.PARTNER_NAME  LIKE '%' || :q || '%'
                             OR p.PARTNER_CODE  LIKE '%' || :q || '%'
                             OR COALESCE(p.PARTNER_PHONE, p.TEL) LIKE '%' || :q || '%'
                             OR COALESCE(p.MANAGER, p.PIC_NAME) LIKE '%' || :q || '%'
                           )
                         ORDER BY p.PARTNER_NAME
                       )
                 WHERE ROWNUM <= 200
            """).setParameter("q", q).getResultList();

            return rows.stream()
                       .map(r -> new PartnerLookupDto(
                           nvl(r[0]), nvl(r[1]), nvl(r[2]), nvl(r[3]
                       )))
                       .toList();
        } catch (PersistenceException e) {
            log.error("search('{}') query failed", q, e);
            throw e;
        }
    }

    private static String nvl(Object o){ return o == null ? "" : String.valueOf(o); }
}
