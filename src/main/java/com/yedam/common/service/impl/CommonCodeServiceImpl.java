package com.yedam.common.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        String[] godeStrings = (str == null ? "" : str).split(",");
        Map<String, List<CommonCode>> map = new HashMap<>();
        for (String code : godeStrings) {
            if (code == null || code.isBlank()) continue;
            map.put(code, codeRepo.findByGroupId(code));
        }
        return map;
    }

    /* ===== READ ===== */
    public List<CommonGroup> findGroups() {
        return groupRepo.findAll();
    }

    public List<CommonCode> findCodes(String groupId, String companyCode) {
        return codeRepo.findByGroupIdAndCompanyCodeInOrderByCompanyCodeDescCodeIdDesc(
                groupId, List.of(companyCode, "admin"));
    }

    /* ===== CREATE ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.CREATE)
    public void createCodes(String companyCode, List<CodeBulkRequest.Item> items, String groupId) {
        if (items == null || items.isEmpty()) return;

        // 빈값 금지 & payload 내부 중복 차단 (대/소문자 무시)
        Set<String> seen = new HashSet<>();
        for (CodeBulkRequest.Item it : items) {
            String id = trim(it.getCodeId());
            if (id.isEmpty()) throw new IllegalArgumentException("empty");
            String idU = id.toUpperCase();
            if (!seen.add(idU)) throw new IllegalArgumentException("dup");
        }

        // DB 보유 ID와 충돌 차단
        Set<String> existing = codeRepo.findCodeIdsByGroupIdAndCompanyCode(groupId, companyCode)
                .stream().map(s -> s == null ? "" : s.trim().toUpperCase())
                .collect(Collectors.toSet());
        for (CodeBulkRequest.Item it : items) {
            String idU = trim(it.getCodeId()).toUpperCase();
            if (existing.contains(idU)) throw new IllegalArgumentException("dup");
        }

        // 저장 (USE_YN 반영)
        for (CodeBulkRequest.Item it : items) {
            CommonCode code = new CommonCode();
            code.setGroupId(groupId);
            code.setCodeId(trim(it.getCodeId()));
            code.setCodeName(trim(it.getCodeName()));
            code.setCompanyCode(companyCode);
            code.setUseYn(normalizeUseYn(it.getUseYn()));
            codeRepo.save(code);
        }
    }

    /* ===== UPDATE ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.UPDATE)
    public void updateCodes(List<CodeBulkRequest.Item> items) {
        if (items == null || items.isEmpty()) return;

        // 빈값 금지 & payload 내부 중복 차단
        Set<String> target = new HashSet<>();
        for (CodeBulkRequest.Item it : items) {
            String id = trim(it.getCodeId());
            if (id.isEmpty()) throw new IllegalArgumentException("empty");
            String idU = id.toUpperCase();
            if (!target.add(idU)) throw new IllegalArgumentException("dup");
        }

        // 각 항목 DB 중복 차단 (자기 자신 제외)
        for (CodeBulkRequest.Item it : items) {
            CommonCode cur = codeRepo.findById(it.getCodeNum())
                .orElseThrow(() -> new IllegalArgumentException("notfound"));
            String newId = trim(it.getCodeId());
            long cnt = codeRepo.countDupOnUpdate(cur.getGroupId(), cur.getCompanyCode(), newId, cur.getCodeNum());
            if (cnt > 0) throw new IllegalArgumentException("dup");
        }

        // 저장 (USE_YN 반영)
        for (CodeBulkRequest.Item it : items) {
            CommonCode code = codeRepo.findById(it.getCodeNum())
                .orElseThrow(() -> new IllegalArgumentException("notfound"));
            code.setCodeId(trim(it.getCodeId()));
            code.setCodeName(trim(it.getCodeName()));
            code.setUseYn(normalizeUseYn(it.getUseYn()));
            codeRepo.save(code);
        }
    }

    /* ===== DELETE ===== */
    @Transactional
    @ScreenPerm(screen = "COM_CODE", action = ScreenPerm.Action.DELETE)
    public void deleteCodes(List<Long> ids) {
        if (ids == null) return;
        for (Long id : ids) {
            codeRepo.deleteById(id);
        }
    }

    private static String trim(String s){ return s == null ? "" : s.trim(); }

    private static String normalizeUseYn(String v){
        if (v == null) return "Y";
        String s = v.trim().toUpperCase();
        return "N".equals(s) ? "N" : "Y";
    }
}
