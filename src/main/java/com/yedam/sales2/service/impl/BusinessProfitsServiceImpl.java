package com.yedam.sales2.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales2.repository.BusinessProfitsRepository;
import com.yedam.sales2.service.BusinessProfitsService;

@Service
public class BusinessProfitsServiceImpl implements BusinessProfitsService{

	  @Autowired
	    private BusinessProfitsRepository businessProfitsRepository;

	    @Override
	    public List<Map<String, Object>> getSalesProfitList(String year, String quarter, String keyword) {
	        return businessProfitsRepository.findSalesProfitList(year, quarter, keyword);
	    }
}
