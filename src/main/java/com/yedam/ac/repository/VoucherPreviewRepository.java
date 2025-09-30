// src/main/java/com/yedam/ac/repository/VoucherPreviewRepository.java
package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.SalesStatement; // ✅ JPA가 아는 "관리 엔티티"를 제네릭으로 명시

/**
 * 종류별(yymm-####) max(voucher_no)만 조회하는 read-only 리포지토리.
 * 엔티티 조작이 아니라서 SalesStatement를 "관리 엔티티"로만 붙여둠.
 */
public interface VoucherPreviewRepository extends Repository<SalesStatement, String> {

    // 매출
    @Query(value = """
        select max(voucher_no)
          from sales_statement
         where company_code = :cc
           and voucher_no like :prefix || '%'
        """, nativeQuery = true)
    String findMaxSales(@Param("cc") String companyCode, @Param("prefix") String prefix);

    // 매입
    @Query(value = """
        select max(voucher_no)
          from buy_statement
         where company_code = :cc
           and voucher_no like :prefix || '%'
        """, nativeQuery = true)
    String findMaxBuy(@Param("cc") String companyCode, @Param("prefix") String prefix);

    // 수금
    @Query(value = """
        select max(voucher_no)
          from money_statement
         where company_code = :cc
           and voucher_no like :prefix || '%'
        """, nativeQuery = true)
    String findMaxMoney(@Param("cc") String companyCode, @Param("prefix") String prefix);

    // 지급
    @Query(value = """
        select max(voucher_no)
          from payment_statement
         where company_code = :cc
           and voucher_no like :prefix || '%'
        """, nativeQuery = true)
    String findMaxPayment(@Param("cc") String companyCode, @Param("prefix") String prefix);
}
