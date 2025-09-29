package com.yedam.hr.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.repository.HrPDFRepository;
import com.yedam.hr.service.HrPDFService;

@Service
public class HrPDFServiceImpl implements HrPDFService {

	@Autowired HrPDFRepository hrPDFRepository;

	@Override
	public Optional<HrPDF> findBySignId(Long signId) {
		return hrPDFRepository.findBySignId(signId);
	}
}
