package com.yedam.sales1.repository;

import java.util.List;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>{

    // ===== 기존 메서드 =====
    List<Loan> findAll();

    @Query("SELECT MAX(p.loanCode) FROM Loan p")
    String findMaxLoanCode();

    Loan findByLoanCode(String loanCode);

    /**
     * 네이티브 인터페이스 프로젝션
     * - 컬럼 alias(camelCase)와 getter명이 매칭되어야 함
     */
    public interface LoanOverdueView {
        String getPartnerCode();
        Date getLoanStartDate();
        Date getLoanEndDate();
        Number getOverdueDays();
        String getPartnerName();
    }

    @Query(value = """
            SELECT partnerCode, partnerName, loanStartDate, loanEndDate, overdueDays
              FROM (
                SELECT
                  l.partner_code    AS partnerCode,
                  p.partner_name    AS partnerName,
                  l.loan_start_date AS loanStartDate,
                  l.loan_end_date   AS loanEndDate,
                  (TRUNC(SYSDATE) - TRUNC(l.loan_end_date)) AS overdueDays
                FROM LOAN l
                LEFT JOIN PARTNER p
                  ON p.company_code = l.company_code
                 AND p.partner_code = l.partner_code
                WHERE l.company_code = :companyCode
                  AND l.is_current_version = 'Y'
                  AND l.loan_end_date IS NOT NULL
                  AND TRUNC(l.loan_end_date) < TRUNC(SYSDATE)
                ORDER BY overdueDays DESC, p.partner_name
              )
             WHERE ROWNUM <= :limit
            """, nativeQuery = true)
        List<LoanOverdueWithNameView> findOverdueTopNWithName(
                @Param("companyCode") String companyCode,
                @Param("limit") int limit
        );

    interface LoanOverdueWithNameView extends LoanOverdueView {
        String getPartnerName();
    }
}
