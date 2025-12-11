package kr.ac.dankook.ace.approval_processing_service.service;

import io.grpc.stub.StreamObserver;
import kr.ac.dankook.ace.approval_processing_service.entity.Approval;
import kr.ac.dankook.ace.approval_processing_service.repository.ApprovalMemoryRepository;
import kr.ac.dankook.ace.erp.proto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApprovalGrpcServerService extends ApprovalGrpc.ApprovalImplBase {

    private final ApprovalMemoryRepository approvalMemoryRepository;

    @Override
    public void requestApproval(ApprovalRequest request, StreamObserver<ApprovalResponse> responseObserver) {
        log.info("gRPC 요청 수신 - RequestId: {}, Title: {}", request.getRequestId(), request.getTitle());
        try {
            request.getStepsList().stream()
                    .filter(step -> step.getStatus().equals("pending"))
                    .findFirst()
                    .ifPresentOrElse(
                            step -> {
                                approvalMemoryRepository.save(step.getApproverId(), new Approval(request));
                            },
                            () -> {
                                log.error("RequestId: {}에서 status가 pending인 step이 없음", request.getRequestId());
                                throw new IllegalArgumentException("status가 pending인 step이 없음");
                            });

            responseObserver.onNext(ApprovalResponse.newBuilder().setStatus("received").build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC 처리 중 에러 발생", e);
            responseObserver.onError(e);
        }
    }
}
