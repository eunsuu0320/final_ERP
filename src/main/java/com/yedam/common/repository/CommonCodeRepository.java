package com.yedam.common.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.CommonCode;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

	@Query("select c from CommonCode c where c.groupId = ?1")
	List<CommonCode> findByGroupId(String groupId);
	
	@Query("select c from CommonCode c where c.groupId = ?1 and c.companyCode = ?2")
	List<CommonCode> findByGroupIdAndCompanyCode(String groupId, String companyCode);
	
	List<CommonCode> findByGroupIdAndCompanyCodeInOrderByCompanyCodeDescCodeIdDesc(
	        String groupId, Collection<String> companyCodes
	);
	
	// 해당 그룹/회사에 이미 존재하는 codeId 목록
    @Query("select upper(c.codeId) from CommonCode c where c.groupId = :groupId and c.companyCode = :companyCode")
    List<String> findCodeIdsByGroupIdAndCompanyCode(
        @org.springframework.data.repository.query.Param("groupId") String groupId,
        @org.springframework.data.repository.query.Param("companyCode") String companyCode
    );

    // 생성 시 중복 여부 (대소문자 무시)
    @Query("select count(c) from CommonCode c " +
           "where c.groupId = :groupId and c.companyCode = :companyCode " +
           "and upper(c.codeId) = upper(:codeId)")
    long countDupOnCreate(
        @org.springframework.data.repository.query.Param("groupId") String groupId,
        @org.springframework.data.repository.query.Param("companyCode") String companyCode,
        @org.springframework.data.repository.query.Param("codeId") String codeId
    );

    // 수정 시 자기 자신 제외하고 중복 여부
    @Query("select count(c) from CommonCode c " +
           "where c.groupId = :groupId and c.companyCode = :companyCode " +
           "and upper(c.codeId) = upper(:codeId) and c.codeNum <> :codeNum")
    long countDupOnUpdate(
        @org.springframework.data.repository.query.Param("groupId") String groupId,
        @org.springframework.data.repository.query.Param("companyCode") String companyCode,
        @org.springframework.data.repository.query.Param("codeId") String codeId,
        @org.springframework.data.repository.query.Param("codeNum") Long codeNum
    );

    Optional<CommonCode> findByGroupIdAndCompanyCodeAndCodeId(String groupId, String companyCode, String codeId);
}
