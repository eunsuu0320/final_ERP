// src/main/java/com/yedam/ac/repository/IncomeStatementRepository.java
package com.yedam.ac.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class IncomeStatementRepository {

  @PersistenceContext
  private EntityManager em;

  private static BigDecimal nz(Object o) {
    if (o == null) return BigDecimal.ZERO;
    if (o instanceof BigDecimal b) return b;
    if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
    return BigDecimal.ZERO;
  }

  /** 매출 합계: SALES_STATEMENT.AMOUNT_TOTAL */
  public BigDecimal sumSalesTotal(String cc, LocalDate from, LocalDate to) {
    Object r = em.createNativeQuery("""
        SELECT NVL(SUM(AMOUNT_TOTAL),0)
          FROM SALES_STATEMENT
         WHERE COMPANY_CODE = :cc
           AND VOUCHER_DATE BETWEEN :fromD AND :toD
        """)
        .setParameter("cc", cc)
        .setParameter("fromD", from)
        .setParameter("toD", to)
        .getSingleResult();
    return nz(r);
  }

  /** 매출원가 합계: BUY_STATEMENT.AMOUNT_TOTAL */
  public BigDecimal sumBuyTotal(String cc, LocalDate from, LocalDate to) {
    Object r = em.createNativeQuery("""
        SELECT NVL(SUM(AMOUNT_TOTAL),0)
          FROM BUY_STATEMENT
         WHERE COMPANY_CODE = :cc
           AND VOUCHER_DATE BETWEEN :fromD AND :toD
        """)
        .setParameter("cc", cc)
        .setParameter("fromD", from)
        .setParameter("toD", to)
        .getSingleResult();
    return nz(r);
  }

  /** 급여 합계: SALARY_MASTER.PAY_DATE 기준 + 회사코드는 MASTER에서 필터 */
  public BigDecimal sumSalary(String cc, LocalDate from, LocalDate to) {
    Object r = em.createNativeQuery("""
        SELECT NVL(SUM(d.NET_PAY),0)
          FROM SALARY_DETAIL d
          JOIN SALARY_MASTER m
            ON m.SALARY_ID = d.SALARY_ID
         WHERE m.COMPANY_CODE = :cc
           AND m.PAY_DATE BETWEEN :fromD AND :toD
        """)
        .setParameter("cc", cc)
        .setParameter("fromD", from)
        .setParameter("toD", to)
        .getSingleResult();
    return nz(r);
  }
}
