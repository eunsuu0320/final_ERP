package com.yedam.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modal")
public class ModalController {

	@GetMapping("/employee")
	public List<Employee> getEmployees() {
		return employeeRepository.findAll();
	}
}
