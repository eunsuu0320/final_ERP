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
    private final Map<String, String> orderBuyerMap = new ConcurrentHashMap<>();

    private static final String HOST = "https://kapi.kakao.com";
    private static final String ADMIN_KEY = "3c4d225c4cfafacf87d4c8cce4342a24"; // 환경변수/설정파일로 분리 권장

    // 환경/프로파일별로 분기하세요. 지금은 로컬 기준.
    private static final String BASE_URL = "http://localhost:8080";

    public KakaoReadyResponse kakaoPayReady(PayRequest payRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + ADMIN_KEY);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", payRequest.getOrderId());
        params.add("partner_user_id", payRequest.getUserId());
        params.add("item_name", payRequest.getItemName());
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(payRequest.getAmount()));
        params.add("tax_free_amount", "0");

        // 반드시 orderId를 세 URL 모두에 포함시킨다
        String orderQS = "?orderId=" + payRequest.getOrderId();
        params.add("approval_url", BASE_URL + "/pay/kakao/success" + orderQS);
        params.add("cancel_url",   BASE_URL + "/pay/kakao/cancel"  + orderQS);
        params.add("fail_url",     BASE_URL + "/pay/kakao/fail"    + orderQS);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        KakaoReadyResponse response = restTemplate.postForObject(
                HOST + "/v1/payment/ready", request, KakaoReadyResponse.class);

        if (response != null && response.getTid() != null) {
            orderTidMap.put(payRequest.getOrderId(), response.getTid());
            orderBuyerMap.put(payRequest.getOrderId(), payRequest.getBuyerName());
        } else {
            throw new IllegalStateException("카카오페이 Ready 응답에 tid가 없습니다.");
        }
        return response;
    }

    public KakaoApproveResponse kakaoPayApprove(String orderId, String pgToken, String userId) {
        String tid = orderTidMap.get(orderId);
        String buyerName = orderBuyerMap.get(orderId);
        if (tid == null) throw new IllegalStateException("tid가 존재하지 않습니다. orderId=" + orderId);

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
        KakaoApproveResponse response = restTemplate.postForObject(
                HOST + "/v1/payment/approve", request, KakaoApproveResponse.class);

        if (response != null) response.setBuyerName(buyerName);

        // 메모리 누수 방지: 승인 후 즉시 정리
        orderTidMap.remove(orderId);
        orderBuyerMap.remove(orderId);
        return response;
    }
}
