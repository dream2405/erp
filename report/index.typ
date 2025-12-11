/************************기본 설정************************/

#set page(
    margin: (x: 10%, y: 7%),
)

#set text(
    font: "NanumGothic",
    size: 10pt,
)

#set list(marker: [•], indent: 3pt)
#set enum(indent: 3pt)

#show raw.where(block: false): box.with(
    fill: luma(240),
    inset: (x: 3pt, y: 0pt),
    outset: (y: 3pt),
    radius: 2pt,
    stroke: luma(100) + .04em,
)
#show raw.where(block: true): block.with(
    stroke: luma(170),
    fill: luma(240),
    inset: (x: 5pt, y: 6pt),
    width: 100%,
    radius: 4pt,
)
#show raw: set text(font: "Elice DigitalCoding ver.H")

#show heading: set text(font: "SB Aggro")
#let qbox(body) = {
    align(center)[
        #box(
            fill: rgb("#D7E2C6"),
            stroke: (y: 1pt + luma(160)),
            inset: 0.8em,
            width: 100%,
            align(center, body),
        )
    ]
}

#let mbox(body) = {
    box(
        fill: rgb("#f5ffe6"),
        stroke: (1pt + luma(200)),
        inset: 0.8em,
        width: 100%,
        radius: 4pt,
        align(center, body),
    )
}

#let frame(stroke) = (x, y) => (
    left: if x > 0 { 0pt } else { stroke },
    right: stroke,
    top: if y < 2 { stroke } else { 0pt },
    bottom: stroke,
)

#set table(
    columns: 2,
    stroke: (x, y) => (
        if y < 2 { (top: (thickness: 1pt)) } else {
            (top: (paint: luma(150), dash: "solid", thickness: 0.5pt))
        }
            + if x > 0 { (left: (paint: luma(100), thickness: .8pt, dash: "dotted")) }
    ),
)

/********************************************************/

#qbox()[*아키텍처 다이어그램*]

#image("/assets/image.png")

#align(
    center,
    table(
        align: (left, left),
        table.cell(colspan: 2, align: center)[컴포넌트],
        [Requester], [결재 요청을 생성하고 관리하는 직원],
        [Approver], [결재 승인/반려하는 직원],
        [Employee Service], [직원 정보만 관리하는 서비스 (MySQL)],
        [Request Service], [전체 흐름 관리 및 기록 보관하는 서비스 (MongoDB)],
        [Processing Service], [빠르고 가벼운 결재 로직 처리하는 서비스 (In-Memory)],
        [Notification Service], [실시간 알림 담당하는 서비스 (WebSocket)],
        table.hline(stroke: 1pt + black),
        table.cell(colspan: 2, align: center)[화살표],
        table.hline(stroke: 1pt + black),
        [파란색 실선], [REST API],
        [빨간색 점선], [gRPC],
        [초록색 실선], [WebSocket],
        table.hline(),
    ),
)

#pagebreak()

#qbox()[*서비스 간 호출 흐름도*]

#image("/assets/d2.png")

#pagebreak()

#qbox()[*REST API*]

