package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

	// 회사코드별 사원 조회
    @Query("""
        select e from Employee e
        left join fetch e.deptCode
        left join fetch e.positionCode
        left join fetch e.gradeCode
        left join fetch e.bankCodeEntity
        where e.companyCode = :companyCode
    """)
	List<Employee> findByCompanyCode(String companyCode);

}
