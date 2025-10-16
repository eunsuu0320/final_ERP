// src/main/java/com/yedam/sales3/domain/dto/MetricsSummary.java
package com.yedam.sales3.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class MetricsSummary {
    private long unshippedCount;   // 미출하
    private long accountedCount;   // 회계반영완료
    private long openEstimateCount;// 미체결 견적
}
