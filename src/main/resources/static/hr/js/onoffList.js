const manager = document.getElementById("companyCode").value; // 회사코드
const token = document.querySelector('meta[name="_csrf"]')?.content || ""; // 토큰

let EMP_MAP = {}; // 전역 사원 맵

// 사원 목록 불러오기 (사원 출퇴근 조회 이름 변경 위함)
async function loadMapEmployees() {
  const res = await fetch(`/employees?companyCode=${encodeURIComponent(manager)}`, {
    headers: {
      "Content-Type": "application/json",
      "X-CSRF-Token": token,
    },
    credentials: "same-origin",
  });
  if (!res.ok) {
    console.error("GET /employees fail:", await res.text());
    return {};
  }
  const list = await res.json();
  return Object.fromEntries((Array.isArray(list) ? list : []).map(it => [it.empCode, it.name]));
}

// 날짜/시간 유틸
const pad2 = n => String(n).padStart(2, "0");
function fmtDate(val) {
  if (!val) return "";
  const d = new Date((typeof val === "string" && !val.includes("T")) ? val.replace(" ", "T") : val);
  if (isNaN(d)) return "";
  return `${d.getFullYear()}/${pad2(d.getMonth() + 1)}/${pad2(d.getDate())}`;
}
function fmtDateTime(val) {
  if (!val) return "";
  const d = new Date((typeof val === "string" && !val.includes("T")) ? val.replace(" ", "T") : val);
  if (isNaN(d)) return "";
  const yyyy = d.getFullYear();
  const MM = pad2(d.getMonth() + 1);
  const dd = pad2(d.getDate());
  const hh = pad2(d.getHours());
  const mm = pad2(d.getMinutes());
  const ss = pad2(d.getSeconds());
  return `${yyyy}-${MM}-${dd} ${hh}:${mm}:${ss}`;
}
function diffToWorkTime(on, off) {
  if (!on || !off) return "";
  const a = new Date((typeof on === "string" && !on.includes("T")) ? on.replace(" ", "T") : on);
  const b = new Date((typeof off === "string" && !off.includes("T")) ? off.replace(" ", "T") : off);
  if (isNaN(a) || isNaN(b)) return "";
  const ms = b - a;
  if (ms <= 0) return "";
  const h = Math.floor(ms / 3600000);
  const m = Math.floor((ms % 3600000) / 60000);
  return `${h}시간 ${pad2(m)}분`;
}

// ==== 보조 유틸 ====
function debounce(fn, delay = 300) {
  let t;
  return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), delay); };
}
// 사용자가 2025-10-10 / 2025.10.10 / 20251010 등 어떤 형식으로 입력해도 비교용 숫자만 남김
function normalizeDateKeyword(s = "") {
  return s.replace(/[^\d]/g, ""); // 숫자만
}

let commuteTable;

function initTable() {
  commuteTable = new Tabulator(document.getElementById("commute-table"), {
    layout: "fitColumns",
    pagination: "local",
    paginationSize: 22,
    placeholder: "조회된 출퇴근 목록이 없습니다.",
    selectable: true,
    columns: [
      {
        title: "선택",
        formatter: "rowSelection",
        titleFormatter: "rowSelection",
        headerSort: false,
        width: 44,
        hozAlign: "center",
        headerHozAlign: "center",
      },
      {
        title: "근태일자",
        field: "onTime",
        sorter: "date",
        sorterParams: { format: "YYYY-MM-DD HH:mm:ss" }, // 대문자 HH 권장
        formatter: cell => fmtDate(cell.getValue()),
        width: 120,
        hozAlign: "center",
      },
      {
        title: "사원",
        field: "empCode",
        formatter: cell => {
          const code = cell.getValue();
          return EMP_MAP[code] || code || "";
        },
      },
      {
        title: "출근시간",
        field: "onTime",
        hozAlign: "center",
        formatter: cell => fmtDateTime(cell.getValue()),
      },
      {
        title: "퇴근시간",
        field: "offTime",
        hozAlign: "center",
        formatter: cell => fmtDateTime(cell.getValue()),
      },
      {
        title: "근무시간",
        field: "workTime", // 서버 Double(시간) 값
        hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue(); // Double(시간) 예상
          if (v != null && !isNaN(v)) {
            const totalMin = Math.round(Number(v) * 60);
            const h = Math.floor(totalMin / 60);
            const m = totalMin % 60;
            return `${h}시간 ${pad2(m)}분`;
          }
          // 서버 값이 없으면 on/off로 계산 fallback
          const r = cell.getRow().getData();
          return diffToWorkTime(r.onTime, r.offTime);
        },
      },
      {
        title: "야간 근무 시간",
        field: "nightTime", // 서버 Double(시간) 값
        hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue(); // Double(시간) 예상
          if (v != null && !isNaN(v)) {
            const totalMin = Math.round(Number(v) * 60);
            const h = Math.floor(totalMin / 60);
            const m = totalMin % 60;
            return `${h}시간 ${pad2(m)}분`;
          }
          // 서버 값이 없으면 on/off로 계산 fallback
          const r = cell.getRow().getData();
          return diffToWorkTime(r.onTime, r.offTime);
        },
      },
      {
        title: "연장 근무 시간",
        field: "otTime", // 서버 Double(시간) 값
        hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue(); // Double(시간) 예상
          if (v != null && !isNaN(v)) {
            const totalMin = Math.round(Number(v) * 60);
            const h = Math.floor(totalMin / 60);
            const m = totalMin % 60;
            return `${h}시간 ${pad2(m)}분`;
          }
          // 서버 값이 없으면 on/off로 계산 fallback
          const r = cell.getRow().getData();
          return diffToWorkTime(r.onTime, r.offTime);
        },
      },
      {
        title: "휴일/특근 근무 시간",
        field: "holidayTime", // 서버 Double(시간) 값
        hozAlign: "center",
        formatter: (cell) => {
          const v = cell.getValue(); // Double(시간) 예상
          if (v != null && !isNaN(v)) {
            const totalMin = Math.round(Number(v) * 60);
            const h = Math.floor(totalMin / 60);
            const m = totalMin % 60;
            return `${h}시간 ${pad2(m)}분`;
          }
          // 서버 값이 없으면 on/off로 계산 fallback
          const r = cell.getRow().getData();
          return diffToWorkTime(r.onTime, r.offTime);
        },
      },
      { title: "비고", field: "note" },
    ],
  });
}

