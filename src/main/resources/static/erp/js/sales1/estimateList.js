document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["견적서코드", "등록일자", "거래처명", "품목명", "유효기간", "견적금액합계", "담당자", "진행상태"];

	// 진행 상태에 따른 스타일을 정의하는 맵
	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"미체결": { label: "미체결" }
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


	// ✅ [완성형 priceDiscount 함수 - 전체 코드 복사 사용 가능]
	window.priceDiscount = async function() {
		try {
			const partnerCode = document.getElementById("partnerCodeModal")?.value || '';
			const detailList = collectQuoteDetails();

			// ✅ 품목코드 목록 추출
			const productCodeList = detailList
				.map(i => i.productCode)
				.filter(c => c && c.trim() !== "");

			// ✅ 유효성 검사
			if (!partnerCode) {
				alert("거래처 코드가 선택되지 않았습니다.");
				return;
			}
			if (productCodeList.length === 0) {
				alert("품목 코드가 없습니다.");
				return;
			}

			// ✅ 요청 데이터
			const payload = { partnerCode, productCodes: productCodeList };
			const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
			const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

			// ✅ 로딩 오버레이 표시
			if (typeof showTableLoading === "function") showTableLoading(true);

			// ✅ 서버 호출
			const res = await fetch("/api/price/findApplicablePriceGroup", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					[csrfHeader]: csrfToken
				},
				body: JSON.stringify(payload),
			});

			if (!res.ok) throw new Error(`서버 오류: ${res.status}`);

			const result = await res.json();
			console.log("📦 서버 응답 결과:", result);

			// ✅ 결과 없을 때
			if (!Array.isArray(result) || result.length === 0) {
				alert("적용 가능한 단가그룹이 없습니다.");
				return;
			}

			// ==========================
			// 🔹 [1] 단가 유형별 할인율 구분
			// ==========================
			let partnerDiscountRate = 0;
			const productDiscountMap = {};

			result.forEach(p => {
				const rate = parseFloat(p.discountPct || 0);
				if (p.priceType === "거래처단가") {
					// 거래처단가 중 가장 큰 할인율 적용
					partnerDiscountRate = Math.max(partnerDiscountRate, rate);
				} else if (p.priceType === "품목단가" && p.productCode) {
					// 품목단가 중 동일 품목에 대해 가장 큰 할인율 적용
					const prev = productDiscountMap[p.productCode] || 0;
					productDiscountMap[p.productCode] = Math.max(prev, rate);
				}
			});

			console.log("✅ 거래처 할인율:", partnerDiscountRate);
			console.log("✅ 품목별 할인율 맵:", productDiscountMap);

			// ==========================
			// 🔹 [2] 품목별 할인 적용
			// ==========================
			const rows = document.querySelectorAll("#itemDetailBody tr");
			rows.forEach(row => {
				const code = row.querySelector('input[name="itemCode"]')?.value;
				if (!code) return;

				const supplyInput = row.querySelector('input[name="supplyAmount"]');
				const taxInput = row.querySelector('input[name="taxAmount"]');
				const discountInput = row.querySelector('input[name="discountAmount"]');
				const finalInput = row.querySelector('input[name="finalAmount"]');

				if (!supplyInput || !taxInput || !discountInput || !finalInput) return;

				const supply = cleanValue(supplyInput.value);
				const tax = cleanValue(taxInput.value);
				const discountRate = productDiscountMap[code] || 0;

				const discountValue = Math.floor((supply + tax) * discountRate);
				const finalAmount = (supply + tax) - discountValue;

				discountInput.value = discountValue.toLocaleString('ko-KR');
				finalInput.value = finalAmount.toLocaleString('ko-KR');
			});

			// ==========================
			// 🔹 [3] 합계 재계산 및 거래처 할인 적용
			// ==========================
			calculateTotal(partnerDiscountRate);

			alert(`적용 가능한 단가 ${result.length}건을 반영했습니다.`);

		} catch (err) {
			console.error("❌ 단가그룹 조회 중 오류 발생:", err);
			alert("단가그룹 조회 중 오류가 발생했습니다.");
		} finally {
			if (typeof showTableLoading === "function") showTableLoading(false);
		}
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

		// ✅ detail 모드일 때만 오버레이 표시 + 로딩
		if (modalType === 'detail' && keyword) {
			showTableLoading(true); // 모달 내부 오버레이 표시

			loadDetailData('estimate', keyword, form)
				.then(d => {
					window.lastLoadedEstimateData = d;
					window.lastModalType = modalType;
				})
				.catch(err => console.error('견적서 상세 로딩 실패:', err))
				.finally(() => {
					showTableLoading(false); // 로딩 종료 후 오버레이 제거
				});
		}

		modal.show();
	};


	const modalEl = document.getElementById("newDetailModal");
	if (modalEl)
		modalEl.addEventListener('hidden.bs.modal', () => window.resetQuote());

	window.saveModal = function() {
		const form = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");

		// ✅ td의 텍스트에서 숫자만 추출
		const partnerDiscountText = document.getElementById("partnerDiscountAmount")?.textContent || "0";
		const partnerDiscountAmount = window.cleanValue(partnerDiscountText); // "1,200 원" → 1200

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
			partnerDiscountAmount: partnerDiscountAmount, // ✅ 숫자값
			detailList: detailList
		};

		console.log("✅ 저장 payload:", payload); // 확인용 로그

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
				alert("견적서 등록이 완료되었습니다."); // ✅ 성공 알림
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide(); // ✅ 모달 닫기
				location.reload(); // ✅ 새로고침
			})
			.catch(err => {
				console.error("저장 실패:", err);
				alert("견적서 등록 중 오류가 발생했습니다.\n자세한 내용은 콘솔을 확인하세요.");
			});
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
				discountAmount: window.cleanValue(row.querySelector('input[name="discountAmount"]').value),

			});
		});
		return detailList;
	}






	// ===============================
	// ✅ [공통] 로딩 오버레이 표시 함수
	// ===============================
	function showTableLoading(show = true) {
		const modalContent = document.querySelector("#newDetailModal .modal-content");
		if (!modalContent) return;

		let overlay = modalContent.querySelector(".modal-loading-overlay");

		// 처음 호출 시 오버레이 요소 생성
		if (!overlay) {
			overlay = document.createElement("div");
			overlay.className = "modal-loading-overlay";
			Object.assign(overlay.style, {
				position: "absolute",
				top: "0",
				left: "0",
				width: "100%",
				height: "100%",
				backgroundColor: "rgba(255, 255, 255, 0.7)",
				display: "flex",
				alignItems: "center",
				justifyContent: "center",
				zIndex: "1056", // 모달 내용 위
				borderRadius: "0.3rem", // Bootstrap 모달 모서리와 일치
			});
			overlay.innerHTML = `
				<div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
					<span class="visually-hidden">Loading...</span>
				</div>
				<span class="ms-3 fw-bold">데이터 로딩 중...</span>
			`;

			// 부모인 modal-content가 relative가 아니라면 position 설정
			const computedStyle = window.getComputedStyle(modalContent);
			if (computedStyle.position === "static") {
				modalContent.style.position = "relative";
			}

			modalContent.appendChild(overlay);
		}

		overlay.style.display = show ? "flex" : "none";
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

					if (val === "체결") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${val}" readonly
							style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}

					if (val === "미체결") {
						return `<input type="text" class="form-control form-control-sm text-center bg-light"
							value="${val}" readonly
							style="font-size:0.75rem; height:auto; min-width:90px; cursor:no-drop;">`;
					}


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





// ✅ 전역 등록 (resetQuote, oninput 등에서 window.*로 접근 가능하게)
window.calculateRow = calculateRow;
window.calculateTotal = calculateTotal;
