// src/main/java/com/yedam/dash/service/DashboardService.java
package com.yedam.dash.service;

import com.yedam.dash.dto.DashboardSummaryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @PersistenceContext
    private EntityManager em;

    private static long asLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }

    public DashboardSummaryDto load(String companyCode){
        LocalDate now = LocalDate.now();
        LocalDate startMonth = now.withDayOfMonth(1);
        LocalDate endMonth   = now.withDayOfMonth(now.lengthOfMonth());
        int year = now.getYear();

        long salesThisMonth = sumSales(companyCode, startMonth, endMonth);
        long buyThisMonth   = sumBuy(companyCode, startMonth, endMonth);
        long salesCount     = countSales(companyCode, startMonth, endMonth);
        long employees      = countEmployees(companyCode);
        long arAmount       = sumAr(companyCode);
        long commuteToday   = countCommuteToday(companyCode); // ✅ 추가

        Map<Integer,Long> salesTrendMap     = monthlySumSales(companyCode, year);
        Map<Integer,Long> buyTrendMap       = monthlySumBuy(companyCode, year);
        Map<Integer,Long> arTrendMap        = monthlySumAr(companyCode, year);
        Map<Integer,Long> salesCntTrendMap  = monthlyCntSales(companyCode, year);

        List<String> months = new ArrayList<>(12);
        List<Long> salesTrend = new ArrayList<>(12);
        List<Long> buyTrend   = new ArrayList<>(12);
        List<Long> arTrend    = new ArrayList<>(12);
        List<Long> salesCntTrend = new ArrayList<>(12);
        for(int m=1; m<=12; m++){
            months.add(String.format("%02d월", m));
            salesTrend.add(salesTrendMap.getOrDefault(m,0L));
            buyTrend.add(buyTrendMap.getOrDefault(m,0L));
            arTrend.add(arTrendMap.getOrDefault(m,0L));
            salesCntTrend.add(salesCntTrendMap.getOrDefault(m,0L));
        }

        // 부서별 인원 매핑
        Map<String, Long> dept = deptCounts(companyCode);
        List<String> deptNames = List.of("인사","회계","영업");
        List<Long> deptCounts = List.of(
                dept.getOrDefault("111",0L),
                dept.getOrDefault("222",0L),
                dept.getOrDefault("333",0L)
        );

        return new DashboardSummaryDto(
                salesThisMonth, buyThisMonth, employees, arAmount, salesCount, commuteToday,
                months, salesTrend, buyTrend, deptNames, deptCounts, arTrend, salesCntTrend
        );
    }

    /* ====== SQL helpers (Oracle) ====== */

    private long sumSales(String company, LocalDate from, LocalDate to){
        Query q = em.createNativeQuery("""
            SELECT NVL(SUM(AMOUNT_TOTAL),0)
              FROM SALES_STATEMENT
             WHERE COMPANY_CODE = :c
               AND VOUCHER_DATE BETWEEN :f AND :t
        """);
        q.setParameter("c", company);
        q.setParameter("f", java.sql.Date.valueOf(from));
        q.setParameter("t", java.sql.Date.valueOf(to));
        return asLong(q.getSingleResult());
    }

    private long countSales(String company, LocalDate from, LocalDate to){
        Query q = em.createNativeQuery("""
            SELECT COUNT(1)
              FROM SALES_STATEMENT
             WHERE COMPANY_CODE = :c
               AND VOUCHER_DATE BETWEEN :f AND :t
        """);
        q.setParameter("c", company);
        q.setParameter("f", java.sql.Date.valueOf(from));
        q.setParameter("t", java.sql.Date.valueOf(to));
        return asLong(q.getSingleResult());
    }

    private long sumBuy(String company, LocalDate from, LocalDate to){
        Query q = em.createNativeQuery("""
            SELECT NVL(SUM(AMOUNT_TOTAL),0)
              FROM BUY_STATEMENT
             WHERE COMPANY_CODE = :c
               AND VOUCHER_DATE BETWEEN :f AND :t
        """);
        q.setParameter("c", company);
        q.setParameter("f", java.sql.Date.valueOf(from));
        q.setParameter("t", java.sql.Date.valueOf(to));
        return asLong(q.getSingleResult());
    }

    private long sumAr(String company){
        Query q = em.createNativeQuery("""
            SELECT NVL(SUM(UNRCT_BALN),0)
              FROM INVOICE
             WHERE COMPANY_CODE = :c
        """);
        q.setParameter("c", company);
        return asLong(q.getSingleResult());
    }

    private long countEmployees(String company){
        Query q = em.createNativeQuery("""
            SELECT COUNT(1) FROM EMPLOYEE WHERE COMPANY_CODE = :c
        """);
        q.setParameter("c", company);
        return asLong(q.getSingleResult());
    }

    /** ✅ 금일 출근 인원 (중복 출근 등록 방지 위해 DISTINCT EMP_CODE) */
    private long countCommuteToday(String company){
        Query q = em.createNativeQuery("""
            SELECT COUNT(DISTINCT EMP_CODE)
              FROM COMMUTE_LIST
             WHERE COMPANY_CODE = :c
               AND TRUNC(ON_TIME) = TRUNC(SYSDATE)
        """);
        q.setParameter("c", company);
        return asLong(q.getSingleResult());
    }

    private Map<Integer,Long> monthlySumSales(String company, int year){
        Query q = em.createNativeQuery("""
            SELECT TO_NUMBER(TO_CHAR(TRUNC(VOUCHER_DATE,'MM'),'MM')) AS MM,
                   NVL(SUM(AMOUNT_TOTAL),0) AS AMT
              FROM SALES_STATEMENT
             WHERE COMPANY_CODE = :c
               AND EXTRACT(YEAR FROM VOUCHER_DATE) = :y
             GROUP BY TRUNC(VOUCHER_DATE,'MM')
             ORDER BY TRUNC(VOUCHER_DATE,'MM')
        """);
        q.setParameter("c", company);
        q.setParameter("y", year);
        return toMonthMap(q.getResultList());
    }

    private Map<Integer,Long> monthlySumBuy(String company, int year){
        Query q = em.createNativeQuery("""
            SELECT TO_NUMBER(TO_CHAR(TRUNC(VOUCHER_DATE,'MM'),'MM')) AS MM,
                   NVL(SUM(AMOUNT_TOTAL),0) AS AMT
              FROM BUY_STATEMENT
             WHERE COMPANY_CODE = :c
               AND EXTRACT(YEAR FROM VOUCHER_DATE) = :y
             GROUP BY TRUNC(VOUCHER_DATE,'MM')
             ORDER BY TRUNC(VOUCHER_DATE,'MM')
        """);
        q.setParameter("c", company);
        q.setParameter("y", year);
        return toMonthMap(q.getResultList());
    }

    private Map<Integer,Long> monthlySumAr(String company, int year){
        Query q = em.createNativeQuery("""
            SELECT TO_NUMBER(TO_CHAR(TRUNC(CREATE_DATE,'MM'),'MM')) AS MM,
                   NVL(SUM(UNRCT_BALN),0) AS AMT
              FROM INVOICE
             WHERE COMPANY_CODE = :c
               AND EXTRACT(YEAR FROM CREATE_DATE) = :y
             GROUP BY TRUNC(CREATE_DATE,'MM')
             ORDER BY TRUNC(CREATE_DATE,'MM')
        """);
        q.setParameter("c", company);
        q.setParameter("y", year);
        return toMonthMap(q.getResultList());
    }

    private Map<Integer,Long> monthlyCntSales(String company, int year){
        Query q = em.createNativeQuery("""
            SELECT TO_NUMBER(TO_CHAR(TRUNC(VOUCHER_DATE,'MM'),'MM')) AS MM,
                   COUNT(1) AS CNT
              FROM SALES_STATEMENT
             WHERE COMPANY_CODE = :c
               AND EXTRACT(YEAR FROM VOUCHER_DATE) = :y
             GROUP BY TRUNC(VOUCHER_DATE,'MM')
             ORDER BY TRUNC(VOUCHER_DATE,'MM')
        """);
        q.setParameter("c", company);
        q.setParameter("y", year);
        return toMonthMap(q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private Map<Integer,Long> toMonthMap(List<?> rows){
        Map<Integer,Long> m = new HashMap<>();
        for(Object row : rows){
            Object[] r = (Object[]) row;
            int mm;
            Object monthCell = r[0];
            if(monthCell instanceof Number n){
                mm = n.intValue();
            }else{
                mm = Integer.parseInt(monthCell.toString());
            }
            m.put(mm, asLong(r[1]));
        }
        return m;
    }

    /** 부서별 인원 매핑 */
    private Map<String, Long> deptCounts(String company){
        Query q = em.createNativeQuery("""
            SELECT d.code_id, NVL(x.cnt,0) AS cnt
              FROM (SELECT '111' code_id FROM dual
                    UNION ALL SELECT '222' FROM dual
                    UNION ALL SELECT '333' FROM dual) d
              LEFT JOIN (
                   SELECT CASE
                              WHEN e.DEPT IN ('111','인사') THEN '111'
                              WHEN e.DEPT IN ('222','회계') THEN '222'
                              WHEN e.DEPT IN ('333','영업') THEN '333'
                          END AS code_id,
                          COUNT(1) AS cnt
                     FROM EMPLOYEE e
                    WHERE e.COMPANY_CODE = :c
                      AND (e.DEPT IN ('111','222','333','인사','회계','영업'))
                    GROUP BY CASE
                              WHEN e.DEPT IN ('111','인사') THEN '111'
                              WHEN e.DEPT IN ('222','회계') THEN '222'
                              WHEN e.DEPT IN ('333','영업') THEN '333'
                             END
              ) x ON x.code_id = d.code_id
             ORDER BY d.code_id
        """);
        q.setParameter("c", company);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        Map<String, Long> res = new HashMap<>();
        for(Object[] r : rows){
            res.put(Objects.toString(r[0], ""), asLong(r[1]));
        }
        return res;
    }
}
