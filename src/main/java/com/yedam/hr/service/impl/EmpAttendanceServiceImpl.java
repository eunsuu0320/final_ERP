package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.EmpAttendance;
import com.yedam.hr.repository.EmpAttendanceRepository;
import com.yedam.hr.service.EmpAttendanceService;

@Service
public class EmpAttendanceServiceImpl implements EmpAttendanceService {

	@Autowired EmpAttendanceRepository empAttendanceRepository;

	@Override
	public List<EmpAttendance> getEmpAttendances(String companyCode) {
		return empAttendanceRepository.findByCompanyCode(companyCode);
	}
}
