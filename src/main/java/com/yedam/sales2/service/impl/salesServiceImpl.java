package com.yedam.sales2.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales2.domain.DSalesPlan;
import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;
import com.yedam.sales2.repository.DSalesPlanRepository;
import com.yedam.sales2.repository.SalesPlanRepository;
import com.yedam.sales2.repository.SalesRepository;
import com.yedam.sales2.service.salesService;

@Service
public class salesServiceImpl implements salesService{

	@Autowired SalesRepository salesRepository;
	@Autowired SalesPlanRepository salesPlanRepository;
	@Autowired DSalesPlanRepository dsalesPlanRepository;

	@Override
	public List<Sales> findAll() {
		return salesRepository.findAll();
	}
	
	// 년도별 영업계획
	@Override
	public List<Map<String, Object>> findSalesStatsByYear() {
		return salesPlanRepository.findSalesStatsByYear();
	}
	
	@Override
	public List<Map<String, Object>> findLastYearSalesData() {
		return salesRepository.findLastYearSalesData();
	}

    @Override
    @Transactional
    public SalesPlan saveSalesPlan(SalesPlan salesplan) {

        // 1️⃣ 메인 테이블 저장
        if (salesplan.getRegDate() == null) {
            salesplan.setRegDate(new Date());
        }
        SalesPlan savedMaster = salesPlanRepository.save(salesplan);

        // 2️⃣ 세부 테이블 저장
        List<DSalesPlan> detailList = salesplan.getDetails(); // details 필드 필요
        if (detailList != null && !detailList.isEmpty()) {
            for (DSalesPlan detail : detailList) {
                // FK 연결
            	detail.setSalesPlan(savedMaster); // 객체로 세팅
            }
            dsalesPlanRepository.saveAll(detailList);
        }

        return savedMaster;
    }

    
}
