// /sales2/js/employeeProfits.js
document.addEventListener("DOMContentLoaded", function () {

  // (옵션) Top5 차트 호출이 남아있을 때 ReferenceError 방지용 no-op
  function renderTopEmpChart() {}

  let totalAmountMax = 0; // 행별 '합계' 막대 width 계산용 max

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
  // 🧩 필터 파라미터 (❗사원명은 id="name"에서 읽음)
  // ===============================
  function getFilterParams() {
    let year = yearSelect?.value ?? "";
    let quarter = quarterSelect?.value ?? "";
    let keyword = document.getElementById("name")?.value ?? "";

    year = (year || "").toString().trim();
    quarter = (quarter || "").toString().trim();
    keyword = (keyword || "").toString().trim();

    return {
      companyCode: "C001",
      ...(year && { year }),
      ...(quarter && { quarter }),
      ...(keyword && { keyword }),
    };
  }

  // ===============================
  // 📊 Tabulator 테이블
  // ===============================
  const table = new Tabulator("#sales-table", {
    layout: "fitDataStretch",
    height: "480px",
    pagination: "local",
    paginationSize: 10,
    ajaxURL: "/api/employeeProfits/list",
    ajaxConfig: "GET",
    // 초기 로딩시에도 현재 조건 반영
    ajaxParams: getFilterParams(),
    ajaxURLGenerator: function (url, config, params) {
      const filters = getFilterParams();
      const qs = new URLSearchParams();
      Object.entries(filters).forEach(([k, v]) => qs.append(k, v));
      qs.append("_", Date.now()); // 캐시 방지
      const full = `${url}?${qs.toString()}`;
      console.log("[employeeProfits] ajaxURL:", full);
      return full;
    },
    ajaxResponse: function (url, params, response) {
      // (옵션) Top5 차트 호출이 남아있다면 안전
      renderTopEmpChart(response);

      // ✅ 요약 합계 갱신
      let totalSalesCount = 0;
      let totalSupply = 0;
      let totalTax = 0;
      let totalAmount = 0;

      if (Array.isArray(response)) {
        response.forEach((r) => {
          totalSalesCount += Number(r.salesQty || 0);
          totalSupply     += Number(r.salesAmount || 0);
          totalTax        += Number(r.tax || 0);
          totalAmount     += Number(r.totalAmount || 0);
        });
      }

      // ✅ '합계' 막대그래프용 max 갱신
      totalAmountMax = Array.isArray(response)
        ? response.reduce((m, r) => Math.max(m, Number(r.totalAmount || 0)), 0)
        : 0;

      const tCount  = document.getElementById("totalSalesCount");
      const tSupply = document.getElementById("totalSupply");
      const tTax    = document.getElementById("totalTax");
      const tAmount = document.getElementById("totalAmount");

      if (tCount)  tCount.textContent  = totalSalesCount.toLocaleString();
      if (tSupply) tSupply.textContent = totalSupply.toLocaleString();
      if (tTax)    tTax.textContent    = totalTax.toLocaleString();
      if (tAmount) tAmount.textContent = totalAmount.toLocaleString();

      return response;
    },
    placeholder: "데이터가 없습니다.",
    columns: [
      { title: "사원코드", field: "empCode", hozAlign: "center", width: 200 },
      { title: "사원명",   field: "name",    hozAlign: "center", width: 200 },
      { title: "수량",     field: "salesQty",     hozAlign: "center", width: 200 },
      { title: "공급가액", field: "salesAmount",  hozAlign: "right", formatter: "money",  width: 200 },
      { title: "부가세",   field: "tax",          hozAlign: "right", formatter: "money", width: 200 },
      {
        title: "합계",
        field: "totalAmount",
        width: 260,
        hozAlign: "left", // 그래프 + 숫자 함께 표시
        // 각 행 셀을 막대그래프로 렌더링
        formatter: function (cell) {
          const value = Number(cell.getValue() || 0);
          const max = totalAmountMax || value || 1; // 0 분모 방지
          const pct = Math.max(0, Math.min(100, Math.round((value / max) * 100)));
          const label = value.toLocaleString();

          const html = `
            <div style="display:flex; align-items:center; gap:8px; width:100%;">
              <div style="flex:1; height:10px; background:#eee; border-radius:6px; overflow:hidden;">
                <div style="height:100%; width:${pct}%; background:#86b7fe;"></div>
              </div>
              <div style="min-width:90px; text-align:right;">${label}</div>
            </div>
          `;
          const wrap = document.createElement("div");
          wrap.innerHTML = html;
          return wrap;
        },

        // 하단 합계 셀(bottomCalc)도 막대 + 숫자 표시
        bottomCalc: "sum",
        bottomCalcFormatter: function (cell) {
          const sum = Number(cell.getValue() || 0);
          const label = sum.toLocaleString();

          const html = `
            <div style="display:flex; align-items:center; gap:8px; width:100%;">
              <div style="flex:1; height:12px; background:#eee; border-radius:6px; overflow:hidden;">
                <div style="height:100%; width:100%; background:#86b7fe;"></div>
              </div>
              <div style="min-width:100px; text-align:right; font-weight:600;">${label}</div>
            </div>
          `;
          const wrap = document.createElement("div");
          wrap.innerHTML = html;
          return wrap;
        },
      }, // 🔚 합계 컬럼
    ],
  });

  // ✅ 행 클릭 → 상세 모달 (기존 로직 유지)
  table.on("rowClick", function (e, row) {
    openEmployeeModal(row.getData());
  });

  // ===============================
  // 🔍 검색 버튼으로만 조회
  // ===============================
  document.getElementById("btn-search")?.addEventListener("click", function () {
    console.log("🔎 검색 조건:", getFilterParams());
    table.setData(); // ajaxURLGenerator가 getFilterParams()로 최신 URL 생성
  });
});

