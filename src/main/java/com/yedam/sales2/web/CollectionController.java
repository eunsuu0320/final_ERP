package com.yedam.sales2.web;

import java.util.Date; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    	 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String companyCode = auth.getName().split(":")[0]; // 로그인 사용자 기준으로 나중에 동적 변경 가능
        List<Map<String, Object>> list = collectionService.getReceivableSummary(companyCode);
        System.out.println("📊 미수금 조회 결과: " + list);
        return list;
    }
    
    // 수금등록
    @PostMapping("/api/collection/insert")
    @ResponseBody
    public Map<String, Object> insertCollection(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String companyCode = auth.getName().split(":")[0];

            // JS에서 받은 값 꺼내기
            String partnerCode = (String) request.get("partnerCode");
            Double recpt = Double.valueOf(request.get("recpt").toString());
            String paymentMethods = (String) request.get("paymentMethods");
            String remk = (String) request.get("remk");

            // 프로시저 호출 (FIFO 방식 수금처리)
            collectionService.executeCollectionFifo(partnerCode, recpt, paymentMethods, remk, companyCode);

            result.put("success", true);
            result.put("message", "수금 FIFO 처리 완료");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "수금 등록 중 오류 발생: " + e.getMessage());
        }

        return result;
    }

 // 로그인 사원명 조회
    @GetMapping("/api/collection/current-employee")
    @ResponseBody
    public Map<String, String> getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 예: auth.getName() → "A001:emp001" 형태라면
        String[] userInfo = auth.getName().split(":");
        String companyCode = userInfo[0];
        String empName = userInfo.length > 1 ? userInfo[1] : "로그인사용자";

        Map<String, String> result = new HashMap<>();
        result.put("companyCode", companyCode);
        result.put("empName", empName);
        return result;
    }
  
}
