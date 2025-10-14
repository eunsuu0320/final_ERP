document.addEventListener("DOMContentLoaded", function () {

  // ===============================
  // 📅 최근 5년 드롭다운
  // ===============================
  const yearSelect = document.getElementById("yearSelect");
  const quarterSelect = document.getElementById("quarterSelect");
  const currentYear = new Date().getFullYear();

  if (yearSelect && yearSelect.options.length === 0) {
    for (let y = currentYear; y > currentYear - 5; y--) {
      const option = document.createElement("option");
      option.value = y;
      option.textContent = y;
      yearSelect.appendChild(option);
    }
  }

  // ===============================
  // 🧭 탭 전환
  // ===============================
  const btnProduct = document.getElementById("btnProductView");
  const btnEmployee = document.getElementById("btnEmployeeView");
  if (btnProduct) btnProduct.addEventListener("click", () => (window.location.href = "/businessProfits"));
  if (btnEmployee) btnEmployee.addEventListener("click", () => (window.location.href = "/employeeProfits"));

  // ===============================
  // 🧩 필터 파라미터
  // ===============================
  function getFilterParams() {
    let year = yearSelect?.value || null;
    let quarter = quarterSelect?.value || null;
    let keyword = document.getElementById("productName")?.value?.trim() || null;

    if (year === "") year = null;
    if (quarter === "") quarter = null;
    if (keyword === "") keyword = null;

    return { year, quarter, keyword };
  }

  // ===============================
  // 📊 Tabulator 테이블
  // ===============================
  const table = new Tabulator("#sales-table", {
    layout: "fitDataStretch",
    height: "480px",
    pagination: "local",
    paginationSize: 10,

    // ❗초기 요청에도 companyCode 포함 (500 방지)
    ajaxURL: "/api/employeeProfits/list",
    ajaxConfig: "GET",
    ajaxParams: { companyCode: "C001" },

    ajaxURLGenerator: function (url, config, params) {
      const filters = getFilterParams();
      const qs = new URLSearchParams();
      // 필수
      qs.append("companyCode", "C001");
      // 선택
      if (filters.year) qs.append("year", filters.year);
      if (filters.quarter) qs.append("quarter", filters.quarter);
      if (filters.keyword) qs.append("keyword", filters.keyword);
      // 캐시 방지
      qs.append("_", Date.now());
      const full = `${url}?${qs.toString()}`;
      console.log("[employeeProfits] ajaxURL:", full);
      return full;
    },

    ajaxResponse: function (url, params, response) {
      console.log("📡 사원별 영업이익 목록:", response);

      // ✅ 요약 합계 갱신
      let totalSalesCount = 0;
      let totalSupply = 0;
      let totalTax = 0;
      let totalAmount = 0;

      if (Array.isArray(response)) {
        response.forEach((r) => {
          totalSalesCount += Number(r.salesQty || 0);
          totalSupply += Number(r.salesAmount || 0);
          totalTax += Number(r.tax || 0);
          totalAmount += Number(r.totalAmount || 0);
        });
      }

      const tCount = document.getElementById("totalSalesCount");
      const tSupply = document.getElementById("totalSupply");
      const tTax = document.getElementById("totalTax");
      const tAmount = document.getElementById("totalAmount");

      if (tCount) tCount.textContent = totalSalesCount.toLocaleString();
      if (tSupply) tSupply.textContent = totalSupply.toLocaleString();
      if (tTax) tTax.textContent = totalTax.toLocaleString();
      if (tAmount) tAmount.textContent = totalAmount.toLocaleString();

      return response; // 반드시 반환
    },

    placeholder: "데이터가 없습니다.",
    columns: [
      { title: "사원코드", field: "empCode", hozAlign: "center", width: 120 },
      { title: "사원명", field: "name", hozAlign: "center", width: 150 },
      { title: "수량", field: "salesQty", hozAlign: "right", width: 120 },
      { title: "공급가액", field: "salesAmount", hozAlign: "right", width: 150 },
      { title: "부가세", field: "tax", hozAlign: "right", width: 120 },
      { title: "합계", field: "totalAmount", hozAlign: "right", width: 150 },
    ],
  });

  // ✅ 행 클릭 → 모달
  table.on("rowClick", function (e, row) {
    const emp = row.getData();
    console.log("🧭 선택된 사원:", emp);
    openEmployeeModal(emp);
  });

  // ===============================
  // 🔍 검색/필터 → 재조회
  // ===============================
  const reloadTable = () => {
    console.log("📡 검색 조건:", getFilterParams());
    table.setData(); // ajaxURLGenerator가 최신 URL 생성
  };

  const searchInput = document.getElementById("productName");
  document.getElementById("btn-search")?.addEventListener("click", reloadTable);
  if (searchInput) {
    searchInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") reloadTable();
    });
  }
  if (yearSelect) yearSelect.addEventListener("change", reloadTable);
  if (quarterSelect) quarterSelect.addEventListener("change", reloadTable);
});

// ===============================
// 🪟 모달: 상세 테이블
// ===============================
function openEmployeeModal(emp) {
  const modalEl = document.getElementById("employeeModal");
  const modalTitle = document.getElementById("employeeModalLabel");
  if (!modalEl) return console.error("❌ employeeModal 요소를 찾을 수 없습니다.");

  modalTitle.textContent = `${emp.name} 사원 판매목록`;
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();

  // 기존 테이블 제거(중복 방지)
  const existing = Tabulator.findTable("#employee-sales-table");
  if (existing.length) existing[0].destroy();

  // 👉 백엔드 상세 API가 준비되면 여기 ajaxURL/Params로 바꾸면 됨.
  //    우선 동작 확인용 더미 데이터로 구성
  const modalTable = new Tabulator("#employee-sales-table", {
    layout: "fitDataStretch",
    height: "380px",
    pagination: "local",
    paginationSize: 5,
    placeholder: "판매 데이터가 없습니다.",
    data: [
      { salesDate: "2025-09-12", correspondent: "예스홈", salesAmount: 350000, collectAmt: 150000, remark: "" },
      { salesDate: "2025-03-15", correspondent: "씽크존", salesAmount: 250000, collectAmt: 0, remark: "" },
      { salesDate: "2025-02-05", correspondent: "바이존", salesAmount: 800000, collectAmt: 700000, remark: "" },
      { salesDate: "2025-08-08", correspondent: "참잘", salesAmount: 750000, collectAmt: 500000, remark: "" },
    ],
    columns: [
      { title: "일자", field: "salesDate", hozAlign: "center", width: 120 },
      { title: "거래처명", field: "correspondent", hozAlign: "center", width: 150 },
      { title: "매출금액", field: "salesAmount", hozAlign: "right", formatter: "money", width: 150 },
      { title: "수금금액", field: "collectAmt", hozAlign: "right", formatter: "money", width: 150 },
      { title: "비고", field: "remark", hozAlign: "left", width: 180 },
    ],
  });

  // 모달 내 검색
  const searchAction = () => {
    const keyword = (document.getElementById("modal-searchInput")?.value || "").trim().toLowerCase();
    modalTable.setFilter("correspondent", "like", keyword);
  };
  const btn = document.getElementById("modal-btn-search");
  const icon = document.getElementById("modal-searchIcon");
  if (btn) btn.onclick = searchAction;
  if (icon) icon.onclick = searchAction;
}
