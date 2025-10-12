document.addEventListener("DOMContentLoaded", function () {

  // 📅 최근 5년 드롭다운
  const yearSelect = document.getElementById("yearSelectProduct");
  const quarterSelect = document.getElementById("quarterSelectProduct");

  // ✅ 필터 파라미터 가져오는 함수 (이것만 유지!)
  function getFilterParams() {
    return {
      year: document.getElementById("yearSelectProduct").value || null,
      quarter: document.getElementById("quarterSelectProduct").value || null,
      keyword: document.getElementById("productName").value || null
    };
  }

  // ⚠️ 이미 옵션이 있다면 다시 안 만들기
  if (yearSelect && yearSelect.options.length === 0) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y > currentYear - 5; y--) {
      const option = document.createElement("option");
      option.value = y;
      option.textContent = y;
      yearSelect.appendChild(option);
    }
  }

  // 🧭 보기 탭 전환
  document.getElementById("btnProductView").addEventListener("click", () => location.href = "/businessProfits");
  document.getElementById("btnEmployeeView").addEventListener("click", () => location.href = "/employeeProfits");

  // 📊 Tabulator 테이블 생성
  const salesTable = new Tabulator("#sales-table", {
    layout: "fitDataStretch",
    height: "480px",
    pagination: "local",
    paginationSize: 10,
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/sales/profit-list",
    ajaxConfig: "GET",
    ajaxParams: getFilterParams(), // 초기 로딩 시 조건
    columns: [
      { title: "품목코드", field: "productCode", hozAlign: "center", width: 120 },
      { title: "품목명", field: "productName", hozAlign: "center", width: 150 },
      {
        title: "판매", columns: [
          { title: "수량", field: "qty", hozAlign: "right" },
          { title: "단가", field: "salePrice", hozAlign: "right", formatter: "money" },
          { title: "금액", field: "saleAmt", hozAlign: "right", formatter: "money" },
        ]
      },
      {
        title: "원가", columns: [
          { title: "단가", field: "costPrice", hozAlign: "right", formatter: "money" },
          { title: "금액", field: "costAmt", hozAlign: "right", formatter: "money" },
        ]
      },
      {
        title: "판매부대비", columns: [
          { title: "금액", field: "expAmt", hozAlign: "right", formatter: "money" },
        ]
      },
      {
        title: "이익", columns: [
          {
            title: "이익률",
            field: "profitRate",
            hozAlign: "center",
            formatter: function (cell) {
              const value = cell.getValue();
              return value !== null && value !== undefined
                ? value.toString().replace('%', '') + '%'
                : '-';
            },
            width: 100
          }
        ]
      }
    ],
    ajaxResponse: function (url, params, response) {
      updateSummary(response);
      return response;
    }
  });

  // ✅ 검색 버튼 클릭 시
  document.getElementById("btn-search").addEventListener("click", function () {
    reloadTableData();
  });
  
  
  function reloadTableData() {
  const params = getFilterParams();
  console.log("📌 yearSelectProduct 엘리먼트:", document.getElementById("yearSelectProduct"));
  console.log("📌 quarterSelectProduct 엘리먼트:", document.getElementById("quarterSelectProduct"));
  console.log("📌 searchInput 엘리먼트:", document.getElementById("searchInput"));
  console.log("✅ 데이터 새로고침 완료", params);

  salesTable.replaceData("/api/sales/profit-list", params, "GET")
    .catch(err => console.error("❌ 데이터 로드 실패:", err));
}


  // ✅ 돋보기 아이콘 클릭
  const searchIcon = document.getElementById("searchIcon");
  if (searchIcon) {
    searchIcon.addEventListener("click", reloadTableData);
  }

  // ✅ 엔터로 검색
  const searchInput = document.getElementById("searchInput");
  if (searchInput) {
    searchInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        reloadTableData();
      }
    });
  }

  // ✅ 테이블 데이터 새로고침 함수
  function reloadTableData() {
    const params = getFilterParams();
    salesTable.replaceData("/api/sales/profit-list", params, "GET")
      .then(() => console.log("✅ 데이터 새로고침 완료", params))
      .catch(err => console.error("❌ 데이터 로드 실패:", err));
  }

  // ✅ 요약 박스 갱신
  function updateSummary(data) {
    let totalSalesCount = 0, totalSupply = 0, totalTax = 0, totalAmount = 0;
    data.forEach(item => {
      totalSalesCount += item.qty || 0;
      totalSupply += item.saleAmt || 0;
    });
    totalTax = totalSupply * 0.1;
    totalAmount = totalSupply + totalTax;

    document.getElementById("totalSalesCount").textContent = totalSalesCount.toLocaleString();
    document.getElementById("totalSupply").textContent = totalSupply.toLocaleString();
    document.getElementById("totalTax").textContent = totalTax.toLocaleString();
    document.getElementById("totalAmount").textContent = totalAmount.toLocaleString();
  }
  
 // ==============================
// 📦 공통 모달을 이용한 품목 검색
// ==============================
const productSearchIcon = document.querySelector(".fa-search");
if (productSearchIcon) {
  productSearchIcon.addEventListener("click", function () {
    // ✅ 공통 모달 호출 (sales 설정 사용)
    openModal('sales', function(selected) {
      document.getElementById("salesCode").value = selected.salesCode || "";
      document.getElementById("productName").value = selected.productName || "";
    });
  });
} else {
  console.warn("[WARN] 돋보기 아이콘(.fa-search)을 찾지 못했습니다.");
}

  
 });


