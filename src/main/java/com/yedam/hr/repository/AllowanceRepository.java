package com.yedam.hr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Allowance;

@Repository
public interface AllowanceRepository extends JpaRepository<Allowance, String> {

	// 회사 코드별 순서대로 수당 전체 조회
	List<Allowance> findByCompanyCodeOrderByAllId(String companyCode);

	// 회사 코드별 수당 전체 조회
	List<Allowance> findByCompanyCode(String companyCode);

	// 선택 건 사용중단 및 재사용
	Optional<Allowance> findByAllIdAndCompanyCode(String allId, String companyCode);

	@Query("""
		       select a
		       from Allowance a
		       where a.companyCode = :companyCode
		         and a.allIs = :allYn
		       order by a.mapNum asc
		       """)
		List<Allowance> findActiveByCompanyCode(@Param("companyCode") String companyCode,
		                                        @Param("allYn") String allYn);
}
