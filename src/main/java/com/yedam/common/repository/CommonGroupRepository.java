package com.yedam.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.CommonGroup;

@Repository
public interface CommonGroupRepository extends JpaRepository<CommonGroup, String> {

}
