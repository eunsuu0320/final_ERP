package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.CollectionEntity;

@Repository
public interface CollectionRepository extends JpaRepository<CollectionEntity, String> {

    // 수금 조회
    List<CollectionEntity> findByMoneyCode(String moneyCode);

    // 거래처별 미수금 현황 조회
    @Query(value = """
           SELECT
			    PARTNER_CODE,
			    PARTNER_NAME AS CUSTOMERNAME,
			    NVL(SUM(DMND_AMT), 0) AS TOTALSALES,
			    NVL(SUM(UNRCT_BALN), 0) AS OUTSTANDING,
			    NVL(SUM(DMND_AMT), 0) - NVL(SUM(UNRCT_BALN), 0) AS TOTALCOLLECTED,
			    COUNT(*) AS INVOICE_COUNT
			FROM INVOICE
			WHERE COMPANY_CODE = :companyCode
			  AND IS_CURRENT_VERSION = 'Y'
              AND STATUS = '진행중'
			GROUP BY PARTNER_CODE, PARTNER_NAME
			ORDER BY PARTNER_NAME
        """, nativeQuery = true)
        List<Map<String, Object>> findReceivableSummary(@Param("companyCode") String companyCode);

    // 등록 프로시저
    @Procedure(procedureName = "PROC_COLLECTION_FIFO")
    void callCollectionFifoProc(
        @Param("p_partner_code") String partnerCode,
        @Param("p_payment_amt") Double paymentAmt,
        @Param("p_post_deduction") Double postDeduction,
        @Param("p_payment_methods") String paymentMethods,
        @Param("p_remk") String remk,
        @Param("p_company_code") String companyCode
    );
    
    // 청구서 조회
    @Query("""
        SELECT i
          FROM Invoice i
         WHERE i.companyCode = :companyCode
           AND i.partnerCode = :partnerCode
           AND i.isCurrentVersion = 'Y'
           AND i.status IN ('진행중','수금완료','수금대기')
         ORDER BY i.dmndDate ASC, i.invoiceUniqueCode ASC
    """)
    List<com.yedam.sales1.domain.Invoice> findInvoicesByPartnerJpa(
        @Param("companyCode") String companyCode,
        @Param("partnerCode") String partnerCode
    );

}
