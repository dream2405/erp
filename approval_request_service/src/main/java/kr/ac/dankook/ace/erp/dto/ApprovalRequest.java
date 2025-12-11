package kr.ac.dankook.ace.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApprovalRequest {
    private Integer requesterId;
    private String title;
    private String content;
    private List<StepRequest> steps;
}
