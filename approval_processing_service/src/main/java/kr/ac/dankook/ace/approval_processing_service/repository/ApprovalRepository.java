package kr.ac.dankook.ace.approval_processing_service.repository;

import kr.ac.dankook.ace.approval_processing_service.entity.Approval;

import java.util.List;
import java.util.Optional;

public interface ApprovalRepository {
    Approval save(Integer approverId, Approval approval);
    List<Approval> findByApproverId(Integer approverId);
    Optional<Approval> findByApproverIdAndRequestId(Integer approverId, Integer requestId);
    void delete(Integer approverId, Integer requestId);
}
