package com.yedam.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	@ResponseBody
	@GetMapping("/api/modal/commonCode")
	public List<CommonCode> commonCode(String commonGroup) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String companyCode;

	    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
	        // 로그인 안 된 경우
	        companyCode = "admin";
	    } else {
	        // 로그인 된 경우
	        companyCode = auth.getName().split(":")[0];
	    }
		return commonCodeService.findByGroupIdAndCompanyCode(commonGroup, companyCode);
	}
	
	@ResponseBody
	@GetMapping("/api/modal/commonCodeAdmin")
	public List<CommonCode> commonCodeAdmin(String commonGroup) {
	    String companyCode = "admin";
		return commonCodeService.findByGroupIdAndCompanyCode(commonGroup, companyCode);
	}
	
	@ResponseBody
	@GetMapping("/api/modal/commonCodes")
	public Map<String, List<CommonCode>> commonCodes(String groupId) {
		return commonCodeService.getCodes(groupId);
	}
	
	private String currentCompanyCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 예: "C001:admin01:0001"
        return auth.getName().split(":")[0];
    }

 
    /* ==== 조회(READ) ==== */
    @ResponseBody
    @GetMapping("/api/common/code-groups")
    public List<CommonGroup> groups() {
        return commonCodeService.findGroups(); // READ 권한 체크는 서비스에서
    }

    @ResponseBody
    @GetMapping("/api/common/codes")
    public List<CommonCode> codes(@RequestParam String groupId) {
        return commonCodeService.findCodes(groupId, currentCompanyCode()); // READ
    }

    /* ==== 변경(등록/수정/삭제) 분기 ==== */
    @ResponseBody
    @PostMapping("/api/common/codes/bulk")
    @Transactional
    public void bulk(@RequestBody CodeBulkRequest req) {
        String companyCode = currentCompanyCode();

        // 요청에 포함된 각 작업을 개별 서비스 호출로 분리 -> 각 서비스 메서드에서 권한 체크
        if (req.getDelete() != null && !req.getDelete().isEmpty()) {
            commonCodeService.deleteCodes(req.getDelete()); // DELETE 권한
        }
        if (req.getUpdate() != null && !req.getUpdate().isEmpty()) {
            commonCodeService.updateCodes(req.getUpdate()); // UPDATE 권한
        }
        if (req.getCreate() != null && !req.getCreate().isEmpty()) {
            commonCodeService.createCodes(companyCode, req.getCreate(), req.getGroupId()); // CREATE 권한
        }
    }
}
