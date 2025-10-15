package com.yedam.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yedam.common.domain.ModuleDto;
import com.yedam.common.domain.Screen;

public interface ScreenRepository extends JpaRepository<Screen, String> {
	@Query("""
			    select distinct new com.yedam.common.domain.ModuleDto(
			        s.moduleCode,
			        coalesce(cc.codeName, s.moduleCode)
			    )
			    from Screen s
			    left join CommonCode cc
			           on cc.groupId = 'GRP016'
			          and cc.codeId  = s.moduleCode
			    where (:onlyY = false or s.usageStatus = 'Y')
			    order by s.moduleCode
			""")
	List<ModuleDto> findModules(boolean onlyY);

}
