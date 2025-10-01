package com.yedam.hr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

	// 회사코드별 근태 목록
	List<Attendance> findByCompanyCode(String companyCode);

	// 선택 건 사용중단 및 재사용
	Optional<Attendance> findByAttIdAndCompanyCode(String attId, String companyCode);
}
