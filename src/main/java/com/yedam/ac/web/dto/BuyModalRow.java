// src/main/java/com/yedam/ac/web/dto/BuyModalRow.java
package com.yedam.ac.web.dto;

import java.sql.Date;

public record BuyModalRow(
    String buyCode,
    String partnerName,
    Long   amountTotal,
    String productName,
    Date   purchaseDate,
    String taxCode
) {}
