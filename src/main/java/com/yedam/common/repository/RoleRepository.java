package com.yedam.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.common.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

	boolean existsByCompanyCodeAndRoleName(String companyCode, String roleName);

    Optional<Role> findFirstByCompanyCodeAndRoleName(String companyCode, String roleName);

    List<Role> findByCompanyCode(String companyCode);
}

