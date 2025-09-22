package com.yedam.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employees;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, String> {

}
