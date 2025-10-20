package com.yedam.hr.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yedam.hr.dto.HrHistoryDTO;

public interface HrHistorySerivce {
    // 기존 그대로
    List<HrHistoryDTO> findByCompanyCode(String companyCode);
}
