package com.yedam.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.yedam.common.domain.CodeBulkRequest;
import com.yedam.common.domain.CommonCode;
import com.yedam.common.domain.CommonGroup;
import com.yedam.common.repository.CommonCodeRepository;
import com.yedam.common.repository.CommonGroupRepository;
import com.yedam.common.service.impl.CommonCodeServiceImpl;

@Controller
public class CommonCodeController {

    @Autowired CommonCodeServiceImpl commonCodeService;
    @Autowired CommonGroupRepository groupRepo;
    @Autowired CommonCodeRepository codeRepo;

    // ===== 모달용 =====
    @ResponseBody
    @GetMapping("/api/modal/commonCode")
    public List<CommonCode> commonCode(@RequestParam("commonGroup") String commonGroup) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String companyCode;
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            companyCode = "admin";
        } else {
            companyCode = auth.getName().split(":")[0];
        }
        return commonCodeService.findByGroupIdAndCompanyCode(commonGroup, companyCode);
    }

    @ResponseBody
    @GetMapping("/api/modal/commonCodeAdmin")
    public List<CommonCode> commonCodeAdmin(@RequestParam("commonGroup") String commonGroup) {
        return commonCodeService.findByGroupIdAndCompanyCode(commonGroup, "admin");
    }

    @ResponseBody
    @GetMapping("/api/modal/commonCodes")
    public Map<String, List<CommonCode>> commonCodes(@RequestParam("groupId") String groupId) {
        return commonCodeService.getCodes(groupId);
    }

    // ===== 화면용 =====
    private String currentCompanyCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "admin" : auth.getName().split(":")[0]; // 예: "C001:admin01:0001"
    }

    /* READ */
    @ResponseBody
    @GetMapping("/api/common/code-groups")
    public List<CommonGroup> groups() {
        return commonCodeService.findGroups();
    }

    @ResponseBody
    @GetMapping("/api/common/codes")
    public List<CommonCode> codes(@RequestParam String groupId) {
        return commonCodeService.findCodes(groupId, currentCompanyCode());
    }

    /* CREATE/UPDATE/DELETE */
    @ResponseBody
    @PostMapping("/api/common/codes/bulk")
    @Transactional
    public void bulk(@RequestBody CodeBulkRequest req) {
        String companyCode = currentCompanyCode();
        try {
            if (req.getDelete() != null && !req.getDelete().isEmpty()) {
                commonCodeService.deleteCodes(req.getDelete());
            }
            if (req.getUpdate() != null && !req.getUpdate().isEmpty()) {
                commonCodeService.updateCodes(req.getUpdate());
            }
            if (req.getCreate() != null && !req.getCreate().isEmpty()) {
                commonCodeService.createCodes(companyCode, req.getCreate(), req.getGroupId());
            }
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("empty".equals(msg)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "공통코드 ID를 입력하세요.");
            }
            // 그 외는 중복으로 간주
            throw new ResponseStatusException(HttpStatus.CONFLICT, "공통코드 ID가 중복되었습니다.");
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "공통코드 ID가 중복되었습니다.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "저장 중 오류가 발생했습니다.");
        }
    }
}
