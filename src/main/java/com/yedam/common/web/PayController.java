package com.yedam.common.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.domain.payment.KakaoApproveResponse;
import com.yedam.common.domain.payment.KakaoReadyResponse;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.service.KakaoPayService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/kakao/ready")
    @ResponseBody // 여기서는 JSON 응답이 필요하니 @ResponseBody 붙임
    public KakaoReadyResponse kakaoPayReady(
            @RequestBody PayRequest payRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
    	String userId = (user != null) ? user.getUsername() : "GUEST";
        payRequest.setUserId(userId);
        
        return kakaoPayService.kakaoPayReady(payRequest);
    }

    @GetMapping("/kakao/success")
    public String kakaoPaySuccess(
            @RequestParam("pg_token") String pgToken,
            @RequestParam("orderId") String orderId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            Model model
    ) {
        // 로그인 안 한 경우 대비
        String userId = (user != null) ? user.getUsername() : "GUEST";

        KakaoApproveResponse response = kakaoPayService.kakaoPayApprove(orderId, pgToken, userId);
        model.addAttribute("info", response);

        return "index";
    }


    @GetMapping("/kakao/cancel")
    public String kakaoCancel() {
        return "payment/cancel";
    }

    @GetMapping("/kakao/fail")
    public String kakaoFail() {
        return "payment/fail";
    }
}

