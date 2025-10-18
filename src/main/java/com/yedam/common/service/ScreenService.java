package com.yedam.common.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.RoleScreenPermDto;
import com.yedam.common.repository.RolePermissionRepository;

import lombok.RequiredArgsConstructor;

public interface ScreenService {
    List<RoleScreenPermDto> getRoleScreensWithPerm(String companyCode, String roleCode, boolean onlyY);
}

@Service
@RequiredArgsConstructor
class RolePermissionServiceImpl implements ScreenService {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleScreenPermDto> getRoleScreensWithPerm(String companyCode, String roleCode, boolean onlyY) {
        return rolePermissionRepository.findScreensWithPermByRole(companyCode, roleCode, onlyY);
    }
}
