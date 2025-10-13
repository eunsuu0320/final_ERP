package com.yedam.sales2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.EsdpPlan;
import com.yedam.sales2.domain.EspPlan;

@Repository
public interface EsdpPlanRepository extends JpaRepository<EsdpPlan, String>{
	
	   // ✅ espCode 로 전체 조회
    List<EsdpPlan> findByEspCode(String espCode);
}
