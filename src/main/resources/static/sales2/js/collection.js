document.addEventListener("DOMContentLoaded", function () {

  // ===============================
  // 📌 테이블 생성
  // ===============================
  const salesTableEl = document.getElementById("sales-table");
  if (!salesTableEl) {
    console.error("❌ #sales-table 요소를 찾을 수 없습니다.");
    return;
  }

  table = new Tabulator(salesTableEl, {
    layout: "fitColumns",
    height: "350px",
    selectable: true,  
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    electable: true,              // 체크박스가 실제 '행 선택'으로 반영됨
  	selectablePersistence: true,   // (선택) 페이지/리렌더 넘어가도 선택 유지
    ajaxResponse: function (url, params, response) {
      console.log("📡 /api/receivable/list 응답:", response);
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";
      return response;
    },
    columns: [
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" ,widthGrow:0.3},
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
  cellClick: (e, cell) => {
    e.stopPropagation();
    const d = cell.getRow().getData();
    updateInvoiceTitle(d?.CUSTOMERNAME);        // 제목 업데이트
    renderInvoiceTable(d);
    document.getElementById("invoice-table")?.scrollIntoView({behavior:"smooth", block:"start"});
    setActiveViewButton(cell);
  }
}
], 
    rowClick: async function (e, row) {
	   if (e.target.closest('.js-view-invoices')) return; // 조회버튼 클릭 시 모달 열지 않음
	   setActiveRow(row);                                  // 행 강조
	   updateInvoiceTitle(d?.CUSTOMERNAME);        // 제목 업데이트
      await openCollectionModal(row.getData());
    }
  });
  
  // 청구내역 거래처명 초기화
  table.on("dataLoaded", function (data) {
  console.log("테이블 로드 완료, 행 수:", data.length);
  updateInvoiceTitle(null);                   // ‘청구내역’로 초기화(선택)
});

// 청구서 제목 업데이트
function updateInvoiceTitle(partnerName) {
  const el = document.getElementById("invoice-title");
  if (!el) return;
  el.textContent = partnerName ? `${partnerName}의 청구내역` : "청구내역";
}

  
  // 모달행 선택
 function setActiveRow(row) {
  const tableEl = document.getElementById("sales-table");
  // 기존 강조 해제
  tableEl.querySelectorAll(".tabulator-row.row-active").forEach(el => {
    el.classList.remove("row-active");
  });
  // 방금 클릭한 행만 강조
  row.getElement().classList.add("row-active");
}

  
  
  function setActiveViewButton(cell) {
  // 1) 테이블 내 모든 조회 버튼 초기화
  const allButtons = document.getElementById("sales-table")
                    .querySelectorAll(".btn-view-invoices");
  allButtons.forEach(btn => {
    btn.classList.remove("active", "btn-primary");
    btn.classList.add("btn-outline-primary");
  });

  // 2) 방금 클릭한 셀의 버튼만 활성화
  const btn = cell.getElement().querySelector(".btn-view-invoices");
  if (btn) {
    btn.classList.remove("btn-outline-primary");
    btn.classList.add("btn-primary", "active");
  }
}

// 행 클릭시 모든 버튼 초기화(선택)
document.getElementById("sales-table")
  .querySelectorAll(".btn-view-invoices.active")
  .forEach(b => b.classList.remove("active", "btn-primary"));



  // ===============================
  // 📌 테이블 로드 로그
  // ===============================
  table.on("dataLoaded", function (data) {
    console.log("테이블 로드 완료, 행 수:", data.length);
  });

  // ===============================
  // 📌 클릭 위임 예외 처리
  // ===============================
  salesTableEl.addEventListener("click", async function (e) {
  if (e.target.closest('.js-view-invoices')) return; // 조회버튼은 스킵
  const rowEl = e.target.closest(".tabulator-row");
  if (!rowEl) return;
  try {
    const row = table.rowManager.activeRows.find(r => r.element === rowEl);
    const data = row ? row.getData() : null;
   if (data) {
      setActiveRow(row);           // 행 강조
      await openCollectionModal(data);
    }
  } catch (err) {
    console.warn("delegate 처리 중 오류:", err);
  }
});

// 모달 닫히면 강조 해제
document.getElementById("insertCollectionModal")
  .addEventListener("hidden.bs.modal", () => {
    document.querySelectorAll("#sales-table .row-active")
      .forEach(el => el.classList.remove("row-active"));
  });
 
  
// =======================================
// (A) 더미데이터 생성기
// =======================================
async function fetchInvoices(partnerCode) {
  if (!partnerCode) return [];
  try {
    const res = await fetch(`/api/receivable/invoices?partnerCode=${encodeURIComponent(partnerCode)}`);
    if (!res.ok) throw new Error("HTTP " + res.status);
    const list = await res.json(); // ← Invoice 엔티티 배열

    // ✅ Tabulator용 포맷으로 가공 (collected 계산, 날짜 포맷)
    return list.map(i => {
      const dmndAmt   = Number(i.dmndAmt ?? 0);
      const unrctBaln = Number(i.unrctBaln ?? 0);
      const collected = dmndAmt - unrctBaln;

      // 날짜 문자열 정규화
      const dmndDateStr = (() => {
        const v = i.dmndDate;
        if (!v) return "";
        // v가 "2025-10-12T00:00:00.000+09:00" 같은 ISO면 앞 10자리만
        if (typeof v === "string") return v.slice(0,10);
        try { return new Date(v).toISOString().slice(0,10); } catch { return ""; }
      })();

      return {
        INVOICE_UNIQUE_CODE: i.invoiceUniqueCode,
        INVOICE_CODE:        i.invoiceCode,
        DMND_DATE:           dmndDateStr,
        ITEM_NAME:           "-",             // 상세JOIN 생기면 교체
        TOTAL_QTY:           null,            // 상세JOIN 생기면 교체
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

// =======================================
// ✅ (B) 청구내역 테이블 렌더러 (Tabulator)
// =======================================
let invoiceTable = null;

async function renderInvoiceTable(rowData) {
  const el = document.getElementById("invoice-table");
  if (!el) return;

  const partnerCode = rowData?.PARTNER_CODE || rowData?.partnerCode || "";
  const data = await fetchInvoices(partnerCode); // ✅ 실데이터 호출

  const columns = [
    { title:"", field:"_check", width:50, hozAlign:"center", headerSort:false,
      formatter:"rowSelection", titleFormatter:"rowSelection",
      bottomCalc: () => "합계" },
    { title:"청구번호", field:"INVOICE_CODE", width:140, hozAlign:"center", widthGrow:0.4 },
    { title:"청구일",   field:"DMND_DATE",   width:110, hozAlign:"center", widthGrow:0.4 },
    { title:"품목명",   field:"ITEM_NAME",   minWidth:180, widthGrow:0.3 },
    { title:"전체수량", field:"TOTAL_QTY",   width:95, hozAlign:"right", bottomCalc:"sum" },
    { title:"청구금액(원)", field:"DMND_AMT",    hozAlign:"right", formatter:"money", formatterParams:{precision:0}, bottomCalc:"sum" , widthGrow:0.5},
    { title:"수금금액(원)", field:"COLLECTED",   hozAlign:"right", formatter:"money", formatterParams:{precision:0}, bottomCalc:"sum", widthGrow:0.5 },
    { title:"미수금액(원)", field:"UNRCT_BALN",  hozAlign:"right", formatter:"money", formatterParams:{precision:0}, bottomCalc:"sum", widthGrow:0.5 },
    {
    title:"상태",
    field:"STATUS",
    width:110,
    hozAlign:"center",
    headerFilter:"select",
    widthGrow:0.6,
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

  { title:"비고", field:"REMK", minWidth:120 , widthGrow:0.5}  // ✅ 이제 여기엔 진짜 비고가 들어오게
];

  if (!invoiceTable) {
    invoiceTable = new Tabulator(el, {
      layout:"fitColumns",
      height:"260px",
      placeholder:"청구내역이 없습니다.",
      data,
      columns,
      columnDefaults:{ headerHozAlign:"center" },
      index:"INVOICE_UNIQUE_CODE",
    });
  } else {
    invoiceTable.setColumns(columns);
    invoiceTable.replaceData(data);
  }
}

  // ===============================
  // 📌 모달 열기
  // ===============================
  async function openCollectionModal(rowData) {
    const modalRoot = document.getElementById("insertCollectionModal");
    if (!modalRoot) return;

     // 거래처/잔액 세팅
	  document.getElementById("modalPartnerName").value = rowData?.CUSTOMERNAME || "";
	  
	  // 모달 내부 hidden partnerCode로 꽂아줌 (중복 id 충돌 회피)
	  const modalPartnerCodeEl = document.querySelector("#insertCollectionModal #partnerCode");
	  if (modalPartnerCodeEl) modalPartnerCodeEl.value = rowData?.PARTNER_CODE || "";
	  
	  // ✅ 미수잔액 숫자/표시 동시 세팅
	  const rawOutstanding = Number(String(rowData?.OUTSTANDING ?? 0).toString().replace(/[^\d]/g, "")) || 0;
	  const outstandingHidden = document.getElementById("outstandingAmt");
	  const outstandingView = document.getElementById("outstandingView");
	  if (outstandingHidden) outstandingHidden.value = rawOutstanding;                       // 숫자용
	  if (outstandingView)  outstandingView.value  = formatNumber(String(rawOutstanding));   // 표시용 (콤마)
			  
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
	  const collectAmtInput = document.getElementById("collectAmt");
	  const postDeductionInput = document.getElementById("postDeductionAmt");
	  if (collectAmtInput) collectAmtInput.value = "";
	  if (postDeductionInput) postDeductionInput.value = "";
	
	  const modal = new bootstrap.Modal(modalRoot);
	  modal.show();
	}

  // ===============================
  // 📌 검색
  // ===============================
  const btnSearch = document.getElementById("btn-search");
  if (btnSearch) {
    btnSearch.addEventListener("click", function () {
      const keyword = (document.getElementById("partnerName")?.value || "").trim();
      if (table) {
        if (keyword) table.setFilter("CUSTOMERNAME", "like", keyword);
        else table.clearFilter();
      }
    });
  }
  
  // ===============================
  // 📌 금액 입력창 콤마 처리
  // ===============================
  const collectAmtInput = document.getElementById("collectAmt");

  function uncomma(value) {
    return value.replace(/[^\d]+/g, "");
  }

  function formatNumber(value) {
    if (!value) return "";
    return value.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  if (collectAmtInput) {
    collectAmtInput.addEventListener("input", function (e) {
      e.target.value = uncomma(e.target.value);
    });

    collectAmtInput.addEventListener("blur", function (e) {
      e.target.value = formatNumber(uncomma(e.target.value));
    });

    collectAmtInput.addEventListener("focus", function (e) {
      e.target.value = uncomma(e.target.value);
    });
  }
  
 // ===============================
// 📌 금액 입력창 콤마 처리 (기존 collectAmt 아래에 추가)
// ===============================
const postDeductionInput = document.getElementById("postDeductionAmt");
if (postDeductionInput) {
  postDeductionInput.addEventListener("input", function (e) {
    e.target.value = uncomma(e.target.value);
  });
  postDeductionInput.addEventListener("blur", function (e) {
    e.target.value = formatNumber(uncomma(e.target.value));
  });
  postDeductionInput.addEventListener("focus", function (e) {
    e.target.value = uncomma(e.target.value);
  });
}


// ===============================
// 📌 수금 등록 (교체)
// ===============================
const btnSave = document.getElementById("btnSave");
if (btnSave) {
  btnSave.addEventListener("click", async function () {
    const moneyDate = document.getElementById("moneyDate").value;
    const recpt = Number(uncomma(document.getElementById("collectAmt").value || "0"));
    const postDeduction = Number(uncomma((document.getElementById("postDeductionAmt")?.value) || "0"));
    const paymentMethods = document.getElementById("paymentType").value;
    const remk = document.getElementById("remarks").value;

    // 모달 내부 partnerCode를 정확히 집자 (상단과 id 중복 방지)
    const partnerCode = (document.querySelector("#insertCollectionModal #partnerCode")?.value) || "";

    const outstandingVal = Number(uncomma(document.getElementById("outstandingAmt").value || "0"));

    // 필수 검증
    if (!partnerCode) { alert("거래처를 선택하세요."); return; }
    if (recpt <= 0) { alert("수금금액은 0보다 커야 합니다."); return; }
    const totalApply = recpt + postDeduction;
    if (totalApply > outstandingVal) {
      alert("수금금액 + 사후공제 금액이 미수잔액보다 큽니다.");
      return;
    }
    if (!paymentMethods) { alert("결제방식을 선택하세요."); return; }

    const data = {
      moneyDate,
      recpt,
      postDeduction,        // ✅ 서버로 함께 전송
      paymentMethods,
      remk,
      partnerCode
    };

    try {
      const res = await fetch("/api/collection/insert", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      const result = await res.json();
      if (result.success) {
        alert("수금 등록되었습니다.");
        const modalEl = document.getElementById("insertCollectionModal");
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal?.hide();
        table?.replaceData();
      } else {
        alert("실패: " + (result.message || "서버 오류"));
      }
    } catch (err) {
      console.error("등록 중 오류:", err);
      alert("서버 통신 오류");
    }
  });
}
  // ===============================
  // 📌 클릭 막힘 방지
  // ===============================
  salesTableEl.style.position = "relative";
  salesTableEl.style.zIndex = "5";
  salesTableEl.style.pointerEvents = "auto";

}); // end DOMContentLoaded

