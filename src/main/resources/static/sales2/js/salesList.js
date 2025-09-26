

document.addEventListener("DOMContentLoaded", () => {
    // Tabulator 테이블 생성
    table = new Tabulator("#sales-table", {
        height: "600px", // 테이블 높이
        layout: "fitColumns", // 컬럼 너비를 테이블 너비에 맞게 조정
        placeholder: "데이터가 없습니다.", // 데이터가 없을 때 표시할 메시지
        
        // 데이터 가져오기 설정
        ajaxURL: "/api/sales/stats", // 데이터를 가져올 API URL
        ajaxResponse: function(url, params, response){
            let processedData = [];
            let prevCount = null;

            // 데이터를 순회하며 재거래율을 계산
            response.forEach(item => {
                const currentCount = item.correspondentCount;
                let retradeRate = 0;

                // ⚠️ 수정된 부분
                if (prevCount !== null && prevCount !== 0) {
                    // 재거래율 계산 (올해 거래처 수 - 작년 거래처 수) / 작년 거래처 수 * 100
                    retradeRate = ((currentCount - prevCount) / prevCount) * 100;
                } else if (prevCount === 0) {
                    retradeRate = 0; // 작년 거래처 수가 0일 경우 재거래율 0%
                }

                // 계산된 재거래율을 소수점 2자리까지 반올림
                item.retradeRate = retradeRate.toFixed(2) + "%"; 
                processedData.push(item);
                prevCount = currentCount; // 다음 순회를 위해 현재 값을 저장 
            });

            // 가공된 데이터를 Tabulator에 반환
            return processedData; 
        },

        // 컬럼 정의
        columns: [
            // 체크박스 컬럼 추가
            {formatter:"rowSelection", titleFormatter:"rowSelection", hozAlign:"center", headerSort:false, cellClick:function(e, cell){
                cell.getRow().toggleSelect();
            }},
            // 오라클 DB에서 반환되는 대문자 키에 맞게 field를 수정
            {title:"년도", field:"SALESYEAR", hozAlign:"center", sorter:"number"},
            {title:"총 매출액", field:"TOTALSALESAMOUNT", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"총 영업이익", field:"TOTALPROFITAMOUNT", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"신규 거래처수", field:"CORRESPONDENTCOUNT", hozAlign:"center", sorter:"number"},
            // 재거래율 컬럼은 이제 계산된 값을 사용
            {title:"재거래율", field:"RETRADERATE", hozAlign:"center"}
        ],
    });
});


// -------------------------
// 신규 버튼
// -------------------------
document.getElementById("btn-new").addEventListener("click", async function() {
    try {
        // 서버에 올해 영업계획이 있는지 확인
        const response = await fetch("/api/sales/check-this-year"); // 🔹 서버에서 boolean 반환
        const data = await response.json();

        if (data.exists) { 
            // 올해 계획이 이미 등록되어 있으면 alert 후 종료
            alert("올해 영업계획이 이미 등록되어 있습니다."); 
            return; // 모달 열기 중단
        }

        // 등록 안 되어 있으면 모달 열기
        const modal = new bootstrap.Modal(document.getElementById("insertSalesModal"));
        modal.show();

    } catch (error) {
        console.error("신규 버튼 처리 중 오류 발생:", error);
        alert("오류 발생: " + error.message);
    }
});


// -------------------------
// 공통: 금액 Formatter
// -------------------------
function moneyFormatter(cell) {
  let value = cell.getValue();
  if (value === null || value === ""  || value == undefined) return "";
  return "₩" + value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// -------------------------
// 공통: % Formatter
// -------------------------
function percentFormatter(cell) {
  let value = cell.getValue();
  if (value === null || value === "" || value == undefined) return "";
  return value.toString().replace("%", "") + "%";
}


// -------------------------
// 작년 영업계획 (조회용)
// -------------------------
var lastYearTable = new Tabulator("#lastYearTable", {
  layout: "fitColumns",
  height: "350px",
  
  // 데이터 로드 설정
  ajaxURL: "/api/sales/last-year-qty", // DB에서 분기별 데이터를 가져올 새로운 API 엔드포인트
  ajaxParams: { year: 2024 }, // 조회할 연도 파라미터 전달
  
  // 컬럼 정의
  columns: [
    {title: "분기", field: "SALES_QUARTER", hozAlign: "center"},
    {title: "작년 총 매출액", field: "TOTAL_SALES_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }},
    {title: "작년 총 매입단가", field: "TOTAL_COST_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }},
    {title: "작년 총 영업이익", field: "TOTAL_PROFIT_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }},
  ],
});

// -------------------------
// 올해 영업계획 (등록용)
// -------------------------
var thisYearTable = new Tabulator("#thisYearTable", {
  layout: "fitColumns",
  height: "350px",
  columns: [
    {title: "분기", field: "qtr", hozAlign: "center", editor: false},
    {title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter},
    {title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter},
    {title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number"},
  ],
  data: [
    {qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
    {qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
    {qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
    {qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
  ],
});

  // 저장 버튼 이벤트
document.getElementById("btn-save-sales").addEventListener("click", function () {
    // 테이블 데이터 가져오기
    const tableData = thisYearTable.getData();

    // VO(SalesPlan)에 맞는 필드만 추출
    const payload = tableData.map(row => ({
        qtr: row.qtr,                       // 분기
        purpSales: row.purpSales || 0,       // 올해 총 매출액
        purpProfitAmt: row.purpProfitAmt || 0, // 올해 총 영업이익
        newVendCnt: row.newVendCnt || 0,     // 신규 거래처수
        planYear: new Date().getFullYear(),  // 계획 연도
        regDate: new Date(),                 // 등록일
        empCode: "EMP001",                   // 사원코드 예시
        companyCode: "COMP001"               // 회사코드 예시
    }));

    // CSRF 토큰 읽기
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    fetch('/api/sales/insert', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(payload), // 🔹 VO 필드만 JSON으로
    })
    .then(response => {
        if (response.ok) {
            alert("영업계획이 성공적으로 저장되었습니다.");
            loadSalesPlanList(); 
        } else {
            return response.text().then(text => { throw new Error(text); });
        }
    })
    .catch(error => {
        console.error("저장 중 오류 발생:", error);
        alert("저장 실패: " + error.message);
    });
});

// 목록 불러오기 함수
function loadSalesPlanList() {
    fetch('/api/sales/list')
        .then(response => response.json())
        .then(data => {
            // '등록용' 테이블 대신 '목록용' 테이블에 데이터를 로드한다고 가정합니다.
            // 아래 코드는 예시이며, 실제로는 목록을 보여줄 별도의 Tabulator 테이블이 필요합니다.
            // thisYearTable.setData(data); // '등록용' 테이블에 목록을 보여주는 예시
            console.log("영업계획 목록을 불러왔습니다:", data);
        })
        .catch(error => {
            console.error("목록 불러오기 실패:", error);
        });
        
// 초기화 버튼 이벤트
document.getElementById("btn-reset-sales").addEventListener("click", function () {
    // 기존 데이터 모두 삭제
    thisYearTable.clearData();

    // 초기 상태 데이터 다시 추가
    thisYearTable.addData([
        {qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
        {qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
        {qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
        {qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: ""},
    ]);
});



// 수정
let editThisYearTable;

// 영업계획 목록 테이블 행 클릭 시 수정 모달 열기
table.on("rowClick", function(e, row) {
    const rowData = row.getData();
    console.log("선택된 행:", rowData); // 디버깅용
    openEditModal(rowData); // 수정 모달 열기 함수 호출
});

// -------------------------
// 수정용 테이블 초기화
// -------------------------
document.addEventListener("DOMContentLoaded", () => {
    editThisYearTable = new Tabulator("#editThisYearTable", {
        layout: "fitColumns",
        height: "350px",
        columns: [
            {title: "분기", field: "qtr", hozAlign: "center", editor: false},
            {title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter},
            {title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter},
            {title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number"},
        ],
    });
});

// -------------------------
// 행 클릭 시 수정 모달 열기
// -------------------------
function openEditModal(rowData) {
    const year = 2025;
    const quarters = ["1분기", "2분기", "3분기", "4분기"];

    // 선택된 행의 분기별 데이터 매핑
    const tableData = quarters.map(q => ({
        qtr: q,
        purpSales: rowData[q]?.purpSales || 0,
        purpProfitAmt: rowData[q]?.purpProfitAmt || 0,
        newVendCnt: rowData[q]?.newVendCnt || 0,
    }));

    editThisYearTable.setData(tableData);

    const modal = new bootstrap.Modal(document.getElementById("editSalesModal"));
    modal.show();

    // 저장 버튼 이벤트
    document.getElementById("btn-save-edit-sales").onclick = async function () {
        try {
            const payload = editThisYearTable.getData().map(row => ({
                qtr: row.qtr,
                purpSales: row.purpSales || 0,
                purpProfitAmt: row.purpProfitAmt || 0,
                newVendCnt: row.newVendCnt || 0,
                planYear: year,
                salesPlanCode: rowData.salesPlanCode, // 기존 영업계획 코드
            }));

            const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

            const response = await fetch('/api/sales/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                alert("영업계획이 성공적으로 수정되었습니다.");
                location.reload(); // 목록 갱신
            } else {
                const text = await response.text();
                throw new Error(text);
            }
        } catch (error) {
            console.error("수정 중 오류 발생:", error);
            alert("수정 실패: " + error.message);
        }
    };
}

// -------------------------
// 금액 Formatter
// -------------------------
function moneyFormatter(cell) {
  let value = cell.getValue();
  if (value === null || value === ""  || value == undefined) return "";
  return "₩" + value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
}