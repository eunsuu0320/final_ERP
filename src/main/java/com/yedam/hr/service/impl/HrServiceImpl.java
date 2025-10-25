package com.yedam.hr.service.impl;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.yedam.hr.dto.EmployeeDTO;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrHistoryRepository;
import com.yedam.hr.repository.HrPDFRepository;
import com.yedam.hr.repository.HrSignRepository;
import com.yedam.hr.service.HrService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class HrServiceImpl implements HrService {

	@Autowired EmployeeRepository employeeRepository;
	@Autowired HrSignRepository hrSignRepository;
	@Autowired HrPDFRepository hrPDFRepository;
	@Autowired HrHistoryRepository hrHistoryRepository;
	@Autowired JdbcTemplate jdbcTemplate;

	@PersistenceContext
	  private EntityManager em;

	@Override
	public List<Employee> findByCompanyCode(String companyCode) {
		return employeeRepository.findByCompanyCode(companyCode);
	}

	@Override
	@Transactional
	public void saveContract(EmployeeDTO dto, HrSign sign, HrPDF pdf, MultipartFile signImg, MultipartFile pdfFile,
			MultiValueMap<String, String> params) {

		// 로그인 사용자 아이디 넣기
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String manager = auth.getName().split(":")[0];
		String managerCode = auth.getName().split(":")[2];
		Employee employee = null;
		String signFileName = null;

		// 1. 사원 등록 (없으면 insert)
		dto.setCompanyCode(manager);
		try {
			employee = employeeRepository.save(dto.toEntity());

		} catch (Exception e) {
			e.printStackTrace();
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
			signFileName = uuid + "_" + employee.getEmpCode() + "_" + safeName + ".png";

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
			String pdfFileName = employee.getEmpCode() + "_" + safeName + "_contract.pdf";

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
		sign.setEmpCode(employee.getEmpCode());
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
			history.setEmpCode(employee.getEmpCode()); // Employee에서 EmpCode 그대로
			history.setEventType("등록"); // 등록/수정 구분
			history.setEventDetail("사원 신규 등록 및 계약서 저장");

			history.setManager(managerCode);

			hrHistoryRepository.save(history);

		} catch (Exception e) {
			throw new RuntimeException("이력 저장 중 오류 발생: " + e.getMessage(), e);
		}
	}

	// 단 건 조회
	@Override
	@Transactional(readOnly = true)
	public Employee getEmployee(String companyCode, String  empCode) {
		return employeeRepository.findByCompanyCodeAndEmpCode(companyCode, empCode);
	}

	@Override
	@Transactional
	public void updateEmployee(Employee employee, MultipartFile signImg, MultipartFile pdfFile) {
	    // 로그인 사용자
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String manager = auth.getName().split(":")[2];

	    // 1) 기존 사원 조회
	    Employee existing = employeeRepository.findById(employee.getEmpCode())
	            .orElseThrow(() -> new RuntimeException("해당 사원이 존재하지 않습니다."));

	    // 2) 수정 전 원본 값 보관
	    String  oldName         = existing.getName();
	    String  oldPhone        = existing.getPhone();
	    var     oldBirth        = existing.getBirth();       // LocalDate
	    String  oldEmail        = existing.getEmail();
	    String  oldDept         = existing.getDept();
	    String  oldPosition     = existing.getPosition();
	    String  oldGrade        = existing.getGrade();
	    Integer oldSalary       = existing.getSalary();
	    var     oldHireDate     = existing.getHireDate();
	    var     oldResignDate   = existing.getResignDate();
	    Integer oldHolyDays     = existing.getHolyDays();
	    Integer oldDepCnt       = existing.getDepCnt();
	    String  oldResignReason = existing.getResignReason();
	    String  oldBankCode     = existing.getBankCode();
	    String  oldAccHolder    = existing.getAccHolder();
	    String  oldAccNo        = existing.getAccNo();
	    Integer oldPostalCode   = existing.getPostalCode();
	    String  oldAddress      = existing.getAddress();

	    // 3) 변경 적용 (문자열: null/blank 무시, 숫자/날짜: null만 무시)
	    if (employee.getName()        != null && !employee.getName().trim().isEmpty())        existing.setName(employee.getName().trim());
	    if (employee.getPhone()       != null && !employee.getPhone().trim().isEmpty())       existing.setPhone(employee.getPhone().trim());
	    if (employee.getBirth()       != null)                                                existing.setBirth(employee.getBirth());
	    if (employee.getEmail()       != null && !employee.getEmail().trim().isEmpty())       existing.setEmail(employee.getEmail().trim());
	    if (employee.getDept()        != null && !employee.getDept().trim().isEmpty())        existing.setDept(employee.getDept().trim());
	    if (employee.getPosition()    != null && !employee.getPosition().trim().isEmpty())    existing.setPosition(employee.getPosition().trim());
	    if (employee.getGrade()       != null && !employee.getGrade().trim().isEmpty())       existing.setGrade(employee.getGrade().trim());
	    if (employee.getSalary()      != null)                                                existing.setSalary(employee.getSalary());
	    if (employee.getHireDate()    != null)                                                existing.setHireDate(employee.getHireDate());
	    if (employee.getResignDate()  != null)                                                existing.setResignDate(employee.getResignDate());
	    if (employee.getHolyDays()    != null)                                                existing.setHolyDays(employee.getHolyDays());
	    if (employee.getDepCnt()      != null)                                                existing.setDepCnt(employee.getDepCnt());
	    if (employee.getResignReason()!= null && !employee.getResignReason().trim().isEmpty())existing.setResignReason(employee.getResignReason().trim());
	    if (employee.getBankCode()    != null && !employee.getBankCode().trim().isEmpty())    existing.setBankCode(employee.getBankCode().trim());
	    if (employee.getAccHolder()   != null && !employee.getAccHolder().trim().isEmpty())   existing.setAccHolder(employee.getAccHolder().trim());
	    if (employee.getAccNo()       != null && !employee.getAccNo().trim().isEmpty())       existing.setAccNo(employee.getAccNo().trim());
	    if (employee.getPostalCode()  != null)                                                existing.setPostalCode(employee.getPostalCode());
	    if (employee.getAddress()     != null && !employee.getAddress().trim().isEmpty())     existing.setAddress(employee.getAddress().trim());
	    // companyCode는 유지

	    em.createNativeQuery("begin dbms_session.set_identifier(?); end;")
	      .setParameter(1, manager)
	      .executeUpdate();

	    // 4) 저장
	    employeeRepository.save(existing);

	    // 5) 선택: 서명 이미지 저장
	    HrSign sign = hrSignRepository.findByEmpCode(existing.getEmpCode()).orElse(null);
	    boolean signUpdated = false;

	    if (signImg != null && !signImg.isEmpty()) {
	        try {
	            String uploadDir = System.getProperty("user.dir") + "/uploads/hr/sign/";
	            File dir = new File(uploadDir);
	            if (!dir.exists()) dir.mkdirs();

	            String uuid = UUID.randomUUID().toString().substring(0, 4);
	            String safeName = existing.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
	            String signFileName = uuid + "_" + existing.getEmpCode() + "_" + safeName + ".png";

	            File signFile = new File(uploadDir, signFileName);
	            signImg.transferTo(signFile);

	            String signPath = "/hr/sign/" + signFileName;

	            if (sign == null) {
	                sign = new HrSign();
	            }
	            sign.setCompanyCode(existing.getCompanyCode());
	            sign.setEmpCode(existing.getEmpCode());
	            sign.setEmpName(existing.getName());
	            sign.setEmpDept(existing.getDept());
	            sign.setImg(signPath);
	            sign = hrSignRepository.save(sign);
	            signUpdated = true;
	        } catch (Exception e) {
//	            throw new RuntimeException("서명 이미지 저장 중 오류", e);
	        	System.out.println(e + "서명 저장 실패");
	        }
	    }

	    // 6) 선택: PDF 저장
	    boolean pdfUpdated = false;
	    if (pdfFile != null && !pdfFile.isEmpty()) {
	        try {
	            String uploadDir = System.getProperty("user.dir") + "/uploads/hr/pdf/";
	            File dir = new File(uploadDir);
	            if (!dir.exists()) dir.mkdirs();

	            String safeName = existing.getName().replaceAll("[^a-zA-Z0-9가-힣]", "_");
	            String pdfFileName = existing.getEmpCode() + "_" + safeName + "_contract.pdf";

	            String pdfPath = "/hr/pdf/" + pdfFileName;
	            String outputPath = uploadDir + pdfFileName;

	            pdfFile.transferTo(new File(outputPath));

	            HrPDF pdf = hrPDFRepository.findBySignId(sign != null ? sign.getSignId() : null)
	                    .orElse(new HrPDF());

	            pdf.setSignId(sign != null ? sign.getSignId() : null);
	            pdf.setPdf(pdfPath);
	            hrPDFRepository.save(pdf);
	            pdfUpdated = true;
	        } catch (Exception e) {
//	            throw new RuntimeException("PDF 저장 중 오류", e);
	        	System.out.println(e + "pdf 저장 실패");
	        }
	    }

	    // 7) 변경 내역 문자열 생성(수정 전 → 수정 후) - 문자열은 blank면 비교/로그 스킵
	    StringBuilder detail = new StringBuilder();
	    int changed = 0;

	    if (employee.getName()        != null && !employee.getName().trim().isEmpty() &&
	        !Objects.equals(oldName, employee.getName().trim())) {
	        detail.append("성명 : '").append(String.valueOf(oldName)).append("'→'")
	              .append(employee.getName().trim()).append("'");
	        changed++;
	    }
	    if (employee.getPhone()       != null && !employee.getPhone().trim().isEmpty() &&
	        !Objects.equals(oldPhone, employee.getPhone().trim())) {
	        detail.append("전화 : '").append(String.valueOf(oldPhone)).append("'→'")
	              .append(employee.getPhone().trim()).append("'");
	        changed++;
	    }
	    if (employee.getBirth()       != null && !Objects.equals(oldBirth, employee.getBirth())) {
	        detail.append("생년월일 : '").append(String.valueOf(oldBirth)).append("'→'")
	              .append(String.valueOf(employee.getBirth())).append("'");
	        changed++;
	    }
	    if (employee.getEmail()       != null && !employee.getEmail().trim().isEmpty() &&
	        !Objects.equals(oldEmail, employee.getEmail().trim())) {
	        detail.append("이메일 : '").append(String.valueOf(oldEmail)).append("'→'")
	              .append(employee.getEmail().trim()).append("'");
	        changed++;
	    }
	    if (employee.getDept()        != null && !employee.getDept().trim().isEmpty() &&
	        !Objects.equals(oldDept, employee.getDept().trim())) {
	        detail.append("부서 : '").append(String.valueOf(oldDept)).append("'→'")
	              .append(employee.getDept().trim()).append("'");
	        changed++;
	    }
	    if (employee.getPosition()    != null && !employee.getPosition().trim().isEmpty() &&
	        !Objects.equals(oldPosition, employee.getPosition().trim())) {
	        detail.append("직책 : '").append(String.valueOf(oldPosition)).append("'→'")
	              .append(employee.getPosition().trim()).append("'");
	        changed++;
	    }
	    if (employee.getGrade()       != null && !employee.getGrade().trim().isEmpty() &&
	        !Objects.equals(oldGrade, employee.getGrade().trim())) {
	        detail.append("직급 : '").append(String.valueOf(oldGrade)).append("'→'")
	              .append(employee.getGrade().trim()).append("'");
	        changed++;
	    }
	    if (employee.getSalary()      != null && !Objects.equals(oldSalary, employee.getSalary())) {
	        detail.append("급여 : '").append(String.valueOf(oldSalary)).append("'→'")
	              .append(String.valueOf(employee.getSalary())).append("'");
	        changed++;
	    }
	    if (employee.getHireDate()    != null && !Objects.equals(oldHireDate, employee.getHireDate())) {
	        detail.append("입사일 : '").append(String.valueOf(oldHireDate)).append("'→'")
	              .append(String.valueOf(employee.getHireDate())).append("'");
	        changed++;
	    }
	    if (employee.getResignDate()  != null && !Objects.equals(oldResignDate, employee.getResignDate())) {
	        detail.append("퇴사일 : '").append(String.valueOf(oldResignDate)).append("'→'")
	              .append(String.valueOf(employee.getResignDate())).append("'");
	        changed++;
	    }
	    if (employee.getHolyDays()    != null && !Objects.equals(oldHolyDays, employee.getHolyDays())) {
	        detail.append("연차 : '").append(String.valueOf(oldHolyDays)).append("'→'")
	              .append(String.valueOf(employee.getHolyDays())).append("'");
	        changed++;
	    }
	    if (employee.getDepCnt()      != null && !Objects.equals(oldDepCnt, employee.getDepCnt())) {
	        detail.append("부양가족수 : '").append(String.valueOf(oldDepCnt)).append("'→'")
	              .append(String.valueOf(employee.getDepCnt())).append("'");
	        changed++;
	    }
	    if (employee.getResignReason()!= null && !employee.getResignReason().trim().isEmpty() &&
	        !Objects.equals(oldResignReason, employee.getResignReason().trim())) {
	        detail.append("퇴사사유 : '").append(String.valueOf(oldResignReason)).append("'→'")
	              .append(employee.getResignReason().trim()).append("'");
	        changed++;
	    }
	    if (employee.getBankCode()    != null && !employee.getBankCode().trim().isEmpty() &&
	        !Objects.equals(oldBankCode, employee.getBankCode().trim())) {
	        detail.append("은행 : '").append(String.valueOf(oldBankCode)).append("'→'")
	              .append(employee.getBankCode().trim()).append("'");
	        changed++;
	    }
	    if (employee.getAccHolder()   != null && !employee.getAccHolder().trim().isEmpty() &&
	        !Objects.equals(oldAccHolder, employee.getAccHolder().trim())) {
	        detail.append("예금주 : '").append(String.valueOf(oldAccHolder)).append("'→'")
	              .append(employee.getAccHolder().trim()).append("'");
	        changed++;
	    }
	    if (employee.getAccNo()       != null && !employee.getAccNo().trim().isEmpty() &&
	        !Objects.equals(oldAccNo, employee.getAccNo().trim())) {
	        detail.append("계좌번호 : '").append(String.valueOf(oldAccNo)).append("'→'")
	              .append(employee.getAccNo().trim()).append("'");
	        changed++;
	    }
	    if (employee.getPostalCode()  != null && !Objects.equals(oldPostalCode, employee.getPostalCode())) {
	        detail.append("우편번호 : '").append(String.valueOf(oldPostalCode)).append("'→'")
	              .append(String.valueOf(employee.getPostalCode())).append("'");
	        changed++;
	    }
	    if (employee.getAddress()     != null && !employee.getAddress().trim().isEmpty() &&
	        !Objects.equals(oldAddress, employee.getAddress().trim())) {
	        detail.append("주소 :'").append(String.valueOf(oldAddress)).append("'→'")
	              .append(employee.getAddress().trim()).append("'");
	        changed++;
	    }

	    if (signUpdated) detail.append("서명이미지 : 업데이트됨");
	    if (pdfUpdated)  detail.append("계약서PDF : 업데이트됨");
	    if (changed == 0 && !signUpdated && !pdfUpdated) {
	        detail.append(" (변경 항목 없음)");
	    }

	    em.createNativeQuery("begin pkg_hr_audit.flush; end;").executeUpdate();
	}
}
