package com.yedam.hr.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.domain.HrPDF;
import com.yedam.hr.domain.HrSign;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrHistoryRepository;
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
	@Autowired
	HrHistoryRepository hrHistoryRepository;

	@Override
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	@Override
	public void saveContract(Employee employee, HrSign sign, HrPDF pdf, MultipartFile signImg, MultipartFile pdfFile,
			MultiValueMap<String, String> params) {

		// 로그인 사용자 아이디 넣기 (지금은 admin, 나중에 로그인 값으로 교체)
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String manager = auth.getName().split(":")[2];

		String signFileName = null;

		// 1. 사원 등록 (없으면 insert)
		employee.setCompanyCode("C001"); // 테스트로 회사 코드 고정. 수정필요
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
			if (!signDirFile.exists())
				signDirFile.mkdirs();

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
			if (!pdfDirFile.exists())
				pdfDirFile.mkdirs();

			String safeName = employee.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
			String pdfFileName = employee.getEmpNo() + "_" + safeName + "_contract.pdf";

			pdfPath = "/hr/pdf/" + pdfFileName; // DB 저장용 경로
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

		// 5. DB 저장 끝난 후 → 이력 저장
		try {
			HrHistory history = new HrHistory();
			history.setCompanyCode(employee.getCompanyCode());
			history.setEmpNo(employee.getEmpNo()); // Employee에서 empNo 그대로
			history.setEventType("등록"); // 등록/수정 구분
			history.setEventDetail("사원 신규 등록 및 계약서 저장");

			history.setManager(manager);

			hrHistoryRepository.save(history);

		} catch (Exception e) {
			throw new RuntimeException("이력 저장 중 오류 발생: " + e.getMessage(), e);
		}
	}

	// 단 건 조회
	@Override
	@Transactional(readOnly = true)
	public Employee getEmployee(String empNo) {
		return employeeRepository.findById(empNo).orElseThrow(() -> new EntityNotFoundException("사원 없음: " + empNo));
	}

	// 단 건 수정
	@Override
	public void updateEmployee(Employee employee, MultipartFile signImg, MultipartFile pdfFile) {
		// 로그인 사용자 아이디 넣기 (지금은 admin, 나중에 로그인 값으로 교체)
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String manager = auth.getName().split(":")[2];

		// 1. 기존 사원 정보 조회
		Employee existing = employeeRepository.findById(employee.getEmpNo())
				.orElseThrow(() -> new RuntimeException("해당 사원이 존재하지 않습니다."));

		// 2. 수정할 필드만 덮어쓰기 (null은 무시 → 기존 값 유지)
		if (employee.getName() != null)
			existing.setName(employee.getName());
		if (employee.getPhone() != null)
			existing.setPhone(employee.getPhone());
		if (employee.getBirth() != null)
			existing.setBirth(employee.getBirth());
		if (employee.getEmail() != null)
			existing.setEmail(employee.getEmail());
		if (employee.getDept() != null)
			existing.setDept(employee.getDept());
		if (employee.getPosition() != null)
			existing.setPosition(employee.getPosition());
		if (employee.getGrade() != null)
			existing.setGrade(employee.getGrade());
		if (employee.getSalary() != null)
			existing.setSalary(employee.getSalary());
		if (employee.getHireDate() != null)
			existing.setHireDate(employee.getHireDate());
		if (employee.getResignDate() != null)
			existing.setResignDate(employee.getResignDate());
		if (employee.getHolyDays() != null)
			existing.setHolyDays(employee.getHolyDays());
		if (employee.getDepCnt() != null)
			existing.setDepCnt(employee.getDepCnt());
		if (employee.getResignReason() != null)
			existing.setResignReason(employee.getResignReason());
		if (employee.getBankCode() != null)
			existing.setBankCode(employee.getBankCode());
		if (employee.getAccHolder() != null)
			existing.setAccHolder(employee.getAccHolder());
		if (employee.getAccNo() != null)
			existing.setAccNo(employee.getAccNo());
		if (employee.getPostalCode() != null)
			existing.setPostalCode(employee.getPostalCode());
		if (employee.getAddress() != null)
			existing.setAddress(employee.getAddress());
		// ⚠ companyCode는 덮어쓰지 않음 (DB 값 유지)

		// 3. 사원 정보 저장
		employeeRepository.save(existing);

		// 4. 선택적으로 서명 이미지 저장 (있으면 갱신)
		HrSign sign = hrSignRepository.findByEmpNo(existing.getEmpNo()).orElse(null);

		if (signImg != null && !signImg.isEmpty()) {
			try {
				String uploadDir = System.getProperty("user.dir") + "/uploads/hr/sign/";
				File dir = new File(uploadDir);
				if (!dir.exists())
					dir.mkdirs();

				String uuid = UUID.randomUUID().toString().substring(0, 4);
				String safeName = existing.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
				String signFileName = uuid + "_" + existing.getEmpNo() + "_" + safeName + ".png";

				File signFile = new File(uploadDir, signFileName);
				signImg.transferTo(signFile);

				String signPath = "/hr/sign/" + signFileName; // DB에 저장될 경로

				if (sign == null) {
					sign = new HrSign();
				}
				sign.setCompanyCode(existing.getCompanyCode());
				sign.setEmpNo(existing.getEmpNo());
				sign.setEmpName(existing.getName());
				sign.setEmpDept(existing.getDept());
				sign.setImg(signPath);
				sign = hrSignRepository.save(sign);

			} catch (Exception e) {
				throw new RuntimeException("서명 이미지 저장 중 오류", e);
			}
		}

		// 5. 선택적으로 PDF 저장 (있으면 갱신)
		if (pdfFile != null && !pdfFile.isEmpty()) {
			try {
				String uploadDir = System.getProperty("user.dir") + "/uploads/hr/pdf/";
				File dir = new File(uploadDir);
				if (!dir.exists())
					dir.mkdirs();

				String safeName = existing.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
				String pdfFileName = existing.getEmpNo() + "_" + safeName + "_contract.pdf";

				String pdfPath = "/hr/pdf/" + pdfFileName; // DB 저장용 경로
				String outputPath = uploadDir + pdfFileName;

				pdfFile.transferTo(new File(outputPath));

				HrPDF pdf = hrPDFRepository.findBySignId(sign.getSignId()) // ✅ signId로 조회
						.orElse(new HrPDF());

				pdf.setSignId(sign.getSignId());
				pdf.setPdf(pdfPath);
				hrPDFRepository.save(pdf);

			} catch (Exception e) {
				throw new RuntimeException("PDF 저장 중 오류", e);
			}
		}

		// Employee 수정 저장 완료 후
		HrHistory history = new HrHistory();
		history.setCompanyCode(existing.getCompanyCode());
		history.setEmpNo(existing.getEmpNo());
		history.setEventType("수정");
		history.setEventDetail("사원 정보 수정");
		history.setManager(manager);
		hrHistoryRepository.save(history);

	}
}
