package kr.ac.dankook.ace.erp.service;

import io.grpc.stub.StreamObserver;
import kr.ac.dankook.ace.erp.proto.ApprovalGrpc;
import kr.ac.dankook.ace.erp.proto.ApprovalResultRequest;
import kr.ac.dankook.ace.erp.proto.ApprovalResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalGrpcServerService extends ApprovalGrpc.ApprovalImplBase {
    
    private final ApprovalService approvalService;

    @Override
    public void returnApprovalResult(ApprovalResultRequest request, StreamObserver<ApprovalResultResponse> responseObserver) {
        log.info("gRPC 요청 수신 - Request ID: {}, Approver ID: {}", request.getRequestId(), request.getApproverId());
        try {
            String status = request.getStatus();
            approvalService.processApprovalResult(request.getRequestId(), request.getApproverId(), status);

            ApprovalResultResponse approvalResultResponse = ApprovalResultResponse.newBuilder().setStatus(status).build();
            responseObserver.onNext(approvalResultResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC 처리 중 에러 발생", e);
            responseObserver.onError(e);
        }
    }
}
