package com.yedam.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.CommonCode;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {

	@Query("select c from CommonCode c where c.codeGroup = ?1")
	List<CommonCode> findByCodeGroup(String codeGroup);
}
