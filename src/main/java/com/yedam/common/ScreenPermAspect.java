// src/main/java/com/yedam/common/ScreenPermAspect.java
package com.yedam.common;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yedam.common.ScreenPerm.Action;
import com.yedam.common.domain.RolePermission;
import com.yedam.common.repository.RolePermissionRepository;
import com.yedam.common.repository.RoleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ScreenPermAspect {

    private final RolePermissionRepository permRepo;
    private final RoleRepository roleRepo;

    @Around("@annotation(perm)")
    public Object check(ProceedingJoinPoint pjp, ScreenPerm perm) throws Throwable {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return denyHere("인증 정보가 없습니다.", false);
        }
        String[] parts = auth.getName().split(":");
        if (parts.length < 4) {
            return denyHere("인증 username 형식 오류", false);
        }
        String companyCode = parts[0];
        String roleCode    = parts[3];
        String screen = perm.screen();

        // 어떤 액션을 체크할지 결정 (anyOf가 있으면 OR, 없으면 단일 action)
        Action[] toCheck = (perm.anyOf().length > 0) ? perm.anyOf() : new Action[]{ perm.action() };

        // MASTER는 바로 통과
        var roleOpt = roleRepo.findById(roleCode);
        if (roleOpt.isPresent() && "MASTER".equalsIgnoreCase(roleOpt.get().getRoleName())) {
            return pjp.proceed();
        }

        RolePermission rp = findPermission(companyCode, roleCode, screen);

        boolean allowed = false;
        if (rp != null) {
            for (Action a : toCheck) {
                boolean ok = switch (a) {
                    case READ   -> "Y".equalsIgnoreCase(rp.getReadRole());
                    case CREATE -> "Y".equalsIgnoreCase(rp.getCreateRole());
                    case UPDATE -> "Y".equalsIgnoreCase(rp.getUpdateRole());
                    case DELETE -> "Y".equalsIgnoreCase(rp.getDeleteRole());
                };
                if (ok) { allowed = true; break; }
            }
        }
        if (!allowed) {
            // READ 페이지면 forbidden 페이지로, 나머지는 403 텍스트로
            boolean containsRead = Arrays.asList(toCheck).contains(Action.READ);
            boolean treatAsReadPage = shouldTreatAsReadPage(containsRead);
            return denyHere("해당 기능 권한이 없습니다.", treatAsReadPage);
        }

        return pjp.proceed();
    }

    /** 멀티테넌시 우선, 없으면 기본 시그니처로 폴백 */
    private RolePermission findPermission(String companyCode, String roleCode, String screen) {
        try {
            Method m = permRepo.getClass().getMethod(
                "findByCompanyCodeAndRoleCodeAndScreenCode",
                String.class, String.class, String.class
            );
            @SuppressWarnings("unchecked")
            Optional<RolePermission> opt =
                (Optional<RolePermission>) m.invoke(permRepo, companyCode, roleCode, screen);
            return opt.orElse(null);
        } catch (NoSuchMethodException ignore) {
            return permRepo.findByRoleCodeAndScreenCode(roleCode, screen).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** READ 페이지로 취급할지 결정: anyOf에 READ가 있고, 현재 요청이 "화면 조회" 성격일 때 */
    private boolean shouldTreatAsReadPage(boolean containsRead) {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return false;

        HttpServletRequest req = attrs.getRequest();
        if (req == null) return false;

        // GET 이고, API/XHR/JSON이 아닌 경우만 화면으로 판단
        boolean get = "GET".equalsIgnoreCase(req.getMethod());
        boolean apiLike = isApi(req);

        return containsRead && get && !apiLike;
    }

    /**
     * 권한없음 응답을 즉시 작성:
     * - READ(보통 GET) 요청이면 /forbidden 으로 forward 해서 페이지 렌더링
     * - 그 외(API/POST/PUT/DELETE 등)는 403 + text/plain 고정 메시지
     * 반환값은 사용하지 않으며, 여기서 응답을 커밋하고 체인을 종료한다.
     */
    private Object denyHere(String message, boolean isReadPage) throws Exception {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new org.springframework.security.access.AccessDeniedException(message);
        }

        HttpServletRequest req   = attrs.getRequest();
        HttpServletResponse res  = attrs.getResponse();

        if (res == null) {
            throw new org.springframework.security.access.AccessDeniedException(message);
        }

        boolean apiLike = isApi(req) || !"GET".equalsIgnoreCase(req.getMethod());

        if (apiLike || !isReadPage) {
            // 등록/수정/삭제(API 포함): 403 + text로 고정 메시지
            if (!res.isCommitted()) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().write("해당 기능 권한이 없습니다.");
                res.getWriter().flush();
            }
            return null;
        } else {
            // 조회(화면): /forbidden으로 forward
            if (!res.isCommitted()) {
                req.setAttribute("forbiddenMessage", message);
                req.getRequestDispatcher("/forbidden").forward(req, res);
            }
            return null;
        }
    }

    /** API/XHR/JSON 요청 탐지 */
    private boolean isApi(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xhr    = req.getHeader("X-Requested-With");
        String ct     = req.getHeader("Content-Type");
        return (accept != null && accept.contains("application/json"))
            || "XMLHttpRequest".equalsIgnoreCase(xhr)
            || (ct != null && ct.contains("application/json"))
            || req.getRequestURI().startsWith("/api/");
    }
}
