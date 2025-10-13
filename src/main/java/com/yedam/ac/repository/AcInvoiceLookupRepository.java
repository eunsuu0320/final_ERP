package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.AcInvoice;

public interface AcInvoiceLookupRepository extends JpaRepository<AcInvoice, String> {

    // ✅ ORACLE 호환: 인라인뷰 + ROWNUM <= :limit (ORDER BY 보장)
    @Query(
        value = """
            SELECT *
            FROM (
               SELECT i.*
               FROM INVOICE i
               WHERE i.STATUS = :status
                 AND (:companyCode IS NULL OR i.COMPANY_CODE = :companyCode)
                 AND (
                      :q IS NULL
                   OR LOWER(i.INVOICE_CODE)        LIKE LOWER('%%' || :q || '%%')
                   OR LOWER(i.PARTNER_CODE)        LIKE LOWER('%%' || :q || '%%')
                   OR LOWER(i.INVOICE_UNIQUE_CODE) LIKE LOWER('%%' || :q || '%%')
                 )
               ORDER BY i.CREATE_DATE DESC, i.INVOICE_CODE DESC
            )
            WHERE ROWNUM <= :limit
            """,
        nativeQuery = true
    )
    List<AcInvoice> searchAccountedTop(
            @Param("status") String status,
            @Param("companyCode") String companyCode,
            @Param("q") String q,
            @Param("limit") int limit
    );
}
