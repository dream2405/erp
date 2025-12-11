package kr.ac.dankook.ace.erp.controller;

import kr.ac.dankook.ace.erp.document.Approval;
import kr.ac.dankook.ace.erp.dto.ApprovalIdResponse;
import kr.ac.dankook.ace.erp.dto.ApprovalRequest;
import kr.ac.dankook.ace.erp.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalIdResponse> createApproval(@RequestBody ApprovalRequest approvalRequest) {
        ApprovalIdResponse approvalIdResponse = approvalService.createApproval(approvalRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(approvalIdResponse);
    }

    @GetMapping
    public ResponseEntity<List<Approval>> getAllApprovals() {
        return ResponseEntity.ok(approvalService.getApprovals());
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Approval> getApprovalById(@PathVariable String requestId) {
        return ResponseEntity.ok(approvalService.getApproval(requestId));
    }
}
