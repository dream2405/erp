package kr.ac.dankook.ace.approval_processing_service.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step {

    private Integer step;
    private Integer approverId;
    private String status;

    public Step(kr.ac.dankook.ace.erp.proto.Step step) {
        this.step = step.getStep();
        this.approverId = step.getApproverId();
    }
}
