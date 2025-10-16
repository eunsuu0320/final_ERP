document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["주문서코드", "등록일자", "거래처명", "담당자", "품목명", "납기일자", "주문금액합계", "진행상태"];

	const STATUS_MAP = {
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"출하지시완료": { label: "출하지시완료" },
		"완료": { label: "완료" }
	};

	window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;


	function initTabFiltering() {

		const tabButtons = document.querySelectorAll('#ordersTab button');

		tabButtons.forEach(btn => {
			btn.addEventListener('click', function() {
				const type = this.dataset.type;

				// 1. 버튼 스타일 변경 (전환)
				tabButtons.forEach(b => {
					b.classList.remove('btn-primary');
					b.classList.add('btn-outline-primary');
				});
				this.classList.remove('btn-outline-primary');
				this.classList.add('btn-primary');

				// 2. Tabulator 필터링 적용
				applyFilter(type);
			});
		});
	}


	function applyFilter(type) {
		// 전역으로 저장된 Tabulator 인스턴스를 가져옵니다.
		const table = window.orderTableInstance;
		if (!table) {
			console.error("Tabulator instance is not initialized.");
			return;
		}

		// '진행상태'에 해당하는 필드 이름은 '진행상태' 문자열 자체를 사용합니다.
		const filterField = "진행상태";
		let filterValue = null;

		// HTML 탭 타입(data-type)과 서버 데이터 값(DB/VO 값)을 매핑
		switch (type) {
			case 'ALL':
				// 'ALL' 탭은 모든 필터를 지웁니다.
				table.clearFilter();
				return;
			case 'NONCHECK':
				console.log("미확인");
				filterValue = "미확인";
				break;
			case 'ONGOING':
				filterValue = "진행중";
				break;
			case 'SHIPMENTSUCCESS':
				filterValue = "출하지시완료";
				break;
			case 'SUCCESS':
				filterValue = "완료";
				break;
			default:
				return;
		}

		// 필터 적용: setFilter(필드 이름, 비교 연산자, 값)
		if (filterValue) {
			table.setFilter(filterField, "=", filterValue);
		}
	}




	// 폼 전체 초기화
	window.resetOrder = function() {
		const form = document.getElementById("orderForm");
		if (form) {
			form.reset();
		}

		// 상세 테이블 초기화 (salesCommon.js에 addItemRow 등이 정의되어 있다고 가정)
		if (window.resetItemGrid) {
			window.resetItemGrid();
		} else {
			// 상세 테이블 내용만 수동 초기화 (예시)
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			window.addItemRow(); // 기본 행 1개 추가 (salesCommon.js에 정의되어야 함)
			window.calculateTotal(); // 합계 초기화
		}

		// 납기일자 초기화
		const deliveryDateInput = document.getElementById('quoteDeliveryDateText');
		if (deliveryDateInput) deliveryDateInput.value = '';

		// 견적서코드 초기화 (추가)
		const estimateUniqueCodeInput = document.getElementById('estimateUniqueCode');
		if (estimateUniqueCodeInput) estimateUniqueCodeInput.value = '';


		console.log("주문 모달 전체 초기화 완료.");
	}

	// 주문서 상세모달 열기
	window.showDetailModal = function(modalType) {
		let modalName = '';

		// 모달 열기
		if (modalType === 'detail') {
			modalName = '주문상세정보'

		} else if (modalType === 'regist') {
			modalName = '주문등록'
			resetOrder(); // 신규 등록 시 폼 초기화
		}
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;
	};


	// --------------------------------------------------------------------------------
	// 2. 주문 등록/수정 (저장) 로직
	// --------------------------------------------------------------------------------

	window.saveModal = function() {
		const orderForm = document.getElementById("orderForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!orderForm) {
			alert("저장 오류: 주문서 등록 폼을 찾을 수 없습니다.");
			return;
		}

		// 1. 주문 기본 정보 수집
		const orderData = new FormData(orderForm);
		const orderDataObject = Object.fromEntries(orderData.entries());

		// HTML ID 'quotePartnerName'을 사용하여 거래처 이름 가져오기 (HTML ID: quotePartnerName)
		const partnerNameValue = document.getElementById('quotePartnerName').value;

		// 납기일자 (HTML name: deliveryDateText)
		const deliveryDateValue = orderDataObject.deliveryDateText;

		// 견적서코드 (HTML name: estimateUniqueCode)
		const estimateUniqueCodeValue = orderDataObject.estimateUniqueCode; // 문자열로 가져옴

		// 2. 주문 상세 정보 (OrderDetail 엔티티 리스트) 수집
		const detailList = collectOrderDetails();

		if (detailList.length === 0) {
			alert("주문 상세 내용을 1개 이상 입력해주세요.");
			return;
		}

		// ********** DTO 구조에 맞게 최종 페이로드 구성 **********
		const finalPayload = {
			// OrdersRegistrationDTO의 기본 필드
			partnerCode: orderDataObject.partnerCode || '', // Hidden input 필드 name="partnerCode"
			partnerName: partnerNameValue,

			// DTO의 필드명과 일치하도록 매핑: HTML name을 DTO 필드명으로 변경
			orderDate: orderDataObject.quoteDate,           // DTO: orderDate, HTML name: quoteDate
			deliveryDate: deliveryDateValue,                // DTO: deliveryDate, HTML name: deliveryDateText

			// ⭐⭐⭐ [수정 사항] estimateUniqueCode를 숫자로 변환하여 전송
			estimateUniqueCode: parseInt(estimateUniqueCodeValue) || null,

			manager: orderDataObject.manager || '',
			remarks: orderDataObject.remarks || '',

			// DTO의 List<OrderDetail> 필드
			detailList: detailList
		};

		console.log("전송할 최종 주문 데이터:", finalPayload);

		// 4. 서버에 API 호출 (Controller의 @PostMapping("api/registOrders") 경로 사용)
		fetch("/api/registOrders", { // <- 수정된 주문 API 경로
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload),
		})
			.then(res => {
				if (!res.ok) {
					// 오류 메시지를 포함하여 JSON 응답을 파싱
					return res.json().then(error => {
						throw new Error(error.message || `서버 오류 발생: ${res.status}`);
					});
				}
				return res.json();
			})
			.then(data => {
				console.log("서버 응답 데이터:", data);
				alert("주문서가 성공적으로 등록되었습니다. ID: " + data.id);

				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();
				// 등록 성공 후 테이블 데이터 리로드 로직 (필요 시)
				if (window.orderTableInstance && window.orderTableInstance.loadData) { // ⭐⭐ 변수명 변경 반영
					window.orderTableInstance.loadData("/api/orders");
				} else {
					window.location.reload();
				}
			})
			.catch(err => {
				console.error("주문서 등록 실패:", err);
				alert(`등록에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요. 오류: ${err.message}`)
			});
	};


	/**
	 * 주문 상세 테이블의 데이터 행들을 순회하며 OrderDetail 엔티티 구조에 맞춰 수집합니다.
	 */
	function collectOrderDetails() {
		const detailList = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return detailList;

		// 템플릿 행(data-row-id="new")을 제외한 모든 데이터 행 순회
		tbody.querySelectorAll('tr').forEach(row => {
			if (row.getAttribute('data-row-id') === 'new') {
				return;
			}

			const supplyAmount = window.cleanValue(row.querySelector('input[name="supplyAmount"]').value);
			const taxAmount = window.cleanValue(row.querySelector('input[name="taxAmount"]').value);

			// 공급가액이 0보다 큰 경우에만 VAT 비율을 계산
			const pctVat = (supplyAmount > 0) ? (taxAmount / supplyAmount) * 100 : 0;

			// OrderDetail DTO 구조에 맞춰 데이터 구성 (price, quantity, amountSupply, pctVat)
			detailList.push({
				productCode: row.querySelector('input[name="productCode"]').value || '',
				quantity: window.cleanValue(row.querySelector('input[name="quantity"]').value),
				price: window.cleanValue(row.querySelector('input[name="price"]').value),

				amountSupply: supplyAmount,
				pctVat: Math.round(pctVat * 100) / 100, // 소수점 2자리로 반올림하여 전송

				remarks: row.querySelector('input[name="remarks"]').value || '',
			});
		});

		return detailList;
	}


	// --------------------------------------------------------------------------------
	// 3. Tabulator 테이블 설정
	// --------------------------------------------------------------------------------

	// 주문서리스트 테이블 컬럼에 대한 정의
	let tabulatorColumns = [
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection",
			hozAlign: "center",
			headerHozAlign: "center",
			headerSort: false,
			width: 50,
			frozen: true
		},
		...columns.map(col => {
			if (col === "상품규격" || col === "단위") return null;

			let columnDef = {
				title: col,
				field: col,
				visible: defaultVisible.includes(col)
			};

			if (col === "주문서코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}
			if (col === "진행상태") {
				// 데이터 필드 이름은 컬럼 제목과 동일하게 '진행상태'를 사용합니다.
				columnDef.field = "진행상태";
				columnDef.formatter = function(cell) {
					const value = cell.getValue(); // 현재 상태 값 (예: "체결")
					const rowData = cell.getData();
					const code = rowData.주문서코드;

					// 옵션 HTML 생성
					const options = Object.keys(STATUS_MAP).map(key => {
						const itemInfo = STATUS_MAP[key];
						// 현재 상태를 'selected' 속성으로 설정
						const isSelected = key === value ? 'selected' : '';
						return `<option value="${key}" ${isSelected}>${itemInfo.label}</option>`;
					}).join('');

					// Select 요소 반환: 변경 시 updateStatusAPI 호출
					// 'this'는 HTML Select 요소를 가리키며, 이를 세 번째 인수로 전달하여 실패 시 복구합니다.
					return `
                        <select class="form-select form-select-sm" 
                                onchange="updateStatusAPI('${code}', this.value, this)"
                                style="font-size: 0.75rem; padding: 0.25rem 0.5rem; height: auto; min-width: 90px;">
                            ${options}
                        </select>
                    `;
				};
			}

			// 견적서 코드 필드도 클릭 가능하게 할 수 있으나, 현재는 주문서 코드만 설정

			return columnDef;
		}).filter(c => c !== null)
	];

	// makeTabulator는 salesCommon.js에 정의된 것으로 가정
	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.orderTableInstance = tableInstance;

	initTabFiltering();
});


