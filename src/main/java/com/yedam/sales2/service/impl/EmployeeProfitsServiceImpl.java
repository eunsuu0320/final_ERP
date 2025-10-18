package com.yedam.sales2.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales2.repository.EmployeeProfitsRepository;
import com.yedam.sales2.service.EmployeeProfitsService;

@Service
public class EmployeeProfitsServiceImpl implements EmployeeProfitsService{

	  @Autowired
	  private EmployeeProfitsRepository employeeProfitsRepository;

	  @Override
	    public List<Map<String, Object>> getEmployeeSummary(
	        String companyCode, Integer year, Integer quarter, String keyword) {

	        List<Object[]> rows =
	            employeeProfitsRepository.findEmployeeSalesSummaryRaw(companyCode, year, quarter, keyword);

	        List<Map<String, Object>> list = new ArrayList<>();

	        for (Object[] r : rows) {
	            Map<String, Object> m = new HashMap<>();
	            m.put("empCode",   r[0]);
	            m.put("name",      r[1]);
	            m.put("salesQty",  r[2]);
	            m.put("salesAmount", r[3]);
	            m.put("tax",       r[4]);
	            m.put("totalAmount", r[5]);
	            list.add(m);
	        }

	        return list;
	    }
	  
	  // 사원관련 모달
	  @Override
	    public List<Map<String, Object>> getEmpPartners(
	        String companyCode, String empCode, Integer year, Integer quarter, String keyword) {

	        // 기간 계산 (fromDate >=, toDate <)
	        java.time.LocalDate from;
	        java.time.LocalDate to;

	        if (year == null) {
	            int y = java.time.LocalDate.now().getYear();
	            from = java.time.LocalDate.of(y, 1, 1);
	            to   = from.plusYears(1);
	        } else if (quarter == null) {
	            from = java.time.LocalDate.of(year, 1, 1);
	            to   = from.plusYears(1);
	        } else {
	            int startMonth = (quarter - 1) * 3 + 1; // 1,4,7,10
	            from = java.time.LocalDate.of(year, startMonth, 1);
	            to   = from.plusMonths(3);
	        }

	        java.util.Date fromDate = java.util.Date.from(from.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
	        java.util.Date toDate   = java.util.Date.from(to  .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
	        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

	        var rows = employeeProfitsRepository.findEmpPartnersSums_ByPartnerBase(
	            companyCode, empCode, fromDate, toDate, kw
	        );

	        List<Map<String, Object>> list = new ArrayList<>();
	        for (var r : rows) {
	            Map<String, Object> m = new HashMap<>();
	            m.put("partnerCode", r.getPartnerCode());
	            m.put("partnerName", r.getPartnerName());
	            // BigDecimal → long 변환(프론트 money formatter 친화적)
	            m.put("salesAmount", r.getSalesAmount() == null ? 0L : r.getSalesAmount().longValue());
	            m.put("collectAmt",  r.getCollectAmt()  == null ? 0L : r.getCollectAmt().longValue());
	            list.add(m);
	        }
	        return list;
	    }
}