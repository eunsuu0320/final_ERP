package com.yedam.common.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.common.ScreenPerm;
import com.yedam.common.domain.ModuleDto;
import com.yedam.common.domain.RolePermSaveReq;
import com.yedam.common.domain.RoleScreenPermDto;
import com.yedam.common.repository.ScreenRepository;
import com.yedam.common.service.RoleService;
import com.yedam.common.service.ScreenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ScreenController {

	private final ScreenService screenService;
	private final ScreenRepository screenRepository;
	private final RoleService roleService;
	
	@GetMapping("/api/roles/{roleCode}/screens")
	public List<RoleScreenPermDto> getScreensByRole(@PathVariable String roleCode,
			@RequestParam(defaultValue = "true") boolean onlyY) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// Username 형식: "C001:admin01:0001"
		String companyCode = auth.getName().split(":")[0];

		return screenService.getRoleScreensWithPerm(companyCode, roleCode, onlyY);
	}
	
    @GetMapping("/api/screens/modules")
    public List<ModuleDto> listModules(@RequestParam(defaultValue = "true") boolean onlyY){
        return screenRepository.findModules(onlyY);
    }
    
    @ScreenPerm(screen = "COM_ROLE", anyOf = { ScreenPerm.Action.CREATE, ScreenPerm.Action.UPDATE })    
    @PutMapping("/api/role-save")
    public ResponseEntity<Void> save(@RequestBody List<RolePermSaveReq> reqs){
        // Username = "C001:admin01:0001"
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String companyCode = auth.getName().split(":")[0];

        roleService.saveRolePerms(companyCode, reqs);
        return ResponseEntity.noContent().build(); // 204
    }
}
