package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.BuyStatement;

public interface BuyStatementRepository extends JpaRepository<BuyStatement, String> {
    @Query("select max(b.voucherNo) from BuyStatement b where b.voucherNo like :prefix%")
    String findMaxVoucherNoLike(@Param("prefix") String prefix);
}