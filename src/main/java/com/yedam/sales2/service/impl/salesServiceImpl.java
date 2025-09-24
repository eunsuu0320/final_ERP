package com.yedam.sales2.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;
import com.yedam.sales2.repository.SalesRepository;
import com.yedam.sales2.service.salesService;

@Service
public class salesServiceImpl implements salesService{

	@Autowired SalesRepository salesRepository;

	@Override
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}

	@Override
	public List<Map<String, Object>> findSalesStatsByYear() {
		return salesRepository.findSalesStatsByYear();
	}

	@Override
	public List<Map<String, Object>> findSalesPlanData() {
		return salesRepository.findSalesPlanData();
	}

	@Override
	public String insertSales(SalesPlan salesPlan) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
