// src/main/java/com/yedam/sales3/service/SalesDashService.java
package com.yedam.sales3.service;

import java.util.List;

import com.yedam.sales3.domain.dto.MetricsSummary;
import com.yedam.sales3.domain.dto.QuarterlyProfitRatePoint;
import com.yedam.sales3.domain.dto.TopEmployeeSales;
import com.yedam.sales3.domain.dto.TopPartnerSales;
import com.yedam.sales3.domain.dto.YearlyProfitPoint;

public interface SalesDashService {
    MetricsSummary getMetrics(String companyCode);
    List<YearlyProfitPoint> getYearlyProfitLast5(String companyCode);
    List<QuarterlyProfitRatePoint> getQuarterlyProfitRate(String companyCode, int year);
    List<TopPartnerSales> getTop5Partners(String companyCode);
    List<TopEmployeeSales> getTop5Employees(String companyCode);
}