// 데이터 로드
async function loadCommutes() {
  const res = await fetch(`/onoff?companyCode=${encodeURIComponent(manager)}`, {
    headers: {
      "Content-Type": "application/json",
      "X-CSRF-Token": token,
    },
    credentials: "same-origin",
  });

  if (!res.ok) {
    console.error("GET /onoff fail:", await res.text());
    commuteTable.setData([]);
    return;
  }

  const ct = res.headers.get("content-type") || "";
  const raw = ct.includes("application/json") ? await res.json() : await res.text();
  const data = Array.isArray(raw) ? raw : (raw && typeof raw === "object" ? [raw] : []);

  // ✅ 서버 값을 그대로 투입(덮어쓰기 없음)
  commuteTable.setData(data);
}

// 초기 구동
document.addEventListener("DOMContentLoaded", async () => {
  try {
    EMP_MAP = await loadMapEmployees(); // 사원맵 먼저
  } catch (e) {
    console.error("사원맵 로드 실패:", e);
    EMP_MAP = {}; // 실패해도 빈 맵으로 진행
  }
  initTable();
  await loadCommutes();

  // ==== 검색 ====
  const $sel = document.getElementById("sel-field");   // name | date
  const $txt = document.getElementById("txt-search");  // 키워드
  const $btn = document.getElementById("btn-search");

  const applySearch = () => {
    if (!commuteTable) return;

    const mode = $sel?.value || "name";
    const keywordRaw = ($txt?.value || "").trim();

    // 필터 초기화
    commuteTable.clearFilter();
    if (!keywordRaw) return;

    if (mode === "name") {
      const kw = keywordRaw.toLowerCase();
      commuteTable.setFilter((data) => {
        const empName = (EMP_MAP[data.empCode] || "").toLowerCase();
        return empName.includes(kw);
      });
      return;
    }

    if (mode === "date") {
      // onTime 기준; 입력은 어떤 형식이든 숫자만 추려서 비교
      const kw = normalizeDateKeyword(keywordRaw);
      commuteTable.setFilter((data) => {
        const ymd = fmtDate(data.onTime).replace(/[^\d]/g, ""); // 2025/10/10 -> 20251010
        return ymd.includes(kw);
      });
      return;
    }
  };

  $btn?.addEventListener("click", applySearch);
  $txt?.addEventListener("keydown", (e) => { if (e.key === "Enter") applySearch(); });
  $txt?.addEventListener("input", debounce(applySearch, 300));
});

