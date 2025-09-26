package com.yedam.hr.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.hibernate.dialect.SelectItemReferenceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.yedam.hr.domain.Employee;
import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.domain.HrSign;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrPDFRepository;
import com.yedam.hr.repository.HrSignRepository;
import com.yedam.hr.service.HrService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class HrServiceImpl implements HrService {

	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	HrSignRepository hrSignRepository;
	@Autowired
	HrPDFRepository hrPDFRepository;

	@Override
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	@Override
	public void saveContract(Employee employee,
	                         HrSign sign,
	                         HrPDF pdf,
	                         MultipartFile signImg,
	                         MultipartFile pdfFile,
	                         MultiValueMap<String, String> params) {
	    String signFileName = null;

	    // 1. 사원 등록 (없으면 insert)
	    employee.setCompanyCode("C001");
	    try {
	        if (!employeeRepository.existsById(employee.getEmpNo())) {
	            employeeRepository.save(employee);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("사원 저장 중 오류 발생", e);
	    }

	    // 2. 서명 이미지 저장
	    String signPath = null;
	    try {
	        if (signImg == null || signImg.isEmpty()) {
	            throw new RuntimeException("서명 이미지가 전달되지 않았습니다.");
	        }

	        String uploadDir = System.getProperty("user.dir") + "/uploads/hr/sign/";
	        File signDirFile = new File(uploadDir);
	        if (!signDirFile.exists()) signDirFile.mkdirs();

	        String uuid = UUID.randomUUID().toString().substring(0, 4);
	        String safeName = employee.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
	        signFileName = uuid + "_" + employee.getEmpNo() + "_" + safeName + ".png";

	        File signFile = new File(uploadDir, signFileName);
	        signImg.transferTo(signFile);

	        signPath = "/hr/sign/" + signFileName; // DB 저장용
	    } catch (Exception e) {
	        throw new RuntimeException("서명 이미지 저장 중 오류 발생: " + e.getMessage(), e);
	    }

	    // 3. PDF 처리
	    String pdfPath = null;
	    try {
	        // ✅ 사용자가 입력한 PDF 업로드 확인
	        if (pdfFile == null || pdfFile.isEmpty()) {
	            throw new RuntimeException("계약서 PDF 파일이 전달되지 않았습니다.");
	        }

	        // 1) 임시 저장
	        String tempPdf = System.getProperty("user.dir") + "/uploads/hr/pdf/contract_temp.pdf";
	        pdfFile.transferTo(new File(tempPdf));

	        // 2) 최종 PDF 저장 경로
	        String pdfDir = System.getProperty("user.dir") + "/uploads/hr/pdf/";
	        File pdfDirFile = new File(pdfDir);
	        if (!pdfDirFile.exists()) pdfDirFile.mkdirs();

	        String safeName = employee.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
	        String pdfFileName = employee.getEmpNo() + "_" + safeName + "_contract.pdf";

	        pdfPath = "/hr/pdf/" + pdfFileName;       // DB 저장용 경로
	        String outputPath = pdfDir + pdfFileName; // 실제 파일 경로

	        // 3) PDF 열고 flatten
	        PdfDocument pdfDoc = new PdfDocument(new PdfReader(tempPdf), new PdfWriter(outputPath));
	        Document document = new Document(pdfDoc);

	        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
	        form.flattenFields(); // 사용자가 입력한 값 그대로 굳히기

	        // 4) 서명 이미지 삽입
	        String realSignPath = System.getProperty("user.dir") + "/uploads/hr/sign/" + signFileName;
	        File signFile = new File(realSignPath);

	        if (!signFile.exists()) {
	            throw new RuntimeException("서명 이미지 파일이 존재하지 않습니다: " + realSignPath);
	        }

	        ImageData imageData = ImageDataFactory.create(realSignPath);
	        Image image = new Image(imageData);
	        image.setFixedPosition(1, 280, 65); // 좌표 조정 필요
	        image.scaleToFit(120, 60);

	        document.add(image);

	        document.close();
	        pdfDoc.close();

	    } catch (Exception e) {
	        throw new RuntimeException("PDF 사본 저장/서명 삽입 중 오류 발생: " + e.getMessage(), e);
	    }

	    // 4. DB 저장
	    sign.setCompanyCode(employee.getCompanyCode());
	    sign.setEmpNo(employee.getEmpNo());
	    sign.setEmpName(employee.getName());
	    sign.setEmpDept(employee.getDept());
	    sign.setImg(signPath);
	    HrSign savedSign = hrSignRepository.save(sign);

	    pdf.setSignId(savedSign.getSignId());
	    pdf.setPdf(pdfPath);
	    hrPDFRepository.save(pdf);
	}

	// 단건조회
	@Override
	@Transactional(readOnly = true)
	public Employee getEmployee(String empNo) {
	    return employeeRepository.findById(empNo)
	            .orElseThrow(() -> new EntityNotFoundException("사원 없음: " + empNo));
	}

	// 수정
	@Override
	  @Transactional
	    public Employee updateEmployee(Employee req) {
	        // 존재 확인 후 부분 업데이트 (허용 필드만 덮어쓰기)
	        return employeeRepository.findById(req.getEmpNo())
	            .map(exist -> {
	                exist.setName(req.getName());
	                exist.setPhone(req.getPhone());
	                exist.setBirth(req.getBirth());
	                exist.setEmail(req.getEmail());
	                exist.setDept(req.getDept());
	                exist.setPosition(req.getPosition());
	                exist.setGrade(req.getGrade());
	                exist.setSalary(req.getSalary());
	                exist.setHireDate(req.getHireDate());
	                exist.setResignDate(req.getResignDate());
	                exist.setHolyDays(req.getHolyDays());
	                exist.setDepCnt(req.getDepCnt());
	                exist.setResignReason(req.getResignReason());
	                exist.setBankCode(req.getBankCode());
	                exist.setAccHolder(req.getAccHolder());
	                exist.setAccNo(req.getAccNo());
	                exist.setPostalCode(req.getPostalCode());
	                exist.setAddress(req.getAddress());

	                return exist;
	            })
	            .orElse(null);
	    }
}
