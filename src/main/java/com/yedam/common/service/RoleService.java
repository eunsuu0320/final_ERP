package com.yedam.common.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.Role;
import com.yedam.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public void seedDefaultsForCompany(String companyCode) {
        if (companyCode == null || companyCode.isBlank()) return;

        // MASTER가 이미 있으면 기본 롤 있다고 보고 종료 (멱등성)
        if (roleRepository.existsByCompanyCodeAndRoleName(companyCode, "MASTER")) return;

        Role master = new Role();
        master.setCompanyCode(companyCode);
        master.setRoleName("MASTER");
        master.setRemk("회사 마스터(최상위)");

        Role admin = new Role();
        admin.setCompanyCode(companyCode);
        admin.setRoleName("ADMIN");
        admin.setRemk("관리자");

        Role user = new Role();
        user.setCompanyCode(companyCode);
        user.setRoleName("USER");
        user.setRemk("일반 사용자");

        roleRepository.saveAll(List.of(master, admin, user));
    }
}
