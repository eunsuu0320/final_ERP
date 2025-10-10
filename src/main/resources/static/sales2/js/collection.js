document.addEventListener("DOMContentLoaded", function() {

  // ================================
  // 📌 메인 목록 테이블
  // ================================
  let table = new Tabulator("#sales-table", {
    layout: "fitColumns",
    height: "500px",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/receivable/list",  // 👉 백엔드 API 엔드포인트 맞게 수정
    columns: [
      { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false },
      { title: "거래처명", field: "CUSTOMERNAME", hozAlign: "center" },
      { title: "총 매출", field: "TOTALSALES", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "총 수금", field: "TOTALCOLLECTED", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수금(잔액)", field: "OUTSTANDING", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
      { title: "미수건수", field: "ARREARSCOUNT", hozAlign: "center" }
    ],
    ajaxResponse: function (url, params, response) {
      document.querySelector("#total-count span").textContent = response.length + "건";
      return response;
    }
  });


  // ================================
  // 📌 신규 버튼 → 모달 열기
  // ================================
  const btnNew = document.getElementById("btn-new");
  const modalEl = document.getElementById("insertCollectionModal");

  if (btnNew && modalEl) {
    btnNew.addEventListener("click", function() {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    });
  } else {
    console.error("❌ 신규 버튼 또는 모달 요소를 찾을 수 없습니다.");
  }

  // ================================
  // 📌 모달이 열릴 때 Tabulator 리렌더링
  // ================================
  modalEl.addEventListener("shown.bs.modal", function() {
    if (window.invoiceTable) {
      invoiceTable.redraw();
    }
  });


  // ================================
  // 📌 청구내역 Tabulator
  // ================================
  window.invoiceTable = new Tabulator("#invoiceTable", {
    height: "300px",
    layout: "fitColumns",
    placeholder: "청구내역이 없습니다.",
    columns: [
      {formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 60, headerSort: false},
      {title: "출하번호", field: "shipmentNo", width: 120},
      {title: "출하일", field: "shipmentDate", hozAlign: "center"},
      {title: "품목명", field: "productName", width: 180},
      {title: "전체수량", field: "quantity", hozAlign: "right"},
      {title: "단가", field: "unitPrice", hozAlign: "right", formatter: "money", formatterParams: {precision: 0}},
      {title: "공급가액", field: "supplyAmt", hozAlign: "right", formatter: "money", formatterParams: {precision: 0}},
      {title: "부가세", field: "taxAmt", hozAlign: "right", formatter: "money", formatterParams: {precision: 0}},
      {title: "최종금액", field: "totalAmt", hozAlign: "right", formatter: "money", formatterParams: {precision: 0}},
      {title: "비고", field: "remark"}
    ],
    data: [
      {shipmentNo: "SHP0001", shipmentDate: "2025-05-05", productName: "크로와상 생지 20개입", quantity: 10, unitPrice: 30000, supplyAmt: 300000, taxAmt: 30000, totalAmt: 330000, remark: ""},
      {shipmentNo: "SHP0002", shipmentDate: "2025-05-05", productName: "치즈롤 생지 10개입", quantity: 5, unitPrice: 25000, supplyAmt: 125000, taxAmt: 12500, totalAmt: 137500, remark: ""}
    ]
  });


  // ================================
  // 📌 저장 버튼 → 수금 등록
  // ================================
  document.getElementById("btn-saveCollection").addEventListener("click", async function() {
    const formData = {
      moneyDate: `${document.getElementById("year").value}-${document.getElementById("month").value}-${document.getElementById("day").value}`,
      partnerName: document.getElementById("partnerName").value,
      manager: document.getElementById("manager").value,
      paymentInfo: document.getElementById("paymentInfo").value
    };

    try {
      const response = await fetch("/api/collection/insert", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(formData)
      });

      const result = await response.json();

      if (result.success) {
        alert("✅ 수금 등록이 완료되었습니다!");
        bootstrap.Modal.getInstance(modalEl).hide();
        location.reload();
      } else {
        alert("등록 실패: " + result.message);
      }
    } catch (error) {
      console.error(error);
      alert("오류 발생: " + error.message);
    }
  });


  // ================================
  // 📌 청구서 조회 버튼
  // ================================
  document.getElementById("btn-searchInvoice").addEventListener("click", function() {
    alert("청구서 조회 모달 연결 예정");
  });

  // ================================
  // 📌 결제정보 조회 버튼
  // ================================
  document.getElementById("btn-searchPayment").addEventListener("click", function() {
    alert("결제정보 검색 기능 예정");
  });


  // ================================
  // 📌 Excel 내보내기
  // ================================
  document.getElementById("btn-excel").addEventListener("click", function () {
    table.download("xlsx", "수금관리.xlsx", { sheetName: "수금관리" });
  });

});