#set text(7pt)
#table(
    columns: 6,
    align: (x, y) => if y == 0 { center } else { left } + { horizon },
    [서비스], [HTTP Method], [URL], [Request Body], [Response Body], [Query Parameters],
    table.cell(rowspan: 5, align: horizon + center)[Employee Service],
    [GET],
    [/employees],
    [],
    [
        ```json
        [
          {
            "id": 0,
            "name": "string",
            "department": "string",
            "position": "string"
          },
          ...
        ]
        ```
    ],
    [department, position],

    [POST],
    [/employees],
    [
        ```json
        {
          "name": "string",
          "department": "string",
          "position": "string"
        }
        ```
    ],
    [
        ```json
        {
          "id": 0
        }
        ```
    ],
    [],

    [GET],
    [/employees/{id}],
    [],
    [
        ```json
        {
          "id": 0,
          "name": "string",
          "department": "string",
          "position": "string"
        }
        ```
    ],
    [],

    [PUT],
    [/employees/{id}],
    [
        ```json
        {
          "name": "string",
          "department": "string",
          "position": "string"
        }
        ```
    ],
    [
        ```json
        {
          "id": 0,
          "name": "string",
          "department": "string",
          "position": "string"
        }
        ```
    ],
    [],

    [DELETE], [/employees/{id}], [], [], [],

    table.cell(rowspan: 3, align: horizon + center)[Request Service],
    [POST],
    [/approvals],
    [
        ```json
        {
          "requesterId": 0,
          "title": "string",
          "content": "string",
          "steps": [
            {
              "step": 0,
              "approverId": 0
            }
          ]
        }
        ```
    ],
    [
        ```json
        {
          "requestId": "string"
        }
        ```
    ],
    [],

    [GET],
    [/approvals],
    [],
    [
        ```json
        [
          {
            "requestId": "string",
            "requesterId": 0,
            "title": "string",
            "content": "string",
            "finalStatus": "string",
            "createAt": "2024-01-01T00:00:00",
            "steps": [
              {
                "step": 0,
                "approverId": 0,
                "status": "string"
              }
            ]
          }
        ]
        ```
    ],
    [],

    [GET],
    [/approvals/{requestId}],
    [],
    [
        ```json
        {
            "requestId": "string",
            "requesterId": 0,
            "title": "string",
            "content": "string",
            "finalStatus": "string",
            "createAt": "2024-01-01T00:00:00",
            "steps": [
              {
                "step": 0,
                "approverId": 0,
                "status": "string"
              }
            ]
        }
        ```
    ],
    [],

    table.cell(rowspan: 2, align: horizon + center)[Processing Service],
    [GET],
    [/process/{approverId}],
    [],
    [
        ```json
        [
          {
            "requestId": "string",
            "requesterId": 0,
            "title": "string",
            "content": "string",
            "finalStatus": "string",
            "createAt": "2024-01-01T00:00:00",
            "steps": [
              {
                "step": 0,
                "approverId": 0,
                "status": "string"
              }
            ]
          }
        ]
        ```
    ],
    [],

    [POST], [/process/{approverId}/{requestId}], [], [], [],

    table.cell(rowspan: 2, align: horizon + center)[Notification Service],
    [POST],
    [/internal/notification/approval],
    [],
    [],
    [requesterId, requestId],

    [POST], [/internal/notification/rejection], [], [], [requesterId, requestId, rejectedBy],
    table.hline(),
)

#set text(10pt)

#pagebreak()

#qbox()[*gRPC proto 파일 내용*]


```protobuf
syntax = "proto3";
package approval;

option java_multiple_files = true;
option java_package = "kr.ac.dankook.ace.erp.proto";
option java_outer_classname = "ApprovalProto";

// Approval Processing Service와 통신하는 서비스
service Approval {
  // 결재 요청 정보를 Processing Service로 전달
  rpc RequestApproval(ApprovalRequest) returns (ApprovalResponse);
  // Processing Service로부터 결재 결과를 전달받음
  rpc ReturnApprovalResult(ApprovalResultRequest) returns (ApprovalResultResponse);
}

message Step {
  int32 step = 1;
  int32 approverId = 2;
  string status = 3; // pending, approved, rejected
}

message ApprovalRequest {
  int32 requestId = 1;
  int32 requesterId = 2;
  string title = 3;
  string content = 4;
  repeated Step steps = 5;
}

message ApprovalResponse {
  string status = 1; // "received" 등 처리 상태
}

message ApprovalResultRequest {
  int32 requestId = 1;
  int32 step = 2;
  int32 approverId = 3;
  string status = 4; // approved or rejected
}

message ApprovalResultResponse {
  string status = 1;
}
```

#pagebreak()

#qbox()[*데이터베이스 스키마 및 문서 구조*]

== MySQL (Employee Service)

#v(8pt)

*Table: `employees`*

#table(
    columns: (1fr, 1fr, 2fr),
    align: (left, left, left),
    [Column Name], [Type], [Description],
    [`id`], [BIGINT], [Primary Key, Auto Increment],
    [`name`], [VARCHAR], [Not Null, 사원명],
    [`department`], [VARCHAR], [Not Null, 부서명],
    [`position`], [VARCHAR], [Not Null, 직급],
    [`created_at`], [DATETIME], [생성일시],
    table.hline(),
)

