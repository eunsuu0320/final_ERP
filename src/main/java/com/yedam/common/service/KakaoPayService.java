package com.yedam.common.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
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

    // 기존대로 인스턴스 생성 유지 (Bean 주입으로 바꾸고 싶으면 RestTemplate @Bean 등록 후 final RestTemplate 주입)
    private final RestTemplate restTemplate = new RestTemplate();

    // ===== application.properties 값 주입 =====
    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    @Value("${kakaopay.base-url}")
    private String baseUrl;

    @Value("${kakaopay.endpoints.approval}")
    private String approvalPath;

    @Value("${kakaopay.endpoints.cancel}")
    private String cancelPath;

    @Value("${kakaopay.endpoints.fail}")
    private String failPath;

    // =========================================

    private final Map<String, String> orderTidMap = new ConcurrentHashMap<>();
    private final Map<String, String> orderBuyerMap = new ConcurrentHashMap<>();

    private static final String HOST = "https://kapi.kakao.com";

    public KakaoReadyResponse kakaoPayReady(PayRequest payRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + adminKey);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", nullToEmpty(cid));                      // ex) TC0ONETIME
        params.add("partner_order_id", payRequest.getOrderId());
        params.add("partner_user_id",  payRequest.getUserId());
        params.add("item_name",        payRequest.getItemName());
        params.add("quantity",         "1");
        params.add("total_amount",     String.valueOf(payRequest.getAmount()));
        params.add("tax_free_amount",  "0");

        // 반드시 orderId 포함
        final String orderQS = "?orderId=" + payRequest.getOrderId();
        final String base = trimTrailingSlash(nullToEmpty(baseUrl));
        params.add("approval_url", base + ensureLeadingSlash(nullToEmpty(approvalPath)) + orderQS);
        params.add("cancel_url",   base + ensureLeadingSlash(nullToEmpty(cancelPath))   + orderQS);
        params.add("fail_url",     base + ensureLeadingSlash(nullToEmpty(failPath))     + orderQS);

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
        headers.add("Authorization", "KakaoAK " + adminKey);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", nullToEmpty(cid));
        params.add("tid", tid);
        params.add("partner_order_id", orderId);
        params.add("partner_user_id",  userId);
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        KakaoApproveResponse response = restTemplate.postForObject(
                HOST + "/v1/payment/approve", request, KakaoApproveResponse.class);

        if (response != null) response.setBuyerName(buyerName);

        // 메모리 누수 방지
        orderTidMap.remove(orderId);
        orderBuyerMap.remove(orderId);
        return response;
    }

    // ---------- helpers ----------
    private static String ensureLeadingSlash(String path) {
        if (path.isEmpty()) return "";
        return path.startsWith("/") ? path : "/" + path;
    }
    private static String trimTrailingSlash(String url) {
        if (url.endsWith("/")) return url.substring(0, url.length()-1);
        return url;
    }
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
