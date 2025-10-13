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
    height: "500px",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",
    ajaxResponse: function (url, params, response) {
      console.log("📡 /api/receivable/list 응답:", response);
      const el = document.querySelector("#total-count span");
      if (el) el.textContent = (Array.isArray(response) ? response.length : 0) + "건";
      return response;
    },
    columns: [
      { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false },
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" },
      { title: "청구금액", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "총 수금", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수금(잔액)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수건수", field: "ARREARSCOUNT", hozAlign: "center" }
    ],
    rowClick: async function (e, row) {
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
    const rowEl = e.target.closest(".tabulator-row");
    if (!rowEl) return;

    try {
      const row = table.rowManager.activeRows.find(r => r.element === rowEl);
      const data = row ? row.getData() : null;
      console.log("delegate click:", data);
      if (data) await openCollectionModal(data);
    } catch (err) {
      console.warn("delegate 처리 중 오류:", err);
    }
  });

  // ===============================
  // 📌 모달 열기
  // ===============================
  async function openCollectionModal(rowData) {
    const modalRoot = document.getElementById("insertCollectionModal");
    if (!modalRoot) return;

    document.getElementById("modalPartnerName").value = rowData?.CUSTOMERNAME || "";
    document.getElementById("partnerCode").value = rowData?.PARTNER_CODE || "";
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
  // 📌 수금 등록
  // ===============================
  const btnSave = document.getElementById("btnSave");
  if (btnSave) {
    btnSave.addEventListener("click", async function () {

      const data = {
        moneyDate: document.getElementById("moneyDate").value,
        recpt: Number(uncomma(document.getElementById("collectAmt").value)),
        paymentMethods: document.getElementById("paymentType").value,
        remk: document.getElementById("remarks").value,
        partnerCode: document.getElementById("partnerCode").value
      };

      const recptVal = data.recpt;
      const outstandingVal = Number(uncomma(document.getElementById("outstandingAmt").value));

      if (!data.partnerCode) {
        alert("거래처를 선택하세요.");
        return;
      }
      if (!recptVal || recptVal <= 0) {
        alert("수금금액은 필수입니다.");
        return;
      }
      if (recptVal > outstandingVal) {
        alert("수금금액이 미수금(잔액)보다 많습니다.");
        return;
      }

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

}); // ✅ DOMContentLoaded 닫는 괄호
