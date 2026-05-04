// k6/load-test.js
import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    vus: 50,        // 사용자 50명
    duration: '1m', // 1분
};

export default function () {
    const res = http.get('http://localhost:18081/api/v1/profiles');

    check(res, {
       'status is 200': (r) => r.status === 200,
        'response time < 300ms': (r) => r.timings.duration < 300,
    });

    sleep(1);
}
