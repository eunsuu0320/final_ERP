package com.yedam.hr.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrSign;
import com.yedam.hr.repository.HrSignRepository;
import com.yedam.hr.service.HrSignService;

@Service
public class HrSignServiceImpl implements HrSignService {

	@Autowired HrSignRepository hrSignRepository;

	@Override
	public Optional<HrSign> findByEmpNo(String empCode) {
		return hrSignRepository.findByEmpCode(empCode);
	}
}
