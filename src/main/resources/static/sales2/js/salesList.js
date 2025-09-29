
document.addEventListener("DOMContentLoaded", function() {

	// ================================
	// 📌 메인 목록 테이블
	// ================================
	table = new Tabulator("#sales-table", {
		height: "600px",
		layout: "fitColumns",
		placeholder: "데이터가 없습니다.",
		ajaxURL: "/api/sales/stats",
		ajaxResponse: function(url, params, response) {
			let processedData = [];
			let prevCount = null;
			response.forEach(item => {
				const currentCount = item.correspondentCount || 0;
				let retradeRate = 0;
				if (prevCount !== null && prevCount !== 0) {
					retradeRate = ((currentCount - prevCount) / prevCount) * 100;
				}
				item.retradeRate = retradeRate.toFixed(2) + "%";

				// 만약 planYear(Date)가 있는 경우 year 추출 (안전 검사)
				if (item.planYear) {
					try {
						item.SALESYEAR = new Date(item.planYear).getFullYear();
					} catch (e) {
						// ignore
					}
				}
				processedData.push(item);
				prevCount = currentCount;
			});
			return processedData;
		},
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false, cellClick: function(e, cell) { cell.getRow().toggleSelect(); } },
			{ title: "년도", field: "SALESYEAR", hozAlign: "center", sorter: "number" },
			{ title: "총 매출액", field: "TOTALSALESAMOUNT", hozAlign: "center", sorter: "number", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "총 영업이익", field: "TOTALPROFITAMOUNT", hozAlign: "center", sorter: "number", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "신규 거래처수", field: "CORRESPONDENTCOUNT", hozAlign: "center", sorter: "number" },
			{ title: "재거래율", field: "RETRADE_RATE", hozAlign: "center", formatter: function(cell) { return cell.getValue() ? cell.getValue() : "0%"; } }
		]
	});


	// ================================
	// 📌 작년 영업계획 (조회용)
	// ================================
	var lastYearTable = new Tabulator("#lastYearTable", {
		layout: "fitColumns",
		height: "350px",
		ajaxURL: "/api/sales/last-year-qty",
		ajaxParams: { year: 2024 },
		columns: [
			{ title: "분기", field: "SALES_QUARTER", hozAlign: "center" },
			{ title: "작년 총 매출액", field: "TOTAL_SALES_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "작년 총 매입단가", field: "TOTAL_COST_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
			{ title: "작년 총 영업이익", field: "TOTAL_PROFIT_AMOUNT", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } }
		]
	});

	// ================================
	// 📌 올해 영업계획 (등록용)
	// ================================
	var thisYearTable = new Tabulator("#thisYearTable", {
		layout: "fitColumns",
		height: "350px",
		columns: [
			{ title: "분기", field: "qtr", hozAlign: "center", editor: false },
			{ title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter },
			{ title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter },
			{ title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number" }
		],
		data: [
			{ qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" }
		]
	});


	// ================================
	// 📌 수정 모달 테이블
	// ================================
	// salesList.js
var editYearTable = new Tabulator("#editYearTable", {
    layout: "fitColumns",
    height: "350px",
    columns: [
        { title: "분기", field: "qtr", hozAlign: "center" },
        { title: "올해 총 매출액", field: "purpSales", hozAlign: "right", editor: "number", formatter: moneyFormatter },
        { title: "올해 총 영업이익", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: moneyFormatter },
        { title: "신규 거래처수", field: "newVendCnt", hozAlign: "center", editor: "number" }
    ]
});

// ================================
// 📌 행 클릭 시 수정 모달 열기
// ================================
table.on("rowClick", function(e, row) {
	const rowData = row.getData();

	// 📌 salesPlanCode를 전역 변수에 저장합니다.
	currentSalesPlanCode = rowData.salesPlanCode;

	fetch(`/api/sales/plan/${rowData.SALESYEAR}/details`)
		.then(res => {
			if (!res.ok) throw new Error("서버 응답 오류: " + res.status);
			return res.json();
		})
		.then(data => {
			console.log("조회 결과:", data);
			
			// 📌 데이터를 불러온 후 테이블을 비우고 새 데이터로 채웁니다.
			editYearTable.clearData(); 

			const modalEl = document.getElementById("modifySalesModal");
			let modal = bootstrap.Modal.getInstance(modalEl);
			if (!modal) {
				modal = new bootstrap.Modal(modalEl);
			}
			modal.show();
			window.setTimeout(function() {
				editYearTable.setData(data);
				
			},200);
		})
		.catch(err => {
			console.error("수정 데이터 로드 실패:", err);
			alert("수정 데이터를 불러올 수 없습니다. 콘솔 로그 확인하세요.");
		});
});

	// ================================
	// 📌 신규 버튼
	// ================================
	document.getElementById("btn-new").addEventListener("click", async function() {
		try {
			const response = await fetch("/api/sales/check-this-year");
			const data = await response.json();
			if (data.exists) {
				alert("올해 영업계획이 이미 등록되어 있습니다.");
				return;
			}
			const modal = new bootstrap.Modal(document.getElementById("insertSalesModal"));
			modal.show();
		} catch (error) {
			console.error(error);
			alert("오류 발생: " + error.message);
		}
	});

	// ================================
	// 📌 저장 버튼 (등록)
	// ================================
	document.getElementById("btn-save-sales").addEventListener("click", function() {
		const tableData = thisYearTable.getData();
		const payload = tableData.map(row => ({
			qtr: row.qtr,
			purpSales: row.purpSales || 0,
			purpProfitAmt: row.purpProfitAmt || 0,
			newVendCnt: row.newVendCnt || 0,
			planYear: new Date().getFullYear(),
			regDate: new Date(),
			empCode: "EMP001",
			companyCode: "COMP001"
		}));
		const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
		const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

		fetch('/api/sales/insert', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
			body: JSON.stringify(payload)
		})
			.then(res => {
				if (res.ok) {
					alert("영업계획이 저장되었습니다.");
					table.replaceData();
				} else {
					return res.text().then(text => { throw new Error(text); });
				}
			})
			.catch(err => { console.error(err); alert("저장 실패: " + err.message); });
	});


	// ================================
	// 📌 초기화 버튼
	// ================================
	document.getElementById("btn-reset-sales").addEventListener("click", function() {
		thisYearTable.setData([
			{ qtr: "1분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "2분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "3분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" },
			{ qtr: "4분기", purpSales: "", purpProfitAmt: "", newVendCnt: "" }
		]);
	});

	// ================================
	// 📌 수정 저장 버튼
	// ================================
document.getElementById("btn-update-sales").addEventListener("click", function() {
    const updatedData = editYearTable.getData();
    
    const payload = updatedData.map(d => ({
        qtr: d.qtr,
        purpSales: d.purpSales,
        purpProfitAmt: d.purpProfitAmt,
        newVendCnt: d.newVendCnt,
        salesPlanCode: d.salesPlanCode // 📌 salesPlanCode 필드에 직접 값을 할당
    }));
    
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
	
    fetch("/api/sales/update", {
        method: "PUT",
        headers: { "Content-Type": "application/json", [csrfHeader]: csrfToken },
        body: JSON.stringify(payload)
    })
    .then(res => {
        if (res.ok) {
            alert("수정되었습니다.");
            table.replaceData();
            bootstrap.Modal.getInstance(document.getElementById("modifySalesModal")).hide();
        } else {
            return res.text().then(text => { throw new Error(text); });
        }
    })
    .catch(err => { console.error(err); alert("저장 실패: " + err.message); });
});

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

});
