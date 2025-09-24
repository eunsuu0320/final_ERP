package com.yedam.hr.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	public void saveContract(Employee employee, HrSign sign, HrPDF pdf, MultipartFile signImg) {
		// 1. 사원 등록 (없으면 insert)
		employee.setCompanyCode("C001");
		try {
			if (!employeeRepository.existsById(employee.getEmpNo())) {
				employeeRepository.save(employee);
			}
		} catch (Exception e) {
			throw new RuntimeException("사원 저장 중 오류 발생", e);
		}

		String signPath = null;
		try {
			// 2. 서명 이미지 저장 (static/hr/sign/)
			String signDir = "src/main/resources/static/hr/sign/";
			File signDirFile = new File(signDir);
			if (!signDirFile.exists())
				signDirFile.mkdirs();

			// UUID 앞 4자리 + 사원번호 + 이름
			String uuid = UUID.randomUUID().toString().substring(0, 4);
			String signFileName = uuid + "_" + employee.getEmpNo() + "_" + employee.getName() + ".png";

			File signFile = new File(signDir, signFileName);
			signImg.transferTo(signFile);

			// DB에는 static 이후 경로 저장
			signPath = "/hr/sign/" + signFileName;
		} catch (Exception e) {
			throw new RuntimeException("서명 이미지 저장 중 오류 발생", e);
		}

		// 3. 서명 이미지 작성 중인 pdf에 삽입
		String pdfPath = null;
		try {
			// 3. iframe에서 편집된 PDF (임시 저장본)
			String inputPdf = "src/main/resources/static/hr/temp/contract_current.pdf";

			// 3-1. 최종 PDF 저장 경로 (static/hr/pdf/)
			String pdfDir = "src/main/resources/static/hr/pdf/";
			File pdfDirFile = new File(pdfDir);
			if (!pdfDirFile.exists())
				pdfDirFile.mkdirs();

			String pdfFileName = employee.getEmpNo() + employee.getName() + "_contract.pdf";
			pdfPath = "/hr/pdf/" + pdfFileName; // DB 저장용 경로 (웹 접근용)
			String outputPath = pdfDir + pdfFileName; // 실제 저장 경로

			// 3-2. PDF 복사 및 열기
			PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputPdf), new PdfWriter(outputPath));
			Document document = new Document(pdfDoc);

			// 4. 서명 이미지 삽입 (위치 좌표 지정 가능)
			String realSignPath = "src/main/resources/static" + signPath; // 실제 파일 경로
			ImageData imageData = ImageDataFactory.create(realSignPath);
			Image image = new Image(imageData);
			image.setFixedPosition(1, 450, 95); // (page=1, x=400, y=150)
			image.scaleToFit(120, 60);

			document.add(image);

			document.close();
			pdfDoc.close();

		} catch (Exception e) {
			throw new RuntimeException("PDF 사본 저장/서명 삽입 중 오류 발생", e);
		}

		// 4. 근로계약서 테이블, pdf 테이블에 저장
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

	@Override
	public int saveEmp(Employee employee) {
		employee.setCompanyCode("C001");
		employeeRepository.save(employee);
		return 1;
	}
}
