package com.yedam.sales2.service;

import org.springframework.stereotype.Service;

import com.yedam.sales2.domain.EspPlan;

@Service
public interface EmpService {
	
	// 사원별 영업계획 등록
	EspPlan insertSalePlan(EspPlan espPlan);
}
