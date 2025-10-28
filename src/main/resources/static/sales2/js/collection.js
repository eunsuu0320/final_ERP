// /sales2/js/collection.js
let collectionTable;

// ===== 오버레이 스타일 주입(한 번) =====
(function injectInvoiceOverlayCSS() {
  if (document.getElementById("invoice-overlay-style")) return;
  const css = `
    #invoice-table{ position:relative; }
    #invoice-table .invoice-loading-overlay{
      position:absolute; inset:0;
      display:none; align-items:center; justify-content:center;
      backdrop-filter: blur(1px);
      background: rgba(255,255,255,0.6);
      z-index: 3;
    }
    #invoice-table .invoice-loading-overlay .overlay-inner{
      display:flex; flex-direction:column; align-items:center; gap:10px;
      padding: 10px 16px; border-radius:10px;
      background: rgba(255,255,255,0.8);
      box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    }
    #invoice-table .invoice-loading-overlay .overlay-text{ font-weight:600; color:#334155; }
    #invoice-table .invoice-loading-overlay.is-error .overlay-inner{ background:#fff0f0; }
    #invoice-table .invoice-loading-overlay.is-error .overlay-text{ color:#b91c1c; }
  `;
  const style = document.createElement("style");
  style.id = "invoice-overlay-style";
  style.textContent = css;
  document.head.appendChild(style);
})();

// ===== 로딩/오버레이 유틸 =====
function ensureInvoiceOverlayHost() {
  const host = document.getElementById("invoice-table");
  if (!host) return null;
  let overlay = host.querySelector(".invoice-loading-overlay");
  if (!overlay) {
    overlay = document.createElement("div");
    overlay.className = "invoice-loading-overlay";
    overlay.innerHTML = `
      <div class="overlay-inner">
        <div class="spinner-border" role="status" aria-label="loading"></div>
        <div class="overlay-text">로딩중…</div>
      </div>`;
    host.style.position = host.style.position || "relative";
    host.appendChild(overlay);
  }
  return overlay;
}
function showInvoiceLoading() {
  const overlay = ensureInvoiceOverlayHost();
  if (!overlay) return;
  overlay.classList.remove("is-error");
  overlay.querySelector(".overlay-text").textContent = "로딩중…";
  overlay.style.display = "flex";
}
function showInvoiceError(msg) {
  const overlay = ensureInvoiceOverlayHost();
  if (!overlay) return;
  overlay.classList.add("is-error");
  overlay.querySelector(".overlay-text").textContent = msg || "불러오는 중 오류가 발생했습니다.";
  overlay.style.display = "flex";
}
function hideInvoiceOverlay() {
  const host = document.getElementById("invoice-table");
  if (!host) return;
  const overlay = host.querySelector(".invoice-loading-overlay");
  if (overlay) overlay.style.display = "none";
}

/* ============================
   ▼▼ 추가: 상세 섹션 자동 닫힘 상태 관리 ▼▼
   ============================ */
// 현재 상세(청구내역)에 표시 중인 거래처 코드
let currentInvoicePartnerCode = null;

// 상세 섹션 비우기(닫기와 동일 효과)
function clearInvoiceSection() {
  try { window.invoiceTable?.clearData(); } catch {}
  try {
    const title = document.getElementById("invoice-title");
    if (title) title.textContent = "청구내역";
  } catch {}
  try {
    // 조회 버튼 하이라이트 제거
    document
      .getElementById("sales-table")
      ?.querySelectorAll(".btn-view-invoices")
      .forEach(btn => {
        btn.classList.remove("active", "btn-primary");
        btn.classList.add("btn-outline-primary");
      });
  } catch {}
}
/* ============================
   ▲▲ 추가 끝 ▲▲
   ============================ */

