document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["견적서코드", "등록일자", "거래처명", "품목명", "유효기간", "견적금액합계", "담당자", "진행상태"];

	// 진행 상태에 따른 스타일을 정의하는 맵
	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"미체결": { label: "미체결" },
		"체결": { label: "체결" }
	};

	// 콤마 제거 후 정수만 추출하는 헬퍼 함수 (전역으로 정의하여 모든 함수에서 사용)
	window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;

	// ===============================
	// ✅ [추가] 컬럼 체크박스 제어 로직
	// ===============================
	function initColumnCheckboxControl(tableInstance) {
		const selectAll = document.getElementById('selectAllColumns');
		const checkboxes = document.querySelectorAll('.colCheckbox');

		if (!selectAll || checkboxes.length === 0) {
			console.warn("⚠️ 컬럼 체크박스를 찾을 수 없습니다.");
			return;
		}

		// 전체 선택 이벤트
		selectAll.addEventListener('change', function() {
			const checked = this.checked;
			checkboxes.forEach(cb => {
				cb.checked = checked;
				if (checked) tableInstance.showColumn(cb.value);
				else tableInstance.hideColumn(cb.value);
			});
			tableInstance.redraw(true);
		});

		// 개별 체크박스 이벤트
		checkboxes.forEach(cb => {
			cb.addEventListener('change', function() {
				if (this.checked) tableInstance.showColumn(this.value);
				else tableInstance.hideColumn(this.value);

				const allChecked = Array.from(checkboxes).every(c => c.checked);
				selectAll.checked = allChecked;
				tableInstance.redraw(true);
			});
		});
	}

	// ===============================
	// ✅ Tabulator 컬럼 및 기능 설정
	// ===============================

	function initTabFiltering() {
		const tabButtons = document.querySelectorAll('#estimateTab button');
		tabButtons.forEach(btn => {
			btn.addEventListener('click', function() {
				const type = this.dataset.type;
				tabButtons.forEach(b => {
					b.classList.remove('btn-primary');
					b.classList.add('btn-outline-primary');
				});
				this.classList.remove('btn-outline-primary');
				this.classList.add('btn-primary');
				applyFilter(type);
			});
		});
	}

	function applyFilter(type) {
		const table = window.estimateTableInstance;
		if (!table) return;

		const filterField = "진행상태";
		let filterValue = null;

		switch (type) {
			case 'ALL': table.clearFilter(); return;
			case 'UNCONFIRMED': filterValue = "미확인"; break;
			case 'IN_PROGRESS': filterValue = "진행중"; break;
			case 'UNSETTLED': filterValue = "미체결"; break;
			case 'SETTLED': filterValue = "체결"; break;
		}

		if (filterValue) table.setFilter(filterField, "=", filterValue);
	}

	window.updateStatusAPI = function(code, status, selectElement) {
		const row = window.estimateTableInstance.getRows().find(r => r.getData().견적서코드 === code);
		const currentStatus = row?.getData()?.진행상태;

		if (currentStatus === status) {
			if (selectElement) selectElement.value = currentStatus;
			return;
		}

		const url = "/api/updateEstimate";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;

		const data = { estimateCode: code, status: status };

		fetch(url, {
			method: "POST",
			headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
			body: JSON.stringify(data)
		})
			.then(res => res.ok ? res.json() : res.json().then(err => { throw new Error(err.message); }))
			.then(response => {
				if (response.success) {
					window.estimateTableInstance.getRows().find(r => r.getData().견적서코드 === code)?.update({ '진행상태': status });
				} else if (selectElement) selectElement.value = currentStatus;
			})
			.catch(err => {
				console.error("상태 변경 실패:", err);
				if (selectElement) selectElement.value = currentStatus;
			});
	};

	window.resetQuote = function() {
		const form = document.getElementById("quoteForm");
		if (form) form.reset();

		if (window.resetItemGrid) window.resetItemGrid();
		else {
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			if (window.addItemRow) window.addItemRow();
		}

		// ✅ 전역에 노출된 calculateTotal 사용
		if (typeof window.calculateTotal === 'function') {
			window.calculateTotal();
		}

		if (window.lastModalType === 'detail' && window.lastLoadedEstimateData)
			bindDataToForm(window.lastLoadedEstimateData, form);
	};

	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '견적서 상세정보' : '견적서 등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("quoteForm");

		window.lastLoadedEstimateData = null;
		window.lastModalType = null;
		window.resetQuote();

		document.getElementById("partnerName").readOnly = modalType !== 'regist';
		document.getElementById("partnerModalBtn").disabled = modalType !== 'regist';
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		if (modalType === 'detail' && keyword)
			loadDetailData('estimate', keyword, form).then(d => {
				window.lastLoadedEstimateData = d;
				window.lastModalType = modalType;
			});
		modal.show();
	};

	const modalEl = document.getElementById("newDetailModal");
	if (modalEl)
		modalEl.addEventListener('hidden.bs.modal', () => window.resetQuote());

	window.saveModal = function() {
		const form = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!form) return console.error("폼을 찾을 수 없습니다.");

		const formData = new FormData(form);
		const formObj = Object.fromEntries(formData.entries());
		const detailList = collectQuoteDetails();
		if (detailList.length === 0) return console.warn("견적 상세 없음");

		const payload = {
			partnerCode: formObj.partnerCode || '',
			deliveryDate: formObj.deliveryDate,
			validPeriod: parseInt(formObj.validPeriod) || 0,
			postCode: parseInt(formObj.postCode) || 0,
			address: formObj.address || '',
			payCondition: formObj.payCondition || '',
			remarks: formObj.remarks || '',
			manager: formObj.manager || '',
			detailList: detailList
		};

		fetch("/api/registEstimate", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(payload),
		})
			.then(res => res.ok ? res.json() : res.json().then(err => { throw new Error(err.message); }))
			.then(() => {
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();
				location.reload();
			})
			.catch(err => console.error("저장 실패:", err));
	};

	function collectQuoteDetails() {
		const detailList = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return detailList;

		tbody.querySelectorAll('tr').forEach(row => {
			if (row.getAttribute('data-row-id') === 'new') return;
			detailList.push({
				productCode: row.querySelector('input[name="itemCode"]').value || '',
				quantity: window.cleanValue(row.querySelector('input[name="quantity"]').value),
				price: window.cleanValue(row.querySelector('input[name="price"]').value),
				remarks: row.querySelector('input[name="remarks"]').value || '',
			});
		});
		return detailList;
	}

	let tabulatorColumns = [
		{
			title: "No",
			formatter: "rownum",
			hozAlign: "center",
			headerHozAlign: "center",
			headerSort: false,
			width: 50,
			frozen: true
		},
		...columns.map(col => {
			if (col === "상품규격" || col === "단위") return null;

			let def = { title: col, field: col, visible: defaultVisible.includes(col) };

			if (col === "견적서코드")
				def.formatter = cell => `<div style="cursor:pointer;color:blue;" onclick="showDetailModal('detail','${cell.getData().견적서고유코드}')">${cell.getValue()}</div>`;

			if (col === "견적금액합계") {
				def.formatter = cell => (isNaN(cell.getValue()) ? "-" : cell.getValue().toLocaleString('ko-KR') + " 원");
				def.sorter = "number";
				def.hozAlign = "right";
			}

			if (col === "비고") {
				def.hozAlign = "left";
			}

			if (col === "진행상태")
				def.formatter = cell => {
					const val = cell.getValue();
					const code = cell.getData().견적서코드;
					const options = Object.keys(STATUS_MAP).map(k => {
						const sel = k === val ? 'selected' : '';
						return `<option value="${k}" ${sel}>${STATUS_MAP[k].label}</option>`;
					}).join('');
					return `<select class="form-select form-select-sm"
						onchange="updateStatusAPI('${code}', this.value, this)"
						style="font-size:0.75rem;min-width:90px;">${options}</select>`;
				};

			return def;
		}).filter(Boolean)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.estimateTableInstance = tableInstance;

	// ✅ 체크박스 제어 연결
	initColumnCheckboxControl(tableInstance);

	initTabFiltering();

	// ===============================
	// ✅ 검색 및 초기화
	// ===============================
	function loadTableData(params = {}) {
		const url = `/api/estimate/search?${new URLSearchParams(params).toString()}`;
		fetch(url)
			.then(res => res.ok ? res.json() : Promise.reject(res))
			.then(data => window.estimateTableInstance.setData(data))
			.catch(err => {
				console.error("데이터 로딩 실패:", err);
				window.estimateTableInstance.setData([]);
			});
	}

	window.filterSearch = function() {
		const t = window.estimateTableInstance;
		if (!t) return;

		const partner = document.getElementById('partnerNameSearch')?.value || '';
		const manager = document.getElementById('managerSearch')?.value || '';
		const start = document.getElementById('quoteDateSearch1')?.value.trim() || '';
		const end = document.getElementById('quoteDateSearch2')?.value.trim() || '';

		t.clearFilter(true);
		t.setFilter((data) => {
			if (start || end) {
				const d = new Date(data['등록일자']);
				if ((start && d < new Date(start)) || (end && d > new Date(end))) return false;
			}
			if (manager && !String(data['담당자'] || '').includes(manager)) return false;
			if (partner && !String(data['거래처명'] || '').includes(partner)) return false;
			return true;
		});
	};

	window.resetSearch = function() {
		document.querySelectorAll('.searchTool input[type=text], .searchTool select').forEach(el => {
			if (el.tagName === 'SELECT' && el.choicesInstance) el.choicesInstance.setChoiceByValue('');
			else el.value = '';
		});
		loadTableData({});
	};
});


