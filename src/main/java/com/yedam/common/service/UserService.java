package com.yedam.common.service;

import com.yedam.common.domain.SystemUser;
import com.yedam.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String combined) throws UsernameNotFoundException {
        String[] parts = combined.split(":");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("잘못된 로그인 요청");
        }

        String companyCode = parts[0];
        String username = parts[1];

        SystemUser user = userRepository.findByUserIdAndCompanyCode(username, companyCode)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getUserId())
                   .password(user.getUserPw())   // 암호화된 비밀번호
                   .roles(user.getRoleCode())    // ROLE_USER, ROLE_ADMIN
                   .build();
    }
}
