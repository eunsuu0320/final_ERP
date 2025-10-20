// src/main/java/com/yedam/ac/web/dto/AcReceiptRow.java
package com.yedam.ac.web.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcReceiptRow {
    private String id;

    // 거래처 정보
    private String partnerName;
    private String partnerPhone;
    private String businessNo;
    private String partnerCeo;
    private String partnerAddress;

    // 품목/수량/금액
    private String productName;
    private String unit;

    private Integer qty;
    private Long amountSupply;
    private Long amountVat;
    private Long amountTotal;

    // 주문일자
    private Date salesDate;
}