// ======================================================
// ✅ [추가] 누락된 계산 함수들 정의 + 전역 등록
// ======================================================
function calculateRow(inputElement) {
	const row = inputElement.closest('tr');
	if (!row) return;

	const cleanValue = window.cleanValue;

	// 1) 입력값 포맷
	const currentValue = cleanValue(inputElement.value);
	inputElement.value = currentValue.toLocaleString('ko-KR');

	// 2) 요소 참조
	const quantityInput = row.querySelector('input[name="quantity"]');
	const unitPriceInput = row.querySelector('input[name="price"]');
	const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
	const taxAmountInput = row.querySelector('input[name="taxAmount"]');
	const finalAmountInput = row.querySelector('input[name="finalAmount"]');

	if (!quantityInput || !unitPriceInput || !supplyAmountInput || !taxAmountInput || !finalAmountInput) return;

	// 3) 계산
	const quantity = cleanValue(quantityInput.value);
	const unitPrice = cleanValue(unitPriceInput.value);
	const supplyAmount = quantity * unitPrice;
	const taxAmount = Math.floor(supplyAmount * 0.1);
	const finalAmount = supplyAmount + taxAmount;

	// 4) 표시
	supplyAmountInput.value = supplyAmount.toLocaleString('ko-KR');
	taxAmountInput.value = taxAmount.toLocaleString('ko-KR');
	finalAmountInput.value = finalAmount.toLocaleString('ko-KR');

	// 5) 합계 갱신
	if (typeof window.calculateTotal === 'function') {
		window.calculateTotal();
	}
}

