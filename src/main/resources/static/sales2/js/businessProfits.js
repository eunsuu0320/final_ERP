document.addEventListener("DOMContentLoaded", function() {

    // ==============================
    // 📅 최근 5년 드롭다운 자동 생성
    // ==============================
    const yearSelect = document.getElementById("yearSelect");
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y > currentYear - 5; y--) {
        const option = document.createElement("option");
        option.value = y;
        option.textContent = y;
        yearSelect.appendChild(option);
    }

// ==============================
// 🧭 보기 탭 전환
// ==============================
const btnProduct = document.getElementById("btnProductView");
const btnEmployee = document.getElementById("btnEmployeeView");

if (btnProduct) {
  btnProduct.addEventListener("click", () => {
    window.location.href = "/businessProfits";
  });
}

if (btnEmployee) {
  btnEmployee.addEventListener("click", () => {
    window.location.href = "/employeeProfits";
  });
}



    // ==============================
    // 📈 요약 더미데이터
    // ==============================
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

    // ==============================
    // 📊 Tabulator 테이블 (더미데이터)
    // ==============================
    const tableData = [
        { productCode: "P001", productName: "김치", qty: 3, salePrice: 16800, saleAmt: 42000, costPrice: 12000, costAmt: 36000, expPrice: 16800, expAmt: 42000, profitRate: "100%" },
        { productCode: "P002", productName: "식빵", qty: 5, salePrice: 12000, saleAmt: 60000, costPrice: 9000, costAmt: 45000, expPrice: 12000, expAmt: 15000, profitRate: "33%" },
        { productCode: "P003", productName: "앙버터", qty: 4, salePrice: 15000, saleAmt: 60000, costPrice: 11000, costAmt: 44000, expPrice: 15000, expAmt: 16000, profitRate: "36%" },
        { productCode: "P004", productName: "소금빵", qty: 2, salePrice: 18000, saleAmt: 36000, costPrice: 13000, costAmt: 26000, expPrice: 18000, expAmt: 10000, profitRate: "27%" },
        { productCode: "P005", productName: "바게트", qty: 6, salePrice: 10000, saleAmt: 60000, costPrice: 8000, costAmt: 48000, expPrice: 10000, expAmt: 12000, profitRate: "25%" }
    ];

    const salesTable = new Tabulator("#sales-table", {
        layout: "fitDataStretch",
        height: "480px",
        pagination: "local",
        paginationSize: 5,
        data: tableData,
        placeholder: "데이터가 없습니다.",
        columns: [
            { title: "품목코드", field: "productCode", hozAlign: "center", width: 120 },
            { title: "품목명", field: "productName", hozAlign: "center", width: 150 },
            {
                title: "판매", columns: [
                    { title: "수량", field: "qty", hozAlign: "right", width: 80 },
                    { title: "단가", field: "salePrice", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                    { title: "금액", field: "saleAmt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                ]
            },
            {
                title: "원가", columns: [
                    { title: "단가", field: "costPrice", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                    { title: "금액", field: "costAmt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                ]
            },
            {
                title: "판매부대비", columns: [
                    { title: "단가", field: "expPrice", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                    { title: "금액", field: "expAmt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                ]
            },
            {
                title: "이익", columns: [
                    { title: "단가", field: "expPrice", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                    { title: "금액", field: "expAmt", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, width: 100 },
                    { title: "이익률", field: "profitRate", hozAlign: "center", width: 100 },
                ]
            }
        ]
    });

    // ==============================
    // 🔍 검색 기능 (품목명 필터링)
    // ==============================
    document.getElementById("searchBtn").addEventListener("click", function() {
        const keyword = document.getElementById("searchInput").value.toLowerCase();
        salesTable.setFilter("productName", "like", keyword);
    });
});
