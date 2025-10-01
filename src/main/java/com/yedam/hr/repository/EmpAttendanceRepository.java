package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.EmpAttendance;

@Repository
public interface EmpAttendanceRepository extends JpaRepository<EmpAttendance, String> {

	// 회사코드별 사원 근태 조회
	List<EmpAttendance> findByCompanyCode(String companyCode);

}
