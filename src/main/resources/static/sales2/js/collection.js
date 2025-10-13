document.addEventListener("DOMContentLoaded", function () {
  // ===== Sanity 체크 (콘솔에서 확인용) =====
  const modalRoot = document.getElementById("insertCollectionModal");
  console.log("[CHECK] modalEl exists:", !!modalRoot, " bootstrap.Modal:", !!(window.bootstrap && bootstrap.Modal));

  // 모달 열기 전용 함수 (안 뜨면 이유 콘솔에 표시)
  function showModalOnly() {
    if (!modalRoot) {
      console.error("[ERROR] #insertCollectionModal 요소를 찾지 못했습니다.");
      return;
    }
    try {
      const modal = new bootstrap.Modal(modalRoot);
      modal.show();
    } catch (e) {
      console.error("[ERROR] bootstrap.Modal 생성/표시 중 오류:", e);
    }
  }

  // ===== 메인 테이블 =====
  let table = new Tabulator("#sales-table", {
    layout: "fitColumns",
    height: "500px",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    columns: [
      { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false },
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" },
      { title: "총 매출", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "총 수금", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수금(잔액)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수건수", field: "ARREARSCOUNT", hozAlign: "center" }
    ],
    ajaxResponse: function (url, params, response) {
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";
      return response;
    },

    // 1) 기본: Tabulator의 rowClick (되면 이걸로 열림)
    rowClick: function () {
      console.log("[rowClick] fired -> modal open");
      showModalOnly();
    }
  });

  // 2) 보강: Tabulator 이벤트가 무력화될 경우를 대비한 **이벤트 위임 fallback**
  //    테이블 컨테이너에서 .tabulator-row 클릭을 가로채 모달을 연다.
  const tableHost = document.getElementById("sales-table");
  if (tableHost) {
    tableHost.addEventListener("click", function (e) {
      const rowEl = e.target.closest(".tabulator-row");
      if (rowEl) {
        console.log("[delegate] .tabulator-row clicked -> modal open");
        showModalOnly();
      }
    });
  } else {
    console.error("[ERROR] #sales-table 요소를 찾지 못했습니다.");
  }

  // ===== 기존 검색/엑셀(그대로 유지) =====
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

  const partnerNameInput = document.getElementById("partnerName");
  if (partnerNameInput) {
    partnerNameInput.addEventListener("keyup", function (e) {
      if (e.key === "Enter") btnSearch?.click();
    });
  }

  const btnExcel = document.getElementById("btn-excel");
  if (btnExcel) {
    btnExcel.addEventListener("click", function () {
      table.download("xlsx", "수금관리.xlsx", { sheetName: "수금관리" });
    });
  }
});
