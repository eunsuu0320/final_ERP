package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.Dedcut;
import com.yedam.hr.repository.DedcutRepository;
import com.yedam.hr.service.DedcutService;

@Service
public class DedcutServiceImpl implements DedcutService {

	@Autowired
	DedcutRepository dedcutRepository;

	@Override
	public List<Dedcut> findByCompanyCode(String companyCode) {
		return dedcutRepository.findByCompanyCode(companyCode);
	}

	@Override
	public List<Dedcut> saveAllDedcuts(List<Dedcut> dedcuts, String companyCode) {
		dedcuts.forEach(a -> a.setCompanyCode(companyCode)); // 회사코드 강제 세팅
		return dedcutRepository.saveAll(dedcuts);
	}

	@Override
	public void updateStatus(List<String> codes, String status, String companyCode) {
		for (String code : codes) {
			Dedcut dedcut = dedcutRepository.findByDedIdAndCompanyCode(code, companyCode)
					.orElseThrow(() -> new RuntimeException("해당 공제 없음 " + code));
			dedcut.setAllIs(status);
			dedcutRepository.save(dedcut);
		}
	}
}
