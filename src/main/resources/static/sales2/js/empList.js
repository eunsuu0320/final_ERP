
document.addEventListener("DOMContentLoaded", function() {
// ================================
// 더미 데이터
// ================================
const employeeData = [
  { id: 1, name: "김영수", 거래처수: 25, 작년매출: 20000000, 작년매입: 7000000, 작년영업이익: 13000000 },
  { id: 2, name: "홍길동", 거래처수: 23, 작년매출: 15000000, 작년매입: 8000000, 작년영업이익: 7500000 },
  { id: 3, name: "이철수", 거래처수: 20, 작년매출: 18000000, 작년매입: 6000000, 작년영업이익: 12000000 }
];

// 분기별 초기 데이터
let planData = [
  { 분기: "1분기", 매출액: "", 영업이익: "", 신규거래처수: "", 재거래율: "" },
  { 분기: "2분기", 매출액: "", 영업이익: "", 신규거래처수: "", 재거래율: "" },
  { 분기: "3분기", 매출액: "", 영업이익: "", 신규거래처수: "", 재거래율: "" },
  { 분기: "4분기", 매출액: "", 영업이익: "", 신규거래처수: "", 재거래율: "" },
];

// ================================
// 왼쪽 사원 목록 테이블
// ================================
const employeeTable = new Tabulator("#employee-table", {
  data: employeeData,
  layout: "fitColumns",
  columns: [
    { title: "사원명", field: "name" },
    { title: "거래처수", field: "거래처수" },
    { title: "작년매출", field: "작년매출", formatter: "money" },
    { title: "작년매입", field: "작년매입", formatter: "money" },
    { title: "작년영업이익", field: "작년영업이익", formatter: "money" },
  ],
  rowClick: function (e, row) {
    const employee = row.getData();
    document.getElementById("plan-title").innerText = `${employee.name} 영업계획`;
    planTable.setData(planData); // 초기화
  },
});

// ================================
// 오른쪽 영업계획 입력 테이블
// ================================
const planTable = new Tabulator("#plan-table", {
  data: planData,
  layout: "fitColumns",
  reactiveData: true,
  columns: [
    { title: "분기", field: "분기", hozAlign: "center" },
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
});




// ================================
// 저장 버튼 이벤트
// ================================
document.getElementById("save-btn").addEventListener("click", () => {
  const data = planTable.getData();
  console.log("저장된 데이터:", data);
  alert("데이터가 저장되었습니다! (콘솔 확인)");
});

// 돋보기
document.getElementById("search-icon").addEventListener("click", function() {
    var myModal = new bootstrap.Modal(document.getElementById('employeeModal'));
    myModal.show();
});

// ================================
// 수정모달
// ================================
var empListTable = new Tabulator("#editYearTable", {
    layout: "fitColumns",
    height: "350px",
    columns: [
        { title: "분기", field: "qtr", hozAlign: "center" },
        { title: "올해 총 매출액", field: "", hozAlign: "right", editor: "number", formatter: moneyFormatter },
        { title: "올해 총 영업이익", field: "", hozAlign: "right", editor: "number", formatter: moneyFormatter },
        { title: "신규 거래처수", field: "", hozAlign: "center", editor: "number" },
        { title: "재거래율", field: "", hozAlign: "center", editor: "number" }
    ]
});

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


})