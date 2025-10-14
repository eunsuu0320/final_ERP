package com.yedam.hr.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.yedam.hr.service.PayrollService;

@Controller
public class PayrollController {

	@Autowired PayrollService payrollService;

	// 요청 바디 DTO
    public static class CalcReq {
        public String salaryId;
        public String companyCode; // 미전달 시 Authentication에서 파싱해 사용 가능
    }

    @PostMapping("/api/calculate")
    public ResponseEntity<?> calculate(@RequestBody CalcReq req, Authentication auth) {
        // companyCode가 없으면 로그인 사용자에서 추출(예: "COMP01:userid" 형태를 쓰는 프로젝트 패턴)
        String companyCode = (req.companyCode != null && !req.companyCode.isBlank())
                ? req.companyCode
                : (auth != null && auth.getName() != null && auth.getName().contains(":")
                    ? auth.getName().split(":")[0]
                    : null);

        payrollService.runSalaryCalc(companyCode, req.salaryId);
        return ResponseEntity.ok().body("{\"status\":\"OK\"}");
    }
}
