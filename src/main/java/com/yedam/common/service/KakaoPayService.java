package com.yedam.common.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.yedam.common.domain.payment.KakaoApproveResponse;
import com.yedam.common.domain.payment.KakaoReadyResponse;
import com.yedam.common.domain.payment.PayRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> orderTidMap = new ConcurrentHashMap<>();

    private static final String HOST = "https://kapi.kakao.com";
    private static final String ADMIN_KEY = "3c4d225c4cfafacf87d4c8cce4342a24";

    public KakaoReadyResponse kakaoPayReady(PayRequest payRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + ADMIN_KEY);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME"); // 테스트 CID
        params.add("partner_order_id", payRequest.getOrderId());
        params.add("partner_user_id", payRequest.getUserId());
        params.add("item_name", payRequest.getItemName());
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(payRequest.getAmount()));
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://localhost:8080/pay/kakao/success?orderId=" + payRequest.getOrderId());
        params.add("cancel_url", "http://localhost:8080/pay/kakao/cancel");
        params.add("fail_url", "http://localhost:8080/pay/kakao/fail");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        KakaoReadyResponse response = restTemplate.postForObject(
                HOST + "/v1/payment/ready",
                request,
                KakaoReadyResponse.class
        );

        orderTidMap.put(payRequest.getOrderId(), response.getTid()); // tid 저장
        return response;
    }

    public KakaoApproveResponse kakaoPayApprove(String orderId, String pgToken, String userId) {
        String tid = orderTidMap.get(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + ADMIN_KEY);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id", userId);
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(
                HOST + "/v1/payment/approve",
                request,
                KakaoApproveResponse.class
        );
    }
}

