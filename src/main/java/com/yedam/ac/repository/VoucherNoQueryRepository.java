// src/main/java/com/yedam/ac/repository/VoucherNoQueryRepository.java
package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.SalesStatement; // ✅ 관리되는 엔티티를 지정(실제 사용 X)

public interface VoucherNoQueryRepository extends Repository<SalesStatement, String> {

    /**
     * 회사코드 + prefix("2509-") 기준 가장 큰 전표번호(예: "2509-0053") 반환. 없으면 null
     */
    @Query(value =
        "select max(voucher_no) from ( " +
        "  select voucher_no from sales_statement   where company_code = :cc and voucher_no like :prefix||'%' " +
        "  union all " +
        "  select voucher_no from buy_statement     where company_code = :cc and voucher_no like :prefix||'%' " +
        "  union all " +
        "  select voucher_no from money_statement   where company_code = :cc and voucher_no like :prefix||'%' " +
        "  union all " +
        "  select voucher_no from payment_statement where company_code = :cc and voucher_no like :prefix||'%' " +
        ")",
        nativeQuery = true)
    String findMaxSequence(@Param("cc") String companyCode,
                           @Param("prefix") String prefix);
}
