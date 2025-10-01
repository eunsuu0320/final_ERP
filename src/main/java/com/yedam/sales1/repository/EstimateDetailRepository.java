package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.EstimateDetail;

@Repository
public interface EstimateDetailRepository extends JpaRepository<EstimateDetail, String> {

	List<EstimateDetail> findAll();

	EstimateDetail findByEstimateUniqueCode(Long estimateUniqueCode);

	@Query("SELECT MAX(ed.estimateDetailCode) FROM EstimateDetail ed")
	String findMaxEstimateDetailCode();
}