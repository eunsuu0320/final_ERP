package com.yedam.sales2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.EsdpPlan;

@Repository
public interface EsdpPlanRepository extends JpaRepository<EsdpPlan, String>{
	
	// espCode 로 전체 조회
	@Query
    List<EsdpPlan> findByEspCode(String espCode);
    
}
