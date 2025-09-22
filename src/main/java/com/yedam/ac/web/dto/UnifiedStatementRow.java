package com.yedam.ac.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 네이티브 쿼리 결과 인터페이스 프로젝션 */
public interface UnifiedStatementRow {
    Long getVoucherNo();
    LocalDate getVoucherDate();
    String getType();                 // SALES / BUY / MONEY / PAYMENT
    BigDecimal getAmountTotal();
    String getPartnerName();
    String getRemark();
}
