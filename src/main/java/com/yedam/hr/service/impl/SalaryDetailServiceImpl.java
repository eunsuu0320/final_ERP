package com.yedam.hr.service.impl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.Allowance;
import com.yedam.hr.domain.Dedcut;
import com.yedam.hr.domain.Employee;
import com.yedam.hr.domain.SalaryDetail;
import com.yedam.hr.domain.SalaryMaster;
import com.yedam.hr.repository.AllowanceRepository;
import com.yedam.hr.repository.DedcutRepository;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.SalaryDetailRepository;
import com.yedam.hr.service.SalaryDetailService;
import com.yedam.hr.service.SalaryMasterService;

@Service
public class SalaryDetailServiceImpl implements SalaryDetailService {

	@Autowired
	SalaryMasterService salaryMasterService;
	@Autowired
	SalaryDetailRepository salaryDetailRepository;
	@Autowired
	EmployeeRepository employeeRepository; // 사원
	@Autowired
	AllowanceRepository allowanceRepository; // 수당
	@Autowired
	DedcutRepository dedcutRepository; // 공제

	@Override
	public List<SalaryDetail> getSalaryDetails(String companyCode) {
		return salaryDetailRepository.findByCompanyCode(companyCode);
	}

	@Override
	public Map<String, Object> getSalaryDetailBundle(String companyCode, String salaryId) {
		// 1) SalaryDetail 전체 조회
		List<SalaryDetail> details = salaryDetailRepository.findByCompanyCodeAndSalaryId(companyCode, salaryId);

		details.sort(
			    java.util.Comparator.comparing(
			        sd -> {
			            String c = sd.getEmpCode();
			            return c == null ? "" : c;
			        },
			        String.CASE_INSENSITIVE_ORDER
			    )
			);

		// 2) 합계 변수(long)
		long all01 = 0, all02 = 0, all03 = 0, all04 = 0, all05 = 0, all06 = 0, all07 = 0, all08 = 0, all09 = 0,
				all10 = 0;
		long ded01 = 0, ded02 = 0, ded03 = 0, ded04 = 0, ded05 = 0, ded06 = 0, ded07 = 0, ded08 = 0, ded09 = 0,
				ded10 = 0;
		long allTotalSum = 0, dedTotalSum = 0, netPaySum = 0;

		// 3) 누적
		for (SalaryDetail sd : details) {
			all01 += sd.getAll01();
			all02 += sd.getAll02();
			all03 += sd.getAll03();
			all04 += sd.getAll04();
			all05 += sd.getAll05();
			all06 += sd.getAll06();
			all07 += sd.getAll07();
			all08 += sd.getAll08();
			all09 += sd.getAll09();
			all10 += sd.getAll10();

			ded01 += sd.getDed01();
			ded02 += sd.getDed02();
			ded03 += sd.getDed03();
			ded04 += sd.getDed04();
			ded05 += sd.getDed05();
			ded06 += sd.getDed06();
			ded07 += sd.getDed07();
			ded08 += sd.getDed08();
			ded09 += sd.getDed09();
			ded10 += sd.getDed10();

			allTotalSum += sd.getAllTotal();
			dedTotalSum += sd.getDedTotal();
			netPaySum += sd.getNetPay();
		}

		// 4) 합계 맵
		Map<String, Long> allSums = new LinkedHashMap<>();
		allSums.put("ALL_01", all01);
		allSums.put("ALL_02", all02);
		allSums.put("ALL_03", all03);
		allSums.put("ALL_04", all04);
		allSums.put("ALL_05", all05);
		allSums.put("ALL_06", all06);
		allSums.put("ALL_07", all07);
		allSums.put("ALL_08", all08);
		allSums.put("ALL_09", all09);
		allSums.put("ALL_10", all10);

		Map<String, Long> dedSums = new LinkedHashMap<>();
		dedSums.put("DED_01", ded01);
		dedSums.put("DED_02", ded02);
		dedSums.put("DED_03", ded03);
		dedSums.put("DED_04", ded04);
		dedSums.put("DED_05", ded05);
		dedSums.put("DED_06", ded06);
		dedSums.put("DED_07", ded07);
		dedSums.put("DED_08", ded08);
		dedSums.put("DED_09", ded09);
		dedSums.put("DED_10", ded10);

		// 5) 마스터에서 쓸 거 가져오기
		SalaryMaster sm = salaryMasterService.findByCompanyCodeAndSalaryId(companyCode, salaryId);
		String payYm = sm.getPayYm();
		String payName = sm.getPayName();
		Double payTotal = sm.getPayTotal();
		LocalDate payDate = sm.getPayDate();

		// 6) 수당/공제 라벨(Map<String,String>) 구성: 키는 "ALL_01" ~ "ALL_10", "DED_01" ~
		// "DED_10"
		Map<String, String> allLabels = new LinkedHashMap<>();
		Map<String, String> dedLabels = new LinkedHashMap<>();
		allLabels.put("ALL_01", null);
		allLabels.put("ALL_02", null);
		allLabels.put("ALL_03", null);
		allLabels.put("ALL_04", null);
		allLabels.put("ALL_05", null);
		allLabels.put("ALL_06", null);
		allLabels.put("ALL_07", null);
		allLabels.put("ALL_08", null);
		allLabels.put("ALL_09", null);
		allLabels.put("ALL_10", null);
		dedLabels.put("DED_01", null);
		dedLabels.put("DED_02", null);
		dedLabels.put("DED_03", null);
		dedLabels.put("DED_04", null);
		dedLabels.put("DED_05", null);
		dedLabels.put("DED_06", null);
		dedLabels.put("DED_07", null);
		dedLabels.put("DED_08", null);
		dedLabels.put("DED_09", null);
		dedLabels.put("DED_10", null);

		// 회사별 수당(ALL_IS='Y') → MAP_NUM 위치에 이름 주입
		List<Allowance> allowList = allowanceRepository.findActiveByCompanyCode(companyCode, "Y");
		for (Allowance a : allowList) {
			Integer n = a.getMapNum();
			if (n != null && n >= 1 && n <= 10) {
				String key = (n < 10) ? "ALL_0" + n : "ALL_" + n;
				allLabels.put(key, a.getAllName()); // 예: 1→"식대", 2→"연장수당"
			}
		}

		// 회사별 공제(DED_IS='Y') → MAP_NUM 위치에 이름 주입
		List<Dedcut> dedList = dedcutRepository.findActiveByCompanyCode(companyCode, "Y");
		for (Dedcut d : dedList) {
			Integer n = d.getMapNum();
			if (n != null && n >= 1 && n <= 10) {
				String key = (n < 10) ? "DED_0" + n : "DED_" + n;
				dedLabels.put(key, d.getDedName()); // 예: 1→"소득세", 2→"지방소득세" 등
			}
		}

		// 사원 가져오기
		// 1) details에서 empCode 모으기
		Set<String> empCodes = details.stream().map(SalaryDetail::getEmpCode)
				.filter(code -> code != null && !code.isBlank()).collect(Collectors.toSet());

		// 2) 레포지토리에 일괄 조회 메서드가 있으면 활용
		List<Employee> empList = empCodes.isEmpty() ? List.of()
				: employeeRepository.findByCompanyCodeAndEmpCodeIn(companyCode, empCodes);

		// 3) empCode → Employee 맵
		Map<String, Employee> empMap = empList.stream()
				.collect(Collectors.toMap(Employee::getEmpCode, Function.identity(), (a, b) -> a, LinkedHashMap::new));

		// ==== 기본급 총합 ====
		long basePaySum = 0L;
		for (Employee e : empList) {
			Number sal = e.getSalary(); // Integer/Long OK
			if (sal != null) {
				basePaySum += sal.longValue();
			}
		}

		allTotalSum += basePaySum; // 수당합계금에 기본급 추가

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("companyCode", companyCode);
		result.put("salaryId", salaryId);
		result.put("payYm", payYm);
		result.put("payName", payName);
		result.put("payTotal", payTotal);
		result.put("payDate", payDate);
		result.put("details", details);
		result.put("allSums", allSums);
		result.put("dedSums", dedSums);
		result.put("allTotalSum", allTotalSum);
		result.put("dedTotalSum", dedTotalSum);
		result.put("netPaySum", netPaySum);

		result.put("allLabels", allLabels);
		result.put("dedLabels", dedLabels);

		result.put("employees", empMap);
		result.put("basePaySum", basePaySum);
		return result;
	}
}
