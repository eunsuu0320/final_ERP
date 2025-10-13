package com.yedam.sales2.service;

import java.util.List;
import java.util.Map;

public interface BusinessProfitsService {
	
	// 품목별 영업이익 조회
	List<Map<String, Object>> getSalesProfitList(String year, String quarter, String keyword);

}
