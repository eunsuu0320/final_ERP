package com.yedam.ac.repository;

public interface CommonQueryRepository {
    String nextVoucherNo(String voucherType, java.time.LocalDate voucherDate); // 예: nextVoucherNo("SALES", 2025-09-24) -> "2509-0001"
}