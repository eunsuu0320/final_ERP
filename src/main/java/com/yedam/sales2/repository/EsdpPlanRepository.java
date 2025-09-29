package com.yedam.sales2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.EsdpPlan;

@Repository
public interface EsdpPlanRepository extends JpaRepository<EsdpPlan, Integer>{

}
