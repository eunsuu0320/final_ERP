package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.SalaryDetail;

@Repository
public interface SalaryDetailRepository extends JpaRepository<SalaryDetail, Long> {

	// 회사코드별 조회
	List<SalaryDetail> findByCompanyCode(String companyCode);
}
