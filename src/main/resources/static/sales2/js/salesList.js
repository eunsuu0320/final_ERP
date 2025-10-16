document.addEventListener("DOMContentLoaded", function () {
  // 동적 높이 계산(반응형); 최소 380px, 화면의 55%
  const getTableHeight = () => Math.max(380, Math.floor(window.innerHeight * 0.55)) + "px";

  // ================================
  // 📌 메인 목록 테이블
  // ================================
  table = new Tabulator("#sales-table", {
    height: getTableHeight(),          // 🔹반응형 높이
    layout: "fitColumns",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/sales/stats",
    ajaxResponse: function (url, params, response) {
      let processedData = [];
      let prevCount = null;
      response.forEach(item => {
        const currentCount = item.correspondentCount || 0;
        let retradeRate = 0;
        if (prevCount !== null && prevCount !== 0) {
          retradeRate = ((currentCount - prevCount) / prevCount) * 100;
        }
        item.retradeRate = retradeRate.toFixed(2) + "%";

        if (item.planYear) {
          try { item.SALESYEAR = new Date(item.planYear).getFullYear(); } catch (e) {}
        }
        processedData.push(item);
        prevCount = currentCount;
      });
      return processedData;
    },
    columns: [
      {
        formatter: "rowSelection",
        titleFormatter: "rowSelection",
        titleFormatterParams: { rowRange: "active" },
        headerHozAlign: "center",
        hozAlign: "center",
        headerSort: false,
        width: 44,
        cssClass: "sel-col",
        cellClick: (e, cell) => cell.getRow().toggleSelect(),
      },
      { title: "년도", field: "SALESYEAR", hozAlign: "center", sorter: "number" },
      { title: "총 매출액", field: "TOTALSALESAMOUNT", sorter: "number", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "총 영업이익", field: "TOTALPROFITAMOUNT", sorter: "number", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "신규 거래처수", field: "CORRESPONDENTCOUNT", hozAlign: "center", sorter: "number" },
    ],
  });

  // 창 크기 변경 시 테이블 높이만 동적으로 재설정 (기능 영향 X)
  window.addEventListener("resize", () => {
    try { table.setHeight(getTableHeight()); } catch (e) {}
  });

  // ================================
  // 📌 작년 영업계획 (조회용)
  // ================================
  var lastYearTable = new Tabulator("#lastYearTable", {
    layout: "fitColumns",
    height: "350px",
    ajaxURL: "/api/sales/last-year-qty",
    ajaxParams: { year: 2024 },
    columns: [
      { title: "분기", field: "SALES_QUARTER", hozAlign: "center" },
      { title: "작년 총 매출액", field: "TOTAL_SALES_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "작년 총 매입단가", field: "TOTAL_COST_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "작년 총 영업이익", field: "TOTAL_PROFIT_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
    ],
  });

  // ================================
  // 📌 올해 영업계획 (등록용)
  // ================================
  var thisYearTable = new Tabulator("#thisYearTable", {
    layout: "fitColumns",
    height: "350px",
    columns: [
      { title: "분기", field: "qtr", hozAlign: "center", editor: false },
      { title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter },
      { title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter },
      { title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number" },
    ],
    data: [
      { qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
    ],
  });

  // ================================
  // 📌 수정 모달 테이블
  // ================================
  var editYearTable = new Tabulator("#editYearTable", {
    layout: "fitColumns",
    height: "350px",
    columns: [
      { title: "분기", field: "qtr", hozAlign: "center" },
      { title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter },
      { title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter },
      { title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number" },
    ],
  });

  // ================================
  // 📌 행 클릭 시 수정 모달 열기
  // ================================
  table.on("rowClick", function (e, row) {
    const rowData = row.getData();
    currentSalesPlanCode = rowData.salesPlanCode;

    fetch(`/api/sales/plan/${rowData.SALESYEAR}/details`)
      .then(res => { if (!res.ok) throw new Error("서버 응답 오류: " + res.status); return res.json(); })
      .then(data => {
        editYearTable.clearData();
        const modalEl = document.getElementById("modifySalesModal");
        let modal = bootstrap.Modal.getInstance(modalEl);
        if (!modal) modal = new bootstrap.Modal(modalEl);
        modal.show();
        setTimeout(() => editYearTable.setData(data), 200);
      })
      .catch(err => { console.error("수정 데이터 로드 실패:", err); alert("수정 데이터를 불러올 수 없습니다. 콘솔 로그 확인하세요."); });
  });

  // ================================
  // 📌 신규 버튼
  // ================================
  document.getElementById("btn-new").addEventListener("click", async function () {
    try {
      const response = await fetch("/api/sales/check-this-year");
      const data = await response.json();
      if (data.exists) { alert("올해 영업계획이 이미 등록되어 있습니다."); return; }
      const modal = new bootstrap.Modal(document.getElementById("insertSalesModal"));
      modal.show();
    } catch (error) {
      console.error(error); alert("오류 발생: " + error.message);
    }
  });

  // ================================
  // 📌 저장 버튼 (등록)
  // ================================
  document.getElementById("btn-save-sales").addEventListener("click", function () {
    const tableData = thisYearTable.getData();
    const payload = tableData.map(row => ({
      qtr: row.qtr,
      purpSales: row.purpSales || 0,
      purpProfitAmt: row.purpProfitAmt || 0,
      newVendCnt: row.newVendCnt || 0,
    }));
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch('/api/sales/insert', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
      body: JSON.stringify(payload)
    })
      .then(res => { if (res.ok) { alert("영업계획이 저장되었습니다."); table.replaceData(); } else { return res.text().then(text => { throw new Error(text); }); } })
      .catch(err => { console.error(err); alert("저장 실패: " + err.message); });
  });

  // ================================
  // 📌 combo-option (존재 시에만)
  // ================================
  const combo = document.getElementById("combo-option");
  if (combo) {
    combo.addEventListener("change", function () {
      const percent = parseFloat(this.options[this.selectedIndex].text.replace("%", "")) / 100;
      const lastYearData = lastYearTable.getData();
      const newData = lastYearData.map(row => {
        const lastSales = row.TOTAL_SALES_AMOUNT || 0;
        const lastProfit = row.TOTAL_PROFIT_AMOUNT || 0;
        return {
          qtr: row.SALES_QUARTER + "분기",
          purpSales: Math.floor(lastSales * (1 + percent)),
          purpProfitAmt: Math.floor(lastProfit * (1 + percent)),
          newVendCnt: 0
        };
      });
      thisYearTable.setData(newData);
    });
  }

  // ================================
  // 📌 초기화 버튼
  // ================================
  document.getElementById("btn-reset-sales").addEventListener("click", function () {
    thisYearTable.setData([
      { qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
      { qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" }
    ]);
  });

  // ================================
  // 📌 수정 저장 버튼
  // ================================
  document.getElementById("btn-update-sales").addEventListener("click", function () {
    const updatedData = editYearTable.getData();
    const payload = updatedData.map(d => ({
      qtr: d.qtr,
      purpSales: d.purpSales,
      purpProfitAmt: d.purpProfitAmt,
      newVendCnt: d.newVendCnt,
      salesPlanCode: d.salesPlanCode,
    }));

    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch("/api/sales/update", {
      method: "PUT",
      headers: { "Content-Type": "application/json", [csrfHeader]: csrfToken },
      body: JSON.stringify(payload)
    })
      .then(res => {
        if (res.ok) {
          alert("수정되었습니다.");
          table.replaceData();
          bootstrap.Modal.getInstance(document.getElementById("modifySalesModal")).hide();
        } else {
          return res.text().then(text => { throw new Error(text); });
        }
      })
      .catch(err => { console.error(err); alert("저장 실패: " + err.message); });
  });

  // ================================
  // 📌 수정 취소 버튼
  // ================================
  document.getElementById("btn-cancel-update").addEventListener("click", function () {
    bootstrap.Modal.getInstance(document.getElementById("modifySalesModal")).hide();
  });

  // ================================
  // 📌 금액 Formatter
  // ================================
  function moneyFormatter(cell) {
    let value = cell.getValue();
    return value ? Number(value).toLocaleString() : "0";
  }
});


// 미수금 top5
function loadTopOutstanding() {
  fetch("/api/invoices/top-outstanding?companyCode=C001&limit=5")
    .then(res => res.json())
    .then(list => {
      const tbody = document.getElementById("top-ar-body");
      if (!tbody) return;
      if (!Array.isArray(list) || list.length === 0) {
        tbody.innerHTML = `<tr><td>데이터 없음</td></tr>`;
        return;
      }
      tbody.innerHTML = list.map(r => {
        const name = (r.partnerName ?? r.PARTNERNAME) ?? '-';
        const amt  = Number((r.totalUnrctBaln ?? r.TOTALUNRCTBALN) ?? 0).toLocaleString();
        return `<tr><td>${name}</td><td class="text-end">${amt}</td></tr>`;
      }).join("");
    })
    .catch(err => {
      console.error("TOP5 로드 실패:", err);
      const tbody = document.getElementById("top-ar-body");
      if (tbody) tbody.innerHTML = `<tr><td>로드 실패</td></tr>`;
    });
}

// 페이지 로드시 1회 호출
document.addEventListener("DOMContentLoaded", () => loadTopOutstanding());
