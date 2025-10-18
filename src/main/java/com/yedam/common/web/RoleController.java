package com.yedam.common.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @Value
    static class RoleDto {
        String roleCode;
        String roleName;
    }

    @GetMapping("/api/role-perms")
    public List<RoleDto> listRoles() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String companyCode = auth.getName().split(":")[0];
        
        return roleRepository.findByCompanyCodeOrderByRoleNameAsc(companyCode)
                .stream()
                .map(r -> new RoleDto(r.getRoleCode(), r.getRoleName()))
                .toList();
    }
}
