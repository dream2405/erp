package kr.ac.dankook.ace.erp.dto;

import kr.ac.dankook.ace.erp.entity.Employee;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeIdResponse {

    private Long id;

    public EmployeeIdResponse(Employee employee) {
        this.id = employee.getId();
    }

}
