package kr.ac.dankook.ace.approval_processing_service.controller;

import kr.ac.dankook.ace.approval_processing_service.entity.Approval;
import kr.ac.dankook.ace.approval_processing_service.service.ApprovalCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/process")
public class ApprovalController {

    private final ApprovalCrudService approvalCrudService;

    @GetMapping("/{approverId}")
    public ResponseEntity<List<Approval>> getApprovalById(@PathVariable Integer approverId) {
        return ResponseEntity.ok(approvalCrudService.getApprovals(approverId));
    }

    @PostMapping("/{approverId}/{requestId}")
    public ResponseEntity<Void> createProcess(@PathVariable Integer approverId, @PathVariable Integer requestId) {
        approvalCrudService.processApproval(approverId, requestId);
        return ResponseEntity.ok().build();
    }

}