#v(12pt)

== MongoDB (Request Service)

#v(8pt)

*Collection: `approval`*

```json
{
  "_id": "requestId (String)",
  "requesterId": 12345 (Integer),
  "title": "휴가 신청서 (String)",
  "content": "개인 사유로 연차 신청합니다. (String)",
  "finalStatus": "pending | approved | rejected",
  "createAt": "2024-01-01T09:00:00 (DateTime)",
  "steps": [
    {
      "step": 1,
      "approverId": 1001,
      "status": "approved"
    },
    {
      "step": 2,
      "approverId": 1002,
      "status": "pending"
    }
  ]
}
```

#pagebreak()

#qbox()[*WebSocket 메시지 구조*]

== Notification Service

#v(8pt)

실시간 알림은 WebSocket을 통해 전달되며, 결재 승인 또는 반려 이벤트 발생 시 클라이언트에게 JSON 형식의 메시지를 전송

*Message Format (ApprovalNotificationDto)*

```json
{
  "requestId": "507f1f77bcf86cd799439011",
  "result": "approved",       // "approved" or "rejected"
  "rejectedBy": 1002,         // 반려 시에만 존재 (Optional)
  "finalResult": "approved"   // 최종 결재 상태
}
```

#pagebreak()

#qbox()[*실행 방법*]

#v(8pt)

Java 및 Docker 설치

```bash
sudo apt-get install ca-certificates curl -y
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \$(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin openjdk-25-jdk -y
```

Docker로 MySQL 및 MongoDB 실행

```bash
docker run --name erp-mysql \
    --rm --detach \
    --env MYSQL_ROOT_PASSWORD=Alpha10@ \
    --env MYSQL_DATABASE=erp \
    --publish 3306:3306 \
    mysql:lts

docker run --name erp-mongo \
    --rm --detach \
    --publish 27017:27017 \
    mongo:latest
```


`32205083` 디렉토리에서 각 서비스들 실행

```bash
cd employee-service
nohup ./gradlew bootRun &

cd ../approval-request-service
nohup ./gradlew bootRun &

cd ../approval-processing-service
nohup ./gradlew bootRun &

cd ../notification-service
nohup ./gradlew bootRun &
```

#pagebreak()

#qbox()[*테스트 시나리오*]

#v(8pt)

K6를 통해 테스트

- JavaScript를 사용하여 스크립트를 작성하며 HTTP, WebSocket 등 다양한 프로토콜을 지원
- 3가지 주요 시나리오에 대한 테스트를 수행
- 각 시나리오는 WebSocket 연결을 포함하여 실제 서비스 흐름과 유사하게 구성

== Approval Scenario (정상 승인 흐름)

- *개요*: 요청자가 결재를 상신하고, 2명의 결재자가 순차적으로 승인하는 Happy Path를 검증한다.
- *주요 검증 항목*
    - 직원(요청자, 결재자) 생성 및 ID 발급
    - WebSocket 실시간 연결 성공 여부
    - 결재 요청 생성 (POST /approvals)
    - 단계별 승인 처리 (POST /process) 및 최종 승인 상태 확인

```javascript
export function approvalFlow() {
    console.log('[Approval Scenario] Starting...');

    // 1. 직원 생성 (요청자 1명, 결재자 2명)
    const requesterId = createEmployee("Appr_Req", "Sales", "Staff");
    const approver1Id = createEmployee("Appr_Mgr", "Sales", "Manager");
    const approver2Id = createEmployee("Appr_Dir", "Sales", "Director");

    if (!requesterId || !approver1Id || !approver2Id) return;

    // 2. WebSocket 연결 및 시나리오 진행
    const wsUrl = `${WEBSOCKET_URL}?id=${requesterId}`;
    const res = ws.connect(wsUrl, {}, function (socket) {
        socket.on('open', function () {
            // 결재 요청 생성
            const requestId = createApproval(requesterId, [approver1Id, approver2Id]);
            if (!requestId) { socket.close(); return; }

            // Step 1: 첫 번째 결재자 승인
            sleep(1);
            processStep(approver1Id, requesterId, "Approver 1");

            // Step 2: 두 번째 결재자 승인 (최종 승인)
            sleep(1);
            processStep(approver2Id, requesterId, "Approver 2");

            sleep(2);
            socket.close();
        });
        // ... 오류 처리 및 종료 로직 ...
    });
    // WebSocket 연결 성공(101) 확인
    check(res, { '[Approval] WS status 101': (r) => r && r.status === 101 });
}
```

