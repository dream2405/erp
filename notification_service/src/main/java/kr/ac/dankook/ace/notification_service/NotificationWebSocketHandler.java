package kr.ac.dankook.ace.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        Long employeeId = getEmployeeIdFromSession(session);

        if (employeeId != null) {
            userSessions.put(employeeId, session); // Map에 저장
            log.info("WebSocket 연결됨: ID={}, Session={}", employeeId, session.getId());
        } else {
            log.warn("ID 없이 접속 시도. 연결 종료.");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    @SuppressWarnings("resource")
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        Long employeeId = getEmployeeIdFromSession(session);
        if (employeeId != null) {
            userSessions.remove(employeeId);
            log.info("WebSocket 연결 해제: ID={}", employeeId);
        }
    }

    public void sendMessageToUser(Long employeeId, Object messageDto) {
        WebSocketSession session = userSessions.get(employeeId);

        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(messageDto);
                session.sendMessage(new TextMessage(jsonMessage));
                log.info("알림 전송 성공 -> ID: {}", employeeId);
            } catch (IOException e) {
                log.error("메시지 전송 실패", e);
            }
        } else {
            log.warn("사용자가 접속해 있지 않음 -> ID: {}", employeeId);
        }
    }

    // URL 쿼리 파라미터(?id=123)에서 ID 추출하는 헬퍼 메소드
    private Long getEmployeeIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        // Spring의 UriComponentsBuilder를 사용해 파싱
        String idStr = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("id");

        try {
            return idStr != null ? Long.parseLong(idStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
