// src/main/java/com/yedam/sales3/service/impl/SalesDashServiceImpl.java
package com.yedam.sales3.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.sales3.domain.dto.MetricsSummary;
import com.yedam.sales3.domain.dto.QuarterlyProfitRatePoint;
import com.yedam.sales3.domain.dto.TopEmployeeSales;
import com.yedam.sales3.domain.dto.TopPartnerSales;
import com.yedam.sales3.domain.dto.YearlyProfitPoint;
import com.yedam.sales3.repository.SalesDashDao;
import com.yedam.sales3.service.SalesDashService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalesDashServiceImpl implements SalesDashService {

    private final SalesDashDao dao;

    @Override
    public MetricsSummary getMetrics(String companyCode) {
        return new MetricsSummary(
                dao.countUnshipped(companyCode),
                dao.countAccounted(companyCode),
                dao.countOpenEstimate(companyCode)
        );
    }

    @Override
    public List<YearlyProfitPoint> getYearlyProfitLast5(String companyCode) {
        int end = Year.now().getValue();
        int start = end - 4;
        Map<Integer, Long> map = dao.sumAmountByYear(companyCode, start, end);
        List<YearlyProfitPoint> list = new ArrayList<>();
        for (int y = start; y <= end; y++) {
            list.add(new YearlyProfitPoint(y, map.getOrDefault(y, 0L)));
        }
        return list;
    }

    @Override
    public List<QuarterlyProfitRatePoint> getQuarterlyProfitRate(String companyCode, int year) {
        Map<Integer, Long> target = dao.targetByQuarter(companyCode, year);
        Map<Integer, Long> actual = dao.sumAmountByQuarter(companyCode, year);
        List<QuarterlyProfitRatePoint> list = new ArrayList<>();
        for (int q=1; q<=4; q++){
            long a = actual.getOrDefault(q, 0L);
            long t = target.getOrDefault(q, 0L);
            double rate = (t==0) ? 0.0 : Math.round((a*1000.0)/t)/10.0; // 소수1자리
            list.add(new QuarterlyProfitRatePoint(q, a, t, rate));
        }
        return list;
    }

    @Override
    public List<TopPartnerSales> getTop5Partners(String companyCode) {
        List<Object[]> rows = dao.top5Partners(companyCode);
        List<TopPartnerSales> list = new ArrayList<>();
        int rank=1;
        for (Object[] r: rows) {
            list.add(new TopPartnerSales(rank++,
                    (String) r[0],
                    ((Number) r[1]).longValue(),
                    ((Number) r[2]).longValue()));
        }
        return list;
    }

    @Override
    public List<TopEmployeeSales> getTop5Employees(String companyCode) {
        List<Object[]> rows = dao.top5Employees(companyCode);
        List<TopEmployeeSales> list = new ArrayList<>();
        int rank=1;
        for (Object[] r: rows) {
            list.add(new TopEmployeeSales(rank++,
                    (String) r[0],
                    ((Number) r[1]).longValue()));
        }
        return list;
    }
}
