package kr.ac.dankook.ace.erp.dto;

import kr.ac.dankook.ace.erp.document.Approval;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalIdResponse {

    private String requestId;

    public ApprovalIdResponse(Approval approval) {
        this.requestId = approval.getRequestId();
    }
}
