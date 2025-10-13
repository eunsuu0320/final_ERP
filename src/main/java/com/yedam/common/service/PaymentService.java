package com.yedam.common.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.repository.PaymentRepository;
import com.yedam.common.repository.SubscriptionRepository;

@Service
public class PaymentService {

	@Autowired
    private PaymentRepository paymentRepository;
	
	@Autowired
    private SubscriptionRepository subscriptionRepository;
	
	@Autowired
	private RoleService roleService;

    @Transactional
    public Company saveCompanyInfoUpsert(Company req) {
        Objects.requireNonNull(req, "company is null");
        if (req.getBizRegNo() == null || req.getBizRegNo().isBlank()) {
            throw new IllegalArgumentException("사업자등록번호가 없습니다.");
        }

        // 기존 회사 조회
        Optional<Company> found = paymentRepository.findAnyByBizRegNo(req.getBizRegNo());

        if (found.isPresent()) {
            Company existing = found.get();
            existing.setCompanyName(nvl(req.getCompanyName(), existing.getCompanyName()));
            existing.setRoadAddress(nvl(req.getRoadAddress(), existing.getRoadAddress()));
            existing.setAddressDetail(nvl(req.getAddressDetail(), existing.getAddressDetail()));
            existing.setTel(nvl(req.getTel(), existing.getTel()));
            existing.setManagerName(nvl(req.getManagerName(), existing.getManagerName()));
            existing.setManagerEmail(nvl(req.getManagerEmail(), existing.getManagerEmail()));
            return paymentRepository.save(existing);
        } else {
            // 신규 생성
            Company saved = paymentRepository.save(req);
            // ⬇ 여기서 한번만 기본 롤 시딩
            roleService.seedDefaultsForCompany(saved.getCompanyCode());
            return saved;
        }
    }

    private static String nvl(String a, String b) {
        return (a == null || a.isBlank()) ? b : a;
    }
	
    /*
     * 결제 성공 시 회사 정보 저장
     */
    public Company saveCompanyInfo(Company company) {
        return paymentRepository.save(company);
    }
    
    /*
     * 결제 성공 시 회사 정보 저장
     */
	public Subscription saveSubscriptionInfo(PayRequest request, String companyCode) {
        Date startDate = new Date();

        // request.getSubPeriod() 예: "3개월"
        int months = 1; // 기본 1개월
        if (request.getSubPeriod() != null && request.getSubPeriod().contains("개월")) {
            months = Integer.parseInt(request.getSubPeriod().replace("개월", "").trim());
        }

        // 만료일 계산 (현재 날짜 + months 개월)
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MONTH, months);
        Date endDate = cal.getTime();

        Subscription subscription = new Subscription();
        subscription.setCompanyCode(companyCode);
        subscription.setSubscriptionStartDate(startDate);
        subscription.setSubscriptionEndDate(endDate);
        subscription.setPeriodMonth(request.getSubPeriod());
        subscription.setPrice((long) request.getAmount());
        subscription.setStatus("구독중");

        return subscriptionRepository.save(subscription);
    }
}
