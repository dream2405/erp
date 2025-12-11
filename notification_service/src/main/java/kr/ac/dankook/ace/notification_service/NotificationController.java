package kr.ac.dankook.ace.notification_service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/approval")
    public void sendApprovalNotification(@RequestParam Long requesterId, @RequestParam String requestId) {
        notificationService.sendApprovalNotification(requesterId, requestId);
    }

    @PostMapping("/rejection")
    public void sendRejectionNotification(@RequestParam Long requesterId,
                                          @RequestParam String requestId,
                                          @RequestParam(required = false) Long rejectedBy) {
        notificationService.sendRejectionNotification(requesterId, requestId, rejectedBy);
    }
}
