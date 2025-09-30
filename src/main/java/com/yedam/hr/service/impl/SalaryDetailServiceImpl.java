package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.SalaryDetail;
import com.yedam.hr.repository.SalaryDetailRepository;
import com.yedam.hr.service.SalaryDetailService;

@Service
public class SalaryDetailServiceImpl implements SalaryDetailService {

	@Autowired SalaryDetailRepository salaryDetailRepository;

	@Override
	public List<SalaryDetail> getSalaryDetails(String companyCode) {
		return salaryDetailRepository.findByCompanyCode(companyCode);
	}
}
