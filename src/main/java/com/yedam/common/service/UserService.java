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

        return User.builder()
                .username(combined)  // 그대로 저장
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
}
