document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["청구서코드", "등록일자", "청구일자", "청구금액", "수금일자", "거래처명", "진행상태"];

	const STATUS_MAP = {
		"미청구": { label: "미청구" },
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"수금완료": { label: "수금완료" },
		"회계반영완료": { label: "회계반영완료" }
	};

	// 품목상세모달
	window.showDetailModal = function(modalType) {
		let modalName = '';
		// 모달 열기
		if (modalType === 'detail') {
			modalName = '청구서상세정보'

		} else if (modalType === 'regist') {
			modalName = '청구서등록'
		}
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;


	};


	window.updateStatusAPI = function(code, status, selectElement) {
		const row = window.invoiceTableInstance.getRows().find(r => r.getData().청구서코드 === code);
		// API 호출 전 현재 상태를 저장합니다.
		const currentStatus = row?.getData()?.진행상태;

		if (currentStatus === status) {
			console.log(`[청구서 ${code}]의 상태는 이미 '${status}'입니다. API 호출을 건너뜁니다.`);
			// 현재 상태와 같더라도 Tabulator가 자동으로 리렌더링하지 않으므로 select 값을 되돌립니다.
			if (selectElement) {
				selectElement.value = currentStatus;
			}
			return;
		}

		// 로딩 상태 등으로 임시 UI 변경을 원할 경우 여기에 로직을 추가할 수 있습니다.

		const url = "/api/updateInvoice";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;

		const data = {
			invoceCode: code, // 서버에 보낼 견적서 코드
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
					if (window.invoiceTableInstance) {
						// 고유 견적서 코드를 기반으로 행을 찾아 '진행상태' 필드를 업데이트합니다.
						// 이 업데이트는 자동으로 Tabulator 셀의 formatter를 다시 호출합니다.
						window.invoiceTableInstance.getRows().find(r => r.getData().청구서코드 === code)?.update({ '진행상태': status });
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






	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const formData = new FormData(form);
		let modalName = document.querySelector('#newDetailModal .modal-title').innerHTML;

		if (!formData.get("productName") || !formData.get("unit") || !formData.get("productSize") || !formData.get("productGroup")) {
			alert("필수항목을 입력하세요!");
			return;
		}


		if (modalName === '품목상세정보') {
			fetch("/api/modifyProduct", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목수정 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("품목수정실패 : ", err);
					alert("저장에 실패했습니다.")
				});

		} else if (modalName === '품목등록') {
			fetch("/api/registProduct", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목등록 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("품목등록실패 : ", err);
					alert("저장에 실패했습니다.")
				});
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

			if (col === "품목코드") {
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
					const code = rowData.청구서코드;

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
	window.priceTableInstance = tableInstance;
});