function calculateTotal() {
	let totalQuantity = 0;
	let totalSupplyAmount = 0;
	let totalTaxAmount = 0;

	const tbody = document.getElementById('itemDetailBody');
	if (!tbody) return;

	tbody.querySelectorAll('tr').forEach(row => {
		if (row.getAttribute('data-row-id') === 'new') return;

		const quantityInput = row.querySelector('input[name="quantity"]');
		const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
		const taxAmountInput = row.querySelector('input[name="taxAmount"]');
		if (!quantityInput || !supplyAmountInput || !taxAmountInput) return;

		const cleanValue = window.cleanValue;
		const quantity = cleanValue(quantityInput.value);
		const supplyAmount = cleanValue(supplyAmountInput.value);
		const taxAmount = cleanValue(taxAmountInput.value);

		totalQuantity += quantity;
		totalSupplyAmount += supplyAmount;
		totalTaxAmount += taxAmount;
	});

	// 합계 표시
	const totalQtyEl = document.getElementById('totalQuantity');
	const totalSupplyEl = document.getElementById('totalSupplyAmount');
	const totalTaxEl = document.getElementById('totalTaxAmount');
	const totalAmountEl = document.getElementById('totalAmount');

	if (totalQtyEl) totalQtyEl.textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	if (totalSupplyEl) totalSupplyEl.textContent = totalSupplyAmount.toLocaleString('ko-KR') + ' 원';
	if (totalTaxEl) totalTaxEl.textContent = totalTaxAmount.toLocaleString('ko-KR') + ' 원';

	const totalAmount = totalSupplyAmount + totalTaxAmount;
	if (totalAmountEl) totalAmountEl.textContent = totalAmount.toLocaleString('ko-KR') + ' 원';
}

// ✅ 전역 등록 (resetQuote, oninput 등에서 window.*로 접근 가능하게)
window.calculateRow = calculateRow;
window.calculateTotal = calculateTotal;
