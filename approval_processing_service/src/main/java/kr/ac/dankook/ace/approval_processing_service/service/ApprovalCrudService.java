package kr.ac.dankook.ace.approval_processing_service.service;

import kr.ac.dankook.ace.approval_processing_service.entity.Approval;
import kr.ac.dankook.ace.approval_processing_service.repository.ApprovalMemoryRepository;
import kr.ac.dankook.ace.erp.proto.ApprovalGrpc;
import kr.ac.dankook.ace.erp.proto.ApprovalResultRequest;
import kr.ac.dankook.ace.erp.proto.ApprovalResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApprovalCrudService {

    private final ApprovalMemoryRepository approvalMemoryRepository;
    private final ApprovalGrpcServerService approvalGrpcServerService;

    private final ApprovalGrpc.ApprovalBlockingStub approvalStub;
    private final String approvalStatus = "approved";

    public void processApproval(Integer approverId, Integer requestId) {

        var approval = approvalMemoryRepository
                .findByApproverIdAndRequestId(approverId, requestId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        Integer stepIdx = approval.getSteps().stream()
                .filter(s -> s.getApproverId().equals(approverId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Step not found"))
                .getStep();

        // pending 목록에서 해당 결재 건을 찾아 제거
        approvalMemoryRepository.delete(approverId, requestId);

        String approvalStatus = "approved"; // or based on some input, but prompt says "Called to approve"

        // gRPC를 통해 Approval Request Service의 ReturnApprovalResult()를 호출하여 결과를 전달
        ApprovalResultRequest approvalResultRequest = ApprovalResultRequest.newBuilder()
                .setRequestId(requestId)
                .setStep(stepIdx)
                .setApproverId(approverId)
                .setStatus(approvalStatus)
                .build();

        ApprovalResultResponse approvalResultResponse = approvalStub.returnApprovalResult(approvalResultRequest);

        if (!approvalResultResponse.getStatus().equals(approvalStatus)) {
            throw new RuntimeException("approvalResultResponse is not equal to approvalStatus");
        }
    }

    public List<Approval> getApprovals(Integer approverId) {
        return approvalMemoryRepository.findByApproverId(approverId);
    }
}
