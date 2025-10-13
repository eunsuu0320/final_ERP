package com.yedam.common.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.hr.domain.Attendance;
import com.yedam.hr.domain.Employee;
import com.yedam.hr.repository.AttendanceRepository;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.repository.ProductRepository;

@RestController
@RequestMapping("/api/modal")
public class ModalController {

	@Autowired EmployeeRepository employeeRepository;
	@Autowired AttendanceRepository attendanceRepository;
	@Autowired ProductRepository productRepository;



	@GetMapping("/employee")
	public List<Employee> getEmployees() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];

		return employeeRepository.findByCompanyCode(companyCode);
	}

	// 모달 회사코드별 사원 근태 조회
	@GetMapping("/attendance")
	public List<Attendance> getModalEmpAttendances() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];

		return attendanceRepository.findByCompanyCode(companyCode);
	}
	
	
	@GetMapping("/productCode")
	public List<Product> getProducts() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];

		return productRepository.findByCompanyCode(companyCode);
	}
}
