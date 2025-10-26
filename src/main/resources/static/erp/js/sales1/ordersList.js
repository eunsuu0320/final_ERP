// ordersList.js
document.addEventListener("DOMContentLoaded", function() {

	// === 공통 / 상수 ===
	const defaultVisible = ["주문서코드", "등록일자", "거래처명", "담당자", "품목명", "납기일자", "주문금액합계", "진행상태"];
	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
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

		const del = document.getElementById('deliveryDate'); if (del) del.value = '';
		const est = document.getElementById('estimateUniqueCode'); if (est) est.value = '';

		window.lastLoadedOrderData = null;
		window.lastModalType = null;

		if (typeof window.calculateTotal === 'function') window.calculateTotal();
	};

	window.showDetailModal = function(modalType, keyword) {
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("quoteForm");

		const modalName = (modalType === 'detail') ? '주문서 상세정보' : '주문서 등록';
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		window.resetOrder();

		if (modalType === 'regist') {
			document.getElementById("partnerName").readOnly = false;
			document.getElementById("partnerModalBtn").disabled = false;
		}

		// ✅ DETAIL 타입일 때도 모달 내부 오버레이 적용
		if (modalType === 'detail' && (keyword !== undefined && keyword !== null)) {
			showModalLoading(); // ✅ 모달 내부 오버레이 표시
			loadDetailData('orders', keyword, form)
				.then(data => {
					window.lastLoadedOrderData = data;
					window.lastModalType = 'detail';
				})
				.catch(err => console.error('주문 상세 로딩 실패:', err))
				.finally(() => {
					hideModalLoading(); // ✅ 로딩 완료 시 제거
				});
		}

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

		const partnerDiscountText = document.getElementById("partnerDiscountAmount")?.textContent || "0";
		const partnerDiscountAmount = window.cleanValue(partnerDiscountText); // "1,200 원" → 1200

		
		const orderData = new FormData(quoteForm);
		const orderDataObject = Object.fromEntries(orderData.entries());

		const partnerNameValue = document.getElementById('partnerName').value;
		const deliveryDateValue = orderDataObject.deliveryDate;
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
			postCode: orderDataObject.postCode ? parseInt(orderDataObject.postCode) : null,
			address: orderDataObject.address || '',
			partnerDiscountAmount: partnerDiscountAmount, // ✅ 이제 정확히 숫자값
			payCondition: orderDataObject.payCondition || ''
		};
		console.log("전송할 최종 주문 데이터:", finalPayload);

		fetch("/api/registOrders", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name=\"_csrf_header\"]').content]:
					document.querySelector('meta[name=\"_csrf\"]').content
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

			const supplyAmount = window.cleanValue(row.querySelector('input[name=\"supplyAmount\"]').value);
			const taxAmount = window.cleanValue(row.querySelector('input[name=\"taxAmount\"]').value);
			const pctVat = (supplyAmount > 0) ? (taxAmount / supplyAmount) * 100 : 0;

			list.push({
				productCode: row.querySelector('input[name=\"productCode\"]').value || '',
				quantity: window.cleanValue(row.querySelector('input[name=\"quantity\"]').value),
				price: window.cleanValue(row.querySelector('input[name=\"price\"]').value),
				amountSupply: supplyAmount,
				pctVat: Math.round(pctVat * 100) / 100,
				discountAmount: window.cleanValue(row.querySelector('input[name="discountAmount"]').value),
			});
		});
		return list;
	}

	// === 메인 Tabulator ===
	let tabulatorColumns = [
		{
			title: "No",
			formatter: "rownum",
			hozAlign: "center", headerHozAlign: "center",
			headerSort: false, width: 50, frozen: true
		},
		...columns.map(col => {
			let columnDef = { title: col, field: col, visible: defaultVisible.includes(col) };

			if (col === "주문서코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const rowData = cell.getData();
					const uk = rowData.주문서고유코드;
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${uk}')">${value}</div>`;
				};
			}


			if (col === "비고") {
				columnDef.hozAlign = "left";
			}

			if (col === "주문금액합계") {
				columnDef.title = col + " (원)";
				columnDef.formatter = function(cell) {
					const v = cell.getValue();
					if (v === null || v === undefined || isNaN(v)) return "-";
					return Number(v).toLocaleString('ko-KR');
				};
				columnDef.sorter = "number";
				columnDef.hozAlign = "right";
			}
			if (col === "진행상태") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const code = cell.getData().주문서코드;
					const options = Object.keys(STATUS_MAP).map(key => {
						const isSelected = key === value ? 'selected' : '';
						return `<option value="${key}" ${isSelected}>${STATUS_MAP[key].label}</option>`;
					}).join('');
					
					if (value === "진행중") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${value}" readonly
							style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}
					
					if (value === "출하지시완료") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${value}" readonly
							style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}
					
					
					return `<select class="form-select form-select-sm"
							onchange="updateStatusAPI('${code}', this.value, this)"
							style="font-size:0.75rem; padding:0.25rem 0.5rem; height:auto; min-width:90px;">
							${options}</select>`;
				};
			}
			return columnDef;
		})
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.orderTableInstance = tableInstance;

	// ------------------------------
	// ✅ 컬럼 체크박스 제어 (PRODUCTLIST.js 스타일)
	// ------------------------------
	const selectAll = document.getElementById('selectAllColumns');
	const columnCheckboxes = document.querySelectorAll('.colCheckbox');

	if (selectAll && columnCheckboxes.length > 0) {
		// 전체 선택 체크박스
		selectAll.addEventListener('change', function() {
			const checked = this.checked;
			columnCheckboxes.forEach(chk => {
				chk.checked = checked;
				if (checked) tableInstance.showColumn(chk.value);
				else tableInstance.hideColumn(chk.value);

				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});

		// 개별 체크박스
		columnCheckboxes.forEach(chk => {
			chk.addEventListener('change', function() {
				if (this.checked) tableInstance.showColumn(this.value);
				else tableInstance.hideColumn(this.value);

				// 전체 선택 상태 반영
				selectAll.checked = Array.from(columnCheckboxes).every(c => c.checked);

				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});
	}

	// === 다중필터 ===
	window.filterSearch = function() {
		const table = window.orderTableInstance;
		if (!table) return;

		const startDate = document.getElementById('startDate').value;
		const endDate = document.getElementById('endDate').value;
		const manager = document.getElementById('managerSearch').value.trim();
		const partner = document.getElementById('partnerNameSearch').value.trim();
		const product = document.getElementById('productSearch').value.trim();

		table.clearFilter(true);

		table.setFilter((data) => {
			if (startDate || endDate) {
				const cellDate = new Date(data['등록일자']);
				const sOk = !startDate || cellDate >= new Date(startDate);
				const eOk = !endDate || cellDate <= new Date(endDate);
				if (!sOk || !eOk) return false;
			}
			if (manager && !String(data['담당자'] || '').includes(manager)) return false;
			if (partner && !String(data['거래처명'] || '').includes(partner)) return false;
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
	const csrfHeader = document.querySelector('meta[name=\"_csrf_header\"]').content;
	const csrfToken = document.querySelector('meta[name=\"_csrf\"]').content;

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



window.showModalLoading = function() {
	const modalContent = document.querySelector("#newDetailModal .modal-content");
	if (!modalContent) return;

	if (modalContent.querySelector(".modal-loading-overlay")) return;

	const overlay = document.createElement("div");
	overlay.className = "modal-loading-overlay";
	overlay.innerHTML = `
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
    <span class="ms-3">데이터 로딩 중...</span>
  `;
	modalContent.appendChild(overlay);
};

window.hideModalLoading = function() {
	const modalContent = document.querySelector("#newDetailModal .modal-content");
	if (!modalContent) return;

	const overlay = modalContent.querySelector(".modal-loading-overlay");
	if (overlay) overlay.remove();
};



// === 행 계산 ===
window.calculateRow = function(inputElement) {
	const row = inputElement.closest('tr');
	if (!row) return;

	const cleanValue = window.cleanValue;
	const currentValue = cleanValue(inputElement.value);
	inputElement.value = currentValue.toLocaleString('ko-KR');

	const quantityInput = row.querySelector('input[name=\"quantity\"]');
	const unitPriceInput = row.querySelector('input[name=\"price\"]');
	const supplyAmountInput = row.querySelector('input[name=\"supplyAmount\"]');
	const taxAmountInput = row.querySelector('input[name=\"taxAmount\"]');
	const finalAmountInput = row.querySelector('input[name=\"finalAmount\"]');

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

function calculateTotal(partnerDiscountRate = 0) {
	let totalQuantity = 0;
	let totalSupplyAmount = 0;
	let totalTaxAmount = 0;
	let totalDiscountAmount = 0; // ✅ 품목별 할인 총합 추가
	let totalFinalAmount = 0;

	const tbody = document.getElementById('itemDetailBody');
	if (!tbody) return;

	tbody.querySelectorAll('tr').forEach(row => {
		const quantity = cleanValue(row.querySelector('input[name="quantity"]')?.value);
		const supply = cleanValue(row.querySelector('input[name="supplyAmount"]')?.value);
		const tax = cleanValue(row.querySelector('input[name="taxAmount"]')?.value);
		const discount = cleanValue(row.querySelector('input[name="discountAmount"]')?.value);
		const final = cleanValue(row.querySelector('input[name="finalAmount"]')?.value);

		totalQuantity += quantity;
		totalSupplyAmount += supply;
		totalTaxAmount += tax;
		totalDiscountAmount += discount; // ✅ 할인금액 합계 누적
		totalFinalAmount += final;
	});

	// ===============================
	// ✅ 합계 표시 영역 업데이트
	// ===============================
	const totalQtyEl = document.getElementById('totalQuantity');
	const totalSupplyEl = document.getElementById('totalSupplyAmount');
	const totalTaxEl = document.getElementById('totalTaxAmount');
	const totalDiscountEl = document.getElementById('totalDiscountAmount');
	const totalAmountEl = document.getElementById('totalAmount');
	const partnerDiscountEl = document.getElementById('partnerDiscountAmount');
	const totalEstimateEl = document.getElementById('totalEstimateAmount');

	if (totalQtyEl) totalQtyEl.textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	if (totalSupplyEl) totalSupplyEl.textContent = totalSupplyAmount.toLocaleString('ko-KR') + ' 원';
	if (totalTaxEl) totalTaxEl.textContent = totalTaxAmount.toLocaleString('ko-KR') + ' 원';
	if (totalDiscountEl) totalDiscountEl.textContent = totalDiscountAmount.toLocaleString('ko-KR') + ' 원'; // ✅ 할인 합계 표시
	if (totalAmountEl) totalAmountEl.textContent = totalFinalAmount.toLocaleString('ko-KR') + ' 원';

	// ===============================
	// ✅ 거래처 전체 할인 계산
	// ===============================
	const partnerDiscountValue = Math.floor(totalFinalAmount * partnerDiscountRate);
	const totalEstimate = totalFinalAmount - partnerDiscountValue;

	if (partnerDiscountEl)
		partnerDiscountEl.textContent = partnerDiscountValue.toLocaleString('ko-KR') + ' 원';
	if (totalEstimateEl)
		totalEstimateEl.textContent = totalEstimate.toLocaleString('ko-KR') + ' 원';
}
