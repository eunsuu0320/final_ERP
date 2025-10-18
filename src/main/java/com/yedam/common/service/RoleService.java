package com.yedam.common.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.Role;
import com.yedam.common.domain.RolePermSaveReq;
import com.yedam.common.domain.RolePermission;
import com.yedam.common.repository.RolePermissionRepository;
import com.yedam.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RolePermissionRepository permRepo;
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
    
    @Transactional
    public void saveRolePerms(String companyCode, List<RolePermSaveReq> reqs) {
        if (reqs == null || reqs.isEmpty()) return;

        final String roleCode = reqs.get(0).getRoleCode();

        if (roleRepository.countByCompanyCodeAndRoleCode(companyCode, roleCode) == 0) {
            throw new IllegalArgumentException("회사에 없는 ROLE: " + roleCode);
        }

        // 현재 role의 권한 맵
        Map<String, RolePermission> existing = permRepo.findByRoleCode(roleCode)
                .stream().collect(Collectors.toMap(RolePermission::getScreenCode, x -> x, (a,b)->a));

        for (RolePermSaveReq r : reqs) {
            RolePermission e = existing.get(r.getScreenCode());
            if (e == null) { // 신규
                e = new RolePermission();
                e.setRoleCode(roleCode);
                e.setScreenCode(r.getScreenCode());
            }
            e.setReadRole   (r.isCanRead()   ? "Y" : "N");
            e.setCreateRole (r.isCanCreate() ? "Y" : "N");
            e.setUpdateRole (r.isCanUpdate() ? "Y" : "N");
            e.setDeleteRole (r.isCanDelete() ? "Y" : "N");

            boolean allN = "N".equals(e.getReadRole())
                        && "N".equals(e.getCreateRole())
                        && "N".equals(e.getUpdateRole())
                        && "N".equals(e.getDeleteRole());

            if (allN) {
                // 모두 N이면 행 삭제(정책) — 원하면 keep으로 바꿔도 됨
                if (e.getPermissionCode() != null) permRepo.delete(e);
            } else {
                permRepo.save(e); // 신규면 INSERT, 기존이면 UPDATE
            }
        }
    }
}
