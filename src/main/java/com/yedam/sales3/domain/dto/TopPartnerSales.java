// src/main/java/com/yedam/sales3/domain/dto/TopPartnerSales.java
package com.yedam.sales3.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class TopPartnerSales {
    private int rank;
    private String partnerName;
    private long sales;        // DMND_AMT
    private long uncollected;  // UNRCT_BALN
}
