package kr.ac.dankook.ace.approval_processing_service.repository;

import jakarta.annotation.Nonnull;
import kr.ac.dankook.ace.approval_processing_service.entity.Approval;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ApprovalMemoryRepository implements ApprovalRepository {

    private final Map<Integer, List<Approval>> store = new ConcurrentHashMap<>();

    @Override
    public Approval save(@Nonnull Integer approverId, @Nonnull Approval approval) {
        store.computeIfAbsent(approverId, _ -> new CopyOnWriteArrayList<>()).add(approval);
        return approval;
    }

    @Override
    public List<Approval> findByApproverId(Integer approverId) {
        return store.getOrDefault(approverId, Collections.emptyList());
    }

    @Override
    public Optional<Approval> findByApproverIdAndRequestId(Integer approverId, Integer requestId) {
        List<Approval> approvals = store.getOrDefault(approverId, Collections.emptyList());
        for (Approval approval : approvals) {
            if (approval.getRequestId().equals(requestId)) {
                return Optional.of(approval);
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(Integer approverId, Integer requestId) {
        store.get(approverId).removeIf(approval -> approval.getRequestId().equals(requestId));
    }
}
