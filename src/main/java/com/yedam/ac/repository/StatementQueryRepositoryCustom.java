// src/main/java/com/yedam/ac/repository/StatementQueryRepositoryCustom.java
package com.yedam.ac.repository;

import java.time.LocalDate;
import java.util.List;

import com.yedam.ac.web.dto.UnifiedStatementRow;

public interface StatementQueryRepositoryCustom {
    List<UnifiedStatementRow> searchUnifiedList(
            String companyCode, String type, String keyword, String voucherNo,
            LocalDate fromDate, LocalDate toDate, int start, int end);

    long countUnified(
            String companyCode, String type, String keyword, String voucherNo,
            LocalDate fromDate, LocalDate toDate);
}
