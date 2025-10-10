document.addEventListener("DOMContentLoaded", function() {

  // 📅 최근 5년 드롭다운 자동 생성
  const yearSelect = document.getElementById("yearSelect");
  const currentYear = new Date().getFullYear();
  for (let y = currentYear; y > currentYear - 5; y--) {
    const option = document.createElement("option");
    option.value = y;
    option.textContent = y;
    yearSelect.appendChild(option);
  }

  // 🧭 보기 탭 전환
  const btnProduct = document.getElementById("btnProductView");
  const btnEmployee = document.getElementById("btnEmployeeView");

  if (btnProduct) {
    btnProduct.addEventListener("click", () => window.location.href = "/businessProfits");
  }
  if (btnEmployee) {
    btnEmployee.addEventListener("click", () => window.location.href = "/employeeProfits");
  }

  // 📈 요약 더미데이터
  const summaryData = {
    totalSalesCount: 100,
    totalSupply: 900000,
    totalTax: 100000,
    totalAmount: 1000000
  };
  document.getElementById("totalSalesCount").textContent = summaryData.totalSalesCount.toLocaleString();
  document.getElementById("totalSupply").textContent = summaryData.totalSupply.toLocaleString();
  document.getElementById("totalTax").textContent = summaryData.totalTax.toLocaleString();
  document.getElementById("totalAmount").textContent = summaryData.totalAmount.toLocaleString();

  // 📊 사원별 테이블
  const tableData = [
    { empCode: "E001", empName: "박효준", qty: 120, supply: 1200000, tax: 120000, total: 1320000 },
    { empCode: "E002", empName: "이영희", qty: 50, supply: 800000, tax: 80000, total: 880000 },
    { empCode: "E003", empName: "정우성", qty: 70, supply: 950000, tax: 95000, total: 1045000 },
    { empCode: "E004", empName: "김민재", qty: 90, supply: 1000000, tax: 100000, total: 1100000 },
    { empCode: "E005", empName: "최보람", qty: 60, supply: 850000, tax: 85000, total: 935000 }
  ];

  const table = new Tabulator("#sales-table", {
    layout: "fitDataStretch",
    height: "480px",
    pagination: "local",
    paginationSize: 5,
    data: tableData,
    placeholder: "데이터가 없습니다.",
    columns: [
      { title: "사원코드", field: "empCode", hozAlign: "center", width: 120 },
      { title: "사원명", field: "empName", hozAlign: "center", width: 150 },
      { title: "수량", field: "qty", hozAlign: "right", width: 120 },
      { title: "공급가액", field: "supply", hozAlign: "right", width: 150 },
      { title: "부가세", field: "tax", hozAlign: "right", width: 120 },
      { title: "합계", field: "total", hozAlign: "right", width: 150 }
    ],
    rowClick: function (e, row) {
      const rowData = row.getData();
      console.log("✅ 행 클릭됨:", rowData);
      openEmployeeModal(rowData);
    }
  });

  // 🔍 검색 기능
  const searchAction = () => {
    const keyword = document.getElementById("searchInput").value.toLowerCase();
    table.setFilter("empName", "like", keyword);
  };
  document.getElementById("btn-search").addEventListener("click", searchAction);
  document.getElementById("searchIcon").addEventListener("click", searchAction);
});


// ✅ 모달 열기 함수 (하나만 남김)
function openEmployeeModal(data) {
  console.log("✅ openEmployeeModal 실행:", data);

  const modalEl = document.getElementById("employeeModal");
  if (!modalEl) {
    console.error("❌ employeeModal 요소를 찾을 수 없습니다.");
    return;
  }

  // 제목 변경
  document.getElementById("employeeModalLabel").textContent = `${data.empName} 사원 판매목록`;

  // Bootstrap 모달 인스턴스 가져오기
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();

  // 📊 더미 데이터
  const salesData = [
    { date: "2025-09-12", customer: "예스홈", salesAmt: 350000, collectAmt: 150000, remark: "" },
    { date: "2025-03-15", customer: "씽크존", salesAmt: 250000, collectAmt: 0, remark: "" },
    { date: "2025-02-05", customer: "바이존", salesAmt: 800000, collectAmt: 700000, remark: "" },
    { date: "2025-08-08", customer: "참잘", salesAmt: 750000, collectAmt: 500000, remark: "" }
  ];

  // 이미 존재하는 테이블 초기화
  const existing = Tabulator.findTable("#employee-sales-table");
  if (existing.length) existing[0].destroy();

  // 새 Tabulator 생성
  const modalTable = new Tabulator("#employee-sales-table", {
    layout: "fitDataStretch",
    height: "400px",
    pagination: "local",
    paginationSize: 5,
    data: salesData,
    placeholder: "판매 데이터가 없습니다.",
    columns: [
      { title: "일자", field: "date", hozAlign: "center", width: 120 },
      { title: "거래처명", field: "customer", hozAlign: "center", width: 150 },
      { title: "매출금액", field: "salesAmt", hozAlign: "right", formatter: "money", width: 150 },
      { title: "수금금액", field: "collectAmt", hozAlign: "right", formatter: "money", width: 150 },
      { title: "비고", field: "remark", hozAlign: "left" }
    ]
  });

  // 모달 내 검색
  const modalSearch = () => {
    const keyword = document.getElementById("modal-searchInput").value.toLowerCase();
    modalTable.setFilter("customer", "like", keyword);
  };
  document.getElementById("modal-btn-search").onclick = modalSearch;
  document.getElementById("modal-searchIcon").onclick = modalSearch;
}
