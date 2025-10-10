package com.yedam.hr.service;

import java.util.Date;
import java.util.List;

import com.yedam.hr.domain.CommuteList;

public interface CommuteListService {

	// 출근 등록
	CommuteList insertCommute(CommuteList commuteList);

	// 회사코드별 출퇴근 목록 조회
	List<CommuteList> getCommuteLists(String companyCode);

    // === 추가: 동일 날짜 매칭으로 퇴근/근무시간 갱신 ===
    int punchOutByDate(String companyCode, String empCode, Date offTime);
}
