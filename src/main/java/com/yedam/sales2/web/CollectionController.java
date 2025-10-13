package com.yedam.sales2.web;

import java.util.Date; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.CollectionEntity;
import com.yedam.sales2.service.CollectionService;

/*
 * 수금관리
 */

@Controller
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    // 수금관리 메인 HTML
    @GetMapping("collection")
    public String collection() {
        return "sales2/collection";
    }

    // 거래처별 미수금 목록
    @GetMapping("/api/receivable/list")
    @ResponseBody
    public List<Map<String, Object>> getReceivableList() {
        String companyCode = "A001"; // 로그인 사용자 기준으로 나중에 동적 변경 가능
        List<Map<String, Object>> list = collectionService.getReceivableSummary(companyCode);
        System.out.println("📊 미수금 조회 결과: " + list);
        return list;
    }
    
    // 수금등록
    @PostMapping("/api/collection/insertCollectionModal")
    @ResponseBody
    public Map<String, Object> insertCollection(@RequestBody CollectionEntity dto) {
        Map<String, Object> result = new HashMap<>();
        try {
            dto.setMoneyCode(UUID.randomUUID().toString().substring(0,8)); // 수금코드 자동생성
            dto.setMoneyDate(new Date());  // 수금일자 자동입력
            dto.setCompanyCode("A001");

            collectionService.insertCollection(dto);

            result.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
