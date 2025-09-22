package com.yedam.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.repository.EmployeeRepository;

@RestController
@RequestMapping("/api/modal")
public class ModalController {

	@Autowired EmployeeRepository employeeRepository;
	
	@GetMapping("/employee")
	public List<Employee> getEmployees() {
		return employeeRepository.findAll();
	}
}
