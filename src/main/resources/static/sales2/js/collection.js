document.addEventListener("DOMContentLoaded", function () {

  // ===============================
  // 📌 테이블 생성
  // ===============================
  const salesTableEl = document.getElementById("sales-table");
  if (!salesTableEl) {
    console.error("❌ #sales-table 요소를 찾을 수 없습니다.");
    return;
  }

  let table = new Tabulator(salesTableEl, {
    layout: "fitColumns",
    height: "350px",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    ajaxResponse: function (url, params, response) {
      console.log("📡 /api/receivable/list 응답:", response);
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";
      return response;
    },
    columns: [
      { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, width: 100},
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" },
      { title: "미수금액", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "총 수금", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수금(잔액)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수건수", field: "INVOICE_COUNT", hozAlign: "center" },
      
      { 
		  title: "조회", 
		  field: "VIEW_BTN",
		  hozAlign: "center",
		  headerSort: false,
		  width: 200,
		  formatter: () => '<button class="btn btn-outline-primary btn-view-invoices js-view-invoices">조회</button>',
		  cellClick: (e, cell) => {
		    e.stopPropagation(); // Tabulator의 rowClick 방지
		    const d = cell.getRow().getData();
		    renderInvoiceTable(d);
		    document.getElementById("invoice-table")?.scrollIntoView({behavior:"smooth", block:"start"});
		  }
}
    ],  
    rowClick: async function (e, row) {
	   if (e.target.closest('.js-view-invoices')) return; // 조회버튼 클릭 시 모달 열지 않음
      await openCollectionModal(row.getData());
    }
  });
  
  

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
    if (data) await openCollectionModal(data);
  } catch (err) {
    console.warn("delegate 처리 중 오류:", err);
  }
});

  
  
// =======================================
// ✅ (A) 더미데이터 생성기
// =======================================
function getDummyInvoices(partnerCode) {
  const base = [
    {
      INVOICE_UNIQUE_CODE: 101001,
      INVOICE_CODE: "INV-2025-0001",
      DMND_DATE: "2025-09-30",
      ITEM_NAME: "크루아상 생지 50입",
      TOTAL_QTY: 120,
      DMND_AMT: 3600000,
      COLLECTED: 2000000,
      UNRCT_BALN: 1600000,
      REMK: "9월말 청구"
    },
    {
      INVOICE_UNIQUE_CODE: 101002,
      INVOICE_CODE: "INV-2025-0005",
      DMND_DATE: "2025-10-05",
      ITEM_NAME: "식빵 생지 30입",
      TOTAL_QTY: 80,
      DMND_AMT: 2400000,
      COLLECTED: 1000000,
      UNRCT_BALN: 1400000,
      REMK: "10월 초 청구"
    },
    {
      INVOICE_UNIQUE_CODE: 101003,
      INVOICE_CODE: "INV-2025-0010",
      DMND_DATE: "2025-10-12",
      ITEM_NAME: "마카롱 시트 100장",
      TOTAL_QTY: 60,
      DMND_AMT: 5050000,
      COLLECTED: 3000000,
      UNRCT_BALN: 2050000,
      REMK: ""
    }
  ];

  if (!partnerCode) return base;
  const salt = partnerCode.split("").reduce((a,c)=>a+c.charCodeAt(0),0)%3;
  return base.map((r,i)=>({
    ...r,
    INVOICE_UNIQUE_CODE: r.INVOICE_UNIQUE_CODE + salt*10 + i,
    INVOICE_CODE: r.INVOICE_CODE.replace("000","00"+((i+salt)%9)),
    DMND_AMT: r.DMND_AMT + salt*100000,
    COLLECTED: r.COLLECTED + ((i%2)? salt*50000 : 0),
    UNRCT_BALN: (r.DMND_AMT + salt*100000) - (r.COLLECTED + ((i%2)? salt*50000 : 0))
  }));
}

// =======================================
// ✅ (B) 청구내역 테이블 렌더러 (Tabulator)
// =======================================
let invoiceTable = null;

