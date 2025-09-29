package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.repository.HrHistoryRepository;
import com.yedam.hr.service.HrHistorySerivce;

@Service
public class HrHistorySerivceImpl implements HrHistorySerivce {

	@Autowired HrHistoryRepository historyRepository;

	@Override
	public List<HrHistory> findByCompanyCode(String companyCode) {
		return historyRepository.findByCompanyCode(companyCode);
	}
}
