package com.yedam.service;

import org.springframework.stereotype.Service;

import com.yedam.repository.SalesPlanRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class salesPlanService {

	private final SalesPlanRepository salesPlanRepository;
	
}
