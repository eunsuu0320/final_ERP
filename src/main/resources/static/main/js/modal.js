// modal.js (Tabulator 적용)

// === 공통 설정 ===
const modalConfigs = {
	employee: {
		url: "/api/modal/employee",
		title: "직원 검색",
		columns: [
			{ title: "사원번호", field: "empCode" },
			{ title: "성명", field: "name" },
			{ title: "부서", field: "deptCode.codeName" },
			{ title: "직급", field: "gradeCode.codeName" }
		]
	},
	// ✅ 거래처
	partner: {
		url: "/api/partners",
		title: "거래처 검색",
		columns: [
			{ title: "거래처코드", field: "partnerCode", width: 140, hozAlign: "center" },
			{ title: "거래처명", field: "partnerName", minWidth: 200 },
			{ title: "연락처", field: "tel", width: 140 },
			{ title: "담당자", field: "picName", width: 120 },
		],
		selectable: 1,
	},
	// ✅ 전표
	voucher: {
		url: "/api/statements/lookup",
		title: "전표 검색",
		columns: [
			{
				title: "전표일자", field: "voucherDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? v : d.toISOString().slice(0, 10); }
			},
			{ title: "전표번호", field: "voucherNo", width: 130, hozAlign: "center" },
			{
				title: "유형", field: "type", width: 90, hozAlign: "center",
				formatter: (c) => ({ SALES: "매출", BUY: "매입", MONEY: "수금", PAYMENT: "지급" }[c.getValue()] || c.getValue())
			},
			{ title: "거래처명", field: "partnerName", minWidth: 180 },
			{ title: "적요", field: "remark", minWidth: 200 },
		],
	},

	// ✅ 추가: 판매코드(= SALES 테이블 조회)
	sales: {
		url: "/api/sales/lookup",
		title: "판매코드 검색",
		columns: [
			{ title: "판매코드", field: "salesCode", width: 140, hozAlign: "center" },
			{ title: "거래처", field: "partnerName", minWidth: 160 },
			{ title: "제품명", field: "productName", minWidth: 180 },
			{
				title: "판매금액", field: "salesAmount", width: 120, hozAlign: "right",
				formatter: (c) => Number(c.getValue() || 0).toLocaleString()
			},
			{
				title: "판매일자", field: "salesDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? v : d.toISOString().slice(0, 10); }
			},
		],
	},
	commonCode: {
		url: "/api/modal/commonCode",
		title: "공통코드 검색",
		columns: [
			{ title: "코드", field: "codeId" },
			{ title: "코드명", field: "codeName" },
		]
	},

	// 매입처 모달
	supplier: {
		url: "/api/suppliers",
		title: "매입처(공급처) 검색",
		columns: [
			{ title: "공급처코드", field: "supplierCode", width: 140, hozAlign: "center" },
			{ title: "공급처명", field: "supplierName", minWidth: 200 },
			{ title: "연락처", field: "phone", width: 140 },
			{ title: "주소", field: "address", minWidth: 220 },
			{ title: "비고", field: "remark", minWidth: 160 },
		],
		selectable: 1,
	},

	// 구매코드 모달
	buy: {
		url: "/api/buys/lookup",
		title: "구매코드 검색",
		columns: [
			{ title: "구매코드", field: "buyCode", width: 140, hozAlign: "center" },
			{ title: "매입처", field: "partnerName", minWidth: 160 },
			{ title: "품목", field: "productName", minWidth: 180 },
			{
				title: "총금액", field: "amountTotal", width: 120, hozAlign: "right",
				formatter: (c) => Number(c.getValue() || 0).toLocaleString()
			},
			{
				title: "구매일자", field: "purchaseDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? v : d.toISOString().slice(0, 10); }
			},
			{ title: "세금유형", field: "taxCode", width: 100, hozAlign: "center" },
		],
		selectable: 1,
	},

	// 근태코드 모달
	attendance: {
		url: "/api/modal/attendance",
		title: "근태 항목 검색",
		columns: [
			{ title: "근태 코드", field: "attId" },
			{ title: "근태 유형", field: "attType" },
			{ title: "사용 여부", field: "attIs" },
			{ title: "비고", field: "note" },
		]
	},

	product: {
		url: "/api/modal/warehouse",
		title: "품목 검색",
		columns: [
			{ title: "창고", field: "productCode" },
			{ title: "품목명", field: "productName" },
			{ title: "품목그룹", field: "productGroup" },
			{ title: "직급", field: "gradeCode.codeName" }
		]
	},

};



let table; // Tabulator 인스턴스 전역 변수

function openModal(type, onSelect, commonGroup) {
	const config = modalConfigs[type];

	if (!config) {
		console.error(`modalConfigs[${type}] 설정이 없습니다.`);
		return;
	}

	// 제목 설정
	document.getElementById("commonModalLabel").textContent = config.title;

	// Tabulator 초기화
	if (table) {
		table.destroy();
	}

	table = new Tabulator("#commonModalTable", {
		ajaxURL: config.url,
		ajaxParams: commonGroup ? { commonGroup: commonGroup } : {}, // 공통그룹 파라미터
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
		movableColumns: true,
		columns: [
			...config.columns,
			{
				title: "선택",
				formatter: () => "<button class='btn btn-sm btn-primary'>선택</button>",
				width: 100,
				hozAlign: "center",
				cellClick: (e, cell) => {
					const selected = cell.getRow().getData();

					// ✅ 근태 모달일 경우 사용 여부 체크
					if (type === "attendance" && selected.attIs === "N") {
						alert("사용안함 항목은 선택할 수 없습니다.");
						return; // 선택 막기
					}

					if (onSelect) {
						onSelect(selected);
					}
					bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
				}
			}
		],
		ajaxResponse: function(url, params, response) {
			return response; // 필요 시 response.data 로 변경
		}
	});

	// 검색 이벤트 연결
	const searchInput = document.getElementById("commonModalSearch");
	if (searchInput) {
		searchInput.value = ""; // 초기화
		searchInput.oninput = function() {
			const keyword = this.value.trim();
			if (keyword) {
				const allFields = table.getColumns()
					.map(col => col.getField())
					.filter(f => f && f !== ""); // field 없는 버튼 컬럼 제외

				const filters = allFields.map(f => ({
					field: f,
					type: "like",
					value: keyword
				}));

				table.setFilter([filters]);
			} else {
				table.clearFilter();
			}
		};
	}

	const modal = new bootstrap.Modal(document.getElementById("commonModal"));
	modal.show();
}