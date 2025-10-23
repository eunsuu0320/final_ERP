// /sales2/js/collection.js

document.addEventListener("DOMContentLoaded", function () {
  // ===============================
  // 📌 테이블 생성
  // ===============================
  const salesTableEl = document.getElementById("sales-table");
  if (!salesTableEl) {
    console.error("❌ #sales-table 요소를 찾을 수 없습니다.");
    return;
  }

  // 전역으로 접근 가능하게
  window.table = new Tabulator(salesTableEl, {
    layout: "fitColumns",
    height: "350px",
    selectable: true,
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    selectablePersistence: true,
    ajaxResponse: function (url, params, response) {
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";
      return response;
    },
    columns: [
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" , widthGrow:0.3 },
      { title: "미수금액(원)", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow:0.3 },
      { title: "총 수금(원)", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow:0.5 },
      { title: "미수잔액(원)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow:0.5 },
      { title: "미수건수", field: "INVOICE_COUNT", hozAlign: "center", widthGrow:0.5 },
      {
        title: "조회",
        field: "VIEW_BTN",
        hozAlign: "center",
        headerSort: false,
        widthGrow:0.5,
        formatter: () => '<button class="btn btn-outline-primary btn-view-invoices js-view-invoices">조회</button>',
        cellClick: async (e, cell) => {
          e.stopPropagation();
          const d = cell.getRow().getData();
          updateInvoiceTitle(d?.CUSTOMERNAME);
          await renderInvoiceTable(d);
          document.getElementById("invoice-table")?.scrollIntoView({behavior:"smooth", block:"start"});
          setActiveViewButton(cell);
        }
      }
    ],
    rowClick: async function (e, row) {
      if (e.target.closest('.js-view-invoices')) return;
      setActiveRow(row);
      const d = row.getData();
      updateInvoiceTitle(d?.CUSTOMERNAME);
      await openCollectionModal(d);
    }
  });

  // ✅ 생성 직후 안전한 지역 참조
  const table = window.table;

  // 테이블 로드 후 제목 초기화
  table.on("dataLoaded", function () {
    updateInvoiceTitle(null);
  });

  // ===============================
  // 📌 제목/선택 강조/버튼 상태
  // ===============================
  function updateInvoiceTitle(partnerName) {
    const el = document.getElementById("invoice-title");
    if (!el) return;
    el.textContent = partnerName ? `${partnerName}의 청구내역` : "청구내역";
  }

  function setActiveRow(row) {
    const tableEl = document.getElementById("sales-table");
    tableEl.querySelectorAll(".tabulator-row.row-active").forEach(el => el.classList.remove("row-active"));
    row.getElement().classList.add("row-active");
  }

  function setActiveViewButton(cell) {
    const allButtons = document.getElementById("sales-table").querySelectorAll(".btn-view-invoices");
    allButtons.forEach(btn => {
      btn.classList.remove("active", "btn-primary");
      btn.classList.add("btn-outline-primary");
    });
    const btn = cell.getElement().querySelector(".btn-view-invoices");
    if (btn) {
      btn.classList.remove("btn-outline-primary");
      btn.classList.add("btn-primary", "active");
    }
  }

  // ===============================
  // 📌 위임 클릭 (원래 있던 핸들러 유지)
  // ===============================
  salesTableEl.addEventListener("click", async function (e) {
    if (e.target.closest('.js-view-invoices')) return;
    const rowEl = e.target.closest(".tabulator-row");
    if (!rowEl) return;
    try {
      const row = table.rowManager.activeRows.find(r => r.element === rowEl);
      const data = row ? row.getData() : null;
      if (data) {
        setActiveRow(row);
        await openCollectionModal(data);
      }
    } catch (err) {
      console.warn("delegate 처리 중 오류:", err);
    }
  });

  // ===============================
  // 🔁 추가: 문서 전역 Fallback 클릭(보호장치)
  //  - 어떤 이유로 salesTableEl 리스너가 못 받는 경우에도 실행
  // ===============================
  document.addEventListener("click", async function (e) {
    // 조회 버튼은 제외
    if (e.target.closest('.js-view-invoices')) return;

    const rowEl = e.target.closest("#sales-table .tabulator-row");
    if (!rowEl) return;

    try {
      const row = table.rowManager.activeRows.find(r => r.element === rowEl);
      const data = row ? row.getData() : null;
      if (data) {
        setActiveRow(row);
        await openCollectionModal(data);
      }
    } catch (err) {
      console.warn("document fallback 처리 중 오류:", err);
    }
  }, true); // capture 단계에서 먼저 받기

  // 모달 닫히면 강조 해제
  document.getElementById("insertCollectionModal")?.addEventListener("hidden.bs.modal", () => {
    document.querySelectorAll("#sales-table .row-active").forEach(el => el.classList.remove("row-active"));
    safeRedrawAll();
  });

  // ===============================
  // (A) API → 가공
  // ===============================
  async function fetchInvoices(partnerCode) {
    if (!partnerCode) return [];
    try {
      const res = await fetch(`/api/receivable/invoices?partnerCode=${encodeURIComponent(partnerCode)}`);
      if (!res.ok) throw new Error("HTTP " + res.status);
      const list = await res.json();
      return list.map(i => {
        const dmndAmt   = Number(i.dmndAmt ?? 0);
        const unrctBaln = Number(i.unrctBaln ?? 0);
        const collected = dmndAmt - unrctBaln;
        const dmndDateStr = (() => {
          const v = i.dmndDate;
          if (!v) return "";
          if (typeof v === "string") return v.slice(0,10);
          try { return new Date(v).toISOString().slice(0,10); } catch { return ""; }
        })();
        return {
          INVOICE_UNIQUE_CODE: i.invoiceUniqueCode,
          INVOICE_CODE:        i.invoiceCode,
          DMND_DATE:           dmndDateStr,
          ITEM_NAME:           "-",
          TOTAL_QTY:           null,
          DMND_AMT:            dmndAmt,
          COLLECTED:           collected,
          UNRCT_BALN:          unrctBaln,
          STATUS:              i.status || "",
          REMK:                i.remk || ""
        };
      });
    } catch (e) {
      console.error("청구서 조회 오류:", e);
      return [];
    }
  }

  // ===============================
  // (B) 청구내역 테이블 렌더러
  // ===============================
  window.invoiceTable = null;

  async function renderInvoiceTable(rowData) {
    const el = document.getElementById("invoice-table");
    if (!el) return;

    const partnerCode = rowData?.PARTNER_CODE || rowData?.partnerCode || "";
    const data = await fetchInvoices(partnerCode);

    const columns = [
      { title:"청구번호",    field:"INVOICE_CODE", width:140, hozAlign:"center", widthGrow:0.4 },
      { title:"청구일",      field:"DMND_DATE",   width:110, hozAlign:"center", widthGrow:0.4 },
      { title:"품목명",      field:"ITEM_NAME",   minWidth:180, widthGrow:0.3 },
      { title:"전체수량",    field:"TOTAL_QTY",   width:95, hozAlign:"right" },
      { title:"청구금액(원)", field:"DMND_AMT",   hozAlign:"right", formatter:"money", formatterParams:{precision:0}, widthGrow:0.5 },
      { title:"수금금액(원)", field:"COLLECTED",  hozAlign:"right", formatter:"money", formatterParams:{precision:0}, widthGrow:0.5 },
      { title:"미수금액(원)", field:"UNRCT_BALN", hozAlign:"right", formatter:"money", formatterParams:{precision:0}, widthGrow:0.5 },
      {
        title:"상태", field:"STATUS", width:110, hozAlign:"center", widthGrow:0.6,
        headerFilter:"select",
        headerFilterParams:{ values: { "": "전체", "진행중":"진행중", "수금완료":"수금완료", "수금대기":"수금대기" } },
        formatter: (cell) => {
          const v = (cell.getValue() || "").trim();
          const cls =
            v === "수금완료" ? "bg-success" :
            v === "진행중"   ? "bg-warning" :
            v === "수금대기" ? "bg-secondary" : "bg-light text-dark";
          return `<span class="badge ${cls}">${v || "-"}</span>`;
        }
      },
      { title:"비고", field:"REMK", minWidth:120 , widthGrow:0.5}
    ];

    if (!window.invoiceTable) {
      window.invoiceTable = new Tabulator(el, {
        layout:"fitColumns",
        height:"260px",
        placeholder:"청구내역이 없습니다.",
        data,
        columns,
        columnDefaults:{ headerHozAlign:"center" },
        index:"INVOICE_UNIQUE_CODE",
      });
    } else {
      window.invoiceTable.setColumns(columns);
      window.invoiceTable.replaceData(data);
      window.invoiceTable.redraw(true);
    }
  }

  // ===============================
  // 📌 모달 열기
  // ===============================
  window.openCollectionModal = async function (rowData) {
    // (사전 클린업) 혹시 남아있는 모달/백드롭/바디 상태 정리
    try {
      document.querySelectorAll('.modal.show').forEach(m => {
        const inst = bootstrap.Modal.getInstance(m);
        inst?.hide();
      });
      document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
      document.body.classList.remove('modal-open');
      document.body.style.removeProperty('padding-right');
      document.body.style.removeProperty('overflow');
    } catch (_) {}

    const modalRoot = document.getElementById("insertCollectionModal");
    if (!modalRoot) return;

    // 거래처/잔액 세팅
    document.getElementById("modalPartnerName").value = rowData?.CUSTOMERNAME || "";
    const modalPartnerCodeEl = document.querySelector("#insertCollectionModal #partnerCode");
    if (modalPartnerCodeEl) modalPartnerCodeEl.value = rowData?.PARTNER_CODE || "";

    const rawOutstanding = Number(String(rowData?.OUTSTANDING ?? 0).replace(/[^\d]/g, "")) || 0;
    const outstandingHidden = document.getElementById("outstandingAmt");
    const outstandingView   = document.getElementById("outstandingView");
    if (outstandingHidden) outstandingHidden.value = rawOutstanding;
    if (outstandingView)   outstandingView.value   = formatNumber(String(rawOutstanding));

    // 담당자 세팅
    let empName = "";
    try {
      const res = await fetch("/api/collection/current-employee");
      if (res.ok) {
        const userInfo = await res.json();
        empName = userInfo.empName || "";
      }
    } catch (err) {
      console.warn("사원명 조회 실패:", err);
    }
    document.getElementById("managerName").value = empName || "로그인사용자";

    // 금액 입력 초기화
    const collectAmtInput     = document.getElementById("collectAmt");
    const postDeductionInput  = document.getElementById("postDeductionAmt");
    if (collectAmtInput) collectAmtInput.value = "";
    if (postDeductionInput) postDeductionInput.value = "";

    // 모달 표시
    const modal = new bootstrap.Modal(modalRoot);
    modal.show();

    // 표시 직후 레이아웃 안정화
    setTimeout(safeRedrawAll, 0);
  };

  // ===============================
  // 📌 검색
  // ===============================
  document.getElementById("btn-search")?.addEventListener("click", function () {
    const keyword = (document.getElementById("partnerName")?.value || "").trim();
    if (keyword) table.setFilter("CUSTOMERNAME", "like", keyword);
    else table.clearFilter();
    safeRedrawAll();
  });

  // ===============================
  // 📌 금액 입력 포맷
  // ===============================
  const collectAmtInput = document.getElementById("collectAmt");
  function uncomma(v) { return String(v || "").replace(/[^\d]+/g, ""); }
  function formatNumber(v) { v = String(v || ""); return v.replace(/\B(?=(\d{3})+(?!\d))/g, ","); }

  if (collectAmtInput) {
    collectAmtInput.addEventListener("input",  e => e.target.value = uncomma(e.target.value));
    collectAmtInput.addEventListener("blur",   e => e.target.value = formatNumber(uncomma(e.target.value)));
    collectAmtInput.addEventListener("focus",  e => e.target.value = uncomma(e.target.value));
  }

  const postDeductionInput = document.getElementById("postDeductionAmt");
  if (postDeductionInput) {
    postDeductionInput.addEventListener("input", e => e.target.value = uncomma(e.target.value));
    postDeductionInput.addEventListener("blur",  e => e.target.value = formatNumber(uncomma(e.target.value)));
    postDeductionInput.addEventListener("focus", e => e.target.value = uncomma(e.target.value));
  }

  // ===============================
  // 📌 저장
  // ===============================
  document.getElementById("btnSave")?.addEventListener("click", async function () {
    const moneyDate = document.getElementById("moneyDate").value;
    const recpt = Number(uncomma(document.getElementById("collectAmt").value || "0"));
    const postDeduction = Number(uncomma((document.getElementById("postDeductionAmt")?.value) || "0"));
    const paymentMethods = document.getElementById("paymentType").value;
    const remk = document.getElementById("remarks").value;
    const partnerCode = (document.querySelector("#insertCollectionModal #partnerCode")?.value) || "";
    const outstandingVal = Number(uncomma(document.getElementById("outstandingAmt").value || "0"));

    if (!partnerCode) { alert("거래처를 선택하세요."); return; }
    if (recpt <= 0) { alert("수금금액은 0보다 커야 합니다."); return; }
    if (recpt + postDeduction > outstandingVal) { alert("수금금액 + 사후공제가 미수잔액보다 큽니다."); return; }
    if (!paymentMethods) { alert("결제방식을 선택하세요."); return; }

    const data = { moneyDate, recpt, postDeduction, paymentMethods, remk, partnerCode };

    try {
      const res = await fetch("/api/collection/insert", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });
      const result = await res.json();
      if (result.success) {
        alert("수금 등록되었습니다.");
        bootstrap.Modal.getInstance(document.getElementById("insertCollectionModal"))?.hide();
        table?.replaceData();
      } else {
        alert("실패: " + (result.message || "서버 오류"));
      }
    } catch (err) {
      console.error("등록 중 오류:", err);
      alert("서버 통신 오류");
    }
  });

  // ===============================
  // 📌 클릭 막힘 방지(유지)
  // ===============================
  salesTableEl.style.position = "relative";
  salesTableEl.style.zIndex = "5";
  salesTableEl.style.pointerEvents = "auto";
}); // end DOMContentLoaded


