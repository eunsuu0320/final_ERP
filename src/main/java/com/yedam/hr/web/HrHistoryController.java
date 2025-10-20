package com.yedam.hr.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.service.HrHistorySerivce;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HrHistoryController {

    private final HrHistorySerivce hrHistorySerivce;

    // 회사코드 기준 전체 이력 DTO 리스트 반환
    @GetMapping("/api/history/{companyCode}")
    public List<HrHistoryDTO> getHistoryByCompany(@PathVariable String companyCode) {
        return hrHistorySerivce.findByCompanyCode(companyCode);
    }
}
