package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrHistoryRepository;
import com.yedam.hr.service.HrHistorySerivce;

@Service
public class HrHistorySerivceImpl implements HrHistorySerivce {

	@Autowired HrHistoryRepository historyRepository;
	@Autowired EmployeeRepository employeeRepository;
	@Override
	public List<HrHistoryDTO> findByCompanyCode(String companyCode) {
		List<HrHistory> list = historyRepository.findByCompanyCode(companyCode);
		System.out.println(list);
		return list.stream()
		        .map(e -> new HrHistoryDTO(e.getCompanyCode(), e.getEventType(),
                        e.getEventDetail(), employeeRepository.findById(e.getManager()).get().getName(), e.getCreatedAt(), e.getEmpCode(), e.getEmployee().getName()))
.toList();
	}
}
