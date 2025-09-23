package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.service.HrService;

@Service
public class HrServiceImpl implements HrService {

	@Autowired EmployeeRepository employeeRepository;

	@Override
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}
}
