package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.dto.EstimateRegistrationDTO;

public interface EstimateService {
	List<Estimate> getAllEstimate();
	
	List<Estimate> getFilterEstimate(Estimate searchVo);


	Map<String, Object> getTableDataFromEstimate(List<Estimate> estimates);

	Estimate saveEstimate(Estimate estimate);
	
	Long registerNewEstimate(EstimateRegistrationDTO dto);
	
	boolean updateEstimateStatus(String estimateCode, String status);
	
	Estimate getEstimateByEstimateUniqueCode(Long estimateUniqueCode);

}
