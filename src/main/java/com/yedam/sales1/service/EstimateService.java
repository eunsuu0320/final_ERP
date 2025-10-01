package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.dto.EstimateRegistrationDTO;

public interface EstimateService {
	List<Estimate> getAllEstimate();

	Map<String, Object> getTableDataFromEstimate(List<Estimate> estimate);

	Estimate saveEstimate(Estimate estimate);
	
	Long registerNewEstimate(EstimateRegistrationDTO dto);
}