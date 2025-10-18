package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional; // Optional 임포트 추가

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Product;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {

	List<Estimate> findAll();

	@Query("""
			    select e from Estimate e
			    where e.companyCode = :companyCode
			    and e.status != '체결'
			""")
	List<Estimate> findByCompanyCode(String companyCode);

	Optional<Estimate> findByEstimateCode(String estimateCode);

	@Query("SELECT MAX(e.estimateCode) FROM Estimate e")
	String findMaxEstimateCode();
}
