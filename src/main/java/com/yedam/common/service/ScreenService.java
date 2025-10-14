package com.yedam.common.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.Screen;
import com.yedam.common.repository.ScreenRepository;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;

    // ----- DTO -----
    @Value
    public static class ScreenDto {
        String screenCode;
        String screenName;
        String moduleCode;
        String moduleName; // 공통코드명
        String usageStatus; // 'Y'/'N'
        String usageName;   // 공통코드명 (예: 사용/미사용)
    }

    @Value
    public static class ModuleDto {
        String code;
        String name;
    }

    @Transactional(readOnly = true)
    public List<ScreenDto> findScreens(String moduleCode, String keyword, boolean onlyY) {
        List<Screen> list = screenRepository.searchWithCodes(
                emptyToNull(moduleCode),
                emptyToNull(keyword),
                onlyY
        );
        List<ScreenDto> out = new ArrayList<>(list.size());
        for (Screen s : list) {
            String modName = (s.getModule() != null) ? nvl(s.getModule().getCodeName(), s.getModuleCode()) : s.getModuleCode();
            String useName = (s.getUsage()  != null) ? nvl(s.getUsage().getCodeName(),  s.getUsageStatus()) : s.getUsageStatus();
            out.add(new ScreenDto(
                    s.getScreenCode(),
                    s.getScreenName(),
                    s.getModuleCode(),
                    modName,
                    s.getUsageStatus(),
                    useName
            ));
        }
        return out;
    }

    /**
     * 화면이 가지고 있는 모듈 코드들 → (코드, 이름)으로 변환해서 반환.
     * 이름은 Screen.module(codeName) 중 첫 발견값을 사용.
     */
    @Transactional(readOnly = true)
    public List<ModuleDto> findModules(boolean onlyY) {
        // 우선 코드 목록
        List<String> codes = screenRepository.findDistinctModuleCodes(onlyY);
        if (codes.isEmpty()) return List.of();

        // 코드별 이름 매핑 만들기 위해 한 번만 fetch-join으로 가져와서 모듈명 뽑기
        List<Screen> sample = screenRepository.searchWithCodes(null, null, onlyY);

        Map<String,String> codeNameMap = new LinkedHashMap<>();
        for (Screen s : sample) {
            String code = s.getModuleCode();
            if (code == null) continue;
            if (!codeNameMap.containsKey(code)) {
                String name = (s.getModule()!=null) ? nvl(s.getModule().getCodeName(), code) : code;
                codeNameMap.put(code, name);
            }
        }

        List<ModuleDto> out = new ArrayList<>();
        for (String code : codes) {
            out.add(new ModuleDto(code, nvl(codeNameMap.get(code), code)));
        }
        return out;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
    private static String nvl(String a, String b) {
        return (a == null || a.isBlank()) ? b : a;
    }
}
