package com.yedam.hr.service;

import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.domain.HrSign;

public interface HrService {

	// 회사코드별 사원 조회
	List<Employee> findByCompanyCode(String companyCode);

	   // 사원 + 근로계약서 + pdf 등록
    void saveContract(Employee employee,
                      HrSign sign,
                      HrPDF pdf,
                      MultipartFile signImg,
                      MultipartFile pdfFile,
                      MultiValueMap<String, String> params);

    // 단건조회
    public Employee getEmployee(String empCode);

    // 수정
    void updateEmployee(Employee employee,
            			MultipartFile signImg,
            			MultipartFile pdfFile);
}
