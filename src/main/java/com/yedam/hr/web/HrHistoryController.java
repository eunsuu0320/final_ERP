package com.yedam.hr.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.service.HrHistorySerivce;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HrHistoryController {

    private final HrHistorySerivce hrHistorySerivce;

    // 기존 사용과 호환: page/size 미지정 → 전체 DTO 리스트 반환
    // page/size 지정 → 페이징된 PageDTO 반환
    @GetMapping("/api/history/{companyCode}")
    public Object getHistoryByCompany(@PathVariable String companyCode,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return hrHistorySerivce.findByCompanyCode(companyCode, pageable);
        }
        List<HrHistoryDTO> list = hrHistorySerivce.findByCompanyCode(companyCode);
        return list;
    }
}
