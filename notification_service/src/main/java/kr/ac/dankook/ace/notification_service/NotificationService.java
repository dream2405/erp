package kr.ac.dankook.ace.notification_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;

    // 최종 승인 알림 전송
    public void sendApprovalNotification(Long requesterId, String requestId) {
        ApprovalNotificationDto message = ApprovalNotificationDto.builder()
                .requestId(requestId)
                .result("approved")
                .finalResult("approved")
                .build();

        // 핸들러를 통해 전송
        webSocketHandler.sendMessageToUser(requesterId, message);
    }

    // 반려 알림 전송
    public void sendRejectionNotification(Long requesterId, String requestId, Long rejectedByApproverId) {
        ApprovalNotificationDto message = ApprovalNotificationDto.builder()
                .requestId(requestId)
                .result("rejected")
                .rejectedBy(rejectedByApproverId)
                .finalResult("rejected")
                .build();

        webSocketHandler.sendMessageToUser(requesterId, message);
    }
}
