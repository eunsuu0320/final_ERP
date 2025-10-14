package com.yedam.common.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.Role;
import com.yedam.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * 회사별 기본 롤 시드.
     * - 이미 MASTER 가 있으면 아무 것도 하지 않음
     */
    @Transactional
    public void seedDefaultsForCompany(String companyCode) {
        if (companyCode == null || companyCode.isBlank()) return;

        if (roleRepository.countByCompanyCodeAndRoleNameCI(companyCode, "MASTER") > 0) return;

        Role master = new Role();
        master.setCompanyCode(companyCode);
        master.setRoleName("MASTER");
        master.setRemk("마스터 관리자");

        Role admin = new Role();
        admin.setCompanyCode(companyCode);
        admin.setRoleName("MANAGER");
        admin.setRemk("일반 관리자");

        Role user = new Role();
        user.setCompanyCode(companyCode);
        user.setRoleName("USER");
        user.setRemk("일반 사용자");

        roleRepository.saveAll(List.of(master, admin, user));
    }

    /**
     * 회사별 MASTER 롤코드 조회 (없으면 null)
     */
    @Transactional(readOnly = true)
    public String getMasterRoleCode(String companyCode) {
        if (companyCode == null || companyCode.isBlank()) return null;
        return roleRepository
                .findAnyByCompanyCodeAndRoleNameCI(companyCode, "MASTER")
                .map(Role::getRoleCode)
                .orElse(null);
    }

    /**
     * 기본 롤이 없다면 시드 후 MASTER 롤코드를 반환.
     * - 그래도 못 찾으면 defaultIfAbsent 반환 (예: "ADMIN")
     */
    @Transactional
    public String ensureDefaultsAndGetMasterRoleCode(String companyCode, String defaultIfAbsent) {
        if (companyCode == null || companyCode.isBlank()) return defaultIfAbsent;

        seedDefaultsForCompany(companyCode);

        String roleCode = getMasterRoleCode(companyCode);
        return (roleCode != null) ? roleCode : defaultIfAbsent;
    }
}
