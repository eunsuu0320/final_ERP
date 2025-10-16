// src/main/java/com/yedam/sales3/domain/dto/YearlyProfitPoint.java
package com.yedam.sales3.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class YearlyProfitPoint {
    private int year;
    private long amountTotal;
}