window.updateStatusAPI = function(code, status, selectElement) {
	const row = window.orderTableInstance.getRows().find(r => r.getData().견적서코드 === code);
	// API 호출 전 현재 상태를 저장합니다.
	const currentStatus = row?.getData()?.진행상태;

	if (currentStatus === status) {
		console.log(`[주문서 ${code}]의 상태는 이미 '${status}'입니다. API 호출을 건너뜁니다.`);
		// 현재 상태와 같더라도 Tabulator가 자동으로 리렌더링하지 않으므로 select 값을 되돌립니다.
		if (selectElement) {
			selectElement.value = currentStatus;
		}
		return;
	}

	// 로딩 상태 등으로 임시 UI 변경을 원할 경우 여기에 로직을 추가할 수 있습니다.

	const url = "/api/updateOrders";
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
	const csrfToken = document.querySelector('meta[name="_csrf"]').content;

	const data = {
		orderCode: code, // 서버에 보낼 견적서 코드
		status: status
	};

	fetch(url, {
		method: "POST",
		headers: {
			'Content-Type': 'application/json',
			[csrfHeader]: csrfToken
		},
		body: JSON.stringify(data)
	})
		.then(res => {
			if (!res.ok) {
				// HTTP 상태 코드가 200번대가 아니면 오류 처리
				return res.json().then(error => {
					throw new Error(error.message || `서버 오류 발생: ${res.status}`);
				});
			}
			return res.json();
		})
		.then(response => {
			if (response.success) {
				if (window.orderTableInstance) {
					window.orderTableInstance.getRows().find(r => r.getData().주문서코드 === code)?.update({ '진행상태': status });
				}
			} else {
				alert(`상태 변경에 실패했습니다: ${response.message || '알 수 없는 오류'}`);

				if (selectElement) {
					selectElement.value = currentStatus;
				}
			}
		})
		.catch(err => {
			console.error("상태 변경 API 호출 실패:", err);
			alert(`상태 변경 중 통신 오류가 발생했습니다. 오류: ${err.message}`);

			if (selectElement) {
				selectElement.value = currentStatus;
			}
		});
}



