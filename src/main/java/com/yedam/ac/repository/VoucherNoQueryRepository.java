// src/main/java/com/yedam/ac/repository/VoucherNoQueryRepository.java
package com.yedam.ac.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class VoucherNoQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /** 매출: prefix(yyMM-)로 시작하는 가장 큰 전표번호 (예: 2509-0012) */
    public String maxSalesVoucher(String prefix) {
        Object r = em.createNativeQuery("""
            SELECT MAX(voucher_no)
              FROM SALES_STATEMENT
             WHERE voucher_no LIKE :prefix || '%'
        """).setParameter("prefix", prefix)
          .getSingleResult();
        return r == null ? null : String.valueOf(r);
    }

    /** 매입: prefix(yyMM-)로 시작하는 가장 큰 전표번호 */
    public String maxBuyVoucher(String prefix) {
        Object r = em.createNativeQuery("""
            SELECT MAX(voucher_no)
              FROM BUY_STATEMENT
             WHERE voucher_no LIKE :prefix || '%'
        """).setParameter("prefix", prefix)
          .getSingleResult();
        return r == null ? null : String.valueOf(r);
    }
    
    public String maxMoneyVoucher(String prefix) {
        Object r = em.createNativeQuery("""
            SELECT MAX(voucher_no)
              FROM MONEY_STATEMENT
             WHERE voucher_no LIKE :prefix || '%'
        """).setParameter("prefix", prefix).getSingleResult();
        return r == null ? null : String.valueOf(r);
    }

    public String maxPaymentVoucher(String prefix) {
        Object r = em.createNativeQuery("""
            SELECT MAX(voucher_no)
              FROM PAYMENT_STATEMENT
             WHERE voucher_no LIKE :prefix || '%'
        """).setParameter("prefix", prefix).getSingleResult();
        return r == null ? null : String.valueOf(r);
    }
}
