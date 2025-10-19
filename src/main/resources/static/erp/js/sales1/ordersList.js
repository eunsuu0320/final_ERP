// ordersList.js
document.addEventListener("DOMContentLoaded", function() {

	// === 공통 / 상수 ===
	const defaultVisible = ["주문서코드", "등록일자", "거래처명", "담당자", "품목명", "납기일자", "주문금액합계", "진행상태"];
	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"출하지시완료": { label: "출하지시완료" },
		"완료": { label: "완료" }
	};
	window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;

	// === 탭 필터 ===
	initTabFiltering();
	function initTabFiltering() {
		const tabButtons = document.querySelectorAll('#ordersTab button');
		tabButtons.forEach(btn => {
			btn.addEventListener('click', function() {
				const type = this.dataset.type;
				tabButtons.forEach(b => { b.classList.remove('btn-primary'); b.classList.add('btn-outline-primary'); });
				this.classList.remove('btn-outline-primary'); this.classList.add('btn-primary');
				applyFilter(type);
			});
		});
	}
	function applyFilter(type) {
		const table = window.orderTableInstance;
		if (!table) return;
		const field = "진행상태";
		let val = null;
		switch (type) {
			case 'ALL': table.clearFilter(true); return;
			case 'NONCHECK': val = "미확인"; break;
			case 'ONGOING': val = "진행중"; break;
			case 'SHIPMENTSUCCESS': val = "출하지시완료"; break;
			case 'SUCCESS': val = "완료"; break;
			default: return;
		}
		table.setFilter([{ field, type: "=", value: val }], true); // replace=true
	}

	// === 모달 ===
	window.resetOrder = function() {
		const form = document.getElementById("quoteForm");
		if (form) form.reset();

		if (window.resetItemGrid) {
			window.resetItemGrid();
		} else {
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			if (window.addItemRow) window.addItemRow();
		}

		// 부가 필드 초기화
		const del = document.getElementById('deliveryDate'); if (del) del.value = '';
		const est = document.getElementById('estimateUniqueCode'); if (est) est.value = '';

		// 견적 로드 캐시 초기화 (요구사항 4 & 6)
		window.lastLoadedOrderData = null;
		window.lastModalType = null;

		// 합계 리셋
		if (typeof window.calculateTotal === 'function') window.calculateTotal();
	};

	window.showDetailModal = function(modalType, keyword) {
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("quoteForm");

		// 헤더 타이틀
		const modalName = (modalType === 'detail') ? '주문서 상세정보' : '주문서 등록';
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		// 항상 초기화
		window.resetOrder();

		// 신규 모드: 파트너 검색 허용
		if (modalType === 'regist') {
			document.getElementById("partnerName").readOnly = false;
			document.getElementById("partnerModalBtn").disabled = false;
		}

		// 상세 모드: 서버에서 주문서 데이터를 로딩/바인딩
		if (modalType === 'detail' && (keyword !== undefined && keyword !== null)) {
			showLoading();
			loadDetailData('orders', keyword, form) // /api/orders/getDetail?keyword=...
				.then(data => {
					window.lastLoadedOrderData = data;
					window.lastModalType = 'detail';
				})
				.catch(err => console.error('주문 상세 로딩 실패:', err))
				.finally(hideLoading);
		}

		// 닫히면 전체 초기화 + 캐시 초기화 (요구사항 4)
		modalEl.addEventListener('hidden.bs.modal', function() {
			window.resetOrder();
		}, { once: true });

		modal.show();
	};

	// 저장
	window.saveModal = function() {
		const quoteForm = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");
		if (!quoteForm) { alert("저장 오류: 주문서 등록 폼을 찾을 수 없습니다."); return; }

		const orderData = new FormData(quoteForm);
		const orderDataObject = Object.fromEntries(orderData.entries());

		const partnerNameValue = document.getElementById('partnerName').value;
		const deliveryDateValue = orderDataObject.deliveryDateText;
		const estimateUniqueCodeValue = orderDataObject.estimateUniqueCode;

		const detailList = collectOrderDetails();
		if (detailList.length === 0) { alert("주문 상세 내용을 1개 이상 입력해주세요."); return; }

		const finalPayload = {
			partnerCode: orderDataObject.partnerCode || '',
			partnerName: partnerNameValue,
			orderDate: orderDataObject.quoteDate,
			deliveryDate: deliveryDateValue,
			estimateUniqueCode: parseInt(estimateUniqueCodeValue) || null,
			manager: orderDataObject.manager || '',
			remarks: orderDataObject.remarks || '',
			detailList: detailList,

			// 🆕 신규 필드
			postCode: orderDataObject.postCode ? parseInt(orderDataObject.postCode) : null,
			address: orderDataObject.address || '',
			payCondition: orderDataObject.payCondition || ''
		};
		console.log("전송할 최종 주문 데이터:", finalPayload);

		fetch("/api/registOrders", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload),
		})
			.then(res => {
				if (!res.ok) return res.json().then(error => { throw new Error(error.message || `서버 오류 발생: ${res.status}`); });
				return res.json();
			})
			.then(data => {
				alert("주문서가 성공적으로 등록되었습니다. ID: " + data.id);
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();
				// 전체 새로고침
				location.reload();
			})
			.catch(err => {
				console.error("주문서 등록 실패:", err);
				alert(`등록에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요. 오류: ${err.message}`);
			});
	};

	function collectOrderDetails() {
		const list = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return list;

		tbody.querySelectorAll('tr').forEach(row => {
			if (row.getAttribute('data-row-id') === 'new') return;

			const supplyAmount = window.cleanValue(row.querySelector('input[name="supplyAmount"]').value);
			const taxAmount = window.cleanValue(row.querySelector('input[name="taxAmount"]').value);
			const pctVat = (supplyAmount > 0) ? (taxAmount / supplyAmount) * 100 : 0;

			list.push({
				productCode: row.querySelector('input[name="productCode"]').value || '',
				quantity: window.cleanValue(row.querySelector('input[name="quantity"]').value),
				price: window.cleanValue(row.querySelector('input[name="price"]').value),
				amountSupply: supplyAmount,
				pctVat: Math.round(pctVat * 100) / 100,
				remarks: row.querySelector('input[name="remarks"]').value || '',
			});
		});
		return list;
	}

	// === 메인 Tabulator ===
	let tabulatorColumns = [
		{
			formatter: "rowSelection", titleFormatter: "rowSelection",
			hozAlign: "center", headerHozAlign: "center",
			headerSort: false, width: 50, frozen: true
		},
		...columns.map(col => {
			let columnDef = { title: col, field: col, visible: defaultVisible.includes(col) };

			// 주문서코드 클릭 → 상세모달 (요구사항 5)
			if (col === "주문서코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const rowData = cell.getData();
					const uk = rowData.주문서고유코드; // 서비스에서 넣어줌
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${uk}')">${value}</div>`;
				};
			}

			// 품목명: 텍스트 그대로 (서비스에서 "A 외 n건" 생성)
			if (col === "주문금액합계") {
				columnDef.formatter = function(cell) {
					const v = cell.getValue();
					if (v === null || v === undefined || isNaN(v)) return "-";
					return Number(v).toLocaleString('ko-KR') + " 원"; // 요구사항 10 포맷
				};
				columnDef.sorter = "number";
				columnDef.hozAlign = "right";
			}

			if (col === "진행상태") {
				columnDef.field = "진행상태";
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const rowData = cell.getData();
					const code = rowData.주문서코드;

					const options = Object.keys(STATUS_MAP).map(key => {
						const isSelected = key === value ? 'selected' : '';
						return `<option value="${key}" ${isSelected}>${STATUS_MAP[key].label}</option>`;
					}).join('');

					return `
            <select class="form-select form-select-sm"
                    onchange="updateStatusAPI('${code}', this.value, this)"
                    style="font-size: 0.75rem; padding: 0.25rem 0.5rem; height: auto; min-width: 90px;">
              ${options}
            </select>
          `;
				};
			}

			return columnDef;
		})
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.orderTableInstance = tableInstance;

	// === 요구사항 1: fetch 없이 다중필터 ===
	window.filterSearch = function() {
		const table = window.orderTableInstance;
		if (!table) return;

		const startDate = document.getElementById('startDate').value;
		const endDate = document.getElementById('endDate').value;
		const manager = document.getElementById('managerSearch').value.trim();
		const partner = document.getElementById('partnerNameSearch').value.trim();
		const product = document.getElementById('productSearch').value.trim();

		table.clearFilter(true); // 기존 필터 초기화

		// 함수형 필터 (Tabulator 전체 필터링)
		table.setFilter((data) => {
			// 등록일자 비교
			if (startDate || endDate) {
				const cellDate = new Date(data['등록일자']);
				const sOk = !startDate || cellDate >= new Date(startDate);
				const eOk = !endDate || cellDate <= new Date(endDate);
				if (!sOk || !eOk) return false;
			}

			// 담당자
			if (manager && !String(data['담당자'] || '').includes(manager)) return false;
			// 거래처명
			if (partner && !String(data['거래처명'] || '').includes(partner)) return false;
			// 품목명
			if (product && !String(data['품목명'] || '').includes(product)) return false;

			return true;
		});
	};


	window.resetSearch = function() {
		document.getElementById('startDate').value = '';
		document.getElementById('endDate').value = '';
		document.getElementById('managerSearch').value = '';
		document.getElementById('partnerNameSearch').value = '';
		document.getElementById('productSearch').value = '';
		const table = window.orderTableInstance;
		if (table) table.clearFilter(true);
	};

});

