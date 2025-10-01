	package com.yedam.hr.service;

	import java.util.List;

	import com.yedam.hr.domain.EmpAttendance;

	public interface EmpAttendanceService {

		// 회사코드별 직원 근태 조회
		List<EmpAttendance> getEmpAttendances(String companyCode);
	}
