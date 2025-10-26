// openSalesModal.js

function openSalesModal(onSelect, partnerCode) {
	console.log("openSalesModal 호출됨, partnerCode:", partnerCode);

	// 모달 제목 설정
	document.getElementById("commonModalLabel").textContent = "주문서 검색";

	// 이전 Tabulator 인스턴스 제거
	if (window.salesTable) {
		window.salesTable.destroy();
	}

	// Tabulator 설정
	window.salesTable = new Tabulator("#commonModalTable", {
		ajaxURL: "/api/modal/order", // 백엔드 API
		ajaxParams: { partnerCode: partnerCode }, // ✅ 거래처코드로 필터링
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
		movableColumns: true,
		columns: [
			{ title: "주문서코드", field: "orderCode", hozAlign: "center" },
			{ title: "거래처코드", field: "partnerCode", hozAlign: "center" },
			{ title: "거래처명", field: "partnerName", hozAlign: "center" },
			{ title: "주문일", field: "createDate", hozAlign: "center" },
			{ title: "납기일", field: "deliveryDate", hozAlign: "center" },
			{ title: "배송지 우편번호", field: "postCode", hozAlign: "center" },
			{ title: "배송지 주소", field: "address", hozAlign: "center" },
			{ title: "담당자", field: "managerName", hozAlign: "center" },
			{ title: "비고", field: "remarks", hozAlign: "center" },
			{
				title: "선택",
				formatter: () => "<button class='btn btn-sm btn-primary'>선택</button>",
				width: 100,
				hozAlign: "center",
				cellClick: (e, cell) => {
					const selected = cell.getRow().getData();
					if (onSelect) onSelect(selected);
					bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
				}
			}
		],
	});

	// ✅ 데이터 로딩 완료 후 처리 (데이터 없으면 alert + 모달 닫기)
	window.salesTable.on("dataLoaded", function(data) {
		console.log("주문 목록 로딩 완료:", data);

		if (!data || data.length === 0) {
			alert("출하 가능한 주문이 없습니다.");
			return; // 모달 열지 않음
		}

		// ✅ 데이터가 있는 경우에만 모달 표시
		const modal = new bootstrap.Modal(document.getElementById("commonModal"));
		modal.show();
	});

	// 검색 이벤트 연결
	const searchInput = document.getElementById("commonModalSearch");
	if (searchInput) {
		searchInput.value = "";
		searchInput.oninput = function() {
			const keyword = this.value.trim();
			if (keyword) {
				const allFields = window.salesTable.getColumns()
					.map(col => col.getField())
					.filter(f => f && f !== "");
				const filters = allFields.map(f => ({
					field: f,
					type: "like",
					value: keyword
				}));
				window.salesTable.setFilter([filters]);
			} else {
				window.salesTable.clearFilter();
			}
		};
	}
}

