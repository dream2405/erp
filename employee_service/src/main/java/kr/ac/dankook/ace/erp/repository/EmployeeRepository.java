package kr.ac.dankook.ace.erp.repository;

import kr.ac.dankook.ace.erp.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
