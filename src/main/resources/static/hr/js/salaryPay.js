function _getTable() {
	return document.querySelector('#payDetailModal #employeeTable');
}
function _getPickAll() {
	return document.querySelector('#payDetailModal #pickAll');
}
function _picks(table) {
	return table ? table.querySelectorAll('tbody input.pick[type="checkbox"]') : [];
}
function _syncHeader(table) {
	const head = _getPickAll();
	const all = _picks(table);
	const total = all.length;
	const checked = Array.from(all).filter(cb => cb.checked).length;
	if (!head) return;
	if (total === 0) { head.checked = false; head.indeterminate = false; return; }
	if (checked === total) { head.checked = true; head.indeterminate = false; }
	else if (checked === 0) { head.checked = false; head.indeterminate = false; }
	else { head.checked = false; head.indeterminate = true; }
}

// 헤더 전체선택 (위임)
document.addEventListener('change', (e) => {
	if (!e.target.matches('#payDetailModal #pickAll')) return;
	const table = _getTable();
	_picks(table).forEach(cb => cb.checked = e.target.checked);
	_syncHeader(table);
});

// 개별 체크박스 변경 → 헤더 상태 동기화 (위임)
document.addEventListener('change', (e) => {
	if (!e.target.matches('#payDetailModal tbody input.pick[type="checkbox"]')) return;
	_syncHeader(_getTable());
});

// 행 클릭으로 토글 (위임)
document.addEventListener('click', (e) => {
	const row = e.target.closest('#payDetailModal #employeeTable tbody tr');
	if (!row) return;
	if (e.target.closest('input,button,a,label,textarea,select,.form-check')) return;
	const cb = row.querySelector('input.pick[type="checkbox"]');
	if (!cb) return;
	cb.checked = !cb.checked;
	_syncHeader(_getTable());
});

// 모달 열릴 때 헤더 상태 초기화(부트스트랩 이벤트)
document.addEventListener('shown.bs.modal', (e) => {
	if (e.target.id !== 'payDetailModal') return;
	_syncHeader(_getTable());
});

