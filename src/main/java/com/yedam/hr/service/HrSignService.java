package com.yedam.hr.service;

import java.util.Optional;

import com.yedam.hr.domain.HrSign;

public interface HrSignService {

	 // empNo 기준으로 1건 찾기
    Optional<HrSign> findByEmpNo(String empCode);

}
