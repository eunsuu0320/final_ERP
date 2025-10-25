package com.yedam.common.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPayService {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.client-key}")
    private String clientKey;

    @Value("${toss.base-url}")
    private String baseUrl;

    @Value("${toss.success-path:/pay/toss/success}")
    private String successPath;

    @Value("${toss.fail-path:/pay/toss/fail}")
    private String failPath;

    /** 프론트 결제창에서 사용할 값 묶어서 전달 (체크아웃 페이지 렌더용) */
    public Map<String, String> buildCheckoutParams(String orderId, String orderName, long amount) {
        String b = baseUrl != null ? baseUrl.trim() : "";
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);

        String sPath = (successPath != null && successPath.startsWith("/")) ? successPath : "/" + successPath;
        String fPath = (failPath    != null && failPath.startsWith("/"))    ? failPath    : "/" + failPath;

        return Map.of(
            "clientKey", clientKey,
            "orderId", orderId,
            "orderName", orderName,
            "amount", String.valueOf(amount),
            "successUrl", b + sPath,
            "failUrl",    b + fPath
        );
    }

    /** 결제 승인(confirm) */
    @SuppressWarnings("unchecked")
    public Map<String, Object> confirm(String paymentKey, String orderId, long amount) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + basic);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "paymentKey", paymentKey,
            "orderId", orderId,
            "amount", amount
        );

        ResponseEntity<Map> res = restTemplate.postForEntity(
            CONFIRM_URL, new HttpEntity<>(body, headers), Map.class);

        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Toss confirm failed: " + res.getStatusCode());
        }
        return res.getBody(); // 필요시 DB 저장에 활용
    }
}
