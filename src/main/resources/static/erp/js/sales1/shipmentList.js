window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;

document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["출하지시서코드", "출하예정일자", "거래처명", "창고명", "품목명", "수량합계", "담당자", "진행상태"];

	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"출하중": { label: "출하중" },
		"출하완료": { label: "출하완료" },
	};

	function initTabFiltering() {
		const tabButtons = document.querySelectorAll('#shipmentTab button');
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
		const table = window.shipmentTableInstance;
		if (!table) {
			console.error("Tabulator instance is not initialized.");
			return;
		}
		const filterField = "진행상태";
		let filterValue = null;
		switch (type) {
			case 'ALL': table.clearFilter(); return;
			case 'NONCHECK': filterValue = "미확인"; break;
			case 'ONSHIPMENT': filterValue = "출하중"; break;
			case 'SUCCESSHIPMENT': filterValue = "출하완료"; break;
			case 'SUCCESSAC': filterValue = "판매완료"; break;
			default: return;
		}
		if (filterValue) table.setFilter(filterField, "=", filterValue);
	}

	window.selectOrders = function() {
		const partnerCode = document.getElementById('partnerCodeModal').value;
		const form = document.getElementById('quoteForm');
		if (!partnerCode) { alert("먼저 거래처를 선택하세요."); return; }
		openSalesModal(function(selected) {
			document.getElementById('postCode').value = selected.postCode || '';
			document.getElementById('address').value = selected.address || '';
			const orderUniqueCode = selected.orderUniqueCode;
			loadDetailData('orders', orderUniqueCode, form)
				.then(responseData => { window.lastLoadedOrderData = responseData; })
				.catch(err => { console.error("주문서 상세 불러오기 실패:", err); alert("주문서 정보를 불러오지 못했습니다."); });
		}, partnerCode);
	};

	window.updateStatusAPI = function(code, status, selectElement) {
		const row = window.shipmentTableInstance.getRows().find(r => r.getData().출하지시서코드 === code);
		const currentStatus = row?.getData()?.진행상태;
		if (currentStatus === status) {
			if (selectElement) selectElement.value = currentStatus;
			return;
		}
		const url = "/api/updateShipment";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;
		const data = { shipmentCode: code, status: status };
		fetch(url, {
			method: "POST",
			headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
			body: JSON.stringify(data)
		})
			.then(res => {
				if (!res.ok) return res.json().then(error => { throw new Error(error.message || `서버 오류 발생: ${res.status}`); });
				return res.json();
			})
			.then(response => {
				if (response.success) {
					window.shipmentTableInstance.getRows().find(r => r.getData().출하지시서코드 === code)?.update({ '진행상태': status });
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

	window.resetQuote = function() {
		const form = document.getElementById("quoteForm");
		if (form) form.reset();
		if (window.resetItemGrid) window.resetItemGrid();
		else {
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			window.addItemRow();
		}
		if (window.calculateTotal) window.calculateTotal();
	};

	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '출하지시서 상세정보' : '출하지시서 등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("quoteForm");
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;
		window.lastLoadedEstimateData = null;
		window.lastModalType = null;
		form.reset();
		const commonCodePromises = [loadCommonCode('GRP003', 'warehouse', '창고')];
		modal.show();
		if (modalType === 'detail' && keyword) {
			Promise.all(commonCodePromises)
				.then(() => { loadDetailData('shipment', keyword, form); })
				.catch(err => { console.error("공통 코드 로딩 오류:", err); alert("필수 데이터 로딩 오류"); });
		} else resetItemGrid();
	};

	window.insertSales = async function() {
		const table = window.shipmentTableInstance;
		if (!table) return alert("테이블이 아직 준비되지 않았습니다.");
		const selectedRows = table.getSelectedData();
		if (selectedRows.length === 0) return alert("판매완료로 변경할 출하지시서를 선택하세요.");
		const eligibleRows = selectedRows.filter(row => row.진행상태 === "출하완료");
		if (eligibleRows.length === 0) return alert("‘출하완료’ 상태만 판매완료로 변경할 수 있습니다.");
		if (!confirm(`선택된 ${eligibleRows.length}건을 '판매완료'로 변경하시겠습니까?`)) return;
		try {
			const updatePayload = eligibleRows.map(row => ({ shipmentCode: row.출하지시서코드, status: "판매완료" }));
			const res = await fetch("/api/updateShipmentStatus", {
				method: "POST",
				headers: { "Content-Type": "application/json", [document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content },
				body: JSON.stringify(updatePayload)
			});
			if (!res.ok) throw new Error("서버 요청 실패");
			await res.json();
			alert("판매완료로 상태가 변경되었습니다.");
			eligibleRows.forEach(row => {
				const rowComponent = table.getRows().find(r => r.getData().출하지시서코드 === row.출하지시서코드);
				if (rowComponent) rowComponent.update({ 진행상태: "판매완료" });
			});
			table.deselectRow();
			table.redraw(true);
		} catch (err) {
			console.error("판매완료 변경 오류:", err);
			alert("판매완료 처리 중 오류 발생.");
		}
	};
	
	
	
	window.showTabulatorOverlay = function(show = true) {
		const tableContainer = document.querySelector("#shipmentTable, #quoteTable, #commonModalTable");
		if (!tableContainer) return;

		let overlay = document.getElementById("tabulator-loading-overlay");

		if (!overlay) {
			overlay = document.createElement("div");
			overlay.id = "tabulator-loading-overlay";
			Object.assign(overlay.style, {
				position: "absolute",
				top: 0,
				left: 0,
				width: "100%",
				height: "100%",
				backgroundColor: "rgba(255,255,255,0.7)",
				display: "flex",
				alignItems: "center",
				justifyContent: "center",
				zIndex: 2000,
			});
			overlay.innerHTML = `
				<div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
					<span class="visually-hidden">Loading...</span>
				</div>
				<span class="ms-3 fw-bold">데이터 갱신 중...</span>
			`;

			// 부모 요소 position 보정
			const parent = tableContainer.parentElement;
			const style = window.getComputedStyle(parent);
			if (style.position === "static") parent.style.position = "relative";

			parent.appendChild(overlay);
		}

		overlay.style.display = show ? "flex" : "none";
	}

	
	

	window.saveModal = function () {
		const quoteForm = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!quoteForm) return alert("출하 등록 폼을 찾을 수 없습니다.");
		if (!window.checkRequired(quoteForm)) return;

		const detailList = collectShipmentDetails();
		if (detailList.length === 0) return alert("출하 상세 항목을 입력해주세요.");

		const formData = new FormData(quoteForm);
		const formObj = Object.fromEntries(formData.entries());

		const finalPayload = {
			deliveryDate: formObj.deliveryDateText || null,
			partnerCode: formObj.partnerCode || '',
			partnerName: formObj.partnerName || '',
			warehouse: formObj.warehouse || null,
			manager: formObj.empCode || '',
			postCode: formObj.postCode || null,
			address: formObj.address || '',
			remarks: formObj.remarks || '',
			status: '미확인',
			detailList: detailList
		};

		fetch("/api/registShipment", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload)
		})
			.then(res => {
				if (!res.ok)
					return res.json().then(error => {
						throw new Error(error.message || `서버 오류 발생: ${res.status}`);
					});
				return res.json();
			})
			.then(data => {
				alert("출하 지시 등록 완료. 코드: " + data.id);

				// ✅ 모달 닫기
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();

				// ✅ 타뷸레이터 로딩 오버레이 표시
				showTabulatorOverlay(true);

				// ✅ 1초 뒤 새로고침 (UX 안정적)
				setTimeout(() => {
					location.reload();
				}, 1000);
			})
			.catch(err => {
				console.error("출하 등록 실패:", err);
				alert(`등록 실패: ${err.message}`);
			});
	};


	window.validateNowQuantity = function(input) {
		const row = input.closest("tr");
		if (!row) return;
		const stock = parseFloat(row.querySelector("[name='stock']")?.value || 0);
		const nonShipment = parseFloat(row.querySelector("[name='nonShipment']")?.value || 0);
		const entered = parseFloat(input.value || 0);
		if (entered > stock || entered > nonShipment) {
			showToast("재고, 미지시수량을 초과합니다.");
			input.value = Math.min(stock, nonShipment);
		}
	}

	window.showToast = function(message) {
		const existingToast = document.getElementById("toastMessage");
		if (existingToast) existingToast.remove();
		const toast = document.createElement("div");
		toast.id = "toastMessage";
		toast.textContent = message;
		Object.assign(toast.style, {
			position: "fixed", bottom: "40px", left: "50%", transform: "translateX(-50%)",
			background: "rgba(0, 0, 0, 0.8)", color: "white", padding: "10px 20px",
			borderRadius: "8px", fontSize: "14px", zIndex: "9999", transition: "opacity 0.5s ease"
		});
		document.body.appendChild(toast);
		setTimeout(() => { toast.style.opacity = "0"; setTimeout(() => toast.remove(), 500); }, 1500);
	}

	function collectShipmentDetails() {
		const detailList = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return detailList;
		const cleanValue = window.cleanValue;
		let isRowValid = true;
		tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {
			const nowQtyInput = row.querySelector('input[name="nowQuantity"]');
			const nowQuantity = cleanValue(nowQtyInput?.value || 0);
			if (!nowQuantity || nowQuantity <= 0) return;
			const validationResult = window.checkRowRequired(row);
			if (!validationResult.isValid) {
				alert(`[${row.rowIndex}번째 행] ${validationResult.missingFieldName}은 필수입니다.`);
				isRowValid = false;
				return;
			}
			const productCode = row.querySelector('input[name="productCode"]')?.value || '';
			const productName = row.querySelector('input[name="productName"]')?.value || '';
			const stock = cleanValue(row.querySelector('input[name="stock"]')?.value || 0);
			const remarks = row.querySelector('input[name="remarks"]')?.value || '';
			const orderDetailCode = row.querySelector('input[name="orderDetailCode"]')?.value || '';
			const totalQuantity = cleanValue(row.querySelector('input[name="quantity"]')?.value || 0);
			const nonShipment = totalQuantity - nowQuantity;
			detailList.push({ orderDetailCode, productCode, productName, nowQuantity, nonShipment, stock, remarks });
		});
		if (!isRowValid) return [];
		return detailList;
	}

	window.calculateTotal = function() {
		let totalQuantity = 0;
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return;
		tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {
			const quantityInput = row.querySelector('input[name="quantity"]');
			if (!quantityInput) return;
			const quantity = window.cleanValue(quantityInput.value);
			totalQuantity += quantity;
		});
		const totalQtyElement = document.getElementById('totalQuantity');
		if (totalQtyElement) totalQtyElement.textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	}

	let tabulatorColumns = [
		{
			formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center",
			headerHozAlign: "center", headerSort: false, width: 50, frozen: true
		},
		...columns.map(col => {
			let columnDef = { title: col, field: col, visible: defaultVisible.includes(col) };
			if (col === "진행상태") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const code = cell.getData().출하지시서코드;
					if (value === "판매완료") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${value}" readonly style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}
					
					
					
					if (value === "출하완료") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${value}" readonly style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}
					
					const options = Object.keys(STATUS_MAP).map(key => {
						const itemInfo = STATUS_MAP[key];
						const isSelected = key === value ? 'selected' : '';
						return `<option value="${key}" ${isSelected}>${itemInfo.label}</option>`;
					}).join('');
					return `<select class="form-select form-select-sm" onchange="updateStatusAPI('${code}', this.value, this)"
						style="font-size:0.75rem; padding:0.25rem 0.5rem; height:auto; min-width:90px;">${options}</select>`;
				};
			}
			
			
			if (col === "비고") {
				columnDef.hozAlign = "left";
			}
			
			if (col === "수량합계") {
				columnDef.title = col + " (개)";
				columnDef.formatter = function(cell) {
					const v = cell.getValue();
					if (v == null || isNaN(v)) return "-";
					return Number(v).toLocaleString('ko-KR');
				};
				columnDef.sorter = "number"; columnDef.hozAlign = "right";
			}
			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = window.makeTabulator(rows, tabulatorColumns);
	window.shipmentTableInstance = tableInstance;
	initTabFiltering();

	// ✅ 컬럼 체크박스 제어 (PRODUCTLIST.js 스타일)
	const selectAll = document.getElementById('selectAllColumns');
	const columnCheckboxes = document.querySelectorAll('.colCheckbox');
	if (selectAll && columnCheckboxes.length > 0) {
		selectAll.addEventListener('change', function() {
			const checked = this.checked;
			columnCheckboxes.forEach(chk => {
				chk.checked = checked;
				if (checked) tableInstance.showColumn(chk.value);
				else tableInstance.hideColumn(chk.value);
				tableInstance.redraw(true);
			});
		});
		columnCheckboxes.forEach(chk => {
			chk.addEventListener('change', function() {
				if (this.checked) tableInstance.showColumn(this.value);
				else tableInstance.hideColumn(this.value);
				selectAll.checked = Array.from(columnCheckboxes).every(c => c.checked);
				tableInstance.redraw(true);
			});
		});
	}

	function loadTableData(params = {}) {
		const queryString = new URLSearchParams(params).toString();
		const url = `/api/shipment/search?${queryString}`;

		fetch(url)
			.then(response => {
				if (!response.ok) throw new Error('데이터 요청 실패: ' + response.statusText);
				return response.json();
			})
			.then(data => {
				console.log("검색 결과 데이터:", data);
				if (window.shipmentTableInstance) window.shipmentTableInstance.setData(data);
			})
			.catch(error => {
				console.error('데이터 로딩 중 오류 발생:', error);
				alert('데이터를 가져오는 데 실패했습니다.');
				if (window.shipmentTableInstance) window.shipmentTableInstance.setData([]);
			});
	}

	// 검색 버튼
	window.filterSearch = function() {
		const clientName = document.getElementById("partnerNameSearch").value.trim();
		const shipmentNo = document.getElementById("shipmentCode").value.trim();
		const warehouseCode = document.getElementById("warehouseCodeSearch").value.trim();

		const filters = [];
		if (clientName) filters.push({ field: "거래처명", type: "like", value: clientName });
		if (shipmentNo) filters.push({ field: "출하지시서코드", type: "like", value: shipmentNo });
		if (warehouseCode) filters.push({ field: "창고명", type: "like", value: warehouseCode });

		const table = window.shipmentTableInstance;
		if (table && typeof table.setFilter === "function") {
			table.clearFilter();
			table.setFilter(filters);
			console.log("✅ 클라이언트 필터 적용 완료:", filters);
		} else {
			console.error("❌ shipmentTable이 초기화되지 않았거나 Tabulator 인스턴스가 아닙니다.", table);
			alert("테이블이 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
		}
	};

	// 초기화 버튼
	window.resetSearch = function() {
		const searchTool = document.querySelector('.searchTool');
		searchTool.querySelectorAll('input[type=text], select').forEach(el => {
			if (el.tagName === 'SELECT' && el.choicesInstance) el.choicesInstance.setChoiceByValue('');
			else el.value = '';
		});
		loadTableData({});
	};

}); // end of DOMContentLoaded

