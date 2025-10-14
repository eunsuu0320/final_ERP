package com.yedam.sales2.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
}
