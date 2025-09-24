package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.SalesStatement;

public interface SalesStatementRepository extends JpaRepository<SalesStatement, String> {
    @Query("select max(s.voucherNo) from SalesStatement s where s.voucherNo like :prefix%")
    String findMaxVoucherNoLike(@Param("prefix") String prefix);
}