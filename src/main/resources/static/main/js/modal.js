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
			{ title: "담당자", field: "picName", width: 120 }

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

	// ✅ 청구서(INVOICE) 모달 - STATUS=회계반영완료만
	acinvoice: {
		// 백엔드 컨트롤러: GET /api/invoices/lookup
		// CompanyContext로 회사코드 필터링, status 기본값은 서버에서도 '회계반영완료' 처리
		// 여기서도 명시적으로 status 쿼리를 붙여서 호출
		url: "/api/invoices/lookup?status=" + encodeURIComponent("회계반영완료"),
		title: "청구서 검색",
		columns: [
			{ title: "청구서코드", field: "invoiceCode", width: 140, hozAlign: "center" },
			{ title: "거래처", field: "partnerName", minWidth: 160 },
			{
				title: "청구금액", field: "dmndAmt", width: 120, hozAlign: "right",
				formatter: (c) => Number(c.getValue() || 0).toLocaleString()
			},
			{ title: "상태", field: "status", width: 120, hozAlign: "center" },
			{
				title: "작성일", field: "createDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? String(v).slice(0, 10) : d.toISOString().slice(0, 10); }
			},
			{
				title: "청구일", field: "dmndDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? String(v).slice(0, 10) : d.toISOString().slice(0, 10); }
			},
			{
				title: "수금예정", field: "rcptDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? String(v).slice(0, 10) : d.toISOString().slice(0, 10); }
			},
		],
		selectable: 1
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

	// ✅ 품목(제품) 모달 - sales 조회로 대체
	product: {
		url: "/api/sales/lookup",        // ← 기존 /api/products/lookup 대신 사용
		title: "품목 검색",
		columns: [
			{ title: "판매코드", field: "salesCode", width: 140, hozAlign: "center" },
			{ title: "품목명", field: "productName", minWidth: 200 },
			{ title: "거래처", field: "partnerName", minWidth: 160 },
			{
				title: "단가", field: "unitPrice", width: 110, hozAlign: "right",
				formatter: (c) => Number(c.getValue() || 0).toLocaleString()
			},
			{
				title: "판매일자", field: "salesDate", width: 120, hozAlign: "center",
				formatter: (cell) => { const v = cell.getValue(); if (!v) return ""; const d = new Date(v); return isNaN(d) ? v : d.toISOString().slice(0, 10); }
			},
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
			{
				title: "사용 여부", field: "attIs",
				formatter: (cell) => cell.getValue() === "Y" ? "사용함" : "사용안함"
			},
			{ title: "비고", field: "note" },
		]
	},




	// ✅ 출하지시서 모달
	shipment: {
		url: "/api/shipments/lookup",  // 백엔드에서 출하지시 목록 조회용 API
		title: "출하지시서 검색",
		columns: [
			{
				title: "출하지시코드",
				field: "shipmentCode",
				width: 160,
				hozAlign: "center"
			},
			{
				title: "출하예정일자",
				field: "shipmentDate",
				width: 150,
				hozAlign: "center",
				formatter: (cell) => {
					const v = cell.getValue();
					if (!v) return "";
					const d = new Date(v);
					return isNaN(d) ? v : d.toISOString().slice(0, 10);
				}
			},
			{
				title: "거래처코드",
				field: "partnerCode",
				width: 160,
				hozAlign: "center"
			},
		],
		selectable: 1,  // 한 건만 선택
	},

	// ✅ 견적서
	estimate: {
		url: "/api/modal/estimate",
		title: "견적서 검색",
		columns: [
			{ title: "견적서코드", field: "estimateCode", hozAlign: "center" },
			{ title: "거래처코드", field: "partnerCode", hozAlign: "center" },
			{ title: "거래처", field: "partnerName", hozAlign: "center" },
			{ title: "납기일", field: "deliveryDate", hozAlign: "center" },
			{ title: "견적금액합계", field: "totalAmount", hozAlign: "center" },
			{ title: "담당자", field: "managerName", hozAlign: "center" },
			{ title: "비고", field: "remarks", hozAlign: "center" },


		],
		selectable: 1,
	},

	// ✅ 품목
	productCode: {
		url: "/api/modal/productCode",
		title: "품목 검색",
		columns: [
			{ title: "품목코드", field: "productCode", hozAlign: "center" },
			{ title: "품목그룹", field: "productGroup", hozAlign: "center" },
			{ title: "품목명", field: "productName", hozAlign: "center" },
			{
				title: "규격/단위",
				hozAlign: "center",
				formatter: function(cell) {
					const data = cell.getRow().getData();

					const size = data.productSize || '';
					const unit = data.unit || '';

					if (size && unit) {
						return size + " " + unit;
					} else if (size) {
						return size;
					} else if (unit) {
						return unit;
					}
					return '';
				}
			},
			{
				title: "입고단가",
				field: "inPrice",
				hozAlign: "center", // 금액은 우측 정렬이 가독성이 좋습니다.
				// 콤마 포맷터 적용
				formatter: function(cell) {
					const price = cell.getValue();
					// 값이 유효한 숫자인지 확인
					if (price === null || price === undefined || isNaN(price) || price === '') {
						return '-';
					}
					// toLocaleString()을 사용하여 콤마를 적용합니다.
					return Number(price).toLocaleString('ko-KR') + '원';
				}
			}, 
			{
				title: "출고단가",
				field: "outPrice",
				hozAlign: "center", // 금액은 우측 정렬이 가독성이 좋습니다.
				// 콤마 포맷터 적용
				formatter: function(cell) {
					const price = cell.getValue();
					// 값이 유효한 숫자인지 확인
					if (price === null || price === undefined || isNaN(price) || price === '') {
						return '-';
					}
					// toLocaleString()을 사용하여 콤마를 적용합니다.
					return Number(price).toLocaleString('ko-KR') + '원';
				}
			},
			{ title: "비고", field: "remarks", hozAlign: "center" },

		],
		selectable: 1,
	},

	//  품목 - sales2
	productCode2: {
		url: "/api/modal/productCode",
		title: "품목 검색",
		columns: [
			{ title: "품목코드", field: "productCode", width: 200, hozAlign: "center" },
			{ title: "품목명", field: "productName", width: 200, hozAlign: "center" },
		],
		selectable: 1,
	},

	salesEmployee: {
		url: "/api/modal/salesEmployee",
		title: "영업사원 검색",
		columns: [
			{ title: "사원코드", field: "empCode", hozAlign: "center" },
			{ title: "사원명", field: "name", hozAlign: "center" },
			{ title: "연락처", field: "phone", hozAlign: "center" },
		],
		selectable: 1,
	},

	salesPartner: {
		url: "/api/modal/salesPartner",
		title: "거래처 검색",
		columns: [
			{ title: "거래처코드", field: "partnerCode", hozAlign: "center" },
			{ title: "거래처명", field: "partnerName" },
			{ title: "연락처", field: "partnerPhone" },
			{ title: "담당자", field: "name" },
			{ title: "우편번호", field: "postCode", hozAlign: "center" },
			{ title: "주소", field: "address", hozAlign: "center" }

		],
		selectable: 1,
	},
	
	orders: {
		url: "/api/modal/order",
		title: "주문서 검색",
		columns: [
			{ title: "주문서코드", field: "orderCode", hozAlign: "center" },
			{ title: "거래처코드", field: "partnerCode", hozAlign: "center" },
			{ title: "거래처", field: "partnerName", hozAlign: "center" },
			{ title: "주문일", field: "createDate", hozAlign: "center" },
			{ title: "납기일", field: "deliveryDate", hozAlign: "center" },
			{ title: "배송지 우편번호", field: "postCode", hozAlign: "center" },
			{ title: "배송지 주소", field: "address", hozAlign: "center" },
			{ title: "담당자", field: "managerName", hozAlign: "center" },
			{ title: "비고", field: "remarks", hozAlign: "center" },


		],
		selectable: 1,
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
		ajaxParams: commonGroup ? { commonGroup: commonGroup } : {},
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

					if (type === "attendance" && selected.attIs === "N") {
						alert("사용안함 항목은 선택할 수 없습니다.");
						return;
					}

					if (onSelect) {
						onSelect(selected);
					}
					bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
				}
			}
		],


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