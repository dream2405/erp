package kr.ac.dankook.ace.notification_service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalNotificationDto {
    private String requestId;
    private String result;        // "approved" or "rejected"
    private Long rejectedBy;      // 반려 시에만 존재 (Optional)
    private String finalResult;   // "approved" or "rejected"
}
