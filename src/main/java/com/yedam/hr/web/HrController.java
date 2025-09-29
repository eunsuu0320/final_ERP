package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.domain.HrSign;
import com.yedam.hr.service.HrHistorySerivce;
import com.yedam.hr.service.HrService;

@Controller
public class HrController {

	@Autowired HrService hrService;

	// 사원 등록 페이지
	@GetMapping("/empPage")
	public String getEmployee(Model model) {
	    return "hr/employee";
	}


	@ResponseBody
	@GetMapping("/selectAllEmp")
	public List<Employee> employee(Model model) {
		return hrService.getAllEmployees();
	}

	// 사원 등록 모달 html
	@GetMapping("/employee")
	public String getEmployeeString(Model model) {
		return "hr/employeeModal";
	}

	// 사원 등록 처리
	@PostMapping(value = "/saveContract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public String saveContract(
	        @ModelAttribute Employee employee,
	        @RequestParam(value="signImg", required=false) MultipartFile signImg,
	        @RequestParam(value="pdfFile", required=false) MultipartFile pdfFile,
	     // 🔹 같은 name의 값들을 모두 받기 (권장)
	        @RequestParam MultiValueMap<String, String> params) {
	    try {
	        HrSign sign = new HrSign();
	        HrPDF pdf = new HrPDF();
	        hrService.saveContract(employee, sign, pdf, signImg, pdfFile, params);
	        return "success";
	    } catch (Exception e) {
	        return "fail: " + e.getMessage();
	    }
	}

	// 단건 조회
	@GetMapping("/api/employees/{empNo}")
	public ResponseEntity<Employee> getEmployeeByPath(@PathVariable String empNo) {
	    Employee emp = hrService.getEmployee(empNo);
	    return (emp != null) ? ResponseEntity.ok(emp)
	                         : ResponseEntity.notFound().build();
	}

	// 단건 수정
	@PostMapping("/updateEmployee")
	@ResponseBody
	public String updateEmployee(@ModelAttribute Employee employee,
	                             @RequestParam(value = "signImg", required = false) MultipartFile signImg,
	                             @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile) {
	    try {
	        hrService.updateEmployee(employee, signImg, pdfFile);
	        return "success";  // ✅ JS에서 검사하는 값
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "fail: " + e.getMessage();
	    }
	}
}