// ===============================
// 모달: 상세 테이블 (기존 함수 그대로 사용)
// ===============================
function openEmployeeModal(emp) {
  const modalEl = document.getElementById("employeeModal");
  const modalTitle = document.getElementById("employeeModalLabel");
  if (!modalEl) return console.error("❌ employeeModal 요소를 찾을 수 없습니다.");

  modalTitle.textContent = `${emp.name} 사원 판매목록`;
  const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
  modal.show();

  const existing = Tabulator.findTable("#employee-sales-table");
  if (existing.length) existing[0].destroy();

  const buildParams = (extra = {}) => {
    const yearSel = document.getElementById("yearSelect")?.value ?? "";
    const quarterSel = document.getElementById("quarterSelect")?.value ?? "";
    const params = {
      companyCode: "C001",
      empCode: emp.empCode,
      ...extra,
    };
    if (yearSel)    params.year    = parseInt(yearSel, 10);
    if (quarterSel) params.quarter = parseInt(quarterSel, 10);
    if (extra.keyword) params.keyword = String(extra.keyword).trim();
    return params;
  };

  const modalTable = new Tabulator("#employee-sales-table", {
    layout: "fitDataStretch",
    height: "380px",
    pagination: "local",
    paginationSize: 8,
    placeholder: "판매 데이터가 없습니다.",
    ajaxURL: "/api/employeeProfits/partners",
    ajaxConfig: "GET",
    ajaxParams: buildParams(),
    ajaxResponse: function (url, params, response) {
      console.log("📡 partners response:", response);
      return response;
    },
    ajaxError: function (error) {
      console.error("❌ partners ajax error:", error);
      alert("서버 에러가 발생했습니다. 콘솔 로그를 확인하세요.");
    },
    columns: [
      { title: "거래처명", field: "partnerName", hozAlign: "left",  minWidth: 200 },
      { title: "매출금액", field: "salesAmount", hozAlign: "right", formatter: "money",
        width: 140, bottomCalc: "sum", bottomCalcFormatter: "money" },
      { title: "수금금액", field: "collectAmt",  hozAlign: "right", formatter: "money",
        width: 140, bottomCalc: "sum", bottomCalcFormatter: "money" },
    ],
  });

  // 모달 안의 검색은 그대로 사용
  const searchAction = () => {
    const keyword = (document.getElementById("modal-searchInput")?.value || "").trim();
    modalTable.setData("/api/employeeProfits/partners", buildParams({ keyword }));
  };
  document.getElementById("modal-btn-search")?.addEventListener("click", searchAction);
  document.getElementById("modal-searchIcon")?.addEventListener("click", searchAction);
  document.getElementById("modal-searchInput")?.addEventListener("keypress", (e) => {
    if (e.key === "Enter") searchAction();
  });
}
