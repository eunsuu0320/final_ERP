

window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;


document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["출하지시서코드", "출하예정일자", "거래처명", "창고명", "품목명", "수량합계", "진행상태"];

	const STATUS_MAP = {
		"미지시": { label: "미지시" },
		"결재중": { label: "결재중" },
		"확인": { label: "확인" },
		"출하중": { label: "출하중" },
		"출하완료": { label: "출하완료" },
		"회계반영완료": { label: "회계반영완료" }
	};


	function initTabFiltering() {

		const tabButtons = document.querySelectorAll('#shipmentTab button');

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
		const table = window.shipmentTableInstance;
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
			case 'NONSHIPMENT':
				filterValue = "미지시";
				break;
			case 'ONCHECKING':
				filterValue = "결재중";
				break;
			case 'CHECK':
				filterValue = "확인";
				break;
			case 'ONSHIPMENT':
				filterValue = "출하중";
				break;
			case 'SUCCESSHIPMENT':
				filterValue = "출하완료";
				break;
			case 'SUCCESSAC':
				filterValue = "회계반영완료";
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
		const row = window.shipmentTableInstance.getRows().find(r => r.getData().출하지시서코드 === code);
		// API 호출 전 현재 상태를 저장합니다.
		const currentStatus = row?.getData()?.진행상태;

		if (currentStatus === status) {
			console.log(`[출하지시서 ${code}]의 상태는 이미 '${status}'입니다. API 호출을 건너뜁니다.`);
			// 현재 상태와 같더라도 Tabulator가 자동으로 리렌더링하지 않으므로 select 값을 되돌립니다.
			if (selectElement) {
				selectElement.value = currentStatus;
			}
			return;
		}

		// 로딩 상태 등으로 임시 UI 변경을 원할 경우 여기에 로직을 추가할 수 있습니다.

		const url = "/api/updateShipment";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;

		const data = {
			shipmentCode: code, // 서버에 보낼 견적서 코드
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
					if (window.shipmentTableInstance) {
						// 고유 견적서 코드를 기반으로 행을 찾아 '진행상태' 필드를 업데이트합니다.
						// 이 업데이트는 자동으로 Tabulator 셀의 formatter를 다시 호출합니다.
						window.shipmentTableInstance.getRows().find(r => r.getData().출하지시서코드 === code)?.update({ '진행상태': status });
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




	// 폼 전체 초기화 (견적서 폼 대신 출하 폼을 초기화)
	window.resetQuote = function() {
		const form = document.getElementById("shipmentForm"); // 출하 폼 ID 사용
		if (form) {
			form.reset();
		}

		// ⭐ 공통함수 window.resetItemGrid 호출 (상세 테이블 초기화 및 입력 행 초기화)
		if (window.resetItemGrid) {
			window.resetItemGrid();
		} else {
			// resetItemGrid가 없을 경우의 대체 로직
			const tbody = document.getElementById('itemDetailBody');
			if (tbody) tbody.innerHTML = '';
			window.addItemRow();
		}

		// 합계 초기화 (window.calculateTotal은 아래에 정의됨)
		if (window.calculateTotal) {
			window.calculateTotal();
		}

		console.log("출하 모달 전체 초기화 완료.");
	}



	// 출하 상세/등록 모달 
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '출하지시서 상세정보' : '출하지시서 등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("shipmentForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		form.reset();



		const commonCodePromises = [
			loadCommonCode('GRP003', 'warehouse', '창고'),
		];

		modal.show();

		if (modalType === 'detail' && keyword) {
			Promise.all(commonCodePromises)
				.then(() => {
					// loadDetailData 함수는 외부에서 제공된다고 가정
					loadDetailData('shipment', keyword, form);
				})
				.catch(err => {
					console.error("공통 코드 로딩 중 치명적인 오류 발생:", err);
					alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
				});
		} else {
			// 신규 등록 시 결제 정보 초기화
			resetItemGrid();
		}
	};


	// ====================================================================
	// 출하 마스터 + 상세 등록 로직 (saveModal)
	// ====================================================================
	window.saveModal = function() {
		const shipmentForm = document.getElementById("shipmentForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!shipmentForm) {
			alert("저장 오류: 출하 등록 폼을 찾을 수 없습니다.");
			return;
		}

		// 1. Shipment 기본 정보 유효성 검사 (공통함수 checkRequired 사용)
		if (!window.checkRequired(shipmentForm)) {
			return;
		}

		// 2. Shipment 상세 정보 수집 (유효성 검사는 collectShipmentDetails에서 처리)
		const detailList = collectShipmentDetails();

		if (detailList.length === 0) {
			alert("출하 상세 항목을 1개 이상 입력해주세요.");
			return;
		}

		// 3. 최종 페이로드 구성
		const shipmentData = new FormData(shipmentForm);
		const shipmentDataObject = Object.fromEntries(shipmentData.entries());

		const finalPayload = {
			shipmentDate: shipmentDataObject.quoteDate,
			partnerCode: shipmentDataObject.partnerCode || '',
			partnerName: shipmentDataObject.partnerName,
			warehouse: shipmentDataObject.warehouse || '',
			manager: shipmentDataObject.manager || '',
			postCode: shipmentDataObject.zipcode || '',
			address: shipmentDataObject.address || '',
			remarks: shipmentDataObject.remarks || '',
			detailList: detailList
		};

		console.log("전송할 최종 출하 데이터:", finalPayload);

		// 4. 서버에 API 호출
		fetch("/api/registShipment", {
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
					return res.json().then(error => {
						throw new Error(error.message || `서버 오류 발생: ${res.status}`);
					});
				}
				return res.json();
			})
			.then(data => {
				console.log("서버 응답 데이터:", data);
				alert("출하 지시가 성공적으로 등록되었습니다. 코드: " + data.id);

				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();

				// 등록 성공 후, 등록 행을 초기화합니다. (clearInputRowValues 호출)
				const tbody = document.getElementById('itemDetailBody');
				const newRowTemplate = tbody ? tbody.querySelector('tr.new-item-row') : null;
				if (newRowTemplate && window.clearInputRowValues) {
					window.clearInputRowValues(newRowTemplate);

					// 초기화 후 합계 재계산
					if (window.calculateTotal) {
						window.calculateTotal();
					}
				}

				// 테이블 데이터 리로드 로직 추가 
				// window.shipmentTableInstance.setData("/api/shipmentList");
			})
			.catch(err => {
				console.error("출하 등록 실패:", err);
				alert(`등록에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요. 오류: ${err.message}`)
			});
	};


	// ====================================================================
	// 출하 상세 항목 수집 로직
	// ====================================================================
	/**
	 * 출하 상세 테이블의 데이터 행들을 순회하며 ShipmentDetail 엔티티 구조에 맞춰 수집합니다.
	 */
	function collectShipmentDetails() {
		const detailList = [];
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return detailList;

		// cleanValue를 전역에서 가져와 사용
		const cleanValue = window.cleanValue;
		let isRowValid = true;

		// 실제 데이터 행만 순회 (템플릿 행 제외)
		tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {

			// 데이터 행에 대해서 유효성 검사 수행 (공통 함수 checkRowRequired 호출)
			const validationResult = window.checkRowRequired(row);

			if (!validationResult.isValid) {
				alert(`[${row.rowIndex}번째 행] ${validationResult.missingFieldName}은(는) 필수 입력 항목입니다.`);
				isRowValid = false;
				return;
			}

			// HTML input name 속성에 맞춰서 데이터 구성
			const productCode = row.querySelector('input[name="productCode"]').value || '';
			const quantity = cleanValue(row.querySelector('input[name="quantity"]').value); // cleanValue 사용
			const remarks = row.querySelector('input[name="remarks"]').value || '';

			detailList.push({
				productCode: productCode,
				quantity: quantity,
				remarks: remarks,
			});
		});

		// 유효성 검사 실패 시 빈 배열 반환
		if (!isRowValid) return [];

		return detailList;
	}


	// ====================================================================
	// 수량 합계 계산 로직 (calculateRow 및 saveModal에서 호출)
	// ====================================================================
	window.calculateTotal = function() {
		let totalQuantity = 0;

		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return;

		// 모든 데이터 행을 순회하며 합계 계산 (템플릿 행 제외)
		tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {

			const quantityInput = row.querySelector('input[name="quantity"]');

			if (!quantityInput || !window.cleanValue) return;

			const quantity = window.cleanValue(quantityInput.value); // cleanValue 사용

			totalQuantity += quantity;
		});

		// 총 수량 합계 필드 업데이트 (HTML ID가 'totalQuantity'로 가정)
		const totalQtyElement = document.getElementById('totalQuantity');
		if (totalQtyElement) {
			totalQtyElement.textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
		}
	}



	// 품목리스트 테이블 컬럼에 대한 정의
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

			let columnDef = {
				title: col,
				field: col,
				visible: defaultVisible.includes(col)
			};

			if (col === "출하지시서코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}


			// "진행상태" 컬럼에 HTML Select 요소 적용 (직접 변경 방식)
			if (col === "진행상태") {
				// 데이터 필드 이름은 컬럼 제목과 동일하게 '진행상태'를 사용합니다.
				columnDef.field = "진행상태";
				columnDef.formatter = function(cell) {
					const value = cell.getValue(); // 현재 상태 값 (예: "체결")
					const rowData = cell.getData();
					const code = rowData.출하지시서코드;

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

	// Tabulator 테이블 생성 (공통함수 window.makeTabulator 사용)
	const tableInstance = window.makeTabulator(rows, tabulatorColumns);
	window.shipmentTableInstance = tableInstance;
	initTabFiltering();
});


function calculateRow(inputElement) {
	if (window.cleanValue) {
		// cleanValue 헬퍼 함수를 사용하여 콤마 포맷팅 적용
		const cleanValue = window.cleanValue;
		const currentValue = cleanValue(inputElement.value);
		inputElement.value = currentValue.toLocaleString('ko-KR');

		// 전체 합계 재계산 (calculateTotal 호출)
		if (window.calculateTotal) {
			window.calculateTotal();
		}
	}
}