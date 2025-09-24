package com.yedam.ac.web.dto;

/** 네이티브 쿼리 결과 인터페이스 프로젝션 */
public interface UnifiedStatementRow {
    String getVoucherNo();            // Long -> String
    java.time.LocalDate getVoucherDate();
    String getType();
    java.math.BigDecimal getAmountTotal();
    String getPartnerName();
    String getRemark();
}