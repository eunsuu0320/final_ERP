package com.yedam.common.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.PayRequestWrapper;
import com.yedam.common.domain.payment.KakaoApproveResponse;
import com.yedam.common.domain.payment.NaverApproveResponse;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.service.KakaoPayService;
import com.yedam.common.service.NaverPayService;
import com.yedam.common.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final NaverPayService naverPayService;
    private final PaymentService paymentService;

    // orderId -> Wrapper 저장 (결제 준비 시 저장)
    private final Map<String, PayRequestWrapper> payRequestStore = new ConcurrentHashMap<>();

    /**
     * 결제 준비 API
     */
    @PostMapping("/ready")
    @ResponseBody
    public Object payReady(
            @RequestBody PayRequestWrapper wrapper,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        PayRequest payRequest = wrapper.getPayRequest();
        Company companyInfo = wrapper.getCompanyInfo();

        String userId = (user != null) ? user.getUsername() : "GUEST";
        payRequest.setUserId(userId);

        // 결제 준비 시 orderId 기준으로 PayRequest + Company 보관
        payRequestStore.put(payRequest.getOrderId(), wrapper);

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

        // PayRequest + Company 꺼내서 COMPANY 저장
        PayRequestWrapper wrapper = payRequestStore.remove(orderId);
        if (wrapper != null) {
        	// 1. 회사 저장
            Company savedCompany = paymentService.saveCompanyInfo(wrapper.getCompanyInfo());

            // 2. 구독 저장
            paymentService.saveSubscriptionInfo(wrapper.getPayRequest(), savedCompany.getCompanyCode());
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

        // PayRequest + Company 꺼내서 COMPANY 저장
        PayRequestWrapper wrapper = payRequestStore.remove(orderId);
        if (wrapper != null) {
            paymentService.saveCompanyInfo(wrapper.getCompanyInfo());
        }

        model.addAttribute("info", response);
        return "common/success";
    }

    @GetMapping("/naver/cancel")
    public String naverCancel() { return "payment/cancel"; }

    @GetMapping("/naver/fail")
    public String naverFail() { return "payment/fail"; }
}
