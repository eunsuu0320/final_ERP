package com.yedam.hr.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.domain.HrSign;

public interface HrService {
	List<Employee> getAllEmployees();

	// 임시 사원만 등록
	public int saveEmp(Employee employee);

	// 사원 + 근로계약서 + pdf 등록
	void saveContract(Employee employee, HrSign sign, HrPDF pdf, MultipartFile signImg);
}
