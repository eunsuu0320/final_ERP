package com.yedam.sales1.service;

import java.util.List;

import com.yedam.sales1.domain.EstimateDetail;

public interface EstimateDetailService {
	List<EstimateDetail> getAllEstimateDetail();
	
	EstimateDetail saveEstimateDetail(EstimateDetail estimateDetail);

	EstimateDetail getLoanByEstimateUniqueCode(String keyword);
}
