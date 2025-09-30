package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.SalaryMaster;

@Repository
public interface SalaryMasterRepository extends JpaRepository<SalaryMaster, Long> {

	// 회사코드별 조회
	List<SalaryMaster> findByCompanyCode(String companyCode);

}
