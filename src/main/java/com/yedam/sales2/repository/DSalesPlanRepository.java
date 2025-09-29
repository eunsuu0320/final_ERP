package com.yedam.sales2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.DSalesPlan;
import com.yedam.sales2.domain.SalesPlan;

@Repository
public interface DSalesPlanRepository extends JpaRepository<DSalesPlan, Integer> { 
	
	// 영업 년도별 세부계획
	 List<DSalesPlan> findBySalesPlan(SalesPlan salesPlan);
}
