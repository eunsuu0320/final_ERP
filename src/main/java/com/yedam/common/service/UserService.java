package com.yedam.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yedam.common.domain.SystemUser;
import com.yedam.common.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired UserRepository userRepository;

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
                .username(userId)  // 그대로 저장 (필요 시 userId만 넣어도 됨)
                .password("{noop}"+user.getUserPw())
                .roles(user.getRoleCode()) // DB에 "USER" 저장되어 있으면 자동으로 "ROLE_USER" 적용됨
                .build();
    }
}

