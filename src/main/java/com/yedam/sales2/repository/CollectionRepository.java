package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    	        p.PARTNER_NAME AS  "CUSTOMERNAME",
    	        NVL(SUM(i.DMND_AMT), 0) AS "TOTALSALES",
    	        NVL(SUM(c.RECPT), 0) AS "TOTALCOLLECTED",
    	        NVL(SUM(i.UNRCT_BALN), 0) AS "OUTSTANDING",  -- ✅ 잔액 직접 반영
    	        COUNT(DISTINCT CASE 
    	            WHEN NVL(i.UNRCT_BALN, 0) > 0 THEN i.INVOICE_CODE
    	        END) AS "ARREARSCOUNT"
    	    FROM PARTNER p
    	    LEFT JOIN INVOICE i 
    	           ON p.PARTNER_CODE = i.PARTNER_CODE
    	          AND i.IS_CURRENT_VERSION = 'Y'
    	          AND i.COMPANY_CODE = :companyCode
    	    LEFT JOIN COLLECTION c 
    	           ON p.PARTNER_CODE = c.PARTNER_CODE
    	          AND c.COMPANY_CODE = :companyCode
    	    WHERE p.COMPANY_CODE = :companyCode
    	    GROUP BY p.PARTNER_NAME
    	    ORDER BY p.PARTNER_NAME
    	    """, nativeQuery = true)
    	List<Map<String, Object>> findReceivableSummary(@Param("companyCode") String companyCode);

}
