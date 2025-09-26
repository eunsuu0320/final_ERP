package com.yedam.common.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@RequiredArgsConstructor
public class PayService {

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    @Value("${kakaopay.cid}")
    private String cid;

    private final RestTemplate restTemplate = new RestTemplate();
    private String tid; // 결제 거래 ID 저장

    // 1) 결제 준비
    public String kakaoPayReady(int amount, String companyName) {
        String url = "https://kapi.kakao.com/v1/payment/ready";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 주문번호 생성 (날짜 + 랜덤 6자리)
        String orderId = "SUB-" 
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) 
                + "-" + UUID.randomUUID().toString().substring(0, 6);

        // 사용자 식별자: 회사명 없으면 임시 UUID
        String userId = (companyName != null && !companyName.isEmpty()) 
                ? companyName 
                : "GUEST-" + UUID.randomUUID().toString().substring(0, 6);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("item_name", "ERP 구독");
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(amount));
        params.add("vat_amount", String.valueOf((int)(amount * 0.1)));
        params.add("approval_url", "http://localhost:8080/pay/kakao/success");
        params.add("cancel_url", "http://localhost:8080/pay/kakao/cancel");
        params.add("fail_url", "http://localhost:8080/pay/kakao/fail");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        tid = (String) response.getBody().get("tid"); // 거래 ID 저장
        return (String) response.getBody().get("next_redirect_pc_url"); // 결제창 URL
    }

    // 2) 결제 승인
    public KakaoPayApproveResponse kakaoPayApprove(String pgToken) {
        String url = "https://kapi.kakao.com/v1/payment/approve";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tid", tid);
        params.add("partner_order_id", "NOT_USED"); // Ready API에서 이미 지정됨
        params.add("partner_user_id", "NOT_USED");
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<KakaoPayApproveResponse> response =
                restTemplate.postForEntity(url, request, KakaoPayApproveResponse.class);

        return response.getBody();
    }
}

