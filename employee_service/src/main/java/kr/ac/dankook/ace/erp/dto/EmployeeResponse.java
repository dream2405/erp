package kr.ac.dankook.ace.erp.dto;

import kr.ac.dankook.ace.erp.entity.Employee;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponse {
    private Long id;
    private String name;
    private String department;
    private String position;

    public EmployeeResponse(Employee employee) {
        this.id = employee.getId();
        this.name = employee.getName();
        this.department = employee.getDepartment();
        this.position = employee.getPosition();
    }
}
