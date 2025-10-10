package com.yedam.hr.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.CommuteList;
import com.yedam.hr.repository.CommuteListRepository;
import com.yedam.hr.service.CommuteListService;

@Service
public class CommuteListServiceImpl implements CommuteListService {

	@Autowired CommuteListRepository commuteListRepository;

	@Override
	public CommuteList insertCommute(CommuteList commuteList) {
		return commuteListRepository.save(commuteList);
	}

	@Override
	public List<CommuteList> getCommuteLists(String companyCode) {
		return commuteListRepository.findByCompanyCode(companyCode);
	}

	@Override
	public int punchOutByDate(String companyCode, String empCode, Date offTime) {
		return commuteListRepository.punchOutByDate(companyCode, empCode, offTime);
	}


}
