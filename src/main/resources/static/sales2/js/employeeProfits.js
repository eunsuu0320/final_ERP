// /sales2/js/employeeProfits.js
document.addEventListener("DOMContentLoaded", function () {

  // (옵션) Top5 차트 호출이 남아있을 때 ReferenceError 방지용 no-op
  function renderTopEmpChart() {}

  // ✅ 막대 비율 계산 기준: "직원들의 총 공급가액 합"
  let totalSupplyAll = 0;   // (= 모든 사원의 salesAmount 합)

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
      ...(year && { year }),
      ...(quarter && { quarter }),
      ...(keyword && { keyword }),
    };
  }

  // ===============================
  // 📊 Tabulator 테이블 (사원 목록) — 페이지네이션 추가
  // ===============================
  const table = new Tabulator("#sales-table", {
    layout: "fitColumns",
    height: "480px",

    // ✅ 페이지네이션 옵션 (local)
    pagination: "local",
    paginationSize: 10,                       // 기본 10개
    paginationCounter: "rows",                // "1-10 of 128" 형태 카운터 표시
	columnDefaults: { vertAlign: "middle", headerHozAlign: "center" },
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

      // ✅ 요약 합계 갱신 + totalSupplyAll(총 공급가액) 계산
      let totalSalesCount = 0;
      let totalSupply = 0;   // = 공급가액 합
      let totalTax = 0;
      let totalAmount = 0;

      if (Array.isArray(response)) {
        response.forEach((r) => {
          const salesQty    = Number(r.salesQty || 0);
          const salesAmount = Number(r.salesAmount || 0); // 공급가액
          const tax         = Number(r.tax || 0);
          const sumAmount   = Number(r.totalAmount || 0); // 공급가액+부가세

          totalSalesCount += salesQty;
          totalSupply     += salesAmount;
          totalTax        += tax;
          totalAmount     += sumAmount;
        });
      }

      // ▶️ 전체 직원의 공급가액 합(비율 분모)
      totalSupplyAll = totalSupply;

      const tCount  = document.getElementById("totalSalesCount");
      const tSupply = document.getElementById("totalSupply");
      const tTax    = document.getElementById("totalTax");
      const tAmount = document.getElementById("totalAmount");

      if (tCount)  tCount.textContent  = totalSalesCount.toLocaleString();
      if (tSupply) tSupply.textContent = totalSupply.toLocaleString();
      if (tTax)    tTax.textContent    = totalTax.toLocaleString();
      if (tAmount) tAmount.textContent = totalAmount.toLocaleString();

      return response; // ← 반환 후 렌더링(이 시점에 totalSupplyAll 세팅됨)
    },
    placeholder: "데이터가 없습니다.",
    columns: [
      { title: "사원코드",        field: "empCode",     hozAlign: "center", widthGrow:0.3},
      { title: "사원명",          field: "name",        hozAlign: "center", widthGrow:0.3},
      { title: "수량",            field: "salesQty",    hozAlign: "center", widthGrow:0.3 },

      // ✅ 숫자 포맷: 소수점 없이 원 단위
      { title: "공급가액(원)",    field: "salesAmount", hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 }, widthGrow:0.3},

      { title: "부가세(원)",      field: "tax",         hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 }, widthGrow:0.3},

      {
  title: "합계(공급가액 기준)",
  field: "totalAmount",
  hozAlign: "left",
  cssClass: "profit-bar-cell",         // ← 이 줄 추가
  formatter: function (cell) {
    const row = cell.getRow().getData();
    const amount = Number(row.salesAmount || 0);
    const denom  = Number(totalSupplyAll || 0);
    const pct    = denom > 0 ? Math.round((amount / denom) * 100) : 0;

    const root  = document.createElement("div");
    root.style.display = "flex";
    root.style.alignItems = "center";
    root.style.gap = "8px";

    const track = document.createElement("div");
    track.style.flex = "0 0 140px";         // ← 막대 폭 고정(핵심)
    track.style.height = "10px";
    track.style.background = "#eee";
    track.style.borderRadius = "6px";
    track.style.overflow = "hidden";

    const fill = document.createElement("div");
    fill.style.height = "100%";
    fill.style.width = `${Math.min(100, Math.max(0, pct))}%`;
    fill.style.background = "#86b7fe";
    track.appendChild(fill);

    const label = document.createElement("div");
    label.style.minWidth = "110px";
    label.style.textAlign = "right";
    label.textContent = `${amount.toLocaleString()}원 (${pct}%)`;

    root.append(track, label);

    // ← nowrap 깨주기 (이 칼럼만)
    cell.getElement().style.whiteSpace = "normal";

    return root;
  }
},

    ],
  });

  // ✅ 행 클릭 → 상세 모달
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
// 모달: 상세 테이블 (페이지네이션 추가)
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
      empCode: emp.empCode,
      ...extra,
    };
    if (yearSel)    params.year    = parseInt(yearSel, 10);
    if (quarterSel) params.quarter = parseInt(quarterSel, 10);
    if (extra.keyword) params.keyword = String(extra.keyword).trim();
    return params;
  };

  const modalTable = new Tabulator("#employee-sales-table", {
    layout: "fitColumns",
    height: "380px",

    // ✅ 페이지네이션 옵션 (local)
    pagination: "local",
    paginationSize: 8,
    paginationCounter: "rows",

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
      { title: "거래처명",     field: "partnerName", hozAlign: "left", widthGrow:1},
      // ⛔️ 모달 테이블도 하단 합계 제거: bottomCalc 설정 없음
      { title: "매출금액(원)", field: "salesAmount", hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 }, widthGrow:1},
      { title: "수금금액(원)", field: "collectAmt",  hozAlign: "right",
        formatter: "money", formatterParams: { precision: 0 }, widthGrow:1},
    ],
  });

  // 모달 안의 검색
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
