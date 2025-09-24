package com.yedam.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.HrPDF;

@Repository
public interface HrPDFRepository extends JpaRepository<HrPDF, Long> {

}
