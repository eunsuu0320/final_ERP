package com.yedam.common.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.repository.PaymentRepository;
import com.yedam.common.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RoleService roleService;

    /**
     * 회사 업서트(사업자번호 기준) + 신규일 때만 기본 롤 시딩
     */
    @Transactional
    public Company saveCompanyInfoUpsert(Company req) {
        Objects.requireNonNull(req, "company is null");
        if (req.getBizRegNo() == null || req.getBizRegNo().isBlank()) {
            throw new IllegalArgumentException("사업자등록번호가 없습니다.");
        }

        return paymentRepository.findAnyByBizRegNo(req.getBizRegNo())
                .map(existing -> {
                    // 업데이트 가능한 필드만 갱신
                    existing.setCompanyName(nvl(req.getCompanyName(), existing.getCompanyName()));
                    existing.setRoadAddress(nvl(req.getRoadAddress(), existing.getRoadAddress()));
                    existing.setAddressDetail(nvl(req.getAddressDetail(), existing.getAddressDetail()));
                    existing.setTel(nvl(req.getTel(), existing.getTel()));
                    existing.setManagerName(nvl(req.getManagerName(), existing.getManagerName()));
                    existing.setManagerEmail(nvl(req.getManagerEmail(), existing.getManagerEmail()));
                    return paymentRepository.save(existing);
                })
                .orElseGet(() -> {
                    // 신규 생성
                    Company saved = paymentRepository.save(req);
                    // 신규 회사에 기본 롤 1회 시딩
                    roleService.seedDefaultsForCompany(saved.getCompanyCode());
                    return saved;
                });
    }

    /**
     * (컨트롤러에서 단순 저장이 필요할 때 사용)
     * 신규 회사면 기본 롤 시딩까지 처리
     */
    @Transactional
    public Company saveCompanyInfo(Company company) {
        if (company.getBizRegNo() == null || company.getBizRegNo().isBlank()) {
            throw new IllegalArgumentException("사업자등록번호가 없습니다.");
        }
        return paymentRepository.findAnyByBizRegNo(company.getBizRegNo())
                .map(existing -> {
                    existing.setCompanyName(nvl(company.getCompanyName(), existing.getCompanyName()));
                    existing.setRoadAddress(nvl(company.getRoadAddress(), existing.getRoadAddress()));
                    existing.setAddressDetail(nvl(company.getAddressDetail(), existing.getAddressDetail()));
                    existing.setTel(nvl(company.getTel(), existing.getTel()));
                    existing.setManagerName(nvl(company.getManagerName(), existing.getManagerName()));
                    existing.setManagerEmail(nvl(company.getManagerEmail(), existing.getManagerEmail()));
                    return paymentRepository.save(existing);
                })
                .orElseGet(() -> {
                    Company saved = paymentRepository.save(company);
                    roleService.seedDefaultsForCompany(saved.getCompanyCode());
                    return saved;
                });
    }

    /**
     * 구독 저장/연장:
     * - ACTIVE가 있으면 종료일 + 개월, periodMonth 누적("N개월"), price 누적
     * - 없거나 만료면 신규 생성
     */
    @Transactional
    public Subscription saveSubscriptionInfo(PayRequest request, String companyCode) {
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalArgumentException("companyCode가 없습니다.");
        }

        final int addMonths   = parseMonths(request.getSubPeriod());   // "3개월" → 3, 기본 1
        final long addAmount  = Long.valueOf(request.getAmount());
        final Date now        = new Date();

        // 0) ACTIVE가 이미 만료되었으면 EXPIRED로 정리
        subscriptionRepository.findLatestByCompanyCodeAndStatus(companyCode, "ACTIVE")
                .ifPresent(active -> {
                    if (active.getSubscriptionEndDate() != null && active.getSubscriptionEndDate().before(now)) {
                        active.setStatus("EXPIRED");
                        subscriptionRepository.save(active);
                    }
                });

        // 1) 다시 ACTIVE 조회
        var activeOpt = subscriptionRepository.findLatestByCompanyCodeAndStatus(companyCode, "ACTIVE");

        if (activeOpt.isPresent()) {
            // ===== 연장 처리 =====
            Subscription cur = activeOpt.get();

            // 종료일 기준 연장
            Date baseEnd = (cur.getSubscriptionEndDate() != null ? cur.getSubscriptionEndDate() : now);
            Date newEnd  = plusMonths(baseEnd, addMonths);
            cur.setSubscriptionEndDate(newEnd);

            // 기존 개월 + 추가 개월 → "N개월"
            int currentMonths = safeMonthsFromLabelOrDates(cur);
            cur.setPeriodMonth(labelMonths(currentMonths + addMonths));

            // 금액 누적
            long curPrice = (cur.getPrice() == null ? 0L : cur.getPrice());
            cur.setPrice(curPrice + addAmount);

            // 상태 유지
            cur.setStatus("ACTIVE");

            return subscriptionRepository.save(cur);
        } else {
            // ===== 신규 생성 =====
            Date start = now;
            Date end   = plusMonths(start, addMonths);

            Subscription sub = new Subscription();
            sub.setCompanyCode(companyCode);
            sub.setSubscriptionStartDate(start);
            sub.setSubscriptionEndDate(end);
            sub.setPeriodMonth(labelMonths(addMonths)); // "N개월"
            sub.setPrice(addAmount);
            sub.setStatus("ACTIVE");

            return subscriptionRepository.save(sub);
        }
    }

    // ===== 유틸 =====
    private static int parseMonths(String subPeriod) {
        if (subPeriod == null) return 1;
        try {
            if (subPeriod.contains("개월")) {
                return Integer.parseInt(subPeriod.replace("개월", "").trim());
            }
        } catch (Exception ignore) {}
        return 1;
    }

    private static String labelMonths(int months) {
        if (months <= 0) months = 1;
        return months + "개월";
    }

    private static int safeMonthsFromLabelOrDates(Subscription s) {
        // 1) periodMonth가 "12개월" 형태면 그대로
        int parsed = parseMonths(s.getPeriodMonth());
        if (parsed > 0) return parsed;

        // 2) 시작/종료일 사이 개월 수 근사
        try {
            LocalDate st = toLocalDate(s.getSubscriptionStartDate());
            LocalDate ed = toLocalDate(s.getSubscriptionEndDate());
            if (st != null && ed != null && !ed.isBefore(st)) {
                long m = ChronoUnit.MONTHS.between(st.withDayOfMonth(1), ed.withDayOfMonth(1));
                // 필요시 +1
                return (int) Math.max(1, m == 0 ? 1 : m);
            }
        } catch (Exception ignore) {}
        return 1;
    }

    private static LocalDate toLocalDate(Date d) {
        return d == null ? null : Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date plusMonths(Date base, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    private static String nvl(String a, String b) {
        return (a == null || a.isBlank()) ? b : a;
    }
}
