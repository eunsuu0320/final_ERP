
document.addEventListener("DOMContentLoaded", function() {
// ================================
// 📌 사원목록 테이블
// ================================
table = new Tabulator("#emp-table", {
		height: "600px",
		layout: "fitColumns",
		placeholder: "데이터가 없습니다.",
		ajaxURL: "/api/emp/",
		ajaxResponse: function(url, params, response) {
		},
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, cellClick: function(e, cell) { cell.getRow().toggleSelect(); } },
			{ title: "사원명", field: "", hozAlign: "center", sorter: "number" },
			{ title: "기존 거래처 수", field: "", hozAlign: "center", sorter: "number", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "작년 총 매출액", field: "", hozAlign: "center", sorter: "number", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "작년 총 매입단가", field: "", hozAlign: "center", sorter: "number" },
			{ title: "작년 총 영업이익", field: "", hozAlign: "center", formatter: function(cell) { return cell.getValue() ? cell.getValue() : "0%"; } }
		]
	});

})