// 인쇄
// ==== 인쇄(선택건만) ====
const printBtn = document.getElementById("onoff-print");
if (printBtn) {
  printBtn.addEventListener("click", () => {
    if (!commuteTable) {
      alert("테이블이 아직 준비되지 않았습니다.");
      return;
    }

    const selected = commuteTable.getSelectedData();
    if (!selected || selected.length === 0) {
      alert("인쇄할 행을 먼저 선택하세요.");
      return;
    }

    // 선택건을 표 HTML로 변환
    const rows = selected.map(r => {
      // workTime(Double 시간) → "H시간 MM분"
      let wt = "";
      if (r.workTime != null && !isNaN(r.workTime)) {
        const tmin = Math.round(Number(r.workTime) * 60);
        wt = `${Math.floor(tmin / 60)}시간 ${pad2(tmin % 60)}분`;
      } else {
        wt = diffToWorkTime(r.onTime, r.offTime);
      }

      return `
        <tr>
          <td>${fmtDate(r.onTime)}</td>
          <td>${EMP_MAP[r.empCode] || r.empCode || ""}</td>
          <td>${fmtDateTime(r.onTime)}</td>
          <td>${fmtDateTime(r.offTime)}</td>
          <td>${wt}</td>
          <td>${r.note ?? ""}</td>
        </tr>`;
    }).join("");

    const now = new Date();
    const madeAt = `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())} ${pad2(now.getHours())}:${pad2(now.getMinutes())}`;
    const count = selected.length;

    const html = `
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>선택된 출퇴근 목록</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    @page { size: A4 portrait; margin: 8mm; }
    html, body { height: 100%; }
    body { margin: 0; font-size: 12px; }
    .print-wrap { width: 100%; padding: 0; }
    .print-header { text-align: center; margin: 0 0 8px 0; }
    .meta { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; padding: 0 2mm; }
    table.print { width: 100%; table-layout: fixed; border-collapse: collapse; }
    table.print th, table.print td {
      border: 1px solid #dee2e6; padding: 6px 8px; vertical-align: middle;
      overflow: hidden; text-overflow: ellipsis; white-space: nowrap; word-break: keep-all;
    }
    thead th { background: #f8f9fa; text-align: center; font-weight: 600; }
    tbody tr:nth-child(even) td { background: #fcfcfc; }
    thead { display: table-header-group; }
    tfoot { display: table-footer-group; }
    tr { page-break-inside: avoid; }
    col.date{width:12%} col.name{width:12%} col.on{width:19%} col.off{width:19%}
    col.work{width:10%} col.note{width:28%}
  </style>
</head>
<body>
  <div class="print-wrap">
    <div class="print-header"><h3 class="m-0">선택된 출퇴근 목록</h3></div>
    <div class="meta"><div>총 <strong>${count}</strong>건</div><div>생성일시: ${madeAt}</div></div>
    <table class="print">
      <colgroup>
        <col class="date"><col class="name"><col class="on"><col class="off"><col class="work"><col class="note">
      </colgroup>
      <thead>
        <tr>
          <th>근태일자</th>
          <th>사원</th>
          <th>출근시간</th>
          <th>퇴근시간</th>
          <th>근무시간</th>
          <th>비고</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  </div>
  <script>window.addEventListener('load', () => window.print());</script>
</body>
</html>`;

    const w = window.open("", "_blank");
    if (!w) {
      alert("팝업이 차단되었습니다. 팝업 허용 후 다시 시도하세요.");
      return;
    }
    w.document.open();
    w.document.write(html);
    w.document.close();
  });
}


// 엑셀
// ===== Excel(선택건만) =====
document.getElementById("onoff-excel").addEventListener("click", async () => {
  if (!window.commuteTable) {
    alert("테이블이 아직 준비되지 않았습니다.");
    return;
  }

  const rows = commuteTable.getSelectedData();
  if (!rows || rows.length === 0) {
    alert("엑셀로 내보낼 행을 먼저 선택하세요.");
    return;
  }

  // SheetJS(XLSX) 로더
  async function ensureXLSX() {
    if (window.XLSX) return;
    await new Promise((resolve, reject) => {
      const s = document.createElement("script");
      s.src = "https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js";
      s.onload = resolve;
      s.onerror = reject;
      document.head.appendChild(s);
    });
  }

  // 선택된 행을 출력용 형태로 매핑
  const exportRows = rows.map(r => {
    let wt = "";
    if (r.workTime != null && !isNaN(r.workTime)) {
      const tmin = Math.round(Number(r.workTime) * 60);
      wt = `${Math.floor(tmin / 60)}시간 ${pad2(tmin % 60)}분`;
    } else {
      wt = diffToWorkTime(r.onTime, r.offTime);
    }
    return {
      "근태일자": fmtDate(r.onTime),
      "사원": EMP_MAP[r.empCode] || r.empCode || "",
      "출근시간": fmtDateTime(r.onTime),
      "퇴근시간": fmtDateTime(r.offTime),
      "근무시간": wt,
      "비고": r.note ?? ""
    };
  });

  try {
    await ensureXLSX();

    // 워크시트/워크북 생성
    const ws = XLSX.utils.json_to_sheet(exportRows, { skipHeader: false });
    // 열 너비 살짝 보기 좋게
    ws["!cols"] = [
      { wch: 12 }, // 근태일자
      { wch: 12 }, // 사원
      { wch: 20 }, // 출근시간
      { wch: 20 }, // 퇴근시간
      { wch: 10 }, // 근무시간
      { wch: 20 }, // 비고
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "선택된 출퇴근");

    // 파일명: commute_selected_YYYYMMDD_HHmmss.xlsx
    const now = new Date();
    const stamp = [
      now.getFullYear(),
      pad2(now.getMonth() + 1),
      pad2(now.getDate()),
      "_",
      pad2(now.getHours()),
      pad2(now.getMinutes()),
      pad2(now.getSeconds())
    ].join("");

    XLSX.writeFile(wb, `commute_selected_${stamp}.xlsx`);
  } catch (e) {
    console.error(e);
    alert("엑셀 생성 중 오류가 발생했습니다.");
  }
});
