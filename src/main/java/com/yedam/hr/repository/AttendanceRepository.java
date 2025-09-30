package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

	// 회사코드별 근태 목록
	List<Attendance> findByCompanyCode(String companyCode);
}
