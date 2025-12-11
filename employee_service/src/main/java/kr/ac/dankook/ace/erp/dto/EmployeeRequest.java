package kr.ac.dankook.ace.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRequest {
    private String name;
    private String department;
    private String position;
}
