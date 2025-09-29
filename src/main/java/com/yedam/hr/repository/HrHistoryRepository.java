package com.yedam.hr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrHistory;

@Repository
public interface HrHistoryRepository extends JpaRepository<HrHistory, Long> {

	@Query("SELECT h FROM HrHistory h JOIN FETCH h.employee e WHERE h.companyCode = :companyCode")
	List<HrHistory> findByCompanyCode(@Param("companyCode") String companyCode);

}