// === 상태 변경 ===
window.updateStatusAPI = function(code, status, selectElement) {
	const row = window.orderTableInstance.getRows().find(r => r.getData().주문서코드 === code);
	const currentStatus = row?.getData()?.진행상태;

	if (currentStatus === status) {
		if (selectElement) selectElement.value = currentStatus;
		return;
	}

	const url = "/api/updateOrders";
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
	const csrfToken = document.querySelector('meta[name="_csrf"]').content;

	fetch(url, {
		method: "POST",
		headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
		body: JSON.stringify({ orderCode: code, status })
	})
		.then(res => {
			if (!res.ok) return res.json().then(error => { throw new Error(error.message || `서버 오류: ${res.status}`); });
			return res.json();
		})
		.then(response => {
			if (response.success) {
				window.orderTableInstance.getRows().find(r => r.getData().주문서코드 === code)?.update({ '진행상태': status });
			} else {
				alert(`상태 변경 실패: ${response.message || '알 수 없는 오류'}`);
				if (selectElement) selectElement.value = currentStatus;
			}
		})
		.catch(err => {
			console.error("상태 변경 실패:", err);
			alert(`통신 오류: ${err.message}`);
			if (selectElement) selectElement.value = currentStatus;
		});
};

// === 행 계산 ===
window.calculateRow = function(inputElement) {
	const row = inputElement.closest('tr');
	if (!row) return;

	const cleanValue = window.cleanValue;
	const currentValue = cleanValue(inputElement.value);
	inputElement.value = currentValue.toLocaleString('ko-KR');

	const quantityInput = row.querySelector('input[name="quantity"]');
	const unitPriceInput = row.querySelector('input[name="price"]');
	const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
	const taxAmountInput = row.querySelector('input[name="taxAmount"]');
	const finalAmountInput = row.querySelector('input[name="finalAmount"]');

	const quantity = cleanValue(quantityInput.value);
	const unitPrice = cleanValue(unitPriceInput.value);

	const supplyAmount = quantity * unitPrice;
	const taxAmount = Math.floor(supplyAmount * 0.1);
	const finalAmount = supplyAmount + taxAmount;

	supplyAmountInput.value = supplyAmount.toLocaleString('ko-KR');
	taxAmountInput.value = taxAmount.toLocaleString('ko-KR');
	finalAmountInput.value = finalAmount.toLocaleString('ko-KR');

	calculateTotal();
};