// 인쇄
function payPrint() {
	const meta = document.getElementById('salaryMeta');
	if (!meta) { alert('라벨 메타가 없습니다.'); return; }

	// ---------- helpers ----------
	const num = (v) => Number(v ?? 0) || 0;
	const money = (n) => Number.isFinite(n = Number(n)) ? n.toLocaleString('ko-KR') : '0';
	const ymTitle = (ym) => {
		const m = String(ym || '').match(/^(\d{4})[-./]?(\d{2})/);
		return m ? `${m[1]}년 ${m[2]}월` : (ym || '');
	};
	const normDate = (s) => {
		if (!s) return '';
		const t = String(s);
		// YYYYMMDD -> YYYY-MM-DD
		if (/^\d{8}$/.test(t)) return `${t.slice(0, 4)}-${t.slice(4, 6)}-${t.slice(6, 8)}`;
		// YYYY[-./]MM[-./]DD or YYYY[-./]MM
		return t.replace(/[.]/g, '-').replace(/\//g, '-').slice(0, 10);
	};
	// dataset 키 접근 (all-01 / all01 / all1 모두 시도)
	const readMetaLabel = (ds, prefix, i) => {
		const ii = String(i).padStart(2, '0');
		return ds[`${prefix}${ii}`] ?? ds[`${prefix}${i}`] ?? '';
	};
	const readRowAmt = (ds, prefix, i) => {
		const ii = String(i).padStart(2, '0');
		return num(ds[`${prefix}${ii}`] ?? ds[`${prefix}${i}`]);
	};

	// ---------- meta ----------
	const ds = meta.dataset;
	const PAY_YM = ds.payYm || '';
	const PAY_DATE = normDate(ds.payDate);

	// 라벨 맵: 1..10
	const ALL_LABELS = {}, DED_LABELS = {};
	for (let i = 1; i <= 10; i++) {
		ALL_LABELS[i] = readMetaLabel(ds, 'all', i) || `수당${i}`;
		DED_LABELS[i] = readMetaLabel(ds, 'ded', i) || `공제${i}`;
	}

	// ---------- rows (selected) ----------
	const picks = Array.from(document.querySelectorAll('#employeeTable tbody input.pick:checked'));
	if (picks.length === 0) { alert('인쇄할 사원을 선택하세요.'); return; }

	// 사번 기준 정렬
	picks.sort((a, b) => String(a.dataset.empno).localeCompare(String(b.dataset.empno), 'ko', { numeric: true }));

	const slipsHtml = picks.map(cb => {
		const d = cb.dataset;

		const empCode = d.empno || '';
		const name = d.name || '';
		const dept = d.dept || '';
		const pos = d.pos || '';

		const salary = num(d.salary);
		const allTotal = num(d.alltotal);
		const dedTotal = num(d.dedtotal);
		const netPay = num(d.netpay);
		const paySum = salary + allTotal;

		// 수당 / 공제 항목 (0 제외)
		const aArr = [], gArr = [];
		for (let i = 1; i <= 10; i++) {
			const aAmt = readRowAmt(d, 'all', i);
			if (aAmt !== 0) aArr.push({ label: ALL_LABELS[i], amt: aAmt });

			const gAmt = readRowAmt(d, 'ded', i);
			if (gAmt !== 0) gArr.push({ label: DED_LABELS[i], amt: gAmt });
		}

		// 표 본문 행
		const rows = [];
		rows.push(`
      <tr>
        <td class="bold tc">기본급</td>
        <td class="tr bold">${money(salary)}</td>
        <td></td><td></td>
      </tr>`);

		const max = Math.max(aArr.length, gArr.length);
		for (let i = 0; i < max; i++) {
			const a = aArr[i], g = gArr[i];
			rows.push(`
        <tr>
          <td>${a ? (a.label || '수당') : ''}</td>
          <td class="tr">${a ? money(a.amt) : ''}</td>
          <td>${g ? (g.label || '공제') : ''}</td>
          <td class="tr">${g ? money(g.amt) : ''}</td>
        </tr>`);
		}

		rows.push(`
      <tr class="sum-row">
        <th class="tc">공제계</th>
        <td class="tr">${money(dedTotal)}</td>
        <th class="tc">급여계</th>
        <td class="tr">${money(paySum)}</td>
      </tr>
      <tr class="sum-row">
        <th class="tc">차감수령액</th>
        <td class="tr bold" colspan="3">${money(netPay)}</td>
      </tr>`);

		// 한 명 명세서
		return `
    <div class="slip">
      <div class="slip-header">
        <div class="title">${ymTitle(PAY_YM)} 급여명세서</div>
      </div>
      <table class="meta">
        <tr>
          <td class="label">성 명</td><td class="value">${name}</td>
          <td class="label">부 서</td><td class="value">${dept}</td>
          <td class="label">직 책</td><td class="value">${pos}</td>
          <td class="label">사 번</td><td class="value">${empCode}</td>
          <td class="label">지급일</td><td class="value">${PAY_DATE}</td>
        </tr>
      </table>
      <table class="pay">
        <thead>
          <tr>
            <th class="tc">지급항목</th>
            <th class="tc" style="width:140px;">지급액</th>
            <th class="tc">공제항목</th>
            <th class="tc" style="width:140px;">공제액</th>
          </tr>
        </thead>
        <tbody>${rows.join('')}</tbody>
      </table>
      <div class="footer">
        <div class="thanks">귀하의 노고에 감사드립니다.</div>
      </div>
    </div>`;
	}).join('');

	// ---------- print window ----------
	const html = `
  <!doctype html>
  <html>
  <head>
    <meta charset="utf-8" />
    <title>급여명세서</title>
    <style>
  @page { size: A4 portrait; margin: 12mm; }
  * { box-sizing: border-box; }
  body { font-family: "Malgun Gothic","맑은 고딕",system-ui,-apple-system,Arial,sans-serif; color:#111; margin:0; }
  .slip { page-break-inside: avoid; margin-bottom: 14mm; }
  .slip + .slip { page-break-before: always; }
  .slip-header { display:flex; align-items:flex-start; gap:8px; margin-bottom:6px; }
  .title { font-weight:800; font-size:20px; letter-spacing:-.5px; border-top:3px solid #000; padding-top:6px; }
  .meta{
    width:100%;
    border:1px solid #000;
    margin-bottom:6px;
    font-size:12px;
    table-layout:fixed;
    border-collapse:collapse;
  }
  .meta td{
    border:1px solid #000;
    padding:4px 6px;
    vertical-align:middle;
  }
  .meta .label{ width:60px; color:#333; text-align:center; }
  .meta .value{ font-weight:600; }

  table.pay{
    width:100%;
    border-collapse:collapse;
    table-layout:fixed;
    border:1px solid #000;
  }
  table.pay th,
  table.pay td{
    padding:6px 8px;
    font-size:12px;
    vertical-align:middle;
  }
  table.pay thead th{
    text-align:center;
    background:#f2f3f7;
    border-bottom:1px solid #000;
  }
  table.pay thead th + th{ border-left:1px solid #000; }
  table.pay td{ border-left:1px dotted #9aa0a6; }
  table.pay td:first-child{ border-left:none; }
  table.pay tbody tr td,
  table.pay tbody tr th{ border-bottom:1px dotted #9aa0a6; }
  table.pay tbody tr:last-child td,
  table.pay tbody tr:last-child th{ border-bottom:none; }

  .tc{ text-align:center; }
  .tr{ text-align:right; }
  .bold{ font-weight:700; }

  .sum-row th,
  .sum-row td{
    border-top:1px solid #000 !important;
    border-bottom:1px solid #000 !important;
    background:#fafbff;
    font-weight:700;
  }

  .footer{ margin-top:6px; font-size:11px; display:flex; align-items:center; }
  .footer .thanks{ color:#666; }
  .footer .corp{ margin-left:auto; }
</style>

  </head>
  <body>
    ${slipsHtml}
    <script>window.addEventListener('load',()=>window.print());<\/script>
  </body>
  </html>`;

	const w = window.open('', '_blank');
	if (!w) { alert('팝업이 차단되었습니다. 허용 후 다시 시도하세요.'); return; }
	w.document.open(); w.document.write(html); w.document.close();
}

// 엑셀
function payExcel() {
	const meta = document.getElementById('salaryMeta');
	if (!meta) { alert('라벨 메타가 없습니다.'); return; }

	// ---------- helpers ----------
	const num = (v) => Number(v ?? 0) || 0;
	const money = (n) => Number.isFinite(n = Number(n)) ? n.toLocaleString('ko-KR') : '0';
	const normDate = (s) => {
		if (!s) return '';
		const t = String(s);
		if (/^\d{8}$/.test(t)) return `${t.slice(0, 4)}-${t.slice(4, 6)}-${t.slice(6, 8)}`;
		return t.replace(/[.]/g, '-').replace(/\//g, '-').slice(0, 10);
	};
	const readMetaLabel = (ds, prefix, i) => {
		const ii = String(i).padStart(2, '0');
		return ds[`${prefix}${ii}`] ?? ds[`${prefix}${i}`] ?? '';
	};
	const readRowAmt = (ds, prefix, i) => {
		const ii = String(i).padStart(2, '0');
		return num(ds[`${prefix}${ii}`] ?? ds[`${prefix}${i}`]);
	};

	// ---------- meta ----------
	const ds = meta.dataset;
	const PAY_YM = ds.payYm || '';
	const PAY_DATE = normDate(ds.payDate);

	// 라벨 맵
	const ALL_LABELS = {}, DED_LABELS = {};
	for (let i = 1; i <= 10; i++) {
		ALL_LABELS[i] = readMetaLabel(ds, 'all', i) || `수당${i}`;
		DED_LABELS[i] = readMetaLabel(ds, 'ded', i) || `공제${i}`;
	}

	// ---------- picks ----------
	const picks = Array.from(document.querySelectorAll('#employeeTable tbody input.pick:checked'));
	if (picks.length === 0) { alert('엑셀로 내보낼 사원을 선택하세요.'); return; }

	// 사번 정렬
	picks.sort((a, b) => String(a.dataset.empno).localeCompare(String(b.dataset.empno), 'ko', { numeric: true }));

	// ---------- make sheet (HTML-based .xls) ----------
	const parts = [];

	// 스타일: 엑셀에서 테두리/정렬 보이게
	const style = `
  <style>
    table { border-collapse:collapse; font-family:"Malgun Gothic","맑은 고딕"; font-size:10pt; }
    td, th { border:1px solid #000; padding:4px 6px; vertical-align:middle; mso-number-format:"\\@"; }
    .tr { text-align:right; mso-number-format:"\\#\\,\\#\\#0"; }
    .tc { text-align:center; }
    .title { font-weight:800; font-size:12pt; border-top:3px solid #000; }
    .meta td { font-weight:600; }
    .label { background:#f2f3f7; font-weight:400; text-align:center; }
    .sum { background:#fafbff; font-weight:700; }
  </style>`;

	parts.push(`
  <html xmlns:o="urn:schemas-microsoft-com:office:office"
        xmlns:x="urn:schemas-microsoft-com:office:excel"
        xmlns="http://www.w3.org/TR/REC-html40">
  <head><meta charset="UTF-8">${style}</head><body>`);

	picks.forEach((cb, idx) => {
		const d = cb.dataset;

		const empCode = d.empno || '';
		const name = d.name || '';
		const dept = d.dept || '';
		const pos = d.pos || '';

		const salary = num(d.salary);
		const allTotal = num(d.alltotal);
		const dedTotal = num(d.dedtotal);
		const netPay = num(d.netpay);
		const paySum = salary + allTotal;

		// 수당/공제(0 제외)
		const aArr = [], gArr = [];
		for (let i = 1; i <= 10; i++) {
			const aAmt = readRowAmt(d, 'all', i);
			if (aAmt !== 0) aArr.push({ label: ALL_LABELS[i], amt: aAmt });

			const gAmt = readRowAmt(d, 'ded', i);
			if (gAmt !== 0) gArr.push({ label: DED_LABELS[i], amt: gAmt });
		}
		const max = Math.max(aArr.length, gArr.length);

		// 타이틀/메타
		parts.push(`
      <table style="width:100%; border:0; margin-bottom:2px;">
        <tr>
          <td class="title" style="border:0; padding:6px 0;">${PAY_YM} 급여명세서</td>
        </tr>
      </table>
      <table class="meta" style="width:100%; margin-bottom:6px;">
        <tr>
          <td class="label">성명</td><td>${name}</td>
          <td class="label">부서</td><td>${dept}</td>
          <td class="label">직책</td><td>${pos}</td>
          <td class="label">사번</td><td>${empCode}</td>
          <td class="label">지급일</td><td>${PAY_DATE}</td>
        </tr>
      </table>
    `);

		// 본표
		parts.push(`
      <table style="width:100%;">
        <tr>
          <th class="tc">지급항목</th>
          <th class="tc" style="width:140px;">지급액</th>
          <th class="tc">공제항목</th>
          <th class="tc" style="width:140px;">공제액</th>
        </tr>
        <tr>
          <td class="tc" style="font-weight:700;">기본급</td>
          <td class="tr" style="font-weight:700;">${money(salary)}</td>
          <td></td><td></td>
        </tr>
    `);

		for (let i = 0; i < max; i++) {
			const a = aArr[i], g = gArr[i];
			parts.push(`
        <tr>
          <td>${a ? a.label : ''}</td>
          <td class="tr">${a ? money(a.amt) : ''}</td>
          <td>${g ? g.label : ''}</td>
          <td class="tr">${g ? money(g.amt) : ''}</td>
        </tr>
      `);
		}

		parts.push(`
        <tr class="sum">
          <td class="tc">공제계</td>
          <td class="tr">${money(dedTotal)}</td>
          <td class="tc">급여계</td>
          <td class="tr">${money(paySum)}</td>
        </tr>
        <tr class="sum">
          <td class="tc">차감수령액</td>
          <td class="tr" colspan="3">${money(netPay)}</td>
        </tr>
      </table>
    `);

		// 다음 시트와 구분용 빈 줄
		if (idx !== picks.length - 1) {
			parts.push('<br style="mso-special-character:line-break;page-break-before:always" />');
		}
	});

	parts.push('</body></html>');

	const html = parts.join('');
	const blob = new Blob(['\ufeff', html], { type: 'application/vnd.ms-excel;charset=utf-8;' });
	const a = document.createElement('a');
	const url = URL.createObjectURL(blob);
	a.href = url;
	a.download = `급여명세서_${PAY_YM}_${picks.length}명.xls`;
	document.body.appendChild(a);
	a.click();
	setTimeout(() => {
		document.body.removeChild(a);
		URL.revokeObjectURL(url);
	}, 0);
}



