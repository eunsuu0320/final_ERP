package com.yedam.sales2.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.sales2.domain.EsdpPlan;
import com.yedam.sales2.domain.EspPlan;

@Service
public interface EmpService {
	
	// 사원별 영업계획 등록
	EspPlan insertSalePlan(EspPlan espPlan);
	
	// 사원별 계획 + 세부계획 동시 등록
	 List<EsdpPlan> insertDetailPlans(List<EsdpPlan> plans);
	 
	 //사원별 조회
	 List<EsdpPlan> findByEspCode(String espCode);

	 
}
