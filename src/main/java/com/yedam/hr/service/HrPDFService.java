package com.yedam.hr.service;

import java.util.Optional;

import com.yedam.hr.domain.HrPDF;

public interface HrPDFService {

	Optional<HrPDF> findBySignId(Long signId);
}
