package com.yedam.sales2.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;

public interface salesService {
	
	List<Sales> findAll(); // 조회
	
	// 연도별 통계 데이터를 조회하는 새로운 메서드 추가
	List<Map<String, Object>> findSalesStatsByYear(); 
	
	// 모달조회
	List<Map<String, Object>> findLastYearSalesData();
	
	// 등록
	SalesPlan saveSalesPlan(SalesPlan salesplan);
	
	List<SalesPlan> getPlanByYear(int year);

}
