package com.afs.restapi.repository;

import com.afs.restapi.entity.Employee;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeJpaRepository extends JpaRepositoryImplementation<Employee, Long> {

    List<Employee> findAllByGender(String gender);

    List<Employee> findEmployeesByCompanyId(Long id);
}
