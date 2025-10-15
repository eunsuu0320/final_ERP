package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.SalaryDetail;

import jakarta.transaction.Transactional;

@Repository
public interface SalaryDetailRepository extends JpaRepository<SalaryDetail, Long> {

	// 회사코드별 조회
	List<SalaryDetail> findByCompanyCode(String companyCode);

	  // ✅ 급여대장 디테일 일괄 생성 (EMPLOYEE → SALARY_DETAIL)
    // - 시퀀스명: SALARY_DETAIL_SEQ (프로젝트명으로 변경)
    // - 재직자만: RESIGN_DATE IS NULL
    // - 중복방지: 동일 salary_id + company_code + emp_code 존재 시 제외
	@Modifying
	@Transactional
	@Query(value = """
	    INSERT INTO SALARY_DETAIL (
	        SAL_DETAIL_ID, SALARY_ID, COMPANY_CODE, EMP_CODE, SALARY,
	        ALL_01, ALL_02, ALL_03, ALL_04, ALL_05,
	        ALL_06, ALL_07, ALL_08, ALL_09, ALL_10,
	        DED_01, DED_02, DED_03, DED_04, DED_05,
	        DED_06, DED_07, DED_08, DED_09, DED_10,
	        ALL_TOTAL, DED_TOTAL, NET_PAY
	    )
	    SELECT
	        SALARY_DETAIL_SEQ.NEXTVAL,
	        :salaryId,              -- SALARY_DETAIL.SALARY_ID가 VARCHAR2인 경우
	        -- TO_NUMBER(:salaryId), -- SALARY_DETAIL.SALARY_ID가 NUMBER인 경우
	        e.COMPANY_CODE, e.EMP_CODE, NVL(e.SALARY,0),
	        0,0,0,0,0,
	        0,0,0,0,0,
	        0,0,0,0,0,
	        0,0,0,0,0,
	        0, 0, NVL(e.SALARY,0)
	    FROM EMPLOYEE e
	    WHERE e.COMPANY_CODE = :companyCode
	      AND e.RESIGN_DATE IS NULL
	      AND NOT EXISTS (
	            SELECT 1
	              FROM SALARY_DETAIL sd
	             WHERE sd.SALARY_ID    = :salaryId      -- VARCHAR2
	             -- AND sd.SALARY_ID   = TO_NUMBER(:salaryId) -- NUMBER
	               AND sd.COMPANY_CODE = e.COMPANY_CODE
	               AND sd.EMP_CODE     = e.EMP_CODE
	    )
	    """, nativeQuery = true)
	int insertDetailsForMaster(String salaryId, String companyCode);

	// 회사코드별, 급여대장별 조회
	List<SalaryDetail> findByCompanyCodeAndSalaryId(String companyCode, String salaryId);
}
