package com.yedam.hr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrPDF;

@Repository
public interface HrPDFRepository extends JpaRepository<HrPDF, Long> {

	Optional<HrPDF> findBySignId(Long signId);

}
