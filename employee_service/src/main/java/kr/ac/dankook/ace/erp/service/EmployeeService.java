package kr.ac.dankook.ace.erp.service;

import kr.ac.dankook.ace.erp.dto.EmployeeIdResponse;
import kr.ac.dankook.ace.erp.dto.EmployeeRequest;
import kr.ac.dankook.ace.erp.dto.EmployeeResponse;
import kr.ac.dankook.ace.erp.entity.Employee;
import kr.ac.dankook.ace.erp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeIdResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee(request.getName(), request.getDepartment(), request.getPosition());
        Employee savedEmployee = employeeRepository.save(employee);
        return new EmployeeIdResponse(savedEmployee);
    }

    public List<EmployeeResponse> getEmployees(String department, String position) {
        Employee probe = new Employee();
        probe.setDepartment(department);
        probe.setPosition(position);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("id", "name", "createdAt");

        Example<Employee> example = Example.of(probe, matcher);
        List<Employee> employees = employeeRepository.findAll(example);

        return employees.stream()
                .map(EmployeeResponse::new)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        return new EmployeeResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        if (request.getName() != null) {
            throw new IllegalArgumentException("Name cannot be updated");
        }

        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }

        return new EmployeeResponse(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }
}
