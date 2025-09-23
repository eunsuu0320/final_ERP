package com.yedam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.domain.SalesPlan;

@Repository
public interface SalesPlanRepository extends JpaRepository<SalesPlan, String>{

	
}