/**
 * 행의 수량 또는 단가가 변경될 때 공급가액, 부가세, 최종금액을 계산
 */
window.calculateRow = function(inputElement) {
	const row = inputElement.closest('tr');
	if (!row) return;

	// cleanValue 헬퍼 함수 사용
	const cleanValue = window.cleanValue;

	// 1. 현재 변경된 필드에 콤마 포맷팅을 적용하여 표시
	const currentValue = cleanValue(inputElement.value);
	inputElement.value = currentValue.toLocaleString('ko-KR');

	// 2. 모든 Input 요소 쿼리
	const quantityInput = row.querySelector('input[name="quantity"]');
	const unitPriceInput = row.querySelector('input[name="price"]');
	const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
	const taxAmountInput = row.querySelector('input[name="taxAmount"]');
	const finalAmountInput = row.querySelector('input[name="finalAmount"]');

	// 3. 계산을 위해 콤마가 제거된 순수한 숫자값 사용
	const quantity = cleanValue(quantityInput.value);
	const unitPrice = cleanValue(unitPriceInput.value);

	// 4. 공급가액 및 부가세 계산
	const supplyAmount = quantity * unitPrice;
	const taxAmount = Math.floor(supplyAmount * 0.1);
	const finalAmount = supplyAmount + taxAmount;

	// 5. 계산된 값에 콤마 포맷팅 적용 후 출력
	supplyAmountInput.value = supplyAmount.toLocaleString('ko-KR');
	taxAmountInput.value = taxAmount.toLocaleString('ko-KR');
	finalAmountInput.value = finalAmount.toLocaleString('ko-KR');

	// 6. 전체 합계 재계산
	calculateTotal();
}