// ===============================
// ✅ 공통 유틸
// ===============================
function safeRedrawAll() {
  try { window.table?.redraw(true); } catch {}
  try { window.invoiceTable?.redraw(true); } catch {}
  setTimeout(() => {
    try { window.table?.redraw(true); } catch {}
    try { window.invoiceTable?.redraw(true); } catch {}
  }, 0);
}

function ensureSalesTableVisible() {
  const el = document.getElementById('sales-table');
  if (!el) return;
  const style = window.getComputedStyle(el);
  if (style.display === 'none' || el.hidden) {
    el.hidden = false;
    el.style.display = 'block';
  }
}

document.addEventListener('shown.bs.modal', function () {
  ensureSalesTableVisible();
  safeRedrawAll();

  try {
    const st = document.getElementById('sales-table');
    if (st) {
      st.style.zIndex = '5';
      st.style.pointerEvents = 'auto';
    }
  } catch (_) {}
});

document.addEventListener('hidden.bs.modal', function () {
  ensureSalesTableVisible();
  safeRedrawAll();

  try {
    document.querySelectorAll('.modal-backdrop').forEach(el => el.remove());
    document.body.classList.remove('modal-open');
    document.body.style.removeProperty('padding-right');
    document.body.style.removeProperty('overflow');
  } catch (_) {}

  try {
    const st = document.getElementById('sales-table');
    if (st) {
      st.style.position = 'relative';
      st.style.zIndex = '1061';
      st.style.pointerEvents = 'auto';
    }
  } catch (_) {}
});

// ✅ 남은 백드롭이 있으면 제거 + 테이블 가시성 보정
document.addEventListener('click', (e) => {
  const hasModalOpen = document.body.classList.contains('modal-open');
  const leftoverBackdrop = document.querySelector('.modal-backdrop');
  if (!hasModalOpen && leftoverBackdrop) {
    leftoverBackdrop.remove();
    document.body.style.removeProperty('padding-right');
    document.body.style.removeProperty('overflow');

    const st = document.getElementById('sales-table');
    if (st) {
      st.style.position = 'relative';
      st.style.zIndex = '1061';
      st.style.pointerEvents = 'auto';
    }
  }
}, true);
