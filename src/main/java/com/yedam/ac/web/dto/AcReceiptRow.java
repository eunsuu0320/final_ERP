// src/main/java/com/yedam/ac/web/dto/AcReceiptRow.java
package com.yedam.ac.web.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AcReceiptRow {
    private Long id;              // 프론트용 행 id (임의 부여)
    private String partnerName;   // 거래처명
    private String productName;   // 품목명 (통합조회에 있으면 내려감)
    private Integer qty;          // 수량 (현재 데이터 없으면 0)
    private Long amountSupply;    // 금액(공급가) - 현재 없어서 total로 채움
    private Long amountVat;       // 부가세 - 현재 0
    private Long amountTotal;     // 총 금액
    private LocalDate salesDate;  // 판매일자(=voucherDate)
}
