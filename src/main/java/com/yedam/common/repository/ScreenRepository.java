package com.yedam.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.common.domain.Screen;

public interface ScreenRepository extends JpaRepository<Screen, String> {

    /**
     * Screen + (module, usage) 공통코드까지 fetch-join 해서 조회
     * - onlyY=true 이면 사용여부 Y만
     * - moduleCode, keyword(화면명 like) 필터 지원
     * - 모듈/화면명 정렬
     */
    @Query("""
           select distinct s
             from Screen s
             left join fetch s.module m
             left join fetch s.usage  u
            where (:onlyY = false or s.usageStatus = 'Y')
              and (:moduleCode is null or s.moduleCode = :moduleCode)
              and (:kw is null or lower(s.screenName) like lower(concat('%', :kw, '%')))
            order by s.moduleCode asc, s.screenName asc
           """)
    List<Screen> searchWithCodes(
            @Param("moduleCode") String moduleCode,
            @Param("kw") String keyword,
            @Param("onlyY") boolean onlyY
    );

    /**
     * 모듈 목록(코드 중복 제거) — 이름은 서비스에서 s.module.codeName 이용해 매핑
     * onlyY=true면 사용여부 Y 화면들만 기준.
     */
    @Query("""
           select distinct s.moduleCode
             from Screen s
            where (:onlyY = false or s.usageStatus = 'Y')
            order by s.moduleCode asc
           """)
    List<String> findDistinctModuleCodes(@Param("onlyY") boolean onlyY);
}
