package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.SalesStatement;

public interface SalesStatementRepository extends JpaRepository<SalesStatement, String> {

    /** 회사코드 범위에서 prefix(예: '2509-%')로 최대 전표 조회 */
    @Query("select max(s.voucherNo) from SalesStatement s " +
           "where s.companyCode = :companyCode and s.voucherNo like concat(:prefix, '%')")
    String findMaxVoucherNoLike(@Param("companyCode") String companyCode,
                                @Param("prefix") String prefix);
}
