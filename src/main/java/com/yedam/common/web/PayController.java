package com.yedam.common.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.yedam.common.domain.payment.KakaoApproveResponse;
import com.yedam.common.domain.payment.NaverApproveResponse;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.service.KakaoPayService;
import com.yedam.common.service.NaverPayService;
import com.yedam.common.service.PaymentService;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final NaverPayService naverPayService;
    private final PaymentService paymentService;

    // orderId -> PayRequest 임시 저장소 (결제 준비 시 저장)
    private final Map<String, PayRequest> payRequestStore = new ConcurrentHashMap<>();

    /** 
     * 공통 결제 준비 API
     */
    @PostMapping("/ready")
    @ResponseBody
    public Object payReady(
            @RequestBody PayRequest payRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        String userId = (user != null) ? user.getUsername() : "GUEST";
        payRequest.setUserId(userId);

        // ✅ 결제 준비 시 orderId 기준으로 PayRequest 보관
        payRequestStore.put(payRequest.getOrderId(), payRequest);

        if ("KAKAO".equalsIgnoreCase(payRequest.getPayMethod())) {
            return kakaoPayService.kakaoPayReady(payRequest);
        } else if ("NAVER".equalsIgnoreCase(payRequest.getPayMethod())) {
            return naverPayService.naverPayReady(payRequest);
        } else {
            throw new IllegalArgumentException("지원하지 않는 결제수단: " + payRequest.getPayMethod());
        }
    }

    /** 
     * 카카오페이 성공 콜백
     */
    @GetMapping("/kakao/success")
    public String kakaoPaySuccess(
            @RequestParam("pg_token") String pgToken,
            @RequestParam("orderId") String orderId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            Model model
    ) {
        String userId = (user != null) ? user.getUsername() : "GUEST";
        KakaoApproveResponse response = kakaoPayService.kakaoPayApprove(orderId, pgToken, userId);

        // ✅ PayRequest 꺼내서 COMPANY 저장
        PayRequest request = payRequestStore.remove(orderId);
        if (request != null) {
            paymentService.saveCompanyInfo(request);
        }

        model.addAttribute("info", response);
        return "common/success";
    }

    @GetMapping("/kakao/cancel")
    public String kakaoCancel() { return "payment/cancel"; }

    @GetMapping("/kakao/fail")
    public String kakaoFail() { return "payment/fail"; }

    /** 
     * 네이버페이 성공 콜백
     */
    @GetMapping("/naver/success")
    public String naverPaySuccess(
            @RequestParam("orderId") String orderId,
            Model model
    ) {
        NaverApproveResponse response = naverPayService.naverPayApprove(orderId);

        // PayRequest 꺼내서 COMPANY 저장
        PayRequest request = payRequestStore.remove(orderId);
        if (request != null) {
            paymentService.saveCompanyInfo(request);
        }

        model.addAttribute("info", response);
        return "common/success";
    }

    @GetMapping("/naver/cancel")
    public String naverCancel() { return "payment/cancel"; }

    @GetMapping("/naver/fail")
    public String naverFail() { return "payment/fail"; }
}
