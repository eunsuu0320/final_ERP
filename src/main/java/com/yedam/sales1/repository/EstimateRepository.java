package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional; // Optional 임포트 추가

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;

@Repository
public interface EstimateRepository extends
		JpaRepository<Estimate, Long>{

	List<Estimate> findAll();
	
	/**
     * 견적서 코드를 기반으로 Estimate 엔티티를 조회합니다.
     * ServiceImpl의 updateEstimateStatus 메서드에서 사용됩니다.
     */
	Optional<Estimate> findByEstimateCode(String estimateCode); // 이 메서드가 추가되었습니다.
	
	@Query("SELECT MAX(e.estimateCode) FROM Estimate e")
	String findMaxEstimateCode();
}
