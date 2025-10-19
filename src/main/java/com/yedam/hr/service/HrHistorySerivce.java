package com.yedam.hr.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yedam.hr.dto.HrHistoryDTO;

public interface HrHistorySerivce {
    // 기존 그대로
    List<HrHistoryDTO> findByCompanyCode(String companyCode);

    // 추가(선택): 페이지네이션 버전
    Page<HrHistoryDTO> findByCompanyCode(String companyCode, Pageable pageable);
}
