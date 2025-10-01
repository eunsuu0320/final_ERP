document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["견적서코드", "등록일자", "거래처명", "품목명", "유효기간", "견적금액합계", "담당자", "진행상태"];

	// 콤마 제거 후 정수만 추출하는 헬퍼 함수 (전역으로 정의하여 모든 함수에서 사용)
	window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;

	window.showDatePicker = function() {
		const dateInput = document.getElementById('quoteDate');
		if (dateInput) {
			dateInput.type = 'date';
			dateInput.focus();
		}
	};

	window.showDeliveryDatePicker = function() {
		const deliveryInput = document.getElementById('quoteDeliveryDateText');
		if (deliveryInput) {
			deliveryInput.type = 'date';
			deliveryInput.focus();
		}
	};

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
    
    // ✨ [핵심 수정] HTML ID 'quotePartnerName'을 사용하여 거래처 이름 가져오기
    // HTML에 name="partnerName"이 있으므로 quoteDataObject.partnerName으로도 가져올 수 있지만,
    // 명시적으로 input ID를 사용하는 것이 안전하고 확실합니다.
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
            // 성공 후 테이블 데이터 리로드 로직 추가 필요 (예: tableInstance.loadData())
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
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}

			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;
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