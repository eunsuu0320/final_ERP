// src/main/java/com/yedam/ac/web/dto/UnifiedStatementRow.java
package com.yedam.ac.web.dto;

import java.time.LocalDate;

// 필요 필드만 예시. 기존 파일이 record면 동일하게 record로 확장해도 됨.
public record UnifiedStatementRow(
    String voucherNo,
    LocalDate voucherDate,
    String type,
    Long amountTotal,
    String partnerName,
    String remark,

    // ▼ 추가/보강
    String productName,   // sales.product_name
    Integer salesQty,     // sales.sales_qty
    Long amountVat        // sales_statement.amount_vat
) {}
