import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  vus: 50,
  duration: '30s',
};

export default function () {
  const url = 'http://localhost:8080/contas/1/deposito';
  const payload = JSON.stringify({ valor: 10.00 });
  const params = { headers: { 'Content-Type': 'application/json' } };
  http.post(url, payload, params);
}
