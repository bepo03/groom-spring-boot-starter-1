// k6/open-model.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        constant_request_rate: {
          executor: 'constant-arrival-rate',
          rate: 500,
          timeUnit: '1s',
          duration: '5m',
          preAllocatedVUs: 200,
          maxVUs: 500,
        },
    },
    thresholds: {
      http_req_duration: ['p(95)<200', 'p(99)<500'],
      http_req_failed: ['rate<0.001'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18081';

export default function () {
    const res = http.get(`${BASE_URL}/api/v1/profiles/1`);
    check(res, { 'status 200': (r) => r.status === 200 });
    // Open Model 에서는 sleep 없이도 OK - 도착률이 이미 제어됨
}