import http from 'k6/http';

export let options = {
  vus: 50,
  duration: '30s',
};

export default function () {
  const url = 'http://localhost:8080/contas-versionadas/1/deposito';
  const payload = JSON.stringify({ valor: 10.00 });
  const params = { headers: { 'Content-Type': 'application/json' } };
  http.post(url, payload, params);
}
