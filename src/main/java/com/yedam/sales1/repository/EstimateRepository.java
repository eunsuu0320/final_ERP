package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional; // Optional 임포트 추가

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {

	@Query(value = """
			SELECT e.*
			FROM ESTIMATE e
			where e.COMPANY_CODE = :companyCode
			ORDER BY e.CREATE_DATE DESC
			""", nativeQuery = true)
	List<Estimate> findAll(String companyCode);

	@Query(value = """
			SELECT e.*
			FROM ESTIMATE e
			where e.COMPANY_CODE = :companyCode
			ORDER BY e.CREATE_DATE DESC
			""", nativeQuery = true)
	List<Estimate> findAllEstimates(String companyCode);

	@Modifying
	@Query("""
			    update Estimate e
			    set e.status = '체결'
			    where e.estimateUniqueCode = :estimateUniqueCode
			""")
	int updateStatusSuccess(@Param("estimateUniqueCode") Long estimateUniqueCode);

	@Query("""
			    select e from Estimate e
			    where e.companyCode = :companyCode
			    and e.status != '체결'
			""")
	List<Estimate> findByCompanyCode(String companyCode);

	Optional<Estimate> findByEstimateCode(String estimateCode);

	@Query("SELECT MAX(e.estimateCode) FROM Estimate e")
	String findMaxEstimateCode();

	Estimate findByEstimateUniqueCode(long estimateUniqueCode);

	@Query("SELECT p FROM Estimate p")
	List<Estimate> findByFilter(@Param("searchVo") Estimate searchVo);
}
