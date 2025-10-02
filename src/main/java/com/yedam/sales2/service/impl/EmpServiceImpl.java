package com.yedam.sales2.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales2.domain.EsdpPlan;
import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.repository.EsdpPlanRepository;
import com.yedam.sales2.repository.EspPlanRepository;
import com.yedam.sales2.service.EmpService;

@Service
public class EmpServiceImpl implements EmpService {

	@Autowired
	EspPlanRepository espPlanRepository;
	
	@Autowired
	EsdpPlanRepository esdpPlanRepository;

	// 사원등록 
	@Override
	@Transactional
	public EspPlan insertSalePlan(EspPlan espPlan) {
		return espPlanRepository.save(espPlan);
	}
	
	// 분기별 사원등록/수정
	@Override
    @Transactional
    public List<EsdpPlan> insertDetailPlans(List<EsdpPlan> plans) {
        return esdpPlanRepository.saveAll(plans);
    }

	// 사원별 조회
	@Override
	public List<EsdpPlan> findByEspCode(String espCode) {
		return esdpPlanRepository.findByEspCode(espCode);
	}

}
