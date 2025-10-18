package com.yedam.hr.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;

import jakarta.transaction.Transactional;

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
			    order by e.empCode asc
			""")
	List<Employee> findByCompanyCode(String companyCode);

	// ✅ 연차(holyDays) 잔여분이 1일 이상일 때만 1일 차감 (NULL 안전)
	@Modifying(clearAutomatically = false, flushAutomatically = false)
	@Transactional
	@Query("""
			    update Employee e
			       set e.holyDays = COALESCE(e.holyDays, 0) - 1
			     where e.companyCode = :companyCode
			       and e.empCode     = :empCode
			       and COALESCE(e.holyDays, 0) > 0
			""")
	int decrementHolyDaysIfAvailable(@Param("companyCode") String companyCode, @Param("empCode") String empCode);

	// 회사코드 사원 단건 조회
	@Query("""
			  select e
			  from Employee e
			  where e.companyCode = :companyCode
			    and e.empCode     = :empCode
			""")
	Employee findByCompanyCodeAndEmpCode(String companyCode, String empCode);

	// 급여대장 명세서 조회할 때 넘겨주는 값.
	@Query("""
		    select e
		    from Employee e
		    where e.companyCode = :companyCode
		      and e.empCode in :empCodes
		      and e.resignDate is null
		""")
		List<Employee> findByCompanyCodeAndEmpCodeIn(String companyCode, Collection<String> empCodes);

}
