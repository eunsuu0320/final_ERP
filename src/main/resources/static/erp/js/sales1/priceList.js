document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["단가그룹코드", "단가그룹명", "단가유형", "할인율", "비고", "단가적용시작일", "단가적용종료일", "사용구분", "거래처설정", "품목설정"];

	// -------------------------------------------------------------------------
	// 1. PriceList 전용 Tabulator 생성 함수 정의 (makePriceListTabulator)
	// -------------------------------------------------------------------------

	/**
	 * PriceList 페이지 전용 Tabulator 인스턴스를 생성합니다.
	 * @param {Array} rows - 초기 데이터 배열
	 * @param {Array} tabulatorColumns - 컬럼 정의 배열
	 * @returns {Tabulator} Tabulator 인스턴스
	 */
	const makePriceListTabulator = function(rows, tabulatorColumns) {

		// fetch를 사용하므로 Tabulator의 내장 AJAX 기능을 우회합니다.
		const priceSpecificOptions = {
			ajaxURL: "/api/priceList",
			ajaxConfig: "GET"
		};

		// [가정] 공통 makeTabulator는 3번째 인수로 옵션 객체 병합을 지원해야 합니다.
		return makeTabulator(rows, tabulatorColumns, priceSpecificOptions);
	};


	window.changeTabData = function(type, clickedButton) {
		// 1. 버튼 스타일 변경 (전환)
		const tabButtons = document.querySelectorAll('#priceTab button');
		tabButtons.forEach(btn => {
			btn.classList.remove('btn-primary');
			btn.classList.add('btn-outline-primary');
		});
		clickedButton.classList.remove('btn-outline-primary');
		clickedButton.classList.add('btn-primary');


		// 전역으로 저장된 Tabulator 인스턴스를 가져옵니다.
		const table = window.priceTableInstance;
		if (!table) {
			console.error("Tabulator instance is not initialized.");
			return;
		}

		let apiUrl = "/api/priceList"; // 기본 API URL (ALL 탭)

		// 탭 타입에 따라 호출할 서버 API 경로를 설정합니다.
		switch (type) {
			case 'ALL':
				apiUrl = "/api/priceList";
				break;
			case 'PRODUCT':
				apiUrl = "/api/pricetabProduct";
				break;
			case 'PARTNER':
				apiUrl = "/api/pricetabPartner";
				break;
			default:
				console.warn(`Unknown tab type: ${type}`);
				return;
		}
        
        // -------------------------------------------------------------
		// [!!! 로딩 오버레이 요소 가져오기 (HTML에 존재해야 함) !!!]
		// -------------------------------------------------------------
		const loadingOverlay = document.getElementById('loading-overlay');
		
		// -------------------------------------------------------------
		// [!!! 로딩 시작: 수동 오버레이 표시 !!!]
		// -------------------------------------------------------------
		if (loadingOverlay) {
			// 'flex'로 변경하여 스피너와 배경을 테이블 위에 표시합니다.
			loadingOverlay.style.display = 'flex'; 
		}

		fetch(apiUrl)
			.then(response => {
				if (!response.ok) {
					// 서버에서 200 응답이 오지 않은 경우 (404, 500 등)
					throw new Error('데이터 로드 실패: ' + response.status + ' ' + response.statusText);
				}
				return response.json();
			})
			.then(data => {
				// 서버 응답 (data)가 {columns: [...], rows: [...]} 형태이므로
				// Tabulator가 기대하는 rows 배열만 추출하여 전달합니다.
				if (data && Array.isArray(data.rows)) {
					// table.setData()에 배열을 전달하여 에러를 해결합니다.
					return table.setData(data.rows);
				} else {
					console.error("Server data structure is invalid:", data);
					// 데이터 구조가 유효하지 않으면 빈 배열로 설정
					return table.setData([]);
				}
			})
			.then(function() {
				console.log(`Tabulator data successfully loaded from: ${apiUrl}`);
			})
			.catch(error => {
				console.error(`Error loading data from ${apiUrl}:`, error);
				alert("데이터 로드 중 오류가 발생했습니다. 콘솔을 확인하세요.");
			})
			.finally(() => {
				// -------------------------------------------------------------
				// [!!! 로딩 끝: 수동 오버레이 숨기기 (성공/실패 모두) !!!]
				// -------------------------------------------------------------
				if (loadingOverlay) {
					loadingOverlay.style.display = 'none'; 
				}
			});
	}


	// 단가상세모달 (변경 없음)
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '단가상세정보' : '단가등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("itemForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		form.reset();


		modal.show();

		if (modalType === 'detail' && keyword) {

			loadDetailData('price', keyword, form);

		}
	};

	// 단가상세모달의 저장버튼 이벤트 (변경 없음)
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const modalEl = document.getElementById("newDetailModal");
		const formData = new FormData(form);

		if (!checkRequired(form)) {
			return;
		};

		const priceGroupCode = formData.get("priceGroupCode");
		const isUpdate = priceGroupCode && priceGroupCode.trim() !== '';
		const url = isUpdate ? "/api/modifyPrice" : "/api/registPrice";

		if (!url) {
			alert("경로 없음.");
			return;
		}

		fetch(url, {
			method: "POST",
			body: formData,
			headers: {
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			}
		})
			.then(res => res.json())
			.then(data => {
				console.log("전송한 데이터 : ", data);
				alert("저장되었습니다.");


				form.reset();   // 폼 초기화

				bootstrap.Modal.getInstance(modalEl).hide();

				// 저장 후 테이블 데이터 새로고침
				if (window.priceTableInstance) {
					location.reload();
				}
			})
			.catch(err => {
				console.error("저장실패 : ", err);
				alert("저장에 실패했습니다.")
			});
	}



	// 품목리스트 테이블 컬럼에 대한 정의 (변경 없음)
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

			if (col === "단가그룹코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`;
				};
			}


			// '단가' 필드에 콤마 포맷터를 적용합니다. (선택 사항)
			if (col === "단가") {
				columnDef.formatter = "money";
				columnDef.formatterParams = {
					decimal: ".",
					thousand: ",",
					symbol: "₩",
					precision: false,
				};
			}

			if (col === "거래처설정") {
				columnDef.formatter = function() {
					return `<button onclick="choosePartner()">거래처설정</div>`;
				};
			}

			if (col === "품목설정") {
				columnDef.formatter = function() {
					return `<button onclick="chooseProduct()">품목설정</div>`;
				};
			}

			return columnDef;
		}).filter(c => c !== null)
	];

	// -------------------------------------------------------------------------
	// 2. Tabulator 인스턴스 생성 시 새로운 함수 사용
	// -------------------------------------------------------------------------
	const tableInstance = makePriceListTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;
});