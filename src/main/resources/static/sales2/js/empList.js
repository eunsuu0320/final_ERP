let empTable;
let planTable;

document.addEventListener("DOMContentLoaded", function () {
// ================================
// 📌 왼쪽 사원 테이블
// ================================
empTable = new Tabulator("#empPlanList-table", {
    height: "600px",
    layout: "fitColumns",
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/sales/empPlanList",
    columns: [
        { title: "사원명", field: "EMPNAME", width: 150 },
        { title: "기존 거래처수", field: "CUSTOMERCOUNT", hozAlign: "right" },
        { title: "작년 매출액", field: "LASTYEARSALES", hozAlign: "right", formatter: "money" },
        { title: "작년 매입단가", field: "LASTYEARCOST", hozAlign: "right", formatter: "money" },
        { title: "작년 영업이익", field: "LASTYEARPROFIT", hozAlign: "right", formatter: "money" }
    ]
});

// ================================
// 📌 오른쪽 영업계획 등록 테이블
// ================================
planTable = new Tabulator("#plan-table", {
    layout: "fitColumns",
    reactiveData: true,
    columns: [
        { title: "분기", field: "QTR", hozAlign: "center", editor: false },
        { title: "올해 총 매출액", field: "PURPSALES", editor: "number", formatter: "money", formatterParams: { thousand: ",", precision: 0, symbol: "₩" } },
        { title: "올해 총 영업이익", field: "PURPPROFITAMT", editor: "number", formatter: "money", formatterParams: { thousand: ",", precision: 0, symbol: "₩" } },
        { title: "신규 거래처수", field: "NEWVENDCNT", editor: "number" },
        { title: "재거래율", field: "재거래율", editor: "number" }
    ],
    data: [
        { 분기: "1분기" },
        { 분기: "2분기" },
        { 분기: "3분기" },
        { 분기: "4분기" },
    ],
});
    
// ================================
// 📌 저장 버튼 → 서버 전송
// ================================
document.getElementById("btn-update-sales").addEventListener("click", () => {
    const empCode = document.getElementById("employCode").value;
    if (!empCode) {
        alert("사원을 먼저 선택해주세요!");
        return;
    }

    const data = planTable.getData();

    // payload를 객체 구조로 감싸기
    const payload = {
        empCode: empCode,
        espCode: document.getElementById("espCode").value,
        detailPlans: data.map(row => ({
            qtr: row.QTR,
            purpSales: row.PURPSALES || 0,
            purpProfitAmt: row.PURPPROFITAMT || 0,
            newVendCnt: row.NEWVENDCNT || 0,
            vendCnt: row.VENDCNT || 0
        }))
    };

    console.log("서버로 전송할 데이터:", payload);

    fetch("/api/sales/insertPlanWithDetails", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [document.querySelector("meta[name='_csrf_header']").content]:
                document.querySelector("meta[name='_csrf']").content
        },
        body: JSON.stringify(payload) // 이제 배열이 아니라 객체
    })
        .then(res => res.text()) 
        .then(result => {
            console.log("등록 성공:", result);
            alert("분기별 영업계획이 저장되었습니다!");
        })
        .catch(err => {
            console.error("등록 실패:", err);
            alert("등록 중 오류 발생");
        });
});



// ================================
// 📌 사원 클릭 시 강조 + 계획 테이블 채우기
// ================================
let selectedRow = null;
empTable.on("rowClick", function (e, row) {
    const data = row.getData();
    console.log("선택된 사원:", data);

    // 이전 강조 해제
    if (selectedRow) {
        selectedRow.getElement().style.fontWeight = "normal";
        selectedRow.getElement().style.backgroundColor = "";
    }
    // 현재 강조
    row.getElement().style.fontWeight = "bold";
    row.getElement().style.backgroundColor = "#f0f0f0";
    selectedRow = row;

    // hidden input 값 세팅
    document.getElementById("employCode").value = data.EMP_CODE;
    document.getElementById("employeeName").value = data.EMPNAME;

	document.getElementById("espCode").value = data.ESP_CODE;
	
    // 오른쪽 제목 업데이트
    document.getElementById("plan-title").innerText = data.EMPNAME + "님의 영업계획";

    // 📌 작년 매출/영업이익 → 5% 증가 후 분기별 균등 분배
    const lastYearSales = data.LASTYEARSALES || 0;
    const lastYearProfit = data.LASTYEARPROFIT || 0;

    const increasedSales = Math.round(lastYearSales * 1.05);
    const increasedProfit = Math.round(lastYearProfit * 1.05);

    const quarterSales = Math.floor(increasedSales / 4);
    const quarterProfit = Math.floor(increasedProfit / 4);

    const newData = [
        { QTR: "1분기", PURPSALES: quarterSales, PURPPROFITAMT: quarterProfit},
        { QTR: "2분기", PURPSALES: quarterSales, PURPPROFITAMT: quarterProfit},
        { QTR: "3분기", PURPSALES: quarterSales, PURPPROFITAMT: quarterProfit},
        { QTR: "4분기", PURPSALES: quarterSales, PURPPROFITAMT: quarterProfit},
    ];

    planTable.replaceData(newData);
});

    // ================================
    // 📌 검색 버튼
    // ================================
    document.getElementById("btn-search").addEventListener("click", function () {
        const keyword = document.getElementById("employeeName").value.trim();
        if (empTable) {
            if (keyword) {
                empTable.setFilter("EMPNAME", "like", keyword);
            } else {
                empTable.clearFilter();
            }
        }
    });

    // ================================
    // 📌 Enter 키 검색
    // ================================
    document.getElementById("employeeName").addEventListener("keyup", function (e) {
        if (e.key === "Enter") {
            document.getElementById("btn-search").click();
        }
    });

 
    // ================================
    // 📌 초기화 버튼
    // ================================
    document.getElementById("btn-cancel-update").addEventListener("click", () => {
        planTable.replaceData([
            { 분기: "1분기" },
            { 분기: "2분기" },
            { 분기: "3분기" },
            { 분기: "4분기" },
        ]);
        alert("계획 테이블이 초기화되었습니다!");
    });
});

// ================================
// 📌 금액 Formatter (₩ 추가)
// ================================
function moneyFormatter(cell) {
    let value = cell.getValue();
    return value ? "₩" + Number(value).toLocaleString() : "₩0";
}
