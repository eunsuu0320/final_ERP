package com.yedam.sales2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.domain.Sales;

@Repository
public interface EspPlanRepository extends JpaRepository<EspPlan, String>{

	
	}
