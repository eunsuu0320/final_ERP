package com.yedam.common.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.yedam.common.domain.payment.NaverApproveResponse;
import com.yedam.common.domain.payment.NaverReadyResponse;
import com.yedam.common.domain.payment.PayRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NaverPayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> orderMap = new ConcurrentHashMap<>();

    private static final String HOST = "https://dev.apis.naver.com"; // ✅ 네이버 테스트 환경
    private static final String CLIENT_ID = "{YOUR_NAVER_CLIENT_ID}";
    private static final String CLIENT_SECRET = "{YOUR_NAVER_CLIENT_SECRET}";
    private static final String MERCHANT_ID = "{YOUR_MERCHANT_ID}"; // 가맹점 ID

    /**
     * 네이버페이 결제 준비
     */
    public NaverReadyResponse naverPayReady(PayRequest payRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", CLIENT_ID);
        headers.add("X-Naver-Client-Secret", CLIENT_SECRET);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("merchantId", MERCHANT_ID);
        params.add("merchantUserKey", payRequest.getUserId());
        params.add("merchantPayKey", payRequest.getOrderId());
        params.add("productName", payRequest.getItemName());
        params.add("totalPayAmount", String.valueOf(payRequest.getAmount()));
        params.add("taxScopeAmount", String.valueOf(payRequest.getAmount())); 
        params.add("taxExScopeAmount", "0");

        // 콜백 URL
        params.add("returnUrl", "http://localhost:8080/pay/naver/success?orderId=" + payRequest.getOrderId());
        params.add("cancelUrl", "http://localhost:8080/pay/naver/cancel");
        params.add("failUrl", "http://localhost:8080/pay/naver/fail");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        NaverReadyResponse response = restTemplate.postForObject(
                HOST + "/naverpay/payments/v2.2/apply/payment", // ✅ 테스트용 API 엔드포인트
                request,
                NaverReadyResponse.class
        );

        orderMap.put(payRequest.getOrderId(), response.getPaymentId());
        return response;
    }

    /**
     * 네이버페이 결제 승인
     */
    public NaverApproveResponse naverPayApprove(String orderId) {
        String paymentId = orderMap.get(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", CLIENT_ID);
        headers.add("X-Naver-Client-Secret", CLIENT_SECRET);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("paymentId", paymentId);
        params.add("merchantId", MERCHANT_ID);
        params.add("merchantPayKey", orderId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(
                HOST + "/naverpay/payments/v2.2/approve",
                request,
                NaverApproveResponse.class
        );
    }
}
