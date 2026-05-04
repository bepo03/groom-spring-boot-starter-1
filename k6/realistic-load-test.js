// k6/realistic-load-test.js
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const getUserDuration = new Trend('get_user_duration');

export const options = {
    // Ramp-up -> Sustain -> Ramp-up -> Sustain -> Ramp-down 패턴
    stages: [
        { duration: '2m', target: 100 },    // 2분 동안 100 VU 까지
        { duration: '5m', target: 100 },    // 5분 유지
        { duration: '2m', target: 300 },    // 2분 동안 300 VU 까지
        { duration: '5m', target: 300 },    // 5분 유지
        { duration: '2m', target: 0 },      // 2분 동안 0으로 감소
    ],

    // SLO를 합격 기준으로 선언 (자동 채점)
    thresholds: {
        'http_req_duration': ['p(95)<300', 'p(99)<500'],    // p95 < 300ms, p99 < 500ms
        'http_req_failed': ['rate<0.01'],                   // 에러율 1% 미만
        'errors': ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18081';

export default function () {
    group('User API', () => {
        // 1. 리스트 조회
       const listRes = http.get(`${BASE_URL}/api/v1/profiles?page=0&size=20`);
       check(listRes, { 'list 200': (r) => r.status ===200 }) || errorRate.add(1);

       sleep(Math.random() * 2 +1);

       // 2. 단건 조회 (랜덤 ID)
        const profileId = Math.floor(Math.random() * 10000) + 1;
        const getRes = http.get(`${BASE_URL}/api/v1/profiles/${profileId}`);
        check(getRes, {
            'get 200 or 404': (r) => r.status === 200 || r.status === 404,
        }) || errorRate.add(1);

        getUserDuration.add(getRes.timings.duration);

        sleep(Math.random() * 3 + 1);
    });
}

// 테스트 종료 시 결과 파일 생성
export function handleSummary(data) {
    return {
        'k6/summary.json': JSON.stringify(data, null, 2),
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    };
}

import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';
