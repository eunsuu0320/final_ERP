package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;

@Repository
public interface EstimateRepository extends
		JpaRepository<Estimate, Long>{

	List<Estimate> findAll();
	
	// ✨ p 대신 Estimate 엔티티를 나타내는 e를 사용하도록 수정
	@Query("SELECT MAX(e.estimateCode) FROM Estimate e")
	String findMaxEstimateCode();
}