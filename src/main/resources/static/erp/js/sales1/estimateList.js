document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["견적서코드", "등록일자", "거래처명", "품목명", "유효기간", "견적금액합계", "담당자", "진행상태"];

	// 진행 상태에 따른 스타일을 정의하는 맵
	const STATUS_MAP = {
		"미확인": { label: "미확인", cls: "bg-secondary" }, // 회색 (기본)
		"진행중": { label: "진행중", cls: "bg-info" },      // 하늘색 (진행 중)
		"미체결": { label: "미체결", cls: "bg-danger" },    // 빨간색 (미체결/위험)
		"체결": { label: "체결", cls: "bg-success" }       // 초록색 (체결/성공)
	};

	// 콤마 제거 후 정수만 추출하는 헬퍼 함수 (전역으로 정의하여 모든 함수에서 사용)
	window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;


	function initTabFiltering() {

		const tabButtons = document.querySelectorAll('#estimateTab button');

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
		const table = window.estimateTableInstance;
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
			case 'UNCONFIRMED':
				filterValue = "미확인";
				break;
			case 'IN_PROGRESS':
				filterValue = "진행중";
				break;
			case 'UNSETTLED':
				filterValue = "미체결";
				break;
			case 'SETTLED':
				filterValue = "체결";
				break;
			default:
				return;
		}

		// 필터 적용: setFilter(필드 이름, 비교 연산자, 값)
		if (filterValue) {
			table.setFilter(filterField, "=", filterValue);
		}
	}






	window.updateStatusAPI = function(code, status, selectElement) {
		const row = window.estimateTableInstance.getRows().find(r => r.getData().견적서코드 === code);
		// API 호출 전 현재 상태를 저장합니다.
		const currentStatus = row?.getData()?.진행상태;

		if (currentStatus === status) {
			console.log(`[견적서 ${code}]의 상태는 이미 '${status}'입니다. API 호출을 건너뜁니다.`);
			// 현재 상태와 같더라도 Tabulator가 자동으로 리렌더링하지 않으므로 select 값을 되돌립니다.
			if (selectElement) {
				selectElement.value = currentStatus;
			}
			return;
		}

		// 로딩 상태 등으로 임시 UI 변경을 원할 경우 여기에 로직을 추가할 수 있습니다.

		const url = "/api/updateEstimate";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;

		const data = {
			estimateCode: code, // 서버에 보낼 견적서 코드
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
				if (response.success) { // 서버 응답에 'success: true'가 있다고 가정
					// Tabulator 행 데이터 업데이트 (화면 새로고침 없이)
					if (window.estimateTableInstance) {
						// 고유 견적서 코드를 기반으로 행을 찾아 '진행상태' 필드를 업데이트합니다.
						// 이 업데이트는 자동으로 Tabulator 셀의 formatter를 다시 호출합니다.
						window.estimateTableInstance.getRows().find(r => r.getData().견적서코드 === code)?.update({ '진행상태': status });
					}
				} else {
					alert(`상태 변경에 실패했습니다: ${response.message || '알 수 없는 오류'}`);
					// 실패 시 <select> 요소를 원래 상태로 되돌립니다.
					if (selectElement) {
						selectElement.value = currentStatus;
					}
				}
			})
			.catch(err => {
				console.error("상태 변경 API 호출 실패:", err);
				alert(`상태 변경 중 통신 오류가 발생했습니다. 오류: ${err.message}`);
				// 실패 시 <select> 요소를 원래 상태로 되돌립니다.
				if (selectElement) {
					selectElement.value = currentStatus;
				}
			});
	}
	// -------------------------------------------------------------


	// 폼 전체 초기화
	window.resetQuote = function() {
		const form = document.getElementById("quoteForm");
		if (form) {
			form.reset();
		}

		if (window.resetItemGrid) {
			// salesCommon.js에 정의된 함수 호출 가정
			window.resetItemGrid();
		} else {
			// console.error("Error: resetItemGrid function is not defined. Check salesCommon.js loading.");
			// 상세 테이블 내용만 수동 초기화 (예시)
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			window.addItemRow(); // 기본 행 1개 추가
			window.calculateTotal(); // 합계 초기화
		}

		console.log("견적 모달 전체 초기화 완료.");
	}

	// 견적서 상세모달
	window.showDetailModal = function(modalType) {
		let modalName = '';


		// 모달 열기
		if (modalType === 'detail') {
			modalName = '견적서 상세정보'

		} else if (modalType === 'regist') {
			modalName = '견적서등록'
			resetQuote();
		}
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;
	};





	// estimateList.js (window.saveModal 함수)
	window.saveModal = function() {
		const quoteForm = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!quoteForm) {
			alert("저장 오류: 견적 등록 폼을 찾을 수 없습니다.");
			return;
		}

		// 1. 견적 기본 정보 수집
		const quoteData = new FormData(quoteForm);
		const quoteDataObject = Object.fromEntries(quoteData.entries());


		const partnerNameValue = document.getElementById('quotePartnerName').value;

		// 2. 견적 상세 정보 (EstimateDetail 엔티티 리스트) 수집
		const detailList = collectQuoteDetails();

		if (detailList.length === 0) {
			alert("견적 상세 내용을 1개 이상 입력해주세요.");
			return;
		}

		// 3. 최종 페이로드 구성: EstimateRegistrationDTO 구조에 맞게 조정
		const finalPayload = {
			// DTO의 기본 필드
			partnerCode: quoteDataObject.partnerCode || '', // Hidden input 필드 name="partnerCode"

			// ✨ 최종적으로 partnerName 값을 사용
			partnerName: partnerNameValue,

			quoteDate: quoteDataObject.quoteDate,
			validPeriod: parseInt(quoteDataObject.validPeriod) || 0,
			remarks: quoteDataObject.remarks || '',
			manager: quoteDataObject.manager || '',

			// DTO의 List<EstimateDetail> 필드
			detailList: detailList
		};

		console.log("전송할 최종 견적 데이터:", finalPayload);

		// 4. 서버에 API 호출 (Controller의 @PostMapping("api/registEstimate") 경로 사용)
		fetch("/api/registEstimate", {
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
				alert("견적서가 성공적으로 등록되었습니다. ID: " + data.id);

				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();

				// 성공 후 테이블 데이터 리로드 (전체 새로고침)
				if (window.estimateTableInstance) {
					location.reload();
				}
			})
			.catch(err => {
				console.error("견적서 등록 실패:", err);
				alert(`등록에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요. 오류: ${err.message}`)
			});
	};


	// estimateList.js (collectQuoteDetails 함수)
	/**
	 * 견적 상세 테이블의 데이터 행들을 순회하며 EstimateDetail 엔티티 구조에 맞춰 수집합니다.
	 */
	function collectQuoteDetails() {
		const detailList = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return detailList;

		// 템플릿 행(data-row-id="new")을 제외한 모든 데이터 행 순회
		tbody.querySelectorAll('tr').forEach(row => {
			if (row.getAttribute('data-row-id') === 'new') {
				return;
			}

			// ✨ 수정된 HTML name 속성을 사용하여 객체 구성
			detailList.push({
				productCode: row.querySelector('input[name="productCode"]').value || '',
				quantity: window.cleanValue(row.querySelector('input[name="quantity"]').value),
				price: window.cleanValue(row.querySelector('input[name="price"]').value),
				remarks: row.querySelector('input[name="remarks"]').value || '',
			});
		});

		return detailList;
	}


	// 견적서리스트 테이블 컬럼에 대한 정의
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

			if (col === "견적서코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					// 견적서 코드 클릭 시 상세 모달을 열 때, 견적서 코드를 인수로 전달합니다.
					const rowData = cell.getData();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${rowData.견적서코드}')">${value}</div>`;
				};
			}

			// "진행상태" 컬럼에 HTML Select 요소 적용 (직접 변경 방식)
			if (col === "진행상태") {
				// 데이터 필드 이름은 컬럼 제목과 동일하게 '진행상태'를 사용합니다.
				columnDef.field = "진행상태";
				columnDef.formatter = function(cell) {
					const value = cell.getValue(); // 현재 상태 값 (예: "체결")
					const rowData = cell.getData();
					const code = rowData.견적서코드;

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

			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.estimateTableInstance = tableInstance;

	initTabFiltering();
});

