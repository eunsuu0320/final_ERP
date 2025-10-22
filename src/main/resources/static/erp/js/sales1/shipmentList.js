

window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;


document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["출하지시서코드", "출하예정일자", "거래처명", "창고명", "품목명", "수량합계", "담당자", "진행상태"];

	const STATUS_MAP = {
		"미확인": { label: "미확인" },
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
			case 'NONCHECK':
				filterValue = "미확인";
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




	// 출하지시서 모달 내 '주문서조회' 버튼 클릭 시
	window.selectOrders = function() {
		const partnerCode = document.getElementById('partnerCodeModal').value;
		const form = document.getElementById('quoteForm');

		if (!partnerCode) {
			alert("먼저 거래처를 선택하세요.");
			return;
		}

		openSalesModal(function(selected) {
			// ✅ 기본정보 (주소 등) 바인딩
			document.getElementById('postCode').value = selected.postCode || '';
			document.getElementById('address').value = selected.address || '';

			// ✅ 주문코드
			const orderCode = selected.orderCode;
			console.log("선택한 주문서:", orderCode);

			const orderUniqueCode = selected.orderUniqueCode;

			// ✅ 공통함수로 상세 데이터 로딩 (자동 폼 & 테이블 바인딩)
			loadDetailData('orders', orderUniqueCode, form)
				.then(responseData => {
					console.log("주문서 상세 데이터 수신:", responseData);
					window.lastLoadedOrderData = responseData; // 디버깅용
				})
				.catch(err => {
					console.error("주문서 상세 불러오기 실패:", err);
					alert("주문서 정보를 불러오지 못했습니다.");
				});
		}, partnerCode);
	};




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
		const form = document.getElementById("quoteForm"); // 출하 폼 ID 사용
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
		const form = document.getElementById("quoteForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		window.lastLoadedEstimateData = null;
		window.lastModalType = null;

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
		const quoteForm = document.getElementById("quoteForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!quoteForm) {
			alert("저장 오류: 출하 등록 폼을 찾을 수 없습니다.");
			return;
		}

		// 1️⃣ 필수 항목 체크
		if (!window.checkRequired(quoteForm)) return;

		// 2️⃣ 상세 항목 수집
		const detailList = collectShipmentDetails();
		if (detailList.length === 0) {
			alert("출하 상세 항목을 1개 이상 입력해주세요.");
			return;
		}

		// 3️⃣ 폼 데이터 수집
		const formData = new FormData(quoteForm);
		const formObj = Object.fromEntries(formData.entries());

		// ✅ 4️⃣ DTO와 필드명 정확히 일치시킨 최종 Payload
		const finalPayload = {
			deliveryDate: formObj.deliveryDateText || null,     // ✅ 출하예정일자
			partnerCode: formObj.partnerCode || '',             // ✅ 거래처코드
			partnerName: formObj.partnerName || '',        // ✅ 거래처명
			warehouse: formObj.warehouse || null,               // ✅ 창고
			manager: formObj.empCode || '',                     // ✅ 담당자코드
			postCode: formObj.postCode || null,                 // ✅ 우편번호
			address: formObj.address || '',                     // ✅ 주소
			remarks: formObj.remarks || '',                     // ✅ 비고
			detailList: detailList                              // ✅ 상세항목
		};

		console.log("전송할 최종 출하 데이터:", finalPayload);

		// 5️⃣ 서버 전송
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

				// 모달 닫기 및 초기화
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				if (modalInstance) modalInstance.hide();

				const tbody = document.getElementById('itemDetailBody');
				const newRowTemplate = tbody?.querySelector('tr.new-item-row');
				if (newRowTemplate && window.clearInputRowValues) {
					window.clearInputRowValues(newRowTemplate);
					if (window.calculateTotal) window.calculateTotal();
				}
			})
			.catch(err => {
				console.error("출하 등록 실패:", err);
				alert(`등록에 실패했습니다. 오류: ${err.message}`);
			});
	};




	// ✅ 출하지시수량 입력 유효성 검사 (반복행 대응)
	window.validateNowQuantity = function(input) {
		// 현재 입력 중인 행 찾기
		const row = input.closest("tr");
		if (!row) return;

		// 같은 행의 stock, nonShipment 값 읽기
		const stock = parseFloat(row.querySelector("[name='stock']")?.value || 0);
		const nonShipment = parseFloat(row.querySelector("[name='nonShipment']")?.value || 0);
		const entered = parseFloat(input.value || 0);

		// 초과 여부 검사
		if (entered > stock || entered > nonShipment) {
			showToast("재고, 미지시수량을 초과합니다.");
			input.value = Math.min(stock, nonShipment); // 두 값 중 더 작은 값으로 되돌림
		}
	}

	// ✅ 토스트 알림 (공통)
	window.showToast = function(message) {
		const existingToast = document.getElementById("toastMessage");
		if (existingToast) existingToast.remove();

		const toast = document.createElement("div");
		toast.id = "toastMessage";
		toast.textContent = message;
		toast.style.position = "fixed";
		toast.style.bottom = "40px";
		toast.style.left = "50%";
		toast.style.transform = "translateX(-50%)";
		toast.style.background = "rgba(0, 0, 0, 0.8)";
		toast.style.color = "white";
		toast.style.padding = "10px 20px";
		toast.style.borderRadius = "8px";
		toast.style.fontSize = "14px";
		toast.style.zIndex = "9999";
		toast.style.transition = "opacity 0.5s ease";

		document.body.appendChild(toast);

		setTimeout(() => {
			toast.style.opacity = "0";
			setTimeout(() => toast.remove(), 500);
		}, 1500);
	}





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

		const cleanValue = window.cleanValue;
		let isRowValid = true;

		// 실제 데이터 행만 순회 (템플릿 행 제외)
		tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {
			const nowQtyInput = row.querySelector('input[name="nowQuantity"]');
			const nowQuantity = cleanValue(nowQtyInput?.value || 0);

			// ✅ nowQuantity가 0 이하이거나 빈 값이면 이 행은 저장 대상에서 제외
			if (!nowQuantity || nowQuantity <= 0) return;

			// ✅ 필수 항목 유효성 검사
			const validationResult = window.checkRowRequired(row);
			if (!validationResult.isValid) {
				alert(`[${row.rowIndex}번째 행] ${validationResult.missingFieldName}은(는) 필수 입력 항목입니다.`);
				isRowValid = false;
				return;
			}

			// ✅ HTML name 속성에 맞춰 데이터 구성
			const productCode = row.querySelector('input[name="productCode"]')?.value || '';
			const productName = row.querySelector('input[name="productName"]')?.value || '';
			const stock = cleanValue(row.querySelector('input[name="stock"]')?.value || 0);
			const remarks = row.querySelector('input[name="remarks"]')?.value || '';
			const orderDetailCode = row.querySelector('input[name="orderDetailCode"]')?.value || '';
			const totalQuantity = cleanValue(row.querySelector('input[name="quantity"]')?.value || 0);
			const nonShipment = totalQuantity - nowQuantity;

			detailList.push({
				orderDetailCode: orderDetailCode,
				productCode: productCode,
				productName: productName,
				nowQuantity: nowQuantity,
				nonShipment: nonShipment,
				stock: stock,
				remarks: remarks,
			});
		});

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



			if (col === "수량합계") {
				columnDef.formatter = function(cell) {
					const v = cell.getValue();
					if (v === null || v === undefined || isNaN(v)) return "-";
					return Number(v).toLocaleString('ko-KR') + " 개";
				};
				columnDef.sorter = "number";
				columnDef.hozAlign = "right";
			}

			return columnDef;
		}).filter(c => c !== null)
	];

	// Tabulator 테이블 생성 (공통함수 window.makeTabulator 사용)
	const tableInstance = window.makeTabulator(rows, tabulatorColumns);
	window.shipmentTableInstance = tableInstance;
	initTabFiltering();


	function loadTableData(params = {}) {
		const queryString = new URLSearchParams(params).toString();
		const url = `/api/shipment/search?${queryString}`;

		// 로딩 상태 표시
		if (window.shipmentTableInstance) {
			// Tabulator의 기본 로딩 애니메이션을 사용하거나, 수동으로 로딩 표시 가능
		}

		fetch(url)
			.then(response => {
				if (!response.ok) {
					throw new Error('데이터 요청 실패: ' + response.statusText);
				}
				return response.json();
			})
			.then(data => {
				console.log("검색 결과 데이터:", data);

				// ★ 3. 검색 결과를 Tabulator에 반영하는 핵심 로직
				if (window.shipmentTableInstance) {
					window.shipmentTableInstance.setData(data);
				}
			})
			.catch(error => {
				console.error('데이터 로딩 중 오류 발생:', error);
				alert('데이터를 가져오는 데 실패했습니다.');

				// 오류 발생 시 빈 배열로 설정하여 테이블 정리
				if (window.shipmentTableInstance) {
					window.shipmentTableInstance.setData([]);
				}
			});
	}

	// ★ 2. 검색 버튼 이벤트 핸들러 (조건에 맞는 목록 조회)
	// 출하지시서 테이블 필터
	window.filterSearch = function() {
		// 🔍 검색 조건 수집
		const clientName = document.getElementById("partnerNameSearch").value.trim();
		const shipmentNo = document.getElementById("shipmentCode").value.trim();
		const warehouseCode = document.getElementById("warehouseCodeSearch").value.trim();

		// 🔧 Tabulator 필터 조건 배열
		const filters = [];
		if (clientName) filters.push({ field: "거래처명", type: "like", value: clientName });
		if (shipmentNo) filters.push({ field: "출하지시서코드", type: "like", value: shipmentNo });
		if (warehouseCode) filters.push({ field: "창고명", type: "like", value: warehouseCode });

		// ✅ 전역 Tabulator 인스턴스 참조
		const table = window.shipmentTableInstance;

		if (table && typeof table.setFilter === "function") {
			table.clearFilter();      // 기존 필터 제거
			table.setFilter(filters); // 새로운 필터 적용
			console.log("✅ 클라이언트 필터 적용 완료:", filters);
		} else {
			console.error("❌ shipmentTable이 초기화되지 않았거나 Tabulator 인스턴스가 아닙니다.", table);
			alert("테이블이 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
		}
	};




	// ★ 4. 초기화 버튼 이벤트 핸들러 (전체 목록 조회)
	window.resetSearch = function() {
		// 검색 조건 필드 초기화 로직 (실제 DOM 구조에 맞게 수정 필요)
		const searchTool = document.querySelector('.searchTool');
		searchTool.querySelectorAll('input[type=text], select').forEach(el => {
			if (el.tagName === 'SELECT' && el.choicesInstance) {
				// Choices.js 인스턴스 초기화
				el.choicesInstance.setChoiceByValue('');
			} else {
				el.value = '';
			}
		});

		// 검색 조건 없이 데이터 로딩 함수 호출 (searchVo가 빈 상태로 넘어가 전체 목록 조회)
		loadTableData({});
	}


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