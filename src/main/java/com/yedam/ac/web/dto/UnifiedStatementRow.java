// src/main/java/com/yedam/ac/web/dto/UnifiedStatementRow.java
package com.yedam.ac.web.dto;

import java.time.LocalDate;

public record UnifiedStatementRow(
        String voucherNo,
        LocalDate voucherDate,
        String type,
        Long amountTotal,
        String partnerName,
        String remark,
        String productName
) {}
