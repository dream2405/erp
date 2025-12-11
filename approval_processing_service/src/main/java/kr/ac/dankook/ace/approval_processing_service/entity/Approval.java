package kr.ac.dankook.ace.approval_processing_service.entity;

import kr.ac.dankook.ace.erp.proto.ApprovalRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Approval {

    private Integer requestId;
    private Integer requesterId;
    private String title;
    private String content;
    private List<Step> steps;

    public Approval(ApprovalRequest approvalRequest) {
        this.requestId = approvalRequest.getRequestId();
        this.requesterId = approvalRequest.getRequesterId();
        this.title = approvalRequest.getTitle();
        this.content = approvalRequest.getContent();
        this.steps = approvalRequest.getStepsList().stream().map(Step::new).toList();
    }
}
