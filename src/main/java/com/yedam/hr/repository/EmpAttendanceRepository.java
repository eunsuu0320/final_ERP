package com.yedam.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.EmpAttendance;

@Repository
public interface EmpAttendanceRepository extends JpaRepository<EmpAttendance, String> {

}
