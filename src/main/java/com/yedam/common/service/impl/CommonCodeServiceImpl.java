package com.yedam.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.ScreenPerm;
import com.yedam.common.domain.CodeBulkRequest;
import com.yedam.common.domain.CommonCode;
import com.yedam.common.domain.CommonGroup;
import com.yedam.common.repository.CommonCodeRepository;
import com.yedam.common.repository.CommonGroupRepository;
import com.yedam.common.service.CommonCodeService;

@Service
public class CommonCodeServiceImpl implements CommonCodeService {

	@Autowired CommonGroupRepository groupRepo;
	@Autowired CommonCodeRepository codeRepo;

	@Override
	public List<CommonCode> findByGroupIdAndCompanyCode(String groupId, String companyCode) {
		return codeRepo.findByGroupIdAndCompanyCode(groupId, companyCode);
	}

	@Override
	public Map<String, List<CommonCode>> getCodes(String str) {
		String[] godeStrings = str.split(",");

		Map<String, List<CommonCode>> map = new HashMap<String, List<CommonCode>>();

		for (String code : godeStrings) {
			map.put(code, codeRepo.findByGroupId(code));
		}
		return map;
	}
	
	/* ===== READ 권한 ===== */
    public List<CommonGroup> findGroups() {
        return groupRepo.findAll();
    }

    public List<CommonCode> findCodes(String groupId, String companyCode) {
        return codeRepo.findByGroupIdAndCompanyCodeInOrderByCompanyCodeDescCodeIdDesc(
                groupId, List.of(companyCode, "admin"));
    }

    /* ===== CREATE 권한 ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.CREATE)
    public void createCodes(String companyCode, List<CodeBulkRequest.Item> items, String groupId) {
        if (items == null) return;
        for (CodeBulkRequest.Item it : items) {
            CommonCode code = new CommonCode();
            code.setGroupId(groupId);
            code.setCodeId(it.getCodeId());
            code.setCodeName(it.getCodeName());
            code.setCompanyCode(companyCode);
            codeRepo.save(code);
        }
    }

    /* ===== UPDATE 권한 ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.UPDATE)
    public void updateCodes(List<CodeBulkRequest.Item> items) {
        if (items == null) return;
        for (CodeBulkRequest.Item it : items) {
            CommonCode code = codeRepo.findById(it.getCodeNum())
                .orElseThrow(() -> new IllegalArgumentException("코드가 없습니다: " + it.getCodeNum()));
            code.setCodeId(it.getCodeId());
            code.setCodeName(it.getCodeName());
            codeRepo.save(code);
        }
    }

    /* ===== DELETE 권한 ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.DELETE)
    public void deleteCodes(List<Long> ids) {
        if (ids == null) return;
        for (Long id : ids) {
            codeRepo.deleteById(id);
        }
    }
}