== Rejection Scenario (반려 흐름)

- *개요*: 결재 진행 중 반려(Reject)가 발생했을 때의 예외 처리 및 알림을 검증한다.
- *주요 검증 항목*:
    - 결재 요청 생성
    - 첫 번째 결재자에 의한 반려 처리
    - 반려 상태 기록 및 WebSocket 알림 발송 확인

```javascript
export function rejectionFlow() {
    console.log('[Rejection Scenario] Starting...');
    const requesterId = createEmployee("Rej_Req", "IT", "Staff");
    const approver1Id = createEmployee("Rej_Mgr", "IT", "Manager");

    const wsUrl = `${WEBSOCKET_URL}?id=${requesterId}`;
    const res = ws.connect(wsUrl, {}, function (socket) {
        socket.on('open', function () {
            const requestId = createApproval(requesterId, [approver1Id]);

            // 승인 대신 반려 프로세스 진행 (시뮬레이션)
            processStep(approver1Id, requesterId, "Approver 1 (Simulated Rejection)");

            sleep(1);
            socket.close();
        });
    });
}
```

== Concurrency Scenario (동시성 테스트)

- *개요*: 다수의 사용자가 동시에 결재를 요청하고 승인할 때 서버의 안정성을 테스트한다.
- *설정*: 10명의 가상 사용자(VUs), 총 20회 반복 수행
- *주요 검증 항목*:
    - 대량의 트래픽에도 서비스가 정상 응답(200 OK)을 반환하는지 확인한다.

```javascript
export function concurrencyFlow() {
    const requesterId = createEmployee("Conc_Req", "Ops", "Staff");
    const approverId = createEmployee("Conc_Mgr", "Ops", "Manager");

    // 간단한 단일 단계 결재 요청 및 승인 반복
    const requestId = createApproval(requesterId, [approverId]);
    if (requestId) {
        processStep(approverId, requesterId, "Concurrency Approver");
    }
}
```

#pagebreak()

#qbox()[*실행 화면*]

#v(8pt)

MySQL 및 MongoDB 도커로 실행

#image("/assets/image-1.png")

연결 확인

#image("/assets/image-2.png")

#pagebreak()

각 서비스들 모두 실행

#image("/assets/image-3.png")

K6로 테스트

#image("/assets/image-4.png")

#pagebreak()

테스트 총 로그

