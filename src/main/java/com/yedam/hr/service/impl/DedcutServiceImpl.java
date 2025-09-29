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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Dedcut> saveAllDedcuts(List<Dedcut> dedcuts, String companyCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateStatus(List<Dedcut> codes, String status, String companyCode) {
		// TODO Auto-generated method stub

	}
}
