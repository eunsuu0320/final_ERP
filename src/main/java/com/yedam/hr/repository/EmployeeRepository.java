package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

	// 회사코드별 사원 조회
	List<Employee> findByCompanyCode(String companyCode);

}
