	package com.yedam.hr.repository;

	import java.util.Date;
	import java.util.List;

	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.data.jpa.repository.Modifying;
	import org.springframework.data.jpa.repository.Query;
	import org.springframework.data.repository.query.Param;
	import org.springframework.stereotype.Repository;

	import com.yedam.hr.domain.CommuteList;

	import jakarta.transaction.Transactional;

	@Repository
	public interface CommuteListRepository extends JpaRepository<CommuteList, Long> {

		// 회사코드별 출퇴근 목록
		List<CommuteList> findByCompanyCode(String companyCode);

		   /**
	     * 회사코드 + 사원코드가 같고, ON_TIME의 '날짜'가 offTime의 '날짜'와 같은 행을 찾아
	     * OFF_TIME, WORK_TIME(시간) 갱신. (이미 퇴근 체크된 행은 제외하고 싶으면 AND OFF_TIME IS NULL 추가)
	     */
		@Modifying
		@Transactional
		@Query(value = """
		    UPDATE COMMUTE_LIST
		       SET OFF_TIME  = CAST(:offTime AS DATE),
		           -- 시간(소수) 저장. 분(int)로 저장하려면 * 24 * 60 로 변경
		           WORK_TIME = ROUND( (CAST(:offTime AS DATE) - ON_TIME) * 24, 2 )
		     WHERE COMPANY_CODE = :companyCode
		       AND EMP_CODE     = :empCode
		       AND ON_TIME >= TRUNC(CAST(:offTime AS DATE))
		       AND ON_TIME <  TRUNC(CAST(:offTime AS DATE)) + 1
		       AND OFF_TIME IS NULL
		    """, nativeQuery = true)
		int punchOutByDate(
		        @Param("companyCode") String companyCode,
		        @Param("empCode")     String empCode,
		        @Param("offTime")     Date offTime
		);

		// onTime이 특정 날짜 범위에 있는 최신 1건 조회
		@Query(value = """
				SELECT *
				FROM (
				  SELECT c.*
				  FROM COMMUTE_LIST c
				  WHERE c.COMPANY_CODE = :cc
				    AND c.EMP_CODE     = :ec
				    AND c.ON_TIME BETWEEN :ds AND :de
				  ORDER BY c.ON_TIME DESC
				)
				WHERE ROWNUM = 1
				""", nativeQuery = true)
				CommuteList findLatestOnTimeRownum(@Param("cc") String companyCode,
				                                   @Param("ec") String empCode,
				                                   @Param("ds") Date dayStartDate,
				                                   @Param("de") Date dayEndDate);
	}