document.addEventListener("DOMContentLoaded", function () {
  // ===============================
  // 📌 테이블 생성 (수금 그리드)
  // ===============================
  const salesTableEl = document.getElementById("sales-table");
  if (!salesTableEl) {
    console.error("❌ #sales-table 요소를 찾을 수 없습니다.");
    return;
  }

  // 전역으로 접근 가능하게
  window.table = new Tabulator(salesTableEl, {
    layout: "fitColumns",
    height: "334px",
    selectable: false, // ★ 행 클릭시 선택/하이라이트 안 되도록
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    pagination: "local",
    paginationSize: 6,
    paginationCounter: "rows",
    columnDefaults: { vertAlign: "middle", headerHozAlign: "center" },
    selectablePersistence: true,
    ajaxResponse: function (url, params, response) {
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";

      /* ---------------------------
         ★ 추가: 목록 갱신 시 상세 자동 닫기
         현재 상세에 띄운 거래처가 목록에서 사라졌다면 상세 섹션 클리어
         --------------------------- */
      try {
        if (currentInvoicePartnerCode) {
          const stillExists = Array.isArray(response) && response.some(r =>
            (r.PARTNER_CODE || r.partnerCode) === currentInvoicePartnerCode
          );
          if (!stillExists) {
            currentInvoicePartnerCode = null;
            clearInvoiceSection();
          }
        }
      } catch (_) {}

      return response;
    },
    columns: [
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center", widthGrow: 0.3 },
      { title: "미수금액(원)", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.3 },
      { title: "총 수금(원)", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.5 },
      { title: "미수잔액(원)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.5 },
      { title: "미수건수", field: "INVOICE_COUNT", hozAlign: "center", widthGrow: 0.5 },
      {
        title: "조회",
        field: "VIEW_BTN",
        hozAlign: "center",
        headerSort: false,
        widthGrow: 0.5,
        formatter: () => '<button class="btn btn-outline-primary btn-view-invoices js-view-invoices">조회</button>',
        cellClick: async (e, cell) => {
          e.stopPropagation();
          const d = cell.getRow().getData();
          updateInvoiceTitle(d?.CUSTOMERNAME);
          await renderInvoiceTable(d);
          document.getElementById("invoice-table")?.scrollIntoView({ behavior: "smooth", block: "start" });
          setActiveViewButton(cell);
        }
      }
    ],
    rowClick: async function (e, row) {
      if (e.target.closest(".js-view-invoices")) return;
      const d = row.getData();
      updateInvoiceTitle(d?.CUSTOMERNAME);
      await openCollectionModal(d);
    }
  });

  // ✅ 생성 직후 안전한 지역 참조
  const table = window.table;
  collectionTable = window.table;
  table.on("dataLoaded", function () {
    updateInvoiceTitle(null);
  });

  // ===============================
  // 📌 제목/버튼 상태
  // ===============================
  function updateInvoiceTitle(partnerName) {
    const el = document.getElementById("invoice-title");
    if (!el) return;
    el.textContent = partnerName ? `${partnerName}의 청구내역` : "청구내역";
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
  // 📌 위임 클릭(모달 열려있으면 무시)
  // ===============================
  salesTableEl.addEventListener("click", async function (e) {
    if (document.body.classList.contains("modal-open")) return;
    if (e.target.closest(".js-view-invoices")) return;
    const rowEl = e.target.closest(".tabulator-row");
    if (!rowEl) return;
    try {
      const row = table.rowManager.activeRows.find(r => r.element === rowEl);
      const data = row ? row.getData() : null;
      if (data) {
        await openCollectionModal(data);
      }
    } catch (err) {
      console.warn("delegate 처리 중 오류:", err);
    }
  });

  // 🔁 전역 Fallback(모달 열려있으면 무시)
  document.addEventListener(
    "click",
    async function (e) {
      if (document.body.classList.contains("modal-open")) return;
      if (e.target.closest(".js-view-invoices")) return;
      const rowEl = e.target.closest("#sales-table .tabulator-row");
      if (!rowEl) return;
      try {
        const row = table.rowManager.activeRows.find(r => r.element === rowEl);
        const data = row ? row.getData() : null;
        if (data) {
          await openCollectionModal(data);
        }
      } catch (err) {
        console.warn("document fallback 처리 중 오류:", err);
      }
    },
    true
  );

  // 모달 닫히면 강조 해제(혹시 남아있다면) + 리드로우
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
        const dmndAmt = Number(i.dmndAmt ?? 0);
        const unrctBaln = Number(i.unrctBaln ?? 0);
        const collected = dmndAmt - unrctBaln;
        const dmndDateStr = (() => {
          const v = i.dmndDate;
          if (!v) return "";
          if (typeof v === "string") return v.slice(0, 10);
          try {
            return new Date(v).toISOString().slice(0, 10);
          } catch {
            return "";
          }
        })();
        return {
          INVOICE_UNIQUE_CODE: i.invoiceUniqueCode,
          INVOICE_CODE: i.invoiceCode,
          DMND_DATE: dmndDateStr,
          ITEM_NAME: "-",
          TOTAL_QTY: null,
          DMND_AMT: dmndAmt,
          COLLECTED: collected,
          UNRCT_BALN: unrctBaln,
          STATUS: i.status || "",
          REMK: i.remk || ""
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

    showInvoiceLoading();

    /* ★ 추가: 현재 상세의 거래처 코드 저장 */
    currentInvoicePartnerCode = partnerCode;

    try {
      const data = await fetchInvoices(partnerCode);
      const columns = [
		
        { title: "청구번호", field: "INVOICE_CODE", width: 140, hozAlign: "center", widthGrow: 0.4 },
        { title: "청구일", field: "DMND_DATE", width: 110, hozAlign: "center", widthGrow: 0.4 },
        { title: "청구금액(원)", field: "DMND_AMT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.5 },
        { title: "수금금액(원)", field: "COLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.5 },
        { title: "미수금액(원)", field: "UNRCT_BALN", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, widthGrow: 0.5 },
        {
          title: "상태",
          field: "STATUS",
          width: 110,
          hozAlign: "center",
          widthGrow: 0.6,
          headerFilter: "select",
          headerFilterParams: { values: { "": "전체", "진행중": "진행중", "수금완료": "수금완료", "수금대기": "수금대기" } },
          formatter: cell => {
            const v = (cell.getValue() || "").trim();
            const cls =
              v === "수금완료" ? "bg-success" :
              v === "진행중" ? "bg-warning" :
              v === "수금대기" ? "bg-secondary" : "bg-light text-dark";
            return `<span class="badge ${cls}">${v || "-"}</span>`;
          }
        },
        { title: "비고", field: "REMK", minWidth: 120, widthGrow: 0.5 }
      ];

      if (!window.invoiceTable) {
        window.invoiceTable = new Tabulator(el, {
          layout: "fitColumns",
          height: "297px",           // ← 높이 증가 (기존 260px)
          placeholder: "청구내역이 없습니다.",
          data,
          columns,
          columnDefaults: { headerHozAlign: "center" },
          index: "INVOICE_UNIQUE_CODE",
          pagination: "local",
          paginationSize: 6,         // ← 페이지당 5건 (기존 8)
          paginationCounter: "rows"
        });
      } else {
        window.invoiceTable.setColumns(columns);
        window.invoiceTable.replaceData(data);
        window.invoiceTable.setPageSize(5); // ← 재조회 시에도 5건 유지
        window.invoiceTable.redraw(true);
      }
    } catch (err) {
      console.error("청구내역 로드 오류:", err);
      showInvoiceError("청구내역을 불러오는 중 오류가 발생했습니다.");
      return;
    } finally {
      hideInvoiceOverlay();
    }
  }

  // ===============================
  // 📌 모달 열기
  // ===============================
  window.openCollectionModal = async function (rowData) {
    const modalRoot = document.getElementById("insertCollectionModal");
    if (!modalRoot) return;

    document.getElementById("modalPartnerName").value = rowData?.CUSTOMERNAME || "";
    const modalPartnerCodeEl = document.querySelector("#insertCollectionModal #partnerCode");
    if (modalPartnerCodeEl) modalPartnerCodeEl.value = rowData?.PARTNER_CODE || "";

    const rawOutstanding = Number(String(rowData?.OUTSTANDING ?? 0).replace(/[^\d]/g, "")) || 0;
    const outstandingHidden = document.getElementById("outstandingAmt");
    const outstandingView = document.getElementById("outstandingView");
    if (outstandingHidden) outstandingHidden.value = rawOutstanding;
    if (outstandingView) outstandingView.value = formatNumber(String(rawOutstanding));

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

    const collectAmtInput = document.getElementById("collectAmt");
    const postDeductionInput = document.getElementById("postDeductionAmt");
    if (collectAmtInput) collectAmtInput.value = "";
    if (postDeductionInput) postDeductionInput.value = "";

    const modal = new bootstrap.Modal(modalRoot);
    modal.show();
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
  function uncomma(v) {
    return String(v || "").replace(/[^\d]+/g, "");
  }
  function formatNumber(v) {
    v = String(v || "");
    return v.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  if (collectAmtInput) {
    collectAmtInput.addEventListener("input", e => (e.target.value = uncomma(e.target.value)));
    collectAmtInput.addEventListener("blur", e => (e.target.value = formatNumber(uncomma(e.target.value))));
    collectAmtInput.addEventListener("focus", e => (e.target.value = uncomma(e.target.value)));
  }

  const postDeductionInput = document.getElementById("postDeductionAmt");
  if (postDeductionInput) {
    postDeductionInput.addEventListener("input", e => (e.target.value = uncomma(e.target.value)));
    postDeductionInput.addEventListener("blur", e => (e.target.value = formatNumber(uncomma(e.target.value))));
    postDeductionInput.addEventListener("focus", e => (e.target.value = uncomma(e.target.value)));
  }

  // ===============================
  // 📌 저장 (로딩 오버레이 + 중복 방지)
  // ===============================
  document.getElementById("btnSave")?.addEventListener("click", async function () {
    const saveBtn = this;
    if (saveBtn.dataset.loading === "1") return; // 중복 클릭 방지

    const moneyDate = document.getElementById("moneyDate").value;
    const recpt = Number(uncomma(document.getElementById("collectAmt").value || "0"));
    const postDeduction = Number(uncomma((document.getElementById("postDeductionAmt")?.value) || "0"));
    const paymentMethods = document.getElementById("paymentType").value;
    const remk = document.getElementById("remarks").value;
    const partnerCode = document.querySelector("#insertCollectionModal #partnerCode")?.value || "";
    const outstandingVal = Number(uncomma(document.getElementById("outstandingAmt").value || "0"));

    // 기본 검증
    if (!partnerCode) {
      alert("거래처를 선택하세요.");
      return;
    }
    if (recpt <= 0) {
      alert("수금금액은 0보다 커야 합니다.");
      return;
    }
    if (recpt + postDeduction > outstandingVal) {
      alert("수금금액 + 사후공제가 미수잔액보다 큽니다.");
      return;
    }
    if (!paymentMethods) {
      alert("결제방식을 선택하세요.");
      return;
    }

    const data = { moneyDate, recpt, postDeduction, paymentMethods, remk, partnerCode };

    // ▼ UI 잠그기
    const closeBtn = document.querySelector("#insertCollectionModal .btn-close");
    const overlay = document.getElementById("collection-loading");
    const originalHtml = saveBtn.innerHTML;
    saveBtn.dataset.loading = "1";
    saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>로딩 중…`;
    saveBtn.disabled = true;
    if (closeBtn) closeBtn.disabled = true;
    if (overlay) overlay.classList.remove("d-none");

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

        // ▼▼▼ 여기만 추가/수정: 모달 닫히면서 청구내역(하단) 비우고 메인테이블만 보이게 ▼▼▼
        try {
          currentInvoicePartnerCode = null; // 현재 상세 상태 초기화
          clearInvoiceSection();            // 청구내역 데이터/제목/버튼 상태 초기화
        } catch (_) {}
        // ▲▲▲ 추가 끝 ▲▲▲

        table?.replaceData();
      } else {
        alert("실패: " + (result.message || "서버 오류"));
      }
    } catch (err) {
      console.error("등록 중 오류:", err);
      alert("서버 통신 오류");
    } finally {
      // ▼ UI 해제
      saveBtn.innerHTML = originalHtml;
      saveBtn.disabled = false;
      if (closeBtn) closeBtn.disabled = false;
      if (overlay) overlay.classList.add("d-none");
      saveBtn.dataset.loading = "0";
    }
  });

  // ===============================
  // 📌 클릭 막힘 방지(유지)
  // ===============================
  salesTableEl.style.position = "relative";
  salesTableEl.style.zIndex = "1"; // ★ 모달보다 항상 아래
  salesTableEl.style.pointerEvents = "auto";
}); // end DOMContentLoaded

// ===============================
// ✅ 공통 유틸
// ===============================
function safeRedrawAll() {
  try {
    window.table?.redraw(true);
  } catch {}
  try {
    window.invoiceTable?.redraw(true);
  } catch {}
  setTimeout(() => {
    try {
      window.table?.redraw(true);
    } catch {}
    try {
      window.invoiceTable?.redraw(true);
    } catch {}
  }, 0);
}

function ensureSalesTableVisible() {
  const el = document.getElementById("sales-table");
  if (!el) return;
  const style = window.getComputedStyle(el);
  if (style.display === "none" || el.hidden) {
    el.hidden = false;
    el.style.display = "block";
  }
}

document.addEventListener("shown.bs.modal", function () {
  ensureSalesTableVisible();
  safeRedrawAll();
  try {
    const st = document.getElementById("sales-table");
    if (st) {
      st.style.pointerEvents = "none"; // 모달 떠있는 동안 뒤 클릭 차단
      st.style.zIndex = "1"; // 항상 모달보다 아래
    }
  } catch {}
});

document.addEventListener("hidden.bs.modal", function () {
  ensureSalesTableVisible();
  safeRedrawAll();
  try {
    document.querySelectorAll(".modal-backdrop").forEach(el => el.remove());
    document.body.classList.remove("modal-open");
    document.body.style.removeProperty("padding-right");
    document.body.style.removeProperty("overflow");
  } catch {}
  try {
    const st = document.getElementById("sales-table");
    if (st) {
      st.style.position = "relative";
      st.style.zIndex = "1"; // 높이지 않음
      st.style.pointerEvents = "auto";
    }
  } catch {}
});

// ✅ 남은 백드롭 제거 + 테이블 가시성 보정
document.addEventListener(
  "click",
  e => {
    const hasModalOpen = document.body.classList.contains("modal-open");
    const leftoverBackdrop = document.querySelector(".modal-backdrop");
    if (!hasModalOpen && leftoverBackdrop) {
      leftoverBackdrop.remove();
      document.body.style.removeProperty("padding-right");
      document.body.style.removeProperty("overflow");
      const st = document.getElementById("sales-table");
      if (st) {
        st.style.position = "relative";
        st.style.zIndex = "1";
        st.style.pointerEvents = "auto";
      }
    }
  },
  true
);
