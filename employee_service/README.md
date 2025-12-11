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