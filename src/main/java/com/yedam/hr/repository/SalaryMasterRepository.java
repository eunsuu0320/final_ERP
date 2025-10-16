package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.SalaryMaster;

@Repository
public interface SalaryMasterRepository extends JpaRepository<SalaryMaster, String> {

	// 회사코드별 조회
	List<SalaryMaster> findByCompanyCodeOrderBySalaryIdDesc(String companyCode);

	// 확정 여부 업데이트
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			    update SalaryMaster m
			       set m.confirmIs = 'Y'
			     where m.companyCode = :companyCode
			       and m.salaryId in :salaryIds
			       and (m.confirmIs is null or m.confirmIs <> 'Y')
			""")
	int confirmSelected(@Param("companyCode") String companyCode, @Param("salaryIds") List<String> salaryIds);

	SalaryMaster findByCompanyCodeAndSalaryId(String companyCode, String salaryId);
}
