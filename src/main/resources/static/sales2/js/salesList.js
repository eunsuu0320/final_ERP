document.addEventListener("DOMContentLoaded", () => {
    // Tabulator 테이블 생성
    const table = new Tabulator("#sales-table", {
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

                if (prevCount !== null) {
                    // 재거래율 계산 (올해 거래처 수 - 작년 거래처 수) / 작년 거래처 수 * 100
                    retradeRate = ((currentCount - prevCount) / prevCount) * 100;
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
            {title:"년도", field:"salesYear", hozAlign:"center", sorter:"number"},
            {title:"총 매출액", field:"totalSalesAmount", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"총 영업이익", field:"totalProfitAmount", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"신규 거래처수", field:"correspondentCount", hozAlign:"center", sorter:"number"},
            // 재거래율 컬럼은 이제 계산된 값을 사용
            {title:"재거래율", field:"retradeRate", hozAlign:"center"}
        ],
    });
});

// 신규 버튼
document.getElementById("btn-new").addEventListener("click", function() {
	const modal = new bootstrap.Modal(document.getElementById("insertSalesModal"));
	modal.show();
});

// -------------------------
// 공통: 금액 Formatter
// -------------------------
function moneyFormatter(cell) {
  let value = cell.getValue();
  if (value === null || value === "") return "";
  return "₩" + value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// -------------------------
// 공통: % Formatter
// -------------------------
function percentFormatter(cell) {
  let value = cell.getValue();
  if (value === null || value === "") return "";
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
    {title: "분기", field: "quarter", hozAlign: "center", editor: false},
    {title: "올해 총 매출액", field: "thisSales", hozAlign: "right", editor: "number", formatter: moneyFormatter},
    {title: "올해 총 영업이익", field: "thisProfit", hozAlign: "right", editor: "number", formatter: moneyFormatter},
    {title: "신규 거래처수", field: "newClients", hozAlign: "center", editor: "number"},
    {title: "재거래율", field: "retradeRate", hozAlign: "center", editor: "input", formatter: percentFormatter,
      mutatorEdit: function(value) {
        if (!value) return "";
        return value.toString().replace("%", "") + "%";
      }
    },
  ],
  data: [
    {quarter: "1분기", thisSales: "", thisProfit: "", newClients: "", retradeRate: ""},
    {quarter: "2분기", thisSales: "", thisProfit: "", newClients: "", retradeRate: ""},
    {quarter: "3분기", thisSales: "", thisProfit: "", newClients: "", retradeRate: ""},
    {quarter: "4분기", thisSales: "", thisProfit: "", newClients: "", retradeRate: ""},
  ],
});

  // 저장 버튼 이벤트
  document.getElementById("btn-save-sales").addEventListener("click", function () {
    const data = insertTable.getData();
    console.log("저장할 데이터:", data);
    alert("저장 기능은 추후 API 연동 필요");
  });

  // 초기화 버튼 이벤트
  document.getElementById("btn-reset-sales").addEventListener("click", function () {
    insertTable.clearData();
    insertTable.addData(thisYearData);
  });


