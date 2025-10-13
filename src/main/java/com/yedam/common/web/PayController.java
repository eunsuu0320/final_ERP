package com.yedam.common.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.Role;
import com.yedam.common.domain.Company;
import com.yedam.common.domain.PayRequestWrapper;
import com.yedam.common.domain.SystemUser;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.repository.RoleRepository;
import com.yedam.common.service.ContractPdfService;
import com.yedam.common.service.KakaoPayService;
import com.yedam.common.service.NaverPayService;
import com.yedam.common.service.PaymentService;
import com.yedam.common.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final NaverPayService naverPayService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final ContractPdfService contractPdfService;
    private final RoleRepository roleRepository;
    
    // 결제 준비 때 저장했다가 성공 콜백에서 사용
    private final Map<String, PayRequestWrapper> payRequestStore = new ConcurrentHashMap<>();

    // 계약서 PDF 치환변수 캐시(다운로드 시 사용)
    private final Map<String, Map<String,String>> contractVarsCache = new ConcurrentHashMap<>();

    // 팝업에서 부모로 postMessage 후 즉시 닫는 HTML (회사코드/마스터ID도 포함)
    private String popupAutoCloseHtml(String type, String status, String orderId, String companyCode, String masterId) {
        String payload = String.format(
            "{type:'%s',status:'%s',orderId:'%s',companyCode:'%s',masterId:'%s'}",
            type,
            status,
            orderId == null ? "" : orderId,
            companyCode == null ? "" : companyCode,
            masterId == null ? "" : masterId
        );
        return "<!doctype html><meta charset='utf-8'><title>closing…</title>"
             + "<script>(function(){try{if(window.opener){window.opener.postMessage("
             + payload
             + ",'*');}}catch(e){} window.close();})();</script>";
    }

    @PostMapping("/ready")
    @ResponseBody
    public Object payReady(@RequestBody PayRequestWrapper wrapper,
                           @AuthenticationPrincipal User user) {
        PayRequest payRequest = wrapper.getPayRequest();
        String userId = (user != null) ? user.getUsername() : "GUEST";
        payRequest.setUserId(userId);

        // orderId 기준으로 결제+회사+유저 보관
        payRequestStore.put(payRequest.getOrderId(), wrapper);

        if ("KAKAO".equalsIgnoreCase(payRequest.getPayMethod())) {
            return kakaoPayService.kakaoPayReady(payRequest);
        } else if ("NAVER".equalsIgnoreCase(payRequest.getPayMethod())) {
            return naverPayService.naverPayReady(payRequest);
        } else {
            throw new IllegalArgumentException("지원하지 않는 결제수단: " + payRequest.getPayMethod());
        }
    }

    // ---------- Kakao ----------

    @GetMapping(value="/kakao/success", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String kakaoPaySuccess(@RequestParam("pg_token") String pgToken,
                                  @RequestParam("orderId") String orderId,
                                  @AuthenticationPrincipal User user) {
        String userId = (user != null) ? user.getUsername() : "GUEST";

        // 1) 승인
        kakaoPayService.kakaoPayApprove(orderId, pgToken, userId);

        // 2) 회사/구독/계정 저장 후 회사코드/마스터ID 준비
        String companyCode = null;
        String masterId = null;

        PayRequestWrapper wrapper = payRequestStore.remove(orderId);
        if (wrapper != null) {
            Company savedCompany = paymentService.saveCompanyInfo(wrapper.getCompanyInfo());
            if (savedCompany != null) companyCode = savedCompany.getCompanyCode();

            paymentService.saveSubscriptionInfo(wrapper.getPayRequest(), companyCode);

            // 마스터 계정 생성
            try {
                SystemUser su = wrapper.getSystemUser();
                if (su != null && su.getUserId() != null && su.getUserPw() != null) {
                	String roleCode = roleRepository
                            .findFirstByCompanyCodeAndRoleName(companyCode, "MASTER")
                            .map(Role::getRoleCode)
                            .orElse("ADMIN"); // 없으면 기본값
                    String empCode = (su.getEmpCode() == null || su.getEmpCode().isBlank())
                                      ? null : su.getEmpCode();

                    userService.createMasterUser(
                        companyCode,
                        su.getUserId(),
                        su.getUserPw(),   // 원문 PW → 서비스에서 해시
                        roleCode,
                        empCode,
                        su.getRemk()
                    );
                    masterId = su.getUserId();
                    
                    // 메일 발송
                    userService.sendWelcomeMail(
                        wrapper.getContactEmail(),
                        companyCode,
                        su.getUserId(),
                        su.getUserPw()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace(); // 결제 성공 자체는 막지 않음
            }

            // PDF는 자동 생성/저장하지 않고, 다운로드용 vars만 캐시에 저장
            Map<String,String> vars = buildContractVars(wrapper, savedCompany);
            contractVarsCache.put(orderId, vars);
        }

        // 3) 부모로 알림(회사코드/마스터ID 포함) + 팝업 닫기
        return popupAutoCloseHtml("KAKAOPAY_RESULT", "success", orderId, companyCode, masterId);
    }

    @GetMapping(value="/kakao/cancel", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String kakaoCancel(@RequestParam(value="orderId", required=false) String orderId) {
        return popupAutoCloseHtml("KAKAOPAY_RESULT", "cancel", orderId, null, null);
    }

    @GetMapping(value="/kakao/fail", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String kakaoFail(@RequestParam(value="orderId", required=false) String orderId) {
        return popupAutoCloseHtml("KAKAOPAY_RESULT", "fail", orderId, null, null);
    }

    // ---------- Naver ----------

    @GetMapping(value="/naver/success", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String naverPaySuccess(@RequestParam("orderId") String orderId) {
        // 1) 승인
        naverPayService.naverPayApprove(orderId);

        // 2) 회사/구독/계정 저장 후 회사코드/마스터ID 준비
        String companyCode = null;
        String masterId = null;

        PayRequestWrapper wrapper = payRequestStore.remove(orderId);
        if (wrapper != null) {
            Company savedCompany = paymentService.saveCompanyInfo(wrapper.getCompanyInfo());
            if (savedCompany != null) companyCode = savedCompany.getCompanyCode();

            paymentService.saveSubscriptionInfo(wrapper.getPayRequest(), companyCode);

            try {
                SystemUser su = wrapper.getSystemUser();
                if (su != null && su.getUserId() != null && su.getUserPw() != null) {
                    String roleCode = (su.getRoleCode() == null || su.getRoleCode().isBlank())
                                      ? "ADMIN" : su.getRoleCode();
                    String empCode = (su.getEmpCode() == null || su.getEmpCode().isBlank())
                                      ? null : su.getEmpCode();

                    userService.createMasterUser(
                        companyCode,
                        su.getUserId(),
                        su.getUserPw(),
                        roleCode,
                        empCode,
                        su.getRemk()
                    );
                    masterId = su.getUserId();
                    
                    // 메일 발송
                    userService.sendWelcomeMail(
                        wrapper.getContactEmail(),
                        companyCode,
                        su.getUserId(),
                        su.getUserPw()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // PDF는 자동 생성/저장하지 않고, 다운로드용 vars만 캐시에 저장
            Map<String,String> vars = buildContractVars(wrapper, savedCompany);
            contractVarsCache.put(orderId, vars);
        }

        // 3) 부모로 알림(회사코드/마스터ID 포함) + 팝업 닫기
        return popupAutoCloseHtml("NAVERPAY_RESULT", "success", orderId, companyCode, masterId);
    }

    @GetMapping(value="/naver/cancel", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String naverCancel(@RequestParam(value="orderId", required=false) String orderId) {
        return popupAutoCloseHtml("NAVERPAY_RESULT", "cancel", orderId, null, null);
    }

    @GetMapping(value="/naver/fail", produces="text/html; charset=UTF-8")
    @ResponseBody
    public String naverFail(@RequestParam(value="orderId", required=false) String orderId) {
        return popupAutoCloseHtml("NAVERPAY_RESULT", "fail", orderId, null, null);
    }

    // ---------- 성공 화면 렌더 (쿼리로 값 전달받아 표시) ----------

    @GetMapping("/complete")
    public String payComplete(
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "buyerName", required = false) String buyerName,
            @RequestParam(value = "total", required = false) Long total,
            @RequestParam(value = "vat", required = false) Long vat,
            @RequestParam(value = "companyCode", required = false) String companyCode, // 추가
            @RequestParam(value = "masterId", required = false) String masterId,       // 추가
            org.springframework.ui.Model model
    ) {
        SuccessViewInfo info = new SuccessViewInfo(
            orderId == null ? "" : orderId,
            buyerName == null ? "" : buyerName,
            new AmountInfo(total == null ? 0L : total, vat == null ? 0L : vat),
            companyCode == null ? "" : companyCode,
            masterId == null ? "" : masterId
        );
        model.addAttribute("info", info);
        return "common/success";
    }

    // ---------- 계약서 다운로드 ----------
    @GetMapping("/contract/download")
    public void downloadContract(@RequestParam("orderId") String orderId,
                                 HttpServletResponse resp) throws Exception {
        Map<String,String> vars = contractVarsCache.get(orderId);
        if (vars == null) {
            resp.sendError(404, "No contract data for this orderId");
            return;
        }
        byte[] pdf = contractPdfService.generateBytesFromTemplate("common/contract-pdf.html", vars);

        String fileName = ("contract_" + vars.getOrDefault("companyCode","") + "_" + orderId + ".pdf")
                .replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLength(pdf.length);
        try (var os = resp.getOutputStream()) {
            os.write(pdf);
            os.flush();
        }
        // 원하면 1회용으로 제거
        // contractVarsCache.remove(orderId);
    }
    
    // ===== 뷰 DTO =====
    public static class SuccessViewInfo {
        private final String partnerOrderId;
        private final String buyerName;
        private final AmountInfo amount;
        private final String companyCode; // 추가
        private final String masterId;    // 추가

        public SuccessViewInfo(String partnerOrderId, String buyerName, AmountInfo amount,
                               String companyCode, String masterId) {
            this.partnerOrderId = partnerOrderId;
            this.buyerName = buyerName;
            this.amount = amount;
            this.companyCode = companyCode;
            this.masterId = masterId;
        }
        public String getPartnerOrderId() { return partnerOrderId; }
        public String getBuyerName() { return buyerName; }
        public AmountInfo getAmount() { return amount; }
        public String getCompanyCode() { return companyCode; }
        public String getMasterId() { return masterId; }
    }

    public static class AmountInfo {
        private final Long total;
        private final Long vat;
        public AmountInfo(Long total, Long vat) { this.total = total; this.vat = vat; }
        public Long getTotal() { return total; }
        public Long getVat() { return vat; }
    }

    // ---------- 내부 유틸: PDF 치환 변수 빌드 ----------
    private Map<String, String> buildContractVars(PayRequestWrapper wrapper, Company savedCompany) {
        var pay = wrapper.getPayRequest();
        var com = wrapper.getCompanyInfo();

        LocalDate start = LocalDate.now();
        int months = 1;
        try {
            if (pay.getSubPeriod() != null && pay.getSubPeriod().contains("개월")) {
                months = Integer.parseInt(pay.getSubPeriod().replace("개월","").trim());
            }
        } catch (Exception ignore) {}
        LocalDate end = start.plusMonths(months);

        long total = (long) pay.getAmount();
        long vat   = Math.round(total * 0.1);

        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

        Map<String, String> vars = new HashMap<>();
        vars.put("contractDate", df.format(start));
        vars.put("companyName",  nvl(com.getCompanyName()));
        vars.put("ceoName",      nvl(com.getCeoName()));
        vars.put("bizRegNo",     nvl(com.getBizRegNo()));
        vars.put("fullAddress",  nvl(com.getRoadAddress()) + " " + nvl(com.getAddressDetail()));
        vars.put("tel",          nvl(com.getTel()));

        vars.put("subPeriod",    nvl(pay.getSubPeriod()));
        vars.put("startDate",    df.format(start));
        vars.put("endDate",      df.format(end));

        vars.put("total",        String.format("%,d", total));
        vars.put("vat",          String.format("%,d",  vat));
        vars.put("orderId",      nvl(pay.getOrderId()));
        vars.put("buyerName",    nvl(pay.getBuyerName()));

        vars.put("signatureDataUrl", nvl(wrapper.getSignatureDataUrl()));
        vars.put("companyCode", nvl(savedCompany.getCompanyCode()));
        return vars;
    }

    private static String nvl(String s) { return (s == null) ? "" : s; }
}
