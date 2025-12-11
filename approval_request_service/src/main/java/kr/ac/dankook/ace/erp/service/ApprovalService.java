package kr.ac.dankook.ace.erp.service;

import kr.ac.dankook.ace.erp.document.Approval;
import kr.ac.dankook.ace.erp.document.Step;
import kr.ac.dankook.ace.erp.dto.ApprovalIdResponse;
import kr.ac.dankook.ace.erp.dto.ApprovalRequest;
import kr.ac.dankook.ace.erp.dto.StepRequest;
import kr.ac.dankook.ace.erp.exception.ResourceNotFoundException;
import kr.ac.dankook.ace.erp.proto.ApprovalGrpc;
import kr.ac.dankook.ace.erp.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final RestClient restClient;
    private final ApprovalGrpc.ApprovalBlockingStub approvalClient;

    private boolean isNotExists(Integer employeeId) {
        return !restClient.get()
                .uri("/employees/{employeeId}", employeeId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                })
                .toBodilessEntity()
                .getStatusCode()
                .is2xxSuccessful();
    }

    private List<Step> getSteps(ApprovalRequest approvalRequest) {
        List<Step> steps = new ArrayList<>();
        int tmp = 0;
        for (StepRequest stepRequest : approvalRequest.getSteps()) {
            if (!stepRequest.getStep().equals(++tmp)) {
                throw new IllegalArgumentException("Step order is not correct");
            }
            if (isNotExists(stepRequest.getApproverId())) {
                throw new IllegalArgumentException("Approval not found with approverId: " + stepRequest.getApproverId());
            }
            Step step = new Step(stepRequest.getStep(), stepRequest.getApproverId(), "pending");
            steps.add(step);
        }
        return steps;
    }

    public ApprovalIdResponse createApproval(ApprovalRequest approvalRequest) {
        //  requesterId 존재 여부 검증
        if (isNotExists(approvalRequest.getRequesterId())) {
            throw new ResourceNotFoundException("Requester not found with requesterId: " + approvalRequest.getRequesterId());
        }

        // Approval 생성
        Approval approval = new Approval();
        approval.setRequesterId(approvalRequest.getRequesterId());
        approval.setTitle(approvalRequest.getTitle());
        approval.setContent(approvalRequest.getContent());

        // grpc message 생성
        var grpcApprovalReq = kr.ac.dankook.ace.erp.proto.ApprovalRequest.newBuilder()
                .setRequestId(approvalRequest.getRequesterId())
                .setTitle(approvalRequest.getTitle())
                .setContent(approval.getContent());

        // steps id 검증 및 생성 후 INSERT
        List<Step> steps = getSteps(approvalRequest);
        approval.setSteps(steps);
        approvalRepository.save(approval);

        // Add steps to gRPC message
        List<kr.ac.dankook.ace.erp.proto.Step> protoSteps = steps.stream()
                .map(s -> kr.ac.dankook.ace.erp.proto.Step.newBuilder()
                        .setStep(s.getStep())
                        .setApproverId(s.getApproverId())
                        .setStatus(s.getStatus())
                        .build())
                .toList();
        grpcApprovalReq.addAllSteps(protoSteps);

        // gRPC를 통해 호출
        var grpcApprovalRes = approvalClient.requestApproval(grpcApprovalReq.build());

        approval.setFinalStatus(grpcApprovalRes.getStatus());

        // Notification Intergration
        try {
            String status = approval.getFinalStatus();
            if ("approved".equalsIgnoreCase(status)) {
                restClient.post()
                        .uri("http://localhost:8082/internal/notification/approval?requesterId={requesterId}&requestId={requestId}",
                                approval.getRequesterId(), approval.getRequestId())
                        .retrieve()
                        .toBodilessEntity();
            } else if ("rejected".equalsIgnoreCase(status)) {
                restClient.post()
                        .uri("http://localhost:8082/internal/notification/rejection?requesterId={requesterId}&requestId={requestId}",
                                approval.getRequesterId(), approval.getRequestId())
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Exception e) {
            // Notification failure should not fail the request
        }

        return new ApprovalIdResponse(approvalRepository.save(approval));
    }



    public List<Approval> getApprovals() {
        return approvalRepository.findAll();
    }

    public Approval getApproval(String id) {
        return approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with id: " + id));
    }

    public void deleteApproval(String id) {
        if (!approvalRepository.existsById(id)) {
            throw new IllegalArgumentException("Approval not found with id: " + id);
        }
        approvalRepository.deleteById(id);
    }

    public void processApprovalResult(Integer requestId, Integer approverId, String status) {
        // requestId passed from ProcessingService is actually requesterId due to int32 constraint in Proto
        List<Approval> approvals = approvalRepository.findByRequesterId(requestId);

        Approval approval = approvals.stream()
                .filter(a -> !"approved".equalsIgnoreCase(a.getFinalStatus()) && !"rejected".equalsIgnoreCase(a.getFinalStatus()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Active Approval not found for requestId: " + requestId));

        List<Step> steps = approval.getSteps();
        boolean found = false;
        int currentStepIdx = -1;

        // Update current step
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (step.getApproverId().equals(approverId)) {
                step.setStatus(status);
                found = true;
                currentStepIdx = i;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Approver not found in steps");
        }

        // Check logic
        if ("rejected".equalsIgnoreCase(status)) {
            approval.setFinalStatus("rejected");
            sendNotification(approval, "rejected");
        } else if ("approved".equalsIgnoreCase(status)) {
            // Check next step
            if (currentStepIdx + 1 < steps.size()) {
                Step nextStep = steps.get(currentStepIdx + 1);
                // Send to Processing Service
                 var grpcApprovalReq = kr.ac.dankook.ace.erp.proto.ApprovalRequest.newBuilder()
                        .setRequestId(approval.getRequesterId()) // NOTE: Sending RequesterId as RequestId again to match create logic
                        .setTitle(approval.getTitle())
                        .setContent(approval.getContent());
                 
                 List<kr.ac.dankook.ace.erp.proto.Step> protoSteps = steps.stream()
                        .map(s -> kr.ac.dankook.ace.erp.proto.Step.newBuilder()
                                .setStep(s.getStep())
                                .setApproverId(s.getApproverId())
                                .setStatus(s.getStatus())
                                .build())
                        .toList();
                grpcApprovalReq.addAllSteps(protoSteps);
                
                approvalClient.requestApproval(grpcApprovalReq.build());
                
            } else {
                // All approved
                approval.setFinalStatus("approved");
                sendNotification(approval, "approved");
            }
        }

        approvalRepository.save(approval);
    }

    private void sendNotification(Approval approval, String status) {
        try {
            if ("approved".equalsIgnoreCase(status)) {
                restClient.post()
                        .uri("http://localhost:8082/internal/notification/approval?requesterId={requesterId}&requestId={requestId}",
                                approval.getRequesterId(), approval.getRequestId())
                        .retrieve()
                        .toBodilessEntity();
            } else if ("rejected".equalsIgnoreCase(status)) {
                restClient.post()
                        .uri("http://localhost:8082/internal/notification/rejection?requesterId={requesterId}&requestId={requestId}",
                                approval.getRequesterId(), approval.getRequestId())
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Exception e) {
             // Ignore notification errors
        }
    }
}