package com.yedam.sales2.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales2.domain.DSalesPlan;
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


    // 연도별 영업계획 상세조회
	List<DSalesPlan> getSalesPlanDetail(int year);

	//수정
	public String updateSalesPlanDetails(int planCode, List<DSalesPlan> updatedDetails);

	String insertSalesPlan(List<DSalesPlan> detailList);

	boolean checkSalesPlanExists(int year);
}