package com.yedam.sales1.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.EstimateDetail;
import com.yedam.sales1.repository.EstimateDetailRepository;
import com.yedam.sales1.service.EstimateDetailService;

@Service
public class EstimateDetailServiceImpl implements EstimateDetailService {

	private final EstimateDetailRepository estimateDetailRepository;

	@Autowired
	public EstimateDetailServiceImpl(EstimateDetailRepository estimateDetailRepository) {
		this.estimateDetailRepository = estimateDetailRepository;
	}

	@Override
	public List<EstimateDetail> getAllEstimateDetail() {
		return estimateDetailRepository.findAll();
	}

	@Override
	public EstimateDetail saveEstimateDetail(EstimateDetail estimateDetail) {
		return estimateDetailRepository.save(estimateDetail);
	}

	@Override
	public List<EstimateDetail> getEstimateDetailByEstimateUniqueCode(long estimateUniqueCode) {
        return estimateDetailRepository.findByEstimateUniqueCode(estimateUniqueCode);
	}



}
