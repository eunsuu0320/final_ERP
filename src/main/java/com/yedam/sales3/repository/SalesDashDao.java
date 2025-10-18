// src/main/java/com/yedam/sales3/repository/SalesDashDao.java
package com.yedam.sales3.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class SalesDashDao {

    @PersistenceContext
    private EntityManager em;

    // === KPI ===
    public long countUnshipped(String companyCode) {
        String sql = """
            SELECT COUNT(*) FROM SHIPMENT
             WHERE NVL(STATUS,'-') NOT IN ('출하완료','회계반영완료')
               AND NVL(COMPANY_CODE,'-') = :cc
        """;
        Number n = (Number) em.createNativeQuery(sql)
                .setParameter("cc", companyCode).getSingleResult();
        return n.longValue();
    }

    public long countAccounted(String companyCode) {
        String sql = """
            SELECT COUNT(*) FROM SHIPMENT
             WHERE NVL(STATUS,'-') = '회계반영완료'
               AND NVL(COMPANY_CODE,'-') = :cc
        """;
        Number n = (Number) em.createNativeQuery(sql)
                .setParameter("cc", companyCode).getSingleResult();
        return n.longValue();
    }

    public long countOpenEstimate(String companyCode) {
        String sql = """
            SELECT COUNT(*) FROM ESTIMATE
             WHERE NVL(STATUS,'-') <> '체결'
               AND NVL(COMPANY_CODE,'-') = :cc
        """;
        Number n = (Number) em.createNativeQuery(sql)
                .setParameter("cc", companyCode).getSingleResult();
        return n.longValue();
    }

    // === 연도별 영업이익 합계 ===
    public Map<Integer, Long> sumAmountByYear(String companyCode, int startYear, int endYear) {
        String sql = """
          SELECT TO_NUMBER(TO_CHAR(VOUCHER_DATE,'YYYY')) AS YR,
                 NVL(SUM(AMOUNT_TOTAL),0) AS AMT
            FROM SALES_STATEMENT
           WHERE TO_NUMBER(TO_CHAR(VOUCHER_DATE,'YYYY')) BETWEEN :s AND :e
             AND NVL(COMPANY_CODE,'-') = :cc
           GROUP BY TO_CHAR(VOUCHER_DATE,'YYYY')
           ORDER BY YR
        """;
        Query q = em.createNativeQuery(sql)
                .setParameter("s", startYear)
                .setParameter("e", endYear)
                .setParameter("cc", companyCode);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    // === 분기별 실적(영업이익) 합계 ===
    public Map<Integer, Long> sumAmountByQuarter(String companyCode, int year) {
        String sql = """
          SELECT TO_NUMBER(TO_CHAR(VOUCHER_DATE,'Q')) AS QTR,
                 NVL(SUM(AMOUNT_TOTAL),0) AS AMT
            FROM SALES_STATEMENT
           WHERE TO_NUMBER(TO_CHAR(VOUCHER_DATE,'YYYY')) = :y
             AND NVL(COMPANY_CODE,'-') = :cc
           GROUP BY TO_CHAR(VOUCHER_DATE,'Q')
           ORDER BY QTR
        """;
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("y", year)
                .setParameter("cc", companyCode)
                .getResultList();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    // === 분기별 목표 합계 ===
 // SalesDashDao.java - targetByQuarter()만 교체
    public Map<Integer, Long> targetByQuarter(String companyCode, int year) {
        String sql = """
          SELECT qtr_num, NVL(SUM(target_amt),0) AS TARGET_AMT
            FROM (
              SELECT
                /* QTR을 안전하게 1~4 숫자로 정규화 */
                CASE
                  WHEN REGEXP_LIKE(TRIM(SPD.QTR), '^[1-4]$')
                    THEN TO_NUMBER(TRIM(SPD.QTR))
                  WHEN REGEXP_LIKE(SPD.QTR, 'Q[1-4]', 'i')
                    THEN TO_NUMBER(REGEXP_SUBSTR(SPD.QTR, '[1-4]'))
                  WHEN REGEXP_LIKE(SPD.QTR, '[1-4]분기')
                    THEN TO_NUMBER(REGEXP_SUBSTR(SPD.QTR, '[1-4]'))
                  ELSE NULL
                END AS qtr_num,
                NVL(SPD.PURP_SALES, 0) AS target_amt
              FROM SALES_PLAN SP
              JOIN SALES_PLAN_DETAIL SPD
                ON TO_CHAR(SP.SALES_PLAN_CODE) = SPD.SALES_PLAN_CODE
              WHERE TO_CHAR(SP.PLAN_YEAR,'YYYY') = :y
                AND NVL(SP.COMPANY_CODE,'-') = :cc
            )
           WHERE qtr_num IS NOT NULL
           GROUP BY qtr_num
           ORDER BY qtr_num
        """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("y", String.valueOf(year))
                .setParameter("cc", companyCode)
                .getResultList();

        Map<Integer, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    // === Top5 거래처(매출 기준) ===
    public List<Object[]> top5Partners(String companyCode) {
        String sql = """
          SELECT PARTNER_NAME, SALES, UNCOL
            FROM (
              SELECT PARTNER_NAME,
                     NVL(SUM(DMND_AMT),0)  AS SALES,
                     NVL(SUM(UNRCT_BALN),0) AS UNCOL
                FROM INVOICE
               WHERE NVL(COMPANY_CODE,'-') = :cc
               GROUP BY PARTNER_CODE, PARTNER_NAME
               ORDER BY NVL(SUM(DMND_AMT),0) DESC
            )
           WHERE ROWNUM <= 5
        """;
        @SuppressWarnings("unchecked")
        List<Object[]> list = em.createNativeQuery(sql)
                .setParameter("cc", companyCode)
                .getResultList();
        return list;
    }

    // === Top5 영업사원(요구사항 변경 반영) ===
    public List<Object[]> top5Employees(String companyCode) {
        String sql = """
          SELECT EMP_NAME, SALES
            FROM (
              SELECT NVL(E.NAME, I.MANAGER) AS EMP_NAME,
                     NVL(SUM(I.DMND_AMT),0) AS SALES
                FROM INVOICE I
                LEFT JOIN EMPLOYEE E ON E.EMP_CODE = I.MANAGER
               WHERE NVL(I.COMPANY_CODE,'-') = :cc
               GROUP BY NVL(E.NAME, I.MANAGER)
               ORDER BY NVL(SUM(I.DMND_AMT),0) DESC
            )
           WHERE ROWNUM <= 5
        """;
        @SuppressWarnings("unchecked")
        List<Object[]> list = em.createNativeQuery(sql)
                .setParameter("cc", companyCode)
                .getResultList();
        return list;
    }
}
