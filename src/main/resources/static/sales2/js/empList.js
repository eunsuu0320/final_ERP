
document.addEventListener("DOMContentLoaded", function() {
// ================================
// 왼쪽 사원 목록 테이블
// ================================
 const tableDiv = document.getElementById("empPlanList-table");
if(tableDiv){
        let table = new Tabulator("#empPlanList-table", {
            height: "600px",
            layout: "fitColumns",
            placeholder: "데이터가 없습니다.",
            ajaxURL: "/api/sales/empPlanList",
            ajaxResponse: function(url, params, response) {
                return response; // planData 제거
            },
            columns: [
                {title: "사원명", field: "EMPNAME", width: 150},
                {title: "기존 거래처수", field: "CUSTOMERCOUNT", hozAlign: "right"},
                {title: "작년 총 매출액", field: "LASTYEARSALES", hozAlign: "right", formatter:"money", formatterParams:{thousand:",",precision:0}},
                {title: "작년 총 매입단가", field: "LASTYEARCOST", hozAlign: "right", formatter:"money", formatterParams:{thousand:",",precision:0}},
                {title: "작년 총 영업이익", field: "LASTYEARPROFIT", hozAlign: "right", formatter:"money", formatterParams:{thousand:",",precision:0}}
            ]
        });
    }
});
    
// ================================
// 오른쪽 영업계획 입력 테이블
// ================================
const planTable = new Tabulator("#plan-table", {
  layout: "fitColumns",
  reactiveData: true,
  columns: [
    { title: "분기", field: "분기", hozAlign: "center", editor: false }, // 수정 불가
    { title: "올해 총 매출액", field: "매출액", editor: "input", formatter: "money" },
    { title: "올해 총 영업이익", field: "영업이익", editor: "input", formatter: "money" },
    { title: "신규 거래처수", field: "신규거래처수", editor: "number" },
    {
      title: "재거래율",
      field: "재거래율",
      editor: "select",
      editorParams: { values: ["50%", "80%", "100%", "직접입력"] },
    },
  ],
  data: [
    { 분기: "1분기" },
    { 분기: "2분기" },
    { 분기: "3분기" },
    { 분기: "4분기" },
  ],
});

// ================================
// 저장 버튼 이벤트
// ================================
document.getElementById("btn-update-sales").addEventListener("click", () => {
  const data = planTable.getData(); // 테이블의 모든 행 데이터 가져오기
  console.log("저장할 데이터:", data);

  fetch("/api/sales/insertEmpPlan", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      [document.querySelector("meta[name='_csrf_header']").content]:
        document.querySelector("meta[name='_csrf']").content
    },
    body: JSON.stringify(data[0]) // 지금은 첫 번째 행만 저장 예시
  })
    .then((res) => res.json())
    .then((result) => {
      console.log("등록 성공:", result);
      alert("데이터가 저장되었습니다!");
    })
    .catch((err) => {
      console.error("등록 실패:", err);
      alert("등록 중 오류 발생");
    });
});

// 돋보기
document.getElementById("search-icon").addEventListener("click", function() {
    var myModal = new bootstrap.Modal(document.getElementById('employeeModal'));
    myModal.show();
});

// ================================
// 수정모달
// ================================


// ================================
// 📌 행 클릭 시 수정 모달 열기
// ================================



// ================================
// 📌 초기화 버튼
// ================================

// ================================
// 📌 수정 저장 버튼
// ================================

// ================================
// 📌 수정 취소 버튼
// ================================
document.getElementById("btn-cancel-update").addEventListener("click", function() {
	bootstrap.Modal.getInstance(document.getElementById("modifySalesModal")).hide();
});

// ================================
// 📌 금액 Formatter
// ================================
function moneyFormatter(cell) {
	let value = cell.getValue();
	return value ? value.toLocaleString() : "0";
}