function calculateRow(inputElement) {
	if (window.cleanValue) {
		const cleanValue = window.cleanValue;
		const currentValue = cleanValue(inputElement.value);
		inputElement.value = currentValue.toLocaleString('ko-KR');
		if (window.calculateTotal) window.calculateTotal();
	}
}


document.addEventListener("keydown", function (e) {
	// Enter 키만 감지
	if (e.key === "Enter") {
		const active = document.activeElement;

		// 현재 포커스된 요소가 '현지시수량' input일 때만 실행
		if (active && active.name === "nowQuantity") {
			e.preventDefault(); // Enter 기본 동작(폼 제출 등) 방지

			// 모든 현지시수량 input 가져오기
			const allInputs = Array.from(document.querySelectorAll('input[name="nowQuantity"]'));
			const currentIndex = allInputs.indexOf(active);

			// 다음 인풋으로 포커스 이동
			if (currentIndex >= 0 && currentIndex < allInputs.length - 1) {
				allInputs[currentIndex + 1].focus();
				allInputs[currentIndex + 1].select(); // 기존 값 선택 상태로 (입력 편의)
			} else {
				// 마지막 항목일 경우 — 선택 해제 or 경고음 등 선택 가능
				console.log("마지막 현지시수량 입력 필드입니다.");
			}
		}
	}
});