/**
 * 주문 상세 테이블 전체의 합계를 계산
 */
window.calculateTotal = function() {
	let totalQuantity = 0;
	let totalSupplyAmount = 0;
	let totalTaxAmount = 0;
	let totalFinalAmount = 0;

	const tbody = document.getElementById('itemDetailBody');
	if (!tbody) return;

	// 모든 행을 순회하며 합계 계산 (템플릿 행 제외)
	tbody.querySelectorAll('tr').forEach(row => {
		if (row.getAttribute('data-row-id') === 'new') {
			return;
		}

		// Input 요소들을 찾습니다.
		const quantityInput = row.querySelector('input[name="quantity"]');
		const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
		const taxAmountInput = row.querySelector('input[name="taxAmount"]');
		// finalAmountInput은 합계 계산에 사용하지 않고, 출력용으로만 사용합니다.

		if (!quantityInput || !supplyAmountInput || !taxAmountInput) return;

		// cleanValue 헬퍼 함수 사용
		const cleanValue = window.cleanValue;

		const quantity = cleanValue(quantityInput.value);
		const supplyAmount = cleanValue(supplyAmountInput.value);
		const taxAmount = cleanValue(taxAmountInput.value);

		totalQuantity += quantity;
		totalSupplyAmount += supplyAmount;
		totalTaxAmount += taxAmount;
		// ⭐ [수정 사항] 최종금액은 공급가액과 부가세의 합으로 정확히 계산합니다.
		totalFinalAmount += (supplyAmount + taxAmount);
	});

	// 1. 합계 필드 업데이트 (HTML ID 사용)
	document.getElementById('totalQuantity').textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	document.getElementById('totalSupplyAmount').textContent = totalSupplyAmount.toLocaleString('ko-KR') + ' 원';
	document.getElementById('totalTaxAmount').textContent = totalTaxAmount.toLocaleString('ko-KR') + ' 원';

	// 2. 최종 총 금액 업데이트
	document.getElementById('totalFinalAmount').textContent = totalFinalAmount.toLocaleString('ko-KR') + ' 원';
}