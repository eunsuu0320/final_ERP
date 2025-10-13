package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.repository.SalaryMasterRepository;
import com.yedam.hr.service.SalaryMasterService;

@Service
public class SalaryMasterServcieImpl implements SalaryMasterService {

	@Autowired SalaryMasterRepository salaryMasterRepository;

	@Override
	public List<SalaryMaster> getSalaryMasters(String companyCode) {
		return salaryMasterRepository.findByCompanyCodeOrderBySalaryIdDesc(companyCode);
	}

	@Override
	public SalaryMaster insertSalaryMaster(SalaryMaster salaryMaster) {
		// 로그인 사용자 아이디 넣기
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];
		salaryMaster.setCompanyCode(companyCode);
		salaryMaster.setConfirmIs("n");

		// payName 없음 → 기본 규칙으로 생성 (예: 2025-10 급여대장(1차))
	    if (salaryMaster.getPayName() == null || salaryMaster.getPayName().isEmpty()) {
	        String ym = salaryMaster.getPayYm();        // "YYYY-MM" (프론트에서 일 빼서 보냄)
	        String type = salaryMaster.getPayType();    // "1" or "2"
	        String typeLabel = "1".equals(type) ? "1차" : ("2".equals(type) ? "2차" : "지급");
	        if (ym != null && ym.length() >= 7) {
	            salaryMaster.setPayName(ym.substring(0,7) + " 급여대장(" + typeLabel + ")");
	        } else {
	            salaryMaster.setPayName("급여대장");
	        }
	    }
		return salaryMasterRepository.save(salaryMaster);
	}
}
