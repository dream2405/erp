## 역할 및 데이터 저장 구조
- 역할: Approval Request Service로부터 gRPC 호출을 받아 결재 대기열을 관리하고, 결재자의 승인/반려 요청을 처리한 후, 그 결과를 다시 gRPC로 회신하는 게이트웨이 역할을 수행
- 저장소: DB 없이 **인메모리 자료구조(예: Map)** 를 사용하여 결재자 ID별 대기 목록을 저장

In-Memory 자료구조 예시:
```
{
    "7": [ // 결재자 ID (Approver ID)
        {
            "requestId": 1,
            "requesterId": 1,
            "title": "Expense Report",
            "content": "Travel expenses",
            "steps": [
                { "step": 1, "approverId": 3, "status": "approved" },
                { "step": 2, "approverId": 7, "status": "pending" }
            ]
        }
    ],
    // ... 다른 결재자 목록
} 
```

## gRPC 서버 처리 흐름 (RequestApproval 호출 수신 시)

1. 수신된 steps 배열에서 상태가 첫 번째 "pending"에 해당하는 approverId를 찾음
2. 해당 approverId를 키로 하는 인메모리 대기 리스트에 수신된 결재 정보를 저장
3. ApprovalResponse로 `{"status": "received"}`를 반환

## REST API 상세

| HTTP Method | URI | 설명 | 요청/응답 예시 및 처리 흐름                                                                                                                                                         |
| --- | --- | --- |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GET | /process/{approverId} | 결재자 대기 목록 조회 | Response: 해당 approverId의 인메모리 대기 리스트 반환                                                                                                                                  |
| POST | /process/{approverId}/{requestId} | 승인 또는 반려 처리 | Request: {"status": "approved"} 또는 {"status": "rejected"} 흐름: 1. pending 목록에서 해당 결재 건을 찾아 제거. 2. gRPC를 통해 Approval Request Service의 ReturnApprovalResult()를 호출하여 결과를 전달. |
