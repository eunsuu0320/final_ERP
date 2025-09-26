package com.yedam.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    // 결제 준비 (회사명 + 금액 받아서 호출)
    @PostMapping("/kakao")
    public String kakaoPayReady(@RequestParam int amount,
                                @RequestParam(required = false) String companyName) {
        String redirectUrl = payService.kakaoPayReady(amount, companyName);
        return "redirect:" + redirectUrl; // 카카오페이 결제창으로 이동
    }

    @GetMapping("/kakao/success")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pgToken, Model model) {
        KakaoPayApproveResponse response = payService.kakaoPayApprove(pgToken);
        model.addAttribute("info", response);
        return "paySuccess";
    }

    @GetMapping("/kakao/cancel")
    public String kakaoPayCancel() {
        return "payCancel";
    }

    @GetMapping("/kakao/fail")
    public String kakaoPayFail() {
        return "payFail";
    }
}