```

         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/

     execution: local
        script: .\k6-test-flow.js
        output: -

     scenarios: (100.00%) 3 scenarios, 12 max VUs, 10m40s max duration (incl. graceful stop):
              * approval_scenario: 1 iterations for each of 1 VUs (maxDuration: 10m0s, exec: approvalFlow, gracefulStop: 30s)
              * rejection_scenario: 1 iterations for each of 1 VUs (maxDuration: 10m0s, exec: rejectionFlow, startTime: 10s, gracefulStop: 30s)
              * concurrency_scenario: 20 iterations shared among 10 VUs (maxDuration: 1m0s, exec: concurrencyFlow, startTime: 20s, gracefulStop: 30s)

INFO[0000] [Approval Scenario] Starting...               source=console
INFO[0000] [Approval Scenario] WS Connected              source=console
INFO[0006] [Approval Scenario] WS Closed                 source=console
INFO[0010] [Rejection Scenario] Starting...              source=console
INFO[0010] [Rejection Scenario] WS Connected             source=console
INFO[0011] [Rejection Scenario] Stop flow after simulated rejection/process  source=console


  █ TOTAL RESULTS

    checks_total.......: 47      2.281113/s
    checks_succeeded...: 100.00% 47 out of 47
    checks_failed......: 0.00%   0 out of 47

    ✓ Approval Created (201)
    ✓ Approver 1 Processed (200)
    ✓ Approver 2 Processed (200)
    ✓ [Approval] WS status 101
    ✓ Approver 1 (Simulated Rejection) Processed (200)
    ✓ [Rejection] WS status 101
    ✓ Concurrency Approver Processed (200)

    HTTP
    http_req_duration..............: avg=82.75ms  min=14.95ms  med=50.26ms  max=1s       p(90)=135.33ms p(95)=155.54ms
      { expected_response:true }...: avg=82.75ms  min=14.95ms  med=50.26ms  max=1s       p(90)=135.33ms p(95)=155.54ms
    http_req_failed................: 0.00% 0 out of 90
    http_reqs......................: 90    4.368089/s

    EXECUTION
    iteration_duration.............: avg=643.14ms min=222.17ms med=283.26ms max=6.26s    p(90)=374.19ms p(95)=2.07s
    iterations.....................: 22    1.067755/s
    vus............................: 0     min=0       max=1
    vus_max........................: 12    min=12      max=12

    NETWORK
    data_received..................: 12 kB 562 B/s
    data_sent......................: 18 kB 852 B/s

    WEBSOCKET
    ws_connecting..................: avg=90.48ms  min=16.19ms  med=90.48ms  max=164.77ms p(90)=149.91ms p(95)=157.34ms
    ws_session_duration............: avg=3.9s     min=2.11s    med=3.9s     max=5.68s    p(90)=5.32s    p(95)=5.5s
    ws_sessions....................: 2     0.097069/s




running (00m20.6s), 00/12 VUs, 22 complete and 0 interrupted iterations
approval_scenario    ✓ [======================================] 1 VUs   00m06.3s/10m0s  1/1 iters, 1 per VU
rejection_scenario   ✓ [======================================] 1 VUs   00m02.2s/10m0s  1/1 iters, 1 per VU
concurrency_scenario ✓ [======================================] 10 VUs  0m00.6s/1m0s    20/20 shared iters
```

#pagebreak()

#qbox()[*쿠버네티스 배포*]

모든 리소스는 `erp` 네임스페이스 하에 배포

=== Namespace

- `namespace.yaml`: `erp` 네임스페이스 생성

=== Database

- `db.yaml`: MySQL와 MongoDB 데이터베이스 리소스 정의

=== Microservices

각 마이크로서비스는 애플리케이션의 배포를 위한 `Deployment`와 내부 통신을 위한 `Service`로 구성

- *Approval Request Service*: 결재 요청 관리 (`approval-request-service-deployment.yaml`, `approval-request-service-service.yaml`)
- *Approval Processing Service*: 결재 처리 로직 (`approval-processing-service-deployment.yaml`, `approval-processing-service-service.yaml`)
- *Notification Service*: 알림 발송 (`notification-service-deployment.yaml`, `notification-service-service.yaml`)
- *Employee Service*: 사원 정보 관리 (`employee-service-deployment.yaml`, `employee-service-service.yaml`)

=== Ingress

- `ingress.yaml`: 클러스터 외부(`erp.local`)에서 내부 서비스로의 접근을 관리하며, 경로 기반 라우팅 설정

=== 실행 및 검증

`gradlew`를 사용하여 이미지 빌드

```bash
cd employee-service
./gradlew bootBuildImage &

cd ../approval-request-service
./gradlew bootBuildImage &

cd ../approval-processing-service
./gradlew bootBuildImage &

cd ../notification-service
./gradlew bootBuildImage &
```

생성된 이미지 확인

#image("/assets/image-6.png")

#pagebreak()

미리 만들어 놓은 깃허브 토큰을 통하여 깃허브 패키지에 이미지 푸시

#image("/assets/image-7.png")

깃허브 패키지 설정에서 푸시된 4개의 이미지 모두 액세스 권한 public으로 변경

#image("/assets/image-8.png")

#pagebreak()

쿠버네티스에 배포

#image("/assets/image-5.png", height: 30%)

