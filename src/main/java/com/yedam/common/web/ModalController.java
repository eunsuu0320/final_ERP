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
import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.dto.EstimateModalDto;
import com.yedam.sales1.dto.PartnerModalDto;
import com.yedam.sales1.repository.EstimateRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.repository.ProductRepository;
import com.yedam.sales1.repository.ShipmentRepository;

@RestController
@RequestMapping("/api/modal")
public class ModalController {

	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	AttendanceRepository attendanceRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	EstimateRepository estimateRepository;
	@Autowired
	ShipmentRepository shipmentRepository;
	@Autowired
	PartnerRepository partnerRepository;

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

	@GetMapping("/estimate")
	public List<EstimateModalDto> getEstimates() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String companyCode = auth.getName().split(":")[0];

	    List<Estimate> estimates = estimateRepository.findByCompanyCode(companyCode);

	    return estimates.stream().map(estimate -> {
	        EstimateModalDto dto = new EstimateModalDto();
	        dto.setEstimateUniqueCode(estimate.getEstimateUniqueCode().intValue());
	        dto.setEstimateCode(estimate.getEstimateCode());
	        dto.setPartnerCode(estimate.getPartnerCode());
	        dto.setPartnerName(estimate.getPartner() != null ? estimate.getPartner().getPartnerName() : null);
	        dto.setManager(estimate.getManager());
	        dto.setManagerName(
	            estimate.getManagerEmp() != null ? estimate.getManagerEmp().getName() : null 
	        );
	        dto.setPostCode(estimate.getPostCode());
	        dto.setAddress(estimate.getAddress());
	        dto.setRemarks(estimate.getRemarks());
	        dto.setTotalAmount(estimate.getTotalAmount());
	        dto.setDeliveryDate(estimate.getDeliveryDate());
	        return dto;
	    }).toList();
	}


	@GetMapping("/salesEmployee")
	public List<Employee> getSalesEmployee() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];
		return shipmentRepository.findByCompanyCodeSalesEmployee(companyCode);
	}

	
	@GetMapping("/salesPartner")
	public List<PartnerModalDto> getSalesPartner() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String companyCode = auth.getName().split(":")[0];
		return partnerRepository.findPartnerModalDataByCompanyCode(companyCode);
	}
}
