package com.yedam.sales2.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.repository.DSalesPlanRepository;
import com.yedam.sales2.repository.EspPlanRepository;
import com.yedam.sales2.repository.SalesPlanRepository;
import com.yedam.sales2.repository.SalesRepository;
import com.yedam.sales2.service.empService;

@Service
public class empServiceImpl implements empService {


	@Autowired
	EspPlanRepository espPlanRepository;
	
	@Override
	public List<EspPlan> findAll() {
		return espPlanRepository.findAll();
	}

	

}
