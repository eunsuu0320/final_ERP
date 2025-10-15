package com.yedam.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.yedam.common.ScreenPerm.Action;
import com.yedam.common.domain.RolePermission;
import com.yedam.common.repository.RolePermissionRepository;
import com.yedam.common.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ScreenPermAspect {

    private final RolePermissionRepository permRepo;
    private final RoleRepository roleRepo;

    // 아주 단순한 인메모리 캐시 (원하면 Caffeine으로 교체)
    private final Map<String, RolePermission> cache = new ConcurrentHashMap<>();
    private String key(String cc, String roleCode, String screen) { return cc + "|" + roleCode + "|" + screen; }

    @Around("@annotation(perm)")
    public Object check(ProceedingJoinPoint pjp, ScreenPerm perm) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new IllegalStateException("인증 정보가 없습니다.");

        // username 형식: "C001:admin01:EMP001" (네 코드 기준)
        String[] parts = auth.getName().split(":");
        if (parts.length < 1) throw new IllegalStateException("인증 username 형식 오류");
        String companyCode = parts[0];

        String roleCode = (parts.length >= 4) ? parts[3] : null;
        if (roleCode == null)
            throw new IllegalStateException("로그인 정보에 roleCode가 없어 권한 검증 불가 (username에 roleCode 추가 필요)");

        String screen = perm.screen();
        Action action = perm.action();

        // MASTER 같은 최상위 롤은 바로 통과 (선택)
        var roleOpt = roleRepo.findById(roleCode);
        if (roleOpt.isPresent() && "MASTER".equalsIgnoreCase(roleOpt.get().getRoleName())) {
            return pjp.proceed();
        }

        // 캐시 → 미스 시 DB
        String k = key(companyCode, roleCode, screen);
        RolePermission rp = cache.computeIfAbsent(k, kk ->
            permRepo.findByRoleCodeAndScreenCode(roleCode, screen).orElse(null)
        );

        boolean allowed = false;
        if (rp != null) {
            switch (action) {
                case READ   -> allowed = "Y".equalsIgnoreCase(rp.getReadRole());
                case CREATE -> allowed = "Y".equalsIgnoreCase(rp.getCreateRole());
                case UPDATE -> allowed = "Y".equalsIgnoreCase(rp.getUpdateRole());
                case DELETE -> allowed = "Y".equalsIgnoreCase(rp.getDeleteRole());
            }
        } else {
            // 행이 없으면 기본 N (정책)
            allowed = false;
        }

        if (!allowed) {
            // 403 성격의 예외 던지기
            throw new org.springframework.security.access.AccessDeniedException(
                "해당 기능 권한이 없습니다. (" + screen + " - " + action + ")"
            );
        }
        return pjp.proceed();
    }
}
