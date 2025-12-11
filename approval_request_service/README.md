# 서비스 구성 요소 요약

| 서비스명 | 핵심 기능 | 통신 프로토콜           | 저장소 유형 |
| --- | --- |-------------------| --- |
|Employee Service | 직원 CRUD | REST              | MySQL |
| Approval Request Service | 결재 요청 생성, 저장, 단계 관리 | REST, gRPC Client | MongoDB |
| Approval Processing Service | 결재 승인/반려 처리 | REST, gRPC Server | In-Memory |
| Notification Service | 실시간 알림 | WebSocket         | 없음 |


POST /employees 직원 생성 Request: {"name": "Kim", "department": "HR", "position": "Manager"}
Response: {"id": 10}
흐름: 필드 검증 후 INSERT 실행 및 생성된 ID 반환.

GET /employees 직원 목록 조회 쿼리: GET /employees?department=HR&position=Manager
Response: [{"id": 7, "name": "Kim", "department": "HR", "position": "Manager"}]

GET /employees/{id} 직원 상세 조회

PUT /employees/{id} 직원 수정 
Request: {"department": "Finance", "position": "Director"}
제약: department와 position만 수정 가능. 이외 필드 수정 요청 시 에러 처리.

DELETE /employees/{id} 직원 삭제

## 흐름

1. Employee Service REST 호출로 requesterId/approverId 존재 여부 검증.
2. steps가 1부터 오름차순인지 검증.
3. 각 steps에 "status": "pending" 추가.
4. MongoDB INSERT.
5. gRPC를 통해 Approval Processing Service에 RequestApproval 호출.

Approval Processing Service로부터 ReturnApprovalResult 호출을 받으면:
1. 해당 requestId의 Document를 찾아 승인/반려 결과(status)를 업데이트 + updatedAt 추가
    - {"step": 1, "approverId": 3, "status": "approved", "updatedAt": "2025-01-01T10:23:11Z"}
2. Status가 "rejected"인 경우:
    - finalStatus를 "rejected"로 변경 + updatedAt 추가
    - Notification Service를 호출하여 requester에게 최종 반려 알림을 전송
3. Status가 "approved"인 경우:
    - 다음 PENDING 단계가 남아있는 경우:
        - 다음 approverId가 포함된 ApprovalRequest를 다시 구성하여 gRPC를 통해 Approval Processing Service에 RequestApproval을 재호출
    - 모든 단계가 완료된 경우:
        - finalStatus를 "approved"로 변경 + updatedAt 추가
        - Notification Service를 호출하여 requester에게 최종 승인 완료 알림을 전송

gRPC 서버 처리 흐름 (RequestApproval 호출 수신 시)
1. 수신된 steps 배열에서 상태가 첫 번째 "pending"에 해당하는 approverId를 찾음
2. 해당 approverId를 키로 하는 인메모리 대기 리스트에 수신된 결재 정보를 저장
3. ApprovalResponse로 {"status": "received"}를 반환
