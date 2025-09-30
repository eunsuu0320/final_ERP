package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.repository.SalaryMasterRepository;
import com.yedam.hr.service.SalaryMasterService;

@Service
public class SalaryMasterServcieImpl implements SalaryMasterService {

	@Autowired SalaryMasterRepository salaryMasterRepository;

	@Override
	public List<SalaryMaster> getSalaryMasters(String companyCode) {
		return salaryMasterRepository.findByCompanyCode(companyCode);
	}
}
