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
    private final Map<String, String> orderBuyerMap = new ConcurrentHashMap<>(); // ⭐ buyerName 저장

    private static final String HOST = "https://kapi.kakao.com";
    private static final String ADMIN_KEY = "3c4d225c4cfafacf87d4c8cce4342a24"; // ✅ 실제 Admin Key 넣기

    /**
     * 카카오페이 결제 준비
     */
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

        // ✅ orderId - tid / buyerName 매핑 저장
        if (response != null && response.getTid() != null) {
            orderTidMap.put(payRequest.getOrderId(), response.getTid());
            orderBuyerMap.put(payRequest.getOrderId(), payRequest.getBuyerName()); // ⭐ 담당자명 저장
            System.out.println("[KakaoPayService] tid 저장: orderId=" + payRequest.getOrderId()
                    + ", tid=" + response.getTid()
                    + ", buyerName=" + payRequest.getBuyerName());
        } else {
            throw new IllegalStateException("카카오페이 Ready 응답에 tid가 없습니다.");
        }

        return response;
    }

    /**
     * 카카오페이 결제 승인
     */
    public KakaoApproveResponse kakaoPayApprove(String orderId, String pgToken, String userId) {
        String tid = orderTidMap.get(orderId);
        String buyerName = orderBuyerMap.get(orderId); // ⭐ 저장했던 담당자명 꺼내기

        if (tid == null) {
            throw new IllegalStateException("tid가 존재하지 않습니다. orderId=" + orderId);
        }

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
                HOST + "/v1/payment/approve",
                request,
                KakaoApproveResponse.class
        );

        if (response != null) {
            response.setBuyerName(buyerName); // ⭐ VO에 담당자명 세팅
        }

        System.out.println("[KakaoPayService] 결제 승인 완료: orderId=" + orderId
                + ", tid=" + tid
                + ", buyerName=" + buyerName);

        return response;
    }
}