function renderInvoiceTable(rowData) {
  const el = document.getElementById("invoice-table");
  if (!el) return;

  const partnerCode = rowData?.PARTNER_CODE || null;
  const data = getDummyInvoices(partnerCode);

  const columns = [
    {
      title: "",
      field: "_check",
      width: 50,
      hozAlign: "center",
      headerSort: false,
      formatter: "rowSelection",
      titleFormatter: "rowSelection",
      bottomCalc: function(){ return "합계"; } // 합계 라벨
    },
    { title: "청구번호", field: "INVOICE_CODE", width: 140, hozAlign: "center" },
    { title: "청구일",   field: "DMND_DATE",   width: 110, hozAlign: "center" },
    { title: "품목명",   field: "ITEM_NAME",   minWidth: 180 },
    { title: "전체수량", field: "TOTAL_QTY",   width: 95, hozAlign: "right", bottomCalc: "sum" },
    { title: "청구금액", field: "DMND_AMT",    hozAlign: "right", formatter: "money", formatterParams:{precision:0}, bottomCalc: "sum" },
    { title: "수금금액", field: "COLLECTED",   hozAlign: "right", formatter: "money", formatterParams:{precision:0}, bottomCalc: "sum" },
    { title: "미수금액", field: "UNRCT_BALN",  hozAlign: "right", formatter: "money", formatterParams:{precision:0}, bottomCalc: "sum" },
    { title: "비고",     field: "REMK",        minWidth: 120 }
  ];

  if (!invoiceTable) {
    invoiceTable = new Tabulator(el, {
      layout: "fitColumns",
      height: "260px",
      placeholder: "청구내역이 없습니다.",
      data,
      columns,
      columnDefaults: { headerHozAlign: "center" },
      index: "INVOICE_UNIQUE_CODE",
      // 필요하면 행 더블클릭 시 모달 열기 등 핸들러 추가 가능
    });
  } else {
    invoiceTable.setColumns(columns);
    invoiceTable.replaceData(data);
  }
}

// =======================================
// ✅ (C) 거래처 행 클릭 시: 청구내역 렌더
// =======================================
// 기존 rowClick을 쓰고 있다면, 아래처럼 호출만 추가
if (typeof table !== "undefined" && table) {
  table.updateOptions({
    rowClick: async function (e, row) {
      const d = row.getData();
      renderInvoiceTable(d);      // 클릭한 거래처의 청구내역 표시
      await openCollectionModal(d);
    }
  });
}

  // ===============================
  // 📌 모달 열기
  // ===============================
  async function openCollectionModal(rowData) {
    const modalRoot = document.getElementById("insertCollectionModal");
    if (!modalRoot) return;

     // 거래처/잔액 세팅
	  document.getElementById("modalPartnerName").value = rowData?.CUSTOMERNAME || "";
	  // ⚠️ 모달 내부 hidden partnerCode로 꽂아줌 (중복 id 충돌 회피)
	  const modalPartnerCodeEl = document.querySelector("#insertCollectionModal #partnerCode");
	  if (modalPartnerCodeEl) modalPartnerCodeEl.value = rowData?.PARTNER_CODE || "";
	
	  document.getElementById("outstandingAmt").value = rowData?.OUTSTANDING || 0;
	  
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
  // 📌 엑셀 다운로드
  // ===============================
  const btnExcel = document.getElementById("btn-excel");
  if (btnExcel) {
    btnExcel.addEventListener("click", function () {
      table.download("xlsx", "수금관리.xlsx", { sheetName: "수금관리" });
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

    // ⚠️ 모달 내부 partnerCode를 정확히 집자 (상단과 id 중복 방지)
    const partnerCode = (document.querySelector("#insertCollectionModal #partnerCode")?.value) || "";

    const outstandingVal = Number(uncomma(document.getElementById("outstandingAmt").value || "0"));

    // ✅ 필수 검증
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
        alert("✅ " + result.message);
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

}); //DOMContentLoaded 닫는 괄호



