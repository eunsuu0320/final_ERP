package com.yedam.common.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.SystemUser;
import com.yedam.common.repository.CompanyRepository;
import com.yedam.common.repository.SubscriptionRepository;
import com.yedam.common.repository.UserRepository;
import com.yedam.common.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;

    // ===== 공통: 로그인 회사코드 꺼내기 =====
    private String currentCompanyCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Username 형식: "C001:admin01:0001"
        return auth.getName().split(":")[0];
    }

    // ===== 사용자 목록 조회 =====
    @GetMapping("/api/common/users")
    public List<SystemUser> userList(@RequestParam(required=false) String kw,
                                     @RequestParam(required=false) String dept,
                                     @RequestParam(required=false) String useYn) {
        String companyCode = currentCompanyCode();
        return userRepository.findUserByCompanyCode(companyCode, kw, dept, useYn);
    }

    // ===== 구독내역 조회 =====
    @GetMapping("/api/common/subs")
    public List<Subscription> subscriptionList() {
        String companyCode = currentCompanyCode();
        return subscriptionRepository.findByCompanyCode(companyCode);
    }

    // ===== 회사 조회 =====
    @GetMapping("/api/common/company")
    public Company companyInfo() {
        String companyCode = currentCompanyCode();
        return companyRepository.findById(companyCode)
                .orElseThrow(() -> new IllegalStateException("회사 정보를 찾을 수 없습니다."));
    }

    // ===== 사용자 등록(임시 비밀번호 생성 + 이메일 발송) =====
    @PostMapping("/api/common/users")
    @Transactional
    public SystemUser createUser(@RequestBody SystemUser req) {
        String companyCode = currentCompanyCode();

        // 필수값 검증
        if (req.getEmpCode() == null || req.getEmpCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사원번호가 필요합니다.");
        }
        if (req.getUserId() == null || req.getUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디가 필요합니다.");
        }

        // 회사 내 userId 중복 체크
        userRepository.findByUserIdAndCompanyCode(req.getUserId(), companyCode)
                .ifPresent(u -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."); });

        // 서비스에 위임(임시비번 생성 + 암호화 저장 + 메일발송)
        SystemUser saved = userService.createUserWithTempPassword(req, companyCode);

        return saved;
    }

    // ===== 사용자 수정(사번/사원명/부서 제외) - PUT 권장 =====
    @PutMapping("/api/common/users/{userCode}")
    @Transactional
    public SystemUser updateUser(@PathVariable String userCode, @RequestBody SystemUser req) {
        String companyCode = currentCompanyCode();
        return userService.updateUser(req, userCode, companyCode);
    }

    // ===== (옵션) 비밀번호 초기화(임시 비번 생성 + 메일 발송) =====
    @PostMapping("/api/common/users/{userId}/reset-password")
    public String resetPassword(@PathVariable String userId,
                                @RequestParam String email) {
        String companyCode = currentCompanyCode();
        String result = userService.resetPassword(companyCode, userId, email);

        // 프론트에서 처리 쉽게 문자열 그대로 반환
        // ("SUCCESS" | "NOT_FOUND" | "MAIL_ERROR")
        if ("NOT_FOUND".equals(result)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "일치하는 사용자/이메일이 없습니다.");
        }
        if ("MAIL_ERROR".equals(result)) {
            // DB는 업데이트 성공, 메일만 실패
            // 필요 시 202/207 등으로 내려도 됨
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "임시비밀번호 설정은 완료했으나 메일 전송에 실패했습니다.");
        }
        return "SUCCESS";
    }
}
