/* ===========================
   공통 Fetch 유틸 (alert 전용)
   =========================== */

function makeHandledError(tag, msg, { silenceToast = false } = {}) {
  const err = new Error(msg || tag || 'HANDLED');
  err.name = 'HandledUiError';
  // 전역(토스트/알림) 핸들러가 또 띄우지 않도록 플래그
  err.__silenceToast = !!silenceToast;
  err.statusTag = tag;
  return err;
}

/** OK 응답 본문 파싱 (json 우선 → text → null) */
async function parseBody(res) {
  if (res.status === 204) return null;
  const raw = await res.text().catch(() => '');
  if (!raw) return null;
  try { return JSON.parse(raw); } catch { return raw; }
}

/** 공통 응답 처리 */
async function handleResponse(res) {
  if (res.ok) return res;

  // 401: 조용히 로그인 페이지로 이동 (alert X)
  if (res.status === 401) {
    location.href = '/common/login';
    throw makeHandledError('UNAUTHORIZED', '로그인이 필요합니다.', { silenceToast: true });
  }

  // 403: 서버 바디가 뭐든 간에 alert만 1번
  if (res.status === 403) {
    let msg = '해당 기능 권한이 없습니다.';
    try {
      const ct = (res.headers.get('content-type') || '').toLowerCase();
      if (ct.includes('application/json')) {
        const obj = await res.clone().json().catch(() => null);
        if (obj?.message && String(obj.message).trim()) msg = String(obj.message).trim();
      } else {
        const txt = await res.clone().text().catch(() => '');
        if (txt && txt.trim()) msg = txt.trim();
      }
    } catch {}

    // ✅ 여기서 즉시 alert 1회만
    alert(msg);

    // 그리고 전역 핸들러가 또 띄우지 못하게 “조용한” 에러로 던짐
    throw makeHandledError('FORBIDDEN', msg, { silenceToast: true });
  }

  // 그 외 에러: 메시지 뽑아서 alert 1회
  let msg = `요청 실패 (HTTP ${res.status})`;
  try {
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (ct.includes('application/json')) {
      const obj = await res.clone().json().catch(() => null);
      if (obj?.message && String(obj.message).trim()) msg = String(obj.message).trim();
    } else {
      const txt = await res.clone().text().catch(() => '');
      if (txt && txt.trim()) msg = txt.trim();
    }
  } catch {}
  alert(msg);
  throw makeHandledError('ERROR', msg, { silenceToast: true });
}

/** 내부 공통 요청 */
async function request(url, init = {}, opt = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), opt.timeoutMs ?? 15000);
  try {
    const res = await fetch(url, { signal: controller.signal, ...init });
    await handleResponse(res);
    return await parseBody(res);
  } finally {
    clearTimeout(timer);
  }
}

/* ====== 공개 유틸 ====== */
async function jget(url, opt = {}) {
  return request(url, { headers: { 'Accept': 'application/json', ...(opt.headers || {}) } }, opt);
}
async function jpost(url, body, opt = {}) {
  return request(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', ...(opt.headers || {}) },
    body: JSON.stringify(body)
  }, opt);
}
async function jput(url, body, opt = {}) {
  return request(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', ...(opt.headers || {}) },
    body: JSON.stringify(body)
  }, opt);
}
async function jdel(url, opt = {}) {
  return request(url, {
    method: 'DELETE',
    headers: { 'Accept': 'application/json', ...(opt.headers || {}) },
  }, opt);
}
