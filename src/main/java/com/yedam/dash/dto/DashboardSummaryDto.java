// src/main/java/com/yedam/dash/dto/DashboardSummaryDto.java
package com.yedam.dash.dto;

import java.util.List;

public record DashboardSummaryDto(
        long salesThisMonth,
        long buyThisMonth,
        long employees,
        long arAmount,
        long salesCount,
        long commuteToday,              // ✅ 추가
        List<String> months,
        List<Long> salesTrend,
        List<Long> buyTrend,
        List<String> deptNames,
        List<Long> deptCounts,
        List<Long> arTrend,
        List<Long> salesCntTrend
) {}
