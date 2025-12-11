import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const SERVER_ADDRESS = 'dream-server';
const EMPLOYEE_SERVICE_URL = `http://${SERVER_ADDRESS}:8080`;
const APPROVAL_REQUEST_SERVICE_URL = `http://${SERVER_ADDRESS}:8081`;
const APPROVAL_PROCESSING_SERVICE_URL = `http://${SERVER_ADDRESS}:8083`;
const WEBSOCKET_URL = `ws://${SERVER_ADDRESS}:8082/ws`;

export const options = {
    scenarios: {
        approval_scenario: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            exec: 'approvalFlow',
            startTime: '0s',
        },
        rejection_scenario: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            exec: 'rejectionFlow',
            startTime: '10s',
        },
        concurrency_scenario: {
            executor: 'shared-iterations',
            vus: 10,
            iterations: 20,
            exec: 'concurrencyFlow',
            startTime: '20s',
            maxDuration: '1m',
        },
    },
};

// --- Helper Functions ---

function createEmployee(prefix, department, position) {
    const uniqueName = `${prefix}_${randomString(5)}`;
    const url = `${EMPLOYEE_SERVICE_URL}/employees`;
    const payload = JSON.stringify({
        name: uniqueName,
        department: department,
        position: position
    });
    const params = { headers: { 'Content-Type': 'application/json' } };

    const res = http.post(url, payload, params);

    if (res.status === 201) {
        return JSON.parse(res.body).id;
    } else {
        console.error(`Failed to create employee ${uniqueName}: ${res.status} ${res.body}`);
        return null;
    }
}

function createApproval(requesterId, approverIds) {
    const url = `${APPROVAL_REQUEST_SERVICE_URL}/approvals`;
    const steps = approverIds.map((id, index) => ({ step: index + 1, approverId: id }));

    const payload = JSON.stringify({
        requesterId: requesterId,
        title: `Test Approval ${randomString(5)}`,
        content: "Please approve this request",
        steps: steps
    });
    const params = { headers: { 'Content-Type': 'application/json' } };

    const res = http.post(url, payload, params);

    check(res, { 'Approval Created (201)': (r) => r.status === 201 });

    if (res.status === 201) {
        return JSON.parse(res.body).requestId;
    }
    return null;
}

function processStep(approverId, processingRequestId, stepName) {
    const url = `${APPROVAL_PROCESSING_SERVICE_URL}/process/${approverId}/${processingRequestId}`;

    const res = http.post(url, null, {});
    check(res, { [`${stepName} Processed (200)`]: (r) => r.status === 200 });
    return res.status === 200;
}

// --- Scenarios ---

export function approvalFlow() {
    console.log('[Approval Scenario] Starting...');

    // 1. Setup Employees
    const requesterId = createEmployee("Appr_Req", "Sales", "Staff");
    const approver1Id = createEmployee("Appr_Mgr", "Sales", "Manager");
    const approver2Id = createEmployee("Appr_Dir", "Sales", "Director");

    if (!requesterId || !approver1Id || !approver2Id) return;

    // 2. Connect WS and Run Flow
    const wsUrl = `${WEBSOCKET_URL}?id=${requesterId}`;
    const res = ws.connect(wsUrl, {}, function (socket) {
        socket.on('open', function () {
            console.log('[Approval Scenario] WS Connected');

            const requestId = createApproval(requesterId, [approver1Id, approver2Id]);
            if (!requestId) { socket.close(); return; }

            // Step 1: Approver 1 approves
            sleep(1);
            processStep(approver1Id, requesterId, "Approver 1"); // Note: passing requesterId as processingRequestId (legacy logic)

            // Step 2: Approver 2 approves
            sleep(1);
            processStep(approver2Id, requesterId, "Approver 2");

            sleep(2); // Wait for finalization
            socket.close();
        });

        socket.on('close', () => console.log('[Approval Scenario] WS Closed'));
        socket.setTimeout(() => socket.close(), 10000);
    });

    check(res, { '[Approval] WS status 101': (r) => r && r.status === 101 });
}

export function rejectionFlow() {
    console.log('[Rejection Scenario] Starting...');

    const requesterId = createEmployee("Rej_Req", "IT", "Staff");
    const approver1Id = createEmployee("Rej_Mgr", "IT", "Manager");

    if (!requesterId || !approver1Id) return;

    const wsUrl = `${WEBSOCKET_URL}?id=${requesterId}`;
    const res = ws.connect(wsUrl, {}, function (socket) {
        socket.on('open', function () {
            console.log('[Rejection Scenario] WS Connected');

            const requestId = createApproval(requesterId, [approver1Id]);
            if (!requestId) { socket.close(); return; }

            sleep(1);

            processStep(approver1Id, requesterId, "Approver 1 (Simulated Rejection)");

            console.log('[Rejection Scenario] Stop flow after simulated rejection/process');
            sleep(1);
            socket.close();
        });

        socket.setTimeout(() => socket.close(), 8000);
    });

    check(res, { '[Rejection] WS status 101': (r) => r && r.status === 101 });
}

export function concurrencyFlow() {
    const requesterId = createEmployee("Conc_Req", "Ops", "Staff");
    const approverId = createEmployee("Conc_Mgr", "Ops", "Manager");

    if (!requesterId || !approverId) return;

    const requestId = createApproval(requesterId, [approverId]);
    if (requestId) {
        processStep(approverId, requesterId, "Concurrency Approver");
    }
}

