package com.yedam.hr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Dedcut;

@Repository
public interface DedcutRepository extends JpaRepository<Dedcut, String> {

	// 회사 코드별 공제 전체 조회
	List<Dedcut> findByCompanyCode(String companyCode);

	// 선택 건 사용중단 및 재사용
	Optional<Dedcut> findByDedIdAndCompanyCode(String dedId, String companyCode);
}
