package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.Allowance;
import com.yedam.hr.repository.AllowanceRepository;
import com.yedam.hr.service.AllowanceService;

@Service
public class AllowanceServiceImpl implements AllowanceService {

	@Autowired AllowanceRepository allowanceRepository;

	@Override
	public List<Allowance> findByCompanyCode(String companyCode) {
		return allowanceRepository.findByCompanyCode(companyCode);
	}

	@Override
	public List<Allowance> saveAllAllowances(List<Allowance> allowances, String companyCode) {
		allowances.forEach(a -> a.setCompanyCode(companyCode)); // 회사코드 강제 세팅
		return allowanceRepository.saveAll(allowances);
	}

	@Override
	public void updateStatus(List<String> codes, String status, String companyCode) {
		for (String code : codes) {
			Allowance allowance = allowanceRepository.findByAllIdAndCompanyCode(code, companyCode)
					.orElseThrow(() -> new RuntimeException("해당 수당 없음: " + code));

			allowance.setAllIs(status); // Y / N
			allowanceRepository.save(allowance);
		}
	}
}