// === 합계 ===
window.calculateTotal = function() {
	let totalQuantity = 0, totalSupplyAmount = 0, totalTaxAmount = 0, totalFinalAmount = 0;
	const tbody = document.getElementById('itemDetailBody');
	if (!tbody) return;

	tbody.querySelectorAll('tr').forEach(row => {
		if (row.getAttribute('data-row-id') === 'new') return;

		const cleanValue = window.cleanValue;
		const quantity = cleanValue(row.querySelector('input[name="quantity"]')?.value || 0);
		const supplyAmount = cleanValue(row.querySelector('input[name="supplyAmount"]')?.value || 0);
		const taxAmount = cleanValue(row.querySelector('input[name="taxAmount"]')?.value || 0);

		totalQuantity += quantity;
		totalSupplyAmount += supplyAmount;
		totalTaxAmount += taxAmount;
		totalFinalAmount += (supplyAmount + taxAmount);
	});

	document.getElementById('totalQuantity').textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	document.getElementById('totalSupplyAmount').textContent = totalSupplyAmount.toLocaleString('ko-KR') + ' 원';
	document.getElementById('totalTaxAmount').textContent = totalTaxAmount.toLocaleString('ko-KR') + ' 원';
	document.getElementById('totalFinalAmount').textContent = totalFinalAmount.toLocaleString('ko-KR') + ' 원';
};