// ====================================================================
// 전역 함수 (HTML oninput 등에서 호출됨)
// ====================================================================

function calculateRow(inputElement) {
	const row = inputElement.closest('tr');
	if (!row) return;

	// cleanValue 헬퍼 함수 사용
	const cleanValue = window.cleanValue;

	// 1. 현재 변경된 필드에 콤마 포맷팅을 적용하여 표시
	const currentValue = cleanValue(inputElement.value);
	inputElement.value = currentValue.toLocaleString('ko-KR');

	// 2. 모든 Input 요소 쿼리 (수정된 name 적용)
	const quantityInput = row.querySelector('input[name="quantity"]');
	const unitPriceInput = row.querySelector('input[name="price"]'); // ✨ name="price" 적용
	const supplyAmountInput = row.querySelector('input[name="supplyAmount"]');
	const taxAmountInput = row.querySelector('input[name="taxAmount"]');

	// 3. 계산을 위해 콤마가 제거된 순수한 숫자값 사용
	const quantity = cleanValue(quantityInput.value);
	const unitPrice = cleanValue(unitPriceInput.value);

	// 4. 공급가액 및 부가세 계산
	const supplyAmount = quantity * unitPrice;
	const taxAmount = Math.floor(supplyAmount * 0.1);

	// 5. 계산된 공급가액과 부가세에 콤마 포맷팅 적용 후 출력
	supplyAmountInput.value = supplyAmount.toLocaleString('ko-KR');
	taxAmountInput.value = taxAmount.toLocaleString('ko-KR');

	// 6. 전체 합계 재계산
	calculateTotal();
}


function calculateTotal() {
	let totalQuantity = 0;
	let totalSupplyAmount = 0;
	let totalTaxAmount = 0;

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

		if (!quantityInput || !supplyAmountInput || !taxAmountInput) return;

		// cleanValue 헬퍼 함수 사용
		const cleanValue = window.cleanValue;

		const quantity = cleanValue(quantityInput.value);
		const supplyAmount = cleanValue(supplyAmountInput.value);
		const taxAmount = cleanValue(taxAmountInput.value);

		totalQuantity += quantity;
		totalSupplyAmount += supplyAmount;
		totalTaxAmount += taxAmount;
	});

	// 1. 합계 필드 업데이트
	document.getElementById('totalQuantity').textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
	document.getElementById('totalSupplyAmount').textContent = totalSupplyAmount.toLocaleString('ko-KR') + ' 원';
	document.getElementById('totalTaxAmount').textContent = totalTaxAmount.toLocaleString('ko-KR') + ' 원';

	// 2. 총 금액 계산 및 업데이트
	const totalAmount = totalSupplyAmount + totalTaxAmount;
	document.getElementById('totalAmount').textContent = totalAmount.toLocaleString('ko-KR') + ' 원';
}
