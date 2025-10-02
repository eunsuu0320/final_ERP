package com.yedam.common.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.SystemUser;
import com.yedam.common.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	@Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JavaMailSender mailSender;

    // ... (기존 loadUserByUsername, resetPassword 그대로)

    /**
     * 결제 완료 후 마스터 계정 생성
     *  - companyCode: 필수 (결제로 생성된 회사코드)
     *  - userId: 화면에서 입력한 마스터 ID
     *  - rawPassword: 화면에서 입력한 마스터 PW(서버에서 해시)
     *  - roleCode: 기본 "ROLE_ADMIN" 추천 (정책에 맞게)
     *  - empCode: NOT NULL 제약 대응. FK 없으면 null로 넘기면 userCode로 채움
     *  - remk: 비고(선택)
     */
    @Transactional
    public SystemUser createMasterUser(
            String companyCode,
            String userId,
            String rawPassword,
            String roleCode,  // 예: "ADMIN" (roles() 사용 시 ROLE_ 자동 prefix 주의)
            String empCode,   // 없으면 null → 저장 후 userCode로 세팅
            String remk
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("USER_ID가 비어있습니다.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("USER_PW가 비어있습니다.");
        }

        // 회사 내 중복만 체크
        if (userRepository.findByUserIdAndCompanyCode(userId, companyCode).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자입니다: " + companyCode + ":" + userId);
        }

        SystemUser u = new SystemUser();
        // @Id(userCode)는 SequenceIdGenerator가 채움
        u.setCompanyCode(companyCode);
        // roles() 사용 중이면 DB에는 "ADMIN"처럼 prefix 없는 값 권장
        u.setRoleCode((roleCode == null || roleCode.isBlank()) ? "ADMIN" : roleCode);

        // EMP_CODE NOT NULL 대응: 값 없으면 임시 → 저장 후 userCode로 교체
        u.setEmpCode((empCode == null || empCode.isBlank()) ? "MASTER" : empCode);

        u.setUserId(userId);
        u.setUserPw(passwordEncoder.encode(rawPassword));
        u.setCreatedDate(new java.util.Date());
        u.setUsageStatus("Y");
        u.setRemk(remk);

        // 1차 저장 (userCode 생성)
        SystemUser saved = userRepository.save(u);

        // empCode를 넘기지 않았으면 userCode로 정렬
        if (empCode == null || empCode.isBlank()) {
            saved.setEmpCode(saved.getUserCode()); // EMP_CODE <- USER_CODE
            saved = userRepository.save(saved);
        }
        return saved;
    }
    
    @Override
    public UserDetails loadUserByUsername(String combined) throws UsernameNotFoundException {
        // "COMP01:user01" 형식으로 넘어옴
        String[] parts = combined.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("잘못된 로그인 형식");
        }

        String companyCode = parts[0];
        String userId = parts[1];

        SystemUser user = userRepository.findByUserIdAndCompanyCode(userId, companyCode)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        String username = companyCode + ":" + userId + ":" + user.getEmpCode();

        return User.builder()
                .username(username)
                .password(user.getUserPw())
                .roles(user.getRoleCode())
                .build();
    }

    /**
     * 비밀번호 초기화
     * 1. DB에는 무조건 임시 비밀번호 저장
     * 2. 메일 발송은 실패해도 DB 롤백 안되도록 분리
     */
    @Transactional
    public String resetPassword(String companyCode, String userId, String email) {
        Optional<SystemUser> optionalUser = userRepository.findUserWithEmail(companyCode, userId, email);

        if (optionalUser.isEmpty()) {
            return "NOT_FOUND"; // 입력 정보 불일치
        }

        SystemUser user = optionalUser.get();

        // 1. 임시 비밀번호 생성 및 저장
        String tempPw = UUID.randomUUID().toString().substring(0, 8);
        user.setUserPw(passwordEncoder.encode(tempPw));
        userRepository.save(user);

        // 2. 메일 발송 (트랜잭션에 영향 안 주도록 try/catch)
        try {
            sendPasswordMail(email, tempPw);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "MAIL_ERROR"; // 메일 실패해도 DB는 이미 업데이트됨
        }
    }

    private void sendPasswordMail(String email, String tempPw) {
    	SimpleMailMessage message = new SimpleMailMessage();
    	message.setTo(email);
    	message.setFrom("gywns8339@naver.com"); // 네이버 SMTP 계정과 동일하게
    	message.setSubject("[ERP 시스템] 임시 비밀번호 발급 안내");
    	message.setText("안녕하세요.\n\n요청하신 임시 비밀번호는 [" + tempPw + "] 입니다.\n\n로그인 후 반드시 비밀번호를 변경해주세요.");
    	mailSender.send(message);

    }
    
    public void sendWelcomeMail(String toEmail, String companyCode, String userId, String rawPassword) {
		if (toEmail == null || toEmail.isBlank()) return;
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setFrom("gywns8339@naver.com"); // 사용 중인 SMTP 계정
		message.setSubject("[ERP 시스템] 마스터 계정 생성 안내");
		message.setText(
		"안녕하세요.\n\n" +
		"아래 정보로 ERP 마스터 계정이 생성되었습니다.\n\n" +
		"회사코드 : " + companyCode + "\n" +
		"사용자ID : " + userId + "\n" +
		"비밀번호 : " + rawPassword + "  (로그인 후 변경 권장)\n\n" +
		"감사합니다."
		);
		
		mailSender.send(message);
	}
}
