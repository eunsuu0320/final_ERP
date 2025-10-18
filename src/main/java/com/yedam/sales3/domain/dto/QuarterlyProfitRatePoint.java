// src/main/java/com/yedam/sales3/domain/dto/QuarterlyProfitRatePoint.java
package com.yedam.sales3.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class QuarterlyProfitRatePoint {
    private int quarter;   // 1~4
    private long actual;   // 분기 실적 합계(AMOUNT_TOTAL)
    private long target;   // 분기 목표 합계(PURP_SALES)
    private double rate;   // actual/target*100
}
