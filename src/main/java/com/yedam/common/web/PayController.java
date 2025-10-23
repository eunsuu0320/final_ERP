package com.yedam.common.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.PayRequestWrapper;
import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.SystemUser;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.service.ContractPdfService;
import com.yedam.common.service.KakaoPayService;
import com.yedam.common.service.PaymentService;
import com.yedam.common.service.RoleService;
import com.yedam.common.service.TossPayService;
import com.yedam.common.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

	@Autowired KakaoPayService kakaoPayService;
	@Autowired PaymentService paymentService;
	@Autowired UserService userService;
	@Autowired ContractPdfService contractPdfService;
	@Autowired RoleService roleService;
	@Autowired TossPayService tossPayService;

	// 결제 준비 때 저장했다가 성공 콜백에서 사용 (키: orderId)
	private final Map<String, PayRequestWrapper> payRequestStore = new ConcurrentHashMap<>();

	// 계약서 PDF 치환변수 캐시(다운로드 시 사용) (키: subscriptionCode)
	private final Map<String, Map<String, String>> contractVarsCache = new ConcurrentHashMap<>();

	// 팝업에서 부모로 postMessage 후 즉시 닫는 HTML
	private String popupAutoCloseHtml(String type, String status, String subscriptionCode, String companyCode,
			String masterId) {
		String payload = String.format("{type:'%s',status:'%s',subscriptionCode:'%s',companyCode:'%s',masterId:'%s'}",
				type, status, subscriptionCode == null ? "" : subscriptionCode, companyCode == null ? "" : companyCode,
				masterId == null ? "" : masterId);
		return "<!doctype html><meta charset='utf-8'><title>closing…</title>"
				+ "<script>(function(){try{if(window.opener){window.opener.postMessage(" + payload
				+ ",'*');}}catch(e){} window.close();})();</script>";
	}

	@PostMapping("/ready")
	@ResponseBody
	public Object payReady(@RequestBody PayRequestWrapper wrapper, @AuthenticationPrincipal User user) {
		PayRequest payRequest = wrapper.getPayRequest();
		String userId = (user != null) ? user.getUsername() : "GUEST";
		payRequest.setUserId(userId);

		// orderId 기준으로 결제+회사+유저 보관 (PG 콜백에서 조회)
		payRequestStore.put(payRequest.getOrderId(), wrapper);

		if ("KAKAO".equalsIgnoreCase(payRequest.getPayMethod())) {
			return kakaoPayService.kakaoPayReady(payRequest);
		} else if ("TOSS".equalsIgnoreCase(payRequest.getPayMethod())) {
			// 팝업에서 열 체크아웃(내 서버 페이지) URL 반환
			var params = tossPayService.buildCheckoutParams(payRequest.getOrderId(), payRequest.getItemName(),
					payRequest.getAmount());
			// 팝업용 서버 엔드포인트로 리디렉트
			String redirectUrl = "/pay/toss/checkout?orderId=" + payRequest.getOrderId();
			// 파라미터는 세션/맵 저장해두고 checkout에서 사용 (아래 (B) 참고)
			return Map.of("redirectUrl", redirectUrl);
		} else {
			throw new IllegalArgumentException("지원하지 않는 결제수단: " + payRequest.getPayMethod());
		}
	}

	// ---------- Kakao ----------
	@GetMapping(value = "/kakao/success", produces = "text/html; charset=UTF-8")
	@ResponseBody
	public String kakaoPaySuccess(@RequestParam("pg_token") String pgToken, @RequestParam("orderId") String orderId,
			@AuthenticationPrincipal User user) {
		String userId = (user != null) ? user.getUsername() : "GUEST";

		// 1) 승인
		kakaoPayService.kakaoPayApprove(orderId, pgToken, userId);

		// 2) 회사/구독/계정 저장 후 정보 준비
		String companyCode = null;
		String masterId = null;
		String subscriptionCode = null;

		boolean isNewCompany = false;

		PayRequestWrapper wrapper = payRequestStore.remove(orderId);
		if (wrapper != null) {
			// ⬇︎ 변경: saveCompanyInfo(...) → ensureCompanyForSubscription(...)
			var _ens = paymentService.ensureCompanyForSubscription(wrapper.getCompanyInfo());
			Company savedCompany = _ens.getCompany();
			isNewCompany = _ens.isNew(); // ★ 값 대입
			if (savedCompany != null)
				companyCode = savedCompany.getCompanyCode();

			// 이하 동일
			Subscription sub = paymentService.saveSubscriptionInfo(wrapper.getPayRequest(), companyCode);
			if (sub != null)
				subscriptionCode = sub.getSubscriptionCode();

			try {
				if (isNewCompany) { // ★ 이제 정상 동작
					SystemUser su = wrapper.getSystemUser();
					if (su != null && su.getUserId() != null && su.getUserPw() != null) {
						String roleCode = roleService.ensureDefaultsAndGetMasterRoleCode(companyCode, "MASTER");
						String empCode = (su.getEmpCode() == null || su.getEmpCode().isBlank()) ? null
								: su.getEmpCode();

						userService.createMasterUser(companyCode, su.getUserId(), su.getUserPw(), roleCode, empCode,
								su.getRemk());
						masterId = su.getUserId();

						userService.sendWelcomeMail(wrapper.getContactEmail(), companyCode, su.getUserId(),
								su.getUserPw());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// PDF 변수 캐시 (키: subscriptionCode)
			Map<String, String> vars = buildContractVars(wrapper, savedCompany, subscriptionCode);
			if (subscriptionCode != null) {
				contractVarsCache.put(subscriptionCode, vars);
			}
		}

		// 3) 부모로 알림(회사코드/마스터ID/구독코드 포함) + 팝업 닫기
		return popupAutoCloseHtml("KAKAOPAY_RESULT", "success", subscriptionCode, companyCode, masterId);
	}

	@GetMapping(value = "/kakao/cancel", produces = "text/html; charset=UTF-8")
	@ResponseBody
	public String kakaoCancel() {
		return popupAutoCloseHtml("KAKAOPAY_RESULT", "cancel", null, null, null);
	}

	@GetMapping(value = "/kakao/fail", produces = "text/html; charset=UTF-8")
	@ResponseBody
	public String kakaoFail() {
		return popupAutoCloseHtml("KAKAOPAY_RESULT", "fail", null, null, null);
	}
	
	// ---------- Toss ----------
	@GetMapping(value = "/toss/checkout", produces = "text/html; charset=UTF-8")
	@ResponseBody
	public String tossCheckout(@RequestParam("orderId") String orderId) {
	    // payRequestStore 에 준비된 주문 정보를 꺼냄 (ready 때 put 해둔 것)
	    PayRequestWrapper wrapper = payRequestStore.get(orderId);
	    if (wrapper == null) return "<script>alert('유효하지 않은 주문입니다.');window.close();</script>";

	    var pay = wrapper.getPayRequest();
	    var params = tossPayService.buildCheckoutParams(
	        pay.getOrderId(),
	        pay.getItemName(),
	        pay.getAmount()
	    );

	    // 간단한 HTML 반환: 토스 SDK 로드 후 requestPayment 호출
	    String html = """
	    <!doctype html><meta charset="utf-8">
	    <title>Toss Checkout</title>
	    <script src="https://js.tosspayments.com/v1"></script>
	    <script>
	      (async function(){
	        try{
	          const clientKey = '%s';
	          const toss = TossPayments(clientKey);
	          await toss.requestPayment('CARD',{
	            amount: %s,
	            orderId: '%s',
	            orderName: '%s',
	            successUrl: '%s',
	            failUrl: '%s'
	          });
	        }catch(e){
	          alert('결제창 호출 실패: ' + (e && e.message ? e.message : e));
	          window.close();
	        }
	      })();
	    </script>
	    """.formatted(
	      params.get("clientKey"),
	      params.get("amount"),
	      params.get("orderId"),
	      escapeJs(params.get("orderName")),
	      params.get("successUrl"),
	      params.get("failUrl")
	    );
	    return html;
	}

	// 토스 성공 콜백 → 서버 승인(confirm) → 기존 후처리 동일 적용
	@GetMapping(value="/toss/success", produces="text/html; charset=UTF-8")
	@ResponseBody
	public String tossSuccess(@RequestParam("paymentKey") String paymentKey,
	                          @RequestParam("orderId") String orderId,
	                          @RequestParam("amount") long amount) {

		try {
	        // 1) 승인(confirm)
	        tossPayService.confirm(paymentKey, orderId, amount);
	    } catch (Exception e) {
	        e.printStackTrace(); // 로그
	        // 실패도 팝업 닫고 부모에 알리도록
	        return popupAutoCloseHtml("TOSS_RESULT", "fail", null, null, null);
	    }

	    // 2) 회사/구독/계정 생성 등 기존 카카오 후처리와 동일
	    String companyCode = null;
	    String masterId = null;
	    String subscriptionCode = null;
	    boolean isNewCompany = false;

	    PayRequestWrapper wrapper = payRequestStore.remove(orderId);
	    if (wrapper != null) {
	        var _ens = paymentService.ensureCompanyForSubscription(wrapper.getCompanyInfo());
	        var savedCompany = _ens.getCompany();
	        isNewCompany = _ens.isNew();
	        if (savedCompany != null) companyCode = savedCompany.getCompanyCode();

	        var sub = paymentService.saveSubscriptionInfo(wrapper.getPayRequest(), companyCode);
	        if (sub != null) subscriptionCode = sub.getSubscriptionCode();

	        try {
	            if (isNewCompany) {
	                var su = wrapper.getSystemUser();
	                if (su != null && su.getUserId() != null && su.getUserPw() != null) {
	                    String roleCode = roleService.ensureDefaultsAndGetMasterRoleCode(companyCode, "MASTER");
	                    String empCode = (su.getEmpCode() == null || su.getEmpCode().isBlank()) ? null : su.getEmpCode();
	                    userService.createMasterUser(companyCode, su.getUserId(), su.getUserPw(), roleCode, empCode, su.getRemk());
	                    masterId = su.getUserId();
	                    userService.sendWelcomeMail(wrapper.getContactEmail(), companyCode, su.getUserId(), su.getUserPw());
	                }
	            }
	        } catch (Exception ignore) {}

	        var vars = buildContractVars(wrapper, savedCompany, subscriptionCode);
	        if (subscriptionCode != null) contractVarsCache.put(subscriptionCode, vars);
	    }

	    // 3) 부모창 postMessage 후 팝업 닫기 (카카오와 동일 패턴)
	    return popupAutoCloseHtml("TOSS_RESULT", "success", subscriptionCode, companyCode, masterId);
	}

	@GetMapping(value="/toss/fail", produces="text/html; charset=UTF-8")
	@ResponseBody
	public String tossFail(@RequestParam(value="code", required=false) String code,
	                       @RequestParam(value="message", required=false) String message) {
	    return popupAutoCloseHtml("TOSS_RESULT", "fail", null, null, null);
	}

	/** 아주 단순한 JS 문자열 이스케이프(쌍따옴표/작은따옴표 최소) */
	private static String escapeJs(String s){
	    if (s == null) return "";
	    return s.replace("\\","\\\\").replace("'","\\'").replace("\"","\\\"");
	}

	
	// ---------- 성공 화면 렌더 (subscriptionCode 표시) ----------
	@GetMapping("/complete")
	public String payComplete(
	        @RequestParam(value = "subscriptionCode", required = false) String subscriptionCode,
	        @RequestParam(value = "buyerName",        required = false) String buyerName,
	        @RequestParam(value = "total",            required = false) Long total,
	        @RequestParam(value = "vat",              required = false) Long vat,
	        @RequestParam(value = "companyCode",      required = false) String companyCode,
	        @RequestParam(value = "masterId",         required = false) String masterId,
	        org.springframework.ui.Model model) {

	    // 1) 파라미터가 비었으면 캐시에서 보강
	    if (subscriptionCode != null) {
	        Map<String, String> vars = contractVarsCache.get(subscriptionCode);
	        if (vars != null) {
	            if (buyerName == null || buyerName.isBlank()) {
	                buyerName = vars.getOrDefault("buyerName", "");
	            }
	            if (total == null || total == 0L) {
	                total = parseLongSafe(vars.get("total")); // "99,000" → 99000
	            }
	            if (vat == null || vat == 0L) {
	                vat = parseLongSafe(vars.get("vat"));
	            }
	            if (companyCode == null || companyCode.isBlank()) {
	                companyCode = vars.getOrDefault("companyCode", "");
	            }
	        }
	    }

	    SuccessViewInfo info = new SuccessViewInfo(
	        nvl(subscriptionCode),
	        nvl(buyerName),
	        new AmountInfo(total == null ? 0L : total, vat == null ? 0L : vat),
	        nvl(companyCode),
	        nvl(masterId)
	    );
	    model.addAttribute("info", info);
	    return "common/success";
	}

	// 아래 유틸 둘을 PayController에 추가
	private static long parseLongSafe(String s) {
	    if (s == null) return 0L;
	    try {
	        return Long.parseLong(s.replaceAll("[^0-9]", ""));
	    } catch (Exception e) { return 0L; }
	}
	
	// ---------- 계약서 다운로드 (키: subscriptionCode) ----------
	@GetMapping("/contract/download")
	public void downloadContract(@RequestParam("subscriptionCode") String subscriptionCode, HttpServletResponse resp)
			throws Exception {
		Map<String, String> vars = contractVarsCache.get(subscriptionCode);
		if (vars == null) {
			resp.sendError(404, "No contract data for this subscriptionCode");
			return;
		}
		byte[] pdf = contractPdfService.generateBytesFromTemplate("common/contract-pdf.html", vars);

		String fileName = ("contract_" + vars.getOrDefault("companyCode", "") + "_" + subscriptionCode + ".pdf")
				.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");

		resp.setContentType("application/pdf");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setContentLength(pdf.length);
		try (var os = resp.getOutputStream()) {
			os.write(pdf);
			os.flush();
		}
	}

	// ===== 뷰 DTO =====
	public static class SuccessViewInfo {
		private final String subscriptionCode;
		private final String buyerName;
		private final AmountInfo amount;
		private final String companyCode;
		private final String masterId;

		public SuccessViewInfo(String subscriptionCode, String buyerName, AmountInfo amount, String companyCode,
				String masterId) {
			this.subscriptionCode = subscriptionCode;
			this.buyerName = buyerName;
			this.amount = amount;
			this.companyCode = companyCode;
			this.masterId = masterId;
		}

		public String getSubscriptionCode() {
			return subscriptionCode;
		}

		public String getBuyerName() {
			return buyerName;
		}

		public AmountInfo getAmount() {
			return amount;
		}

		public String getCompanyCode() {
			return companyCode;
		}

		public String getMasterId() {
			return masterId;
		}
	}

	public static class AmountInfo {
		private final Long total;
		private final Long vat;

		public AmountInfo(Long total, Long vat) {
			this.total = total;
			this.vat = vat;
		}

		public Long getTotal() {
			return total;
		}

		public Long getVat() {
			return vat;
		}
	}

	private Map<String, String> buildContractVars(PayRequestWrapper wrapper, Company savedCompany,
			String subscriptionCode) {
		var pay = wrapper.getPayRequest();
		var com = wrapper.getCompanyInfo();

		LocalDate start = LocalDate.now();
		int months = 1;
		try {
			if (pay.getSubPeriod() != null && pay.getSubPeriod().contains("개월")) {
				months = Integer.parseInt(pay.getSubPeriod().replace("개월", "").trim());
			}
		} catch (Exception ignore) {
		}
		LocalDate end = start.plusMonths(months);

		long total = (long) pay.getAmount();
		long vat = Math.round(total * 0.1);

		DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

		Map<String, String> vars = new HashMap<>();
		vars.put("contractDate", df.format(start));
		vars.put("companyName", nvl(com.getCompanyName()));
		vars.put("ceoName", nvl(com.getCeoName()));
		vars.put("bizRegNo", nvl(com.getBizRegNo()));
		vars.put("fullAddress", nvl(com.getRoadAddress()) + " " + nvl(com.getAddressDetail()));
		vars.put("tel", nvl(com.getTel()));

		vars.put("subPeriod", nvl(pay.getSubPeriod()));
		vars.put("startDate", df.format(start));
		vars.put("endDate", df.format(end));

		vars.put("total", String.format("%,d", total));
		vars.put("vat", String.format("%,d", vat));
		vars.put("subscriptionCode", nvl(subscriptionCode));
		vars.put("buyerName", nvl(pay.getBuyerName()));

		vars.put("signatureDataUrl", nvl(wrapper.getSignatureDataUrl()));
		vars.put("companyCode", nvl(savedCompany.getCompanyCode()));
		return vars;
	}

	private static String nvl(String s) {
		return (s == null) ? "" : s;
	}
}
