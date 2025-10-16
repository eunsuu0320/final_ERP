// ==========================================================================
// 전역 변수 및 캐시 정의
// HTML onclick에서 호출되는 모든 함수가 접근할 수 있도록 전역으로 선언합니다.
// ==========================================================================
let priceTableInstance = null;
let allPartnersCache = [];
let allProductsCache = [];

// 메인 테이블의 초기 가시성 정의 (탭 전환 시 변경됨)
let defaultVisible = [
	"단가그룹코드",
	"단가그룹명",
	"단가유형",
	"할인율",
	"비고",
	"단가적용시작일",
	"단가적용종료일",
	"사용구분",
	"거래처설정",
	"품목설정"
];

// --------------------------------------------------------------------------
// [함수] HTML 체크박스 상태 설정 (전역)
// --------------------------------------------------------------------------
function setCheckboxState(visibleColumns) {
	const checkboxes = document.querySelectorAll('.colCheckbox');
	checkboxes.forEach(checkbox => {
		const colValue = checkbox.value;
		checkbox.checked = visibleColumns.includes(colValue);
	});
};


// --------------------------------------------------------------------------
// [함수] 체크박스 목록 표시 제어 (전역)
// --------------------------------------------------------------------------
function setCheckboxListVisibility(columnsToShow) {
	const allLabels = document.querySelectorAll('.mb-2 label');
	allLabels.forEach(label => {
		const checkbox = label.querySelector('.colCheckbox');
		if (checkbox) {
			const colValue = checkbox.value;
			label.style.display = columnsToShow.includes(colValue) ? 'inline-block' : 'none';
		}
	});
};

// --------------------------------------------------------------------------
// [함수] 메인 테이블 데이터 새로고침 (전역)
// --------------------------------------------------------------------------
function reloadMainTableData() {
	const activeBtn = document.querySelector('#priceTab button.btn-primary');
	if (activeBtn) {
		const activeType = activeBtn.getAttribute('data-type');
		changeTabData(activeType, activeBtn);
	} else {
		const allBtn = document.querySelector('#priceTab button[data-type="ALL"]');
		if (allBtn) {
			changeTabData('ALL', allBtn);
		} else {
			console.error("❌ 메인 테이블을 새로고침할 탭 버튼을 찾을 수 없습니다.");
		}
	}
};

// --------------------------------------------------------------------------
// [함수] Tabulator 컬럼 정의 생성 (⭐ 핵심 로직: 탭 유형에 따라 컬럼 구조 생성)
// --------------------------------------------------------------------------
function generateTabulatorColumns(visibleFields, tabType) {
	// columns 변수는 HTML에서 전역적으로 정의되어 있다고 가정합니다.
	if (typeof columns === 'undefined') {
		console.error("❌ 'columns' 변수가 정의되지 않았습니다. 컬럼 정의를 건너킵니다.");
		return [];
	}

	let dynamicColumns = [];

	// 탭 타입에 따른 추가/제거 컬럼 정의 (이 필드들은 columns 배열에 없을 수 있으므로 여기서 정의)
	const tabSpecificFields = (tabType === 'PRODUCT')
		? ["품목코드", "품목명", "품목그룹"]
		: (tabType === 'PARTNER')
			? ["거래처코드", "거래처명", "거래처유형"]
			: [];

	// 모든 잠재적인 컬럼 필드를 합치되 중복 제거
	const allPotentialColumns = [...new Set([...columns, ...tabSpecificFields])];

	// 1. 선택 박스 컬럼 (고정)
	dynamicColumns.push({
		formatter: "rowSelection",
		titleFormatter: "rowSelection",
		hozAlign: "center",
		headerHozAlign: "center",
		headerSort: false,
		width: 50,
		frozen: true
	});

	// 2. 데이터 컬럼 생성
	allPotentialColumns.forEach(col => {
		// '상품규격'이나 '단위'는 단독 컬럼으로 표시하지 않음 (기존 makeTabulator 로직 가정)
		if (col === "상품규격" || col === "단위") return;

		let def = {
			title: col,
			field: col,
			visible: visibleFields.includes(col) // 탭에 맞는 가시성 설정
		};

		// ⭐ 수정된 부분: 'ALL' 탭일 때만 단가그룹코드에 링크 및 스타일 적용
		if (col === "단가그룹코드") {
			if (tabType === 'ALL') {
				def.formatter = cell => {
					const val = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${val}')">${val}</div>`;
				};
			}
			// 'PRODUCT' 또는 'PARTNER' 탭일 경우 formatter를 정의하지 않아 기본 텍스트로 표시됩니다.
		}


		// 거래처 설정 버튼
		if (col === "거래처설정") {
			def.formatter = cell => {
				const row = cell.getRow().getData();
				if (row["단가유형"] === "거래처단가" || row["단가유형"] === "전체단가") {
					return `<button onclick="choosePartner('${row["단가고유코드"]}')" class="btn btn-sm btn-outline-secondary">거래처설정</button>`;
				}
				return "";
			};
		}

		// 품목 설정 버튼
		if (col === "품목설정") {
			def.formatter = cell => {
				const row = cell.getRow().getData();
				if (row["단가유형"] === "품목단가") {
					return `<button onclick="chooseProduct('${row["단가고유코드"]}')" class="btn btn-sm btn-outline-secondary">품목설정</button>`;
				}
				return "";
			};
		}

		// 단가/품목 탭에만 필요한 컬럼에 대한 기본값 설정
		if (col === "품목코드" || col === "거래처코드") {
			def.width = 120;
			def.hozAlign = "center";
		}

		dynamicColumns.push(def);
	});

	return dynamicColumns.filter(Boolean); // null 제거 (상품규격/단위)
}

// --------------------------------------------------------------------------
// [함수] 모달 테이블 검색 처리 (filterModalTable) - 전역
// --------------------------------------------------------------------------
function filterModalTable(event) {
    const button = event.currentTarget;
    // 클릭된 버튼에서 가장 가까운 .input-group을 찾습니다.
    const inputGroup = button.closest('.input-group');
    if (!inputGroup) return console.error("Could not find input-group for search button.");

    // .input-group 내에서 검색 텍스트를 가져옵니다.
    const searchInput = inputGroup.querySelector('input[type="text"]');
    const searchValue = searchInput ? searchInput.value.trim() : '';

    // 버튼이 속한 모달 요소를 찾습니다.
    const modalEl = button.closest('.modal');
    if (!modalEl) return console.error("Could not find parent modal for search button.");

    let tableInstance = null;
    let searchField = '';

    // 모달 ID를 기반으로 테이블 인스턴스와 검색 필드를 결정합니다.
    if (modalEl.id === 'choosePartnerModal') {
        tableInstance = window.partnerTableInstance;
        if (searchInput.id === 'partnerName') searchField = '거래처명';
        else if (searchInput.id === 'partnerLevelNameDisplay') searchField = '등급';
    } else if (modalEl.id === 'chooseProductModal') {
        tableInstance = window.productTableInstance;
        if (searchInput.id === 'productNameSearch') searchField = '품목명';
        else if (searchInput.id === 'productGroupNameDisplay') searchField = '품목그룹';
    }

    if (!tableInstance) return console.warn("Tabulator instance is not yet available for this modal.");
    if (!searchField) return console.error("Could not determine the search field for filtering.");

    console.log(`🔍 모달 테이블 필터링 시작: [${searchField}] = "${searchValue}"`);

    // Tabulator setFilter를 사용하여 화면 단에서 필터링을 적용합니다.
    // 검색어가 있을 경우 필터 적용, 없을 경우 필터 클리어
    if (searchValue) {
        // 'like' 오퍼레이터는 부분 일치(contains) 검색을 수행합니다.
        tableInstance.setFilter(searchField, 'like', searchValue);
    } else {
        // 검색어가 비어 있을 경우 해당 필드에 대한 필터만 제거합니다.
        // 현재는 단일 필터만 가정하므로 clearFilter()를 사용합니다.
        tableInstance.clearFilter(); 
    }

    // 필터링 후, 선택된 항목을 상단에 두는 정렬을 다시 적용합니다.
    tableInstance.setSort([{ field: "_sorter", dir: "asc" }]);
}


// --------------------------------------------------------------------------
// [함수] 거래처 모달용 Tabulator 렌더링 (전역)
// --------------------------------------------------------------------------
function renderPartnerTable(selectedPartners = []) {
	const tableContainerId = "partnerListTable";

	let current = Tabulator.findTable(`#${tableContainerId}`)[0];
	if (current) current.destroy();

	const PARTNER_CODE_FIELD = "거래처코드";
	const rowsToDisplay = allPartnersCache || [];
	const selectedCodes = new Set(selectedPartners);

	const formattedData = rowsToDisplay.map((p) => {
		const isSelected = selectedCodes.has(p[PARTNER_CODE_FIELD]);
		return {
			...p,
			_selected: isSelected,
			_sorter: isSelected ? 0 : 1, // 0이면 선택됨, 1이면 선택되지 않음
		};
	});

	const columns = [
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection",
			hozAlign: "center",
			headerHozAlign: "center",
			width: 50, // 고정 너비 (체크박스)
			headerSort: false,
			cellClick: (e, cell) => cell.getRow().toggleSelect(),
		},
		{ title: "거래처명", field: "거래처명" }, // 유동 너비 (남은 공간 모두 채움)
		{ title: "거래처코드", field: PARTNER_CODE_FIELD, width: 120 }, // 고정 너비
		{ title: "유형", field: "거래처유형", width: 100 }, // 고정 너비
		{ title: "등급", field: "등급", width: 80, hozAlign: "center" }, // 고정 너비
	];

	const instance = new Tabulator(`#${tableContainerId}`, {
		data: formattedData,
		// ⭐ 변경: 존재하는 컬럼들로 테이블을 꽉 채우기 위해 fitColumns 사용
		layout: "fitColumns", 
		height: "320px",
		index: PARTNER_CODE_FIELD,
		selectable: true,
		initialSort: [{ field: "_sorter", dir: "asc" }],
		columns,
		// 검색 결과가 없을 때 메시지 표시
		placeholder: "검색된 데이터가 없습니다.", 
	});

	instance.on("renderComplete", () => {
		let selectedCount = 0;
		instance.getRows().forEach((row) => {
			if (row.getData()._selected) {
				row.select();
				selectedCount++;
			}
		});
		console.log(`✅ 거래처 체크박스 자동 선택 완료 (${selectedCount}개)`);
	});

	window.partnerTableInstance = instance; // 모달 내 테이블 인스턴스 전역 저장
}

// --------------------------------------------------------------------------
// [함수] 품목 모달용 Tabulator 렌더링 (전역)
// --------------------------------------------------------------------------
function renderProductTable(selectedProducts = []) {
	const tableContainerId = "productListTable";

	let current = Tabulator.findTable(`#${tableContainerId}`)[0];
	if (current) current.destroy();

	const PRODUCT_CODE_FIELD = "품목코드";
	const rowsToDisplay = allProductsCache || [];
	const selectedCodes = new Set(selectedProducts);

	const formattedData = rowsToDisplay.map(p => {
		const isSelected = selectedCodes.has(p[PRODUCT_CODE_FIELD]);
		return {
			...p,
			_selected: isSelected,
			_sorter: isSelected ? 0 : 1,
		};
	});

	const columns = [
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection",
			hozAlign: "center",
			headerHozAlign: "center",
			width: 50, // 고정 너비 (체크박스)
			headerSort: false,
			cellClick: (e, cell) => cell.getRow().toggleSelect(),
		},
		{ title: "품목명", field: "품목명" }, // 유동 너비 (남은 공간 모두 채움)
		{ title: "품목코드", field: PRODUCT_CODE_FIELD, width: 120 }, // 고정 너비
		{ title: "품목그룹", field: "품목그룹", width: 100 }, // 고정 너비
		{ title: "규격/단위", field: "규격/단위", width: 80, hozAlign: "center" }, // 고정 너비
	];

	const instance = new Tabulator(`#${tableContainerId}`, {
		data: formattedData,
		// ⭐ 변경: 존재하는 컬럼들로 테이블을 꽉 채우기 위해 fitColumns 사용
		layout: "fitColumns", 
		height: "320px",
		index: PRODUCT_CODE_FIELD,
		selectable: true,
		initialSort: [{ field: "_sorter", dir: "asc" }],
		columns,
		// 검색 결과가 없을 때 메시지 표시
		placeholder: "검색된 데이터가 없습니다.",
	});

	instance.on("renderComplete", () => {
		let selectedCount = 0;
		instance.getRows().forEach(row => {
			if (row.getData()._selected) {
				row.select();
				selectedCount++;
			}
		});
		console.log(`✅ 품목 체크박스 자동 선택 완료 (${selectedCount}개)`);
	});

	window.productTableInstance = instance; // 모달 내 테이블 인스턴스 전역 저장
}

// --------------------------------------------------------------------------
// [함수] 탭 전환 (changeTabData) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
function changeTabData(type, clickedButton) {
	console.log(`[changeTabData] 탭 클릭 감지: ${type}`);

	const tabButtons = document.querySelectorAll('#priceTab button');
	tabButtons.forEach(btn => {
		btn.classList.remove('btn-primary');
		btn.classList.add('btn-outline-primary');
	});
	if (clickedButton) {
		clickedButton.classList.remove('btn-outline-primary');
		clickedButton.classList.add('btn-primary');
	}

	const table = priceTableInstance;
	if (!table) return console.error("Tabulator instance not initialized (priceTableInstance is null)");

	let apiUrl = "/api/priceList";

	// 탭에 따라 visible 필드와 API URL 정의
	switch (type) {
		case 'ALL':
			apiUrl = "/api/priceList";
			defaultVisible = [
				"단가그룹코드", "단가그룹명", "단가유형", "할인율", "비고",
				"단가적용시작일", "단가적용종료일", "사용구분", "거래처설정", "품목설정"
			];
			break;
		case 'PRODUCT':
			apiUrl = "/api/pricetabProduct";
			defaultVisible = [
				"품목코드", "품목명", "품목그룹", "단가그룹코드", "단가그룹명",
				"단가유형", "할인율", "비고", "단가적용시작일", "단가적용종료일", "사용구분"
			];
			break;
		case 'PARTNER':
			apiUrl = "/api/pricetabPartner";
			defaultVisible = [
				"거래처코드", "거래처명", "거래처유형", "단가그룹코드", "단가그룹명",
				"단가유형", "할인율", "비고", "단가적용시작일", "단가적용종료일", "사용구분"
			];
			break;
	}

	// 1. 새 컬럼 정의 생성 및 Tabulator에 적용 (⭐ 핵심 수정)
	const newColumns = generateTabulatorColumns(defaultVisible, type);
	table.setColumns(newColumns); // 컬럼 구조를 변경하여 새로운 데이터 필드를 수용

	// 2. 체크박스 및 가시성 업데이트
	setCheckboxListVisibility(defaultVisible);
	setCheckboxState(defaultVisible);
	// updateTabulatorVisibility(table, defaultVisible); // 오류 유발 함수 호출 제거

	// 3. API 호출 및 데이터 설정
	const loadingOverlay = document.getElementById('loading-overlay');
	if (loadingOverlay) {
		console.log("loadingOverlay 실행");
		loadingOverlay.style.display = 'flex';
	} else {
		console.log("loadingOverlay 없음.");
	}

	fetch(apiUrl)
		.then(res => res.json())
		.then(data => {
			table.setData(data.rows || []);
			table.redraw(true); // setColumns 후 레이아웃 재적용
		})
		.finally(() => {
			if (loadingOverlay) loadingOverlay.style.display = 'none';
		});
};

// --------------------------------------------------------------------------
// [함수] 거래처 설정 모달 열기 (choosePartner) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
async function choosePartner(priceUniqueCode) {
	const modalEl = document.getElementById("choosePartnerModal");

	if (!modalEl) {
		console.error("❌ Modal Element not found: #choosePartnerModal");
		const overlayToControl = document.getElementById('loading-overlay');
		if (overlayToControl) overlayToControl.style.display = 'none';
		return;
	}

	let modal = bootstrap.Modal.getInstance(modalEl);
	if (!modal) {
		try {
			modal = new bootstrap.Modal(modalEl);
		} catch (e) {
			console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
			const overlayToControl = document.getElementById('loading-overlay');
			if (overlayToControl) overlayToControl.style.display = 'none';
			return;
		}
	}

	const overlayToControl = document.getElementById('loading-overlay');
	const saveButton = modalEl.querySelector('.savePartnerButton');

	if (overlayToControl) overlayToControl.style.display = 'flex';

	if (saveButton) {
		saveButton.setAttribute('data-price-unique-code', priceUniqueCode);
	} else {
		console.warn("📌 경고: 'savePartnerButton' 클래스를 가진 저장 버튼을 찾을 수 없습니다. HTML을 확인하세요.");
	}

	try {
		const res = await fetch(`/api/price/getPartner?priceUniqueCode=${encodeURIComponent(priceUniqueCode)}`);
		const partners = await res.json();
		console.log("📋 서버에서 받은 거래처 목록:", partners);

		if (Array.isArray(partners)) {
			renderPartnerTable(partners);
		} else {
			renderPartnerTable([]);
		}

		modal.show();

	} catch (err) {
		console.error("❌ 거래처 설정 로드 실패:", err);
		renderPartnerTable([]);
	} finally {
		if (overlayToControl) overlayToControl.style.display = 'none';
	}
};


// --------------------------------------------------------------------------
// [함수] 품목 설정 모달 열기 (chooseProduct) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
async function chooseProduct(priceUniqueCode) {
	const modalEl = document.getElementById("chooseProductModal");

	if (!modalEl) {
		console.error("❌ Modal Element not found: #chooseProductModal");
		const overlayToControl = document.getElementById('loading-overlay');
		if (overlayToControl) overlayToControl.style.display = 'none';
		return;
	}

	let modal = bootstrap.Modal.getInstance(modalEl);
	if (!modal) {
		try {
			modal = new bootstrap.Modal(modalEl);
		} catch (e) {
			console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
			const overlayToControl = document.getElementById('loading-overlay');
			if (overlayToControl) overlayToControl.style.display = 'none';
			return;
		}
	}

	const overlayToControl = document.getElementById('loading-overlay');
	const saveButton = modalEl.querySelector('.saveProductButton');

	if (overlayToControl) overlayToControl.style.display = 'flex';


	if (saveButton) {
		saveButton.setAttribute('data-price-unique-code', priceUniqueCode);
		console.log(priceUniqueCode);
	} else {
		console.warn("📌 경고: 'saveProductButton' ID를 가진 저장 버튼을 찾을 수 없습니다. HTML을 확인하세요.");
	}

	try {
		const res = await fetch(`/api/price/getProduct?priceUniqueCode=${encodeURIComponent(priceUniqueCode)}`);
		const products = await res.json();
		console.log("📋 서버에서 받은 품목 목록:", products);

		if (Array.isArray(products)) {
			renderProductTable(products);
		} else {
			renderProductTable([]);
		}

		modal.show();

	} catch (err) {
		console.error("❌ 품목 설정 로드 실패:", err);
		renderProductTable([]);
	} finally {
		if (overlayToControl) overlayToControl.style.display = 'none';
	}
};


// --------------------------------------------------------------------------
// [함수] 거래처 설정 저장 (saveChoosePartner) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
function saveChoosePartner(priceUniqueCode) {
	const tableInstance = window.partnerTableInstance; // 모달 테이블 인스턴스 사용
	const modalEl = document.getElementById("choosePartnerModal");

	if (!tableInstance) {
		console.error("❌ 거래처 목록 Tabulator 인스턴스를 찾을 수 없습니다.");
		return;
	}

	const selectedRows = tableInstance.getSelectedData();

	if (!priceUniqueCode) {
		console.error("저장할 단가그룹 코드를 찾을 수 없습니다.");
		return;
	}

	const selectedPartnerCodes = selectedRows.map(row => row["거래처코드"]);
	console.log(`저장할 단가그룹: ${priceUniqueCode}, 선택된 거래처 코드:`, selectedPartnerCodes);

	const payload = {
		priceUniqueCode: priceUniqueCode,
		partnerCodes: selectedPartnerCodes
	};

	const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
	const csrfTokenEl = document.querySelector('meta[name="_csrf"]');

	if (!csrfHeaderEl || !csrfTokenEl) {
		console.error("❌ CSRF 토큰 메타 태그를 찾을 수 없습니다.");
		return;
	}

	const csrfHeader = csrfHeaderEl.content;
	const csrfToken = csrfTokenEl.content;

	fetch("/api/price/savePartners", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[csrfHeader]: csrfToken
		},
		body: JSON.stringify(payload)
	})
		.then(res => {
			if (!res.ok) throw new Error("Server response was not ok.");
			return res.json();
		})
		.then(() => {
			bootstrap.Modal.getInstance(modalEl).hide();
			reloadMainTableData();
		})
		.catch(err => {
			console.error("❌ 거래처 설정 저장 실패:", err);
		});
}


// --------------------------------------------------------------------------
// [함수] 품목 설정 저장 (saveChooseProduct) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
function saveChooseProduct(priceUniqueCode) {
	const tableInstance = window.productTableInstance; // 모달 테이블 인스턴스 사용
	const modalEl = document.getElementById("chooseProductModal");

	if (!tableInstance) {
		console.error("❌ 품목 목록 Tabulator 인스턴스를 찾을 수 없습니다.");
		return;
	}

	const selectedRows = tableInstance.getSelectedData();

	if (!priceUniqueCode) {
		console.error("저장할 단가그룹 코드를 찾을 수 없습니다.");
		return;
	}

	const selectedProductCodes = selectedRows.map(row => row["품목코드"]);
	console.log(`저장할 단가그룹: ${priceUniqueCode}, 선택된 품목 코드:`, selectedProductCodes);

	const payload = {
		priceUniqueCode: priceUniqueCode,
		productCodes: selectedProductCodes
	};


	const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
	const csrfTokenEl = document.querySelector('meta[name="_csrf"]');

	if (!csrfHeaderEl || !csrfTokenEl) {
		console.error("❌ CSRF 토큰 메타 태그를 찾을 수 없습니다.");
		return;
	}

	const csrfHeader = csrfHeaderEl.content;
	const csrfToken = csrfTokenEl.content;

	fetch("/api/price/saveProducts", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			[csrfHeader]: csrfToken
		},
		body: JSON.stringify(payload)
	})
		.then(res => {
			if (!res.ok) throw new Error("Server response was not ok.");
			return res.json();
		})
		.then(() => {
			bootstrap.Modal.getInstance(modalEl).hide();
			reloadMainTableData();
		})
		.catch(err => {
			console.error("❌ 품목 설정 저장 실패:", err);
		});
}


// --------------------------------------------------------------------------
// [함수] 단가 상세 모달 열기 (showDetailModal) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
function showDetailModal(modalType, keyword) {
	const modalName = modalType === 'detail' ? '단가상세정보' : '단가등록';
	const modalEl = document.getElementById("newDetailModal");

	if (!modalEl) {
		console.error("❌ Modal Element not found: #newDetailModal");
		return;
	}

	let modal = bootstrap.Modal.getInstance(modalEl);
	if (!modal) {
		modal = new bootstrap.Modal(modalEl);
	}

	const form = document.getElementById("itemForm");

	document.querySelector("#newDetailModal .modal-title").textContent = modalName;
	form.reset();
	modal.show();

	if (modalType === 'detail' && keyword) {
		// loadDetailData 함수는 외부에서 정의된 것으로 가정하고 호출
		if (typeof loadDetailData === 'function') {
			loadDetailData('price', keyword, form);
		} else {
			console.warn("loadDetailData 함수가 정의되지 않았습니다. 상세 데이터 로드를 건너킵니다.");
		}
	}
};

// --------------------------------------------------------------------------
// [함수] 모달 저장 (saveModal) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
function saveModal() {
	const form = document.getElementById("itemForm");
	const modalEl = document.getElementById("newDetailModal");
	const formData = new FormData(form);

	// checkRequired 함수는 외부에서 정의된 것으로 가정
	if (typeof checkRequired === 'function' && !checkRequired(form)) return;
	if (typeof checkRequired !== 'function') console.warn("checkRequired 함수가 정의되지 않아 유효성 검사를 건너킵니다.");


	const priceGroupCode = formData.get("priceGroupCode");
	const isUpdate = priceGroupCode && priceGroupCode.trim() !== '';
	const url = isUpdate ? "/api/modifyPrice" : "/api/registPrice";

	fetch(url, {
		method: "POST",
		body: formData,
		headers: {
			[document.querySelector('meta[name="_csrf_header"]').content]:
				document.querySelector('meta[name="_csrf"]').content
		}
	})
		.then(res => res.json())
		.then(() => {
			form.reset();
			bootstrap.Modal.getInstance(modalEl).hide();
			reloadMainTableData();
		})
		.catch(err => {
			console.error("저장 실패:", err);
		});
};

// --------------------------------------------------------------------------
// [함수] 거래처 설정 모달 초기화 (resetChoosePartner) - 모달 닫힘 시 호출
// --------------------------------------------------------------------------
function resetChoosePartner() {
	const tableInstance = window.partnerTableInstance;
	if (tableInstance) {
		tableInstance.deselectRow();
		// 검색 필드 초기화 (ID 사용은 모달이 닫힐 때 모든 필드가 확실히 초기화되도록 하기 위함)
		document.getElementById("partnerName").value = "";
		document.getElementById("partnerLevelNameDisplay").value = "";
		document.getElementById("partnerCode").value = "";
		document.getElementById("partnerLevel").value = "";

		console.log("✅ 거래처 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

		if (allPartnersCache) {
			// 필터도 초기화
			tableInstance.clearFilter();
			tableInstance.setData(allPartnersCache.map(p => ({
				...p,
				_selected: false,
				_sorter: 1
			})));
		} else {
			console.warn("전체 거래처 캐시 데이터(allPartnersCache)가 없어 테이블 데이터 초기화는 건너킵니다.");
		}
	}
}

// --------------------------------------------------------------------------
// [함수] 품목 설정 모달 초기화 (resetChooseProduct) - 모달 닫힘 시 호출
// --------------------------------------------------------------------------
function resetChooseProduct() {
	const tableInstance = window.productTableInstance;
	if (tableInstance) {
		tableInstance.deselectRow();
		// 검색 필드 초기화 (ID 사용은 모달이 닫힐 때 모든 필드가 확실히 초기화되도록 하기 위함)
		document.getElementById("productCodeSearch").value = "";
		document.getElementById("productNameSearch").value = "";
		document.getElementById("productGroupCode").value = "";
		document.getElementById("productGroupNameDisplay").value = "";

		console.log("✅ 품목 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

		if (allProductsCache) {
			// 필터도 초기화
			tableInstance.clearFilter();
			tableInstance.setData(allProductsCache.map(p => ({
				...p,
				_selected: false,
				_sorter: 1
			})));
		} else {
			console.warn("전체 품목 캐시 데이터(allProductsCache)가 없어 테이블 데이터 초기화는 건너깁니다.");
		}
	}
}

// ==========================================================================
// DOMContentLoaded: 초기화 및 Tabulator 생성 로직 (단 한번만 실행됨)
// ==========================================================================
document.addEventListener("DOMContentLoaded", async function() {

	const mainLoadingOverlay = document.getElementById('loading-overlay');
	if (mainLoadingOverlay) {
		mainLoadingOverlay.style.display = 'none';
	}

	// --------------------------------------------------------------------------
	// 1. 거래처 및 품목 전체 목록 캐시 로드
	// --------------------------------------------------------------------------
	async function loadAllDataForCaching() {
		try {
			console.log("📦 거래처 전체 목록을 최초로 로딩합니다...");
			const allRes = await fetch("/api/price/getAllPartner");
			const allData = await allRes.json();
			if (allData && Array.isArray(allData.rows)) {
				allPartnersCache = allData.rows;
				console.log("✅ 거래처 전체 목록 캐싱 완료:", allPartnersCache.length);
			} else {
				allPartnersCache = [];
			}
		} catch (e) {
			allPartnersCache = [];
		}

		try {
			console.log("📦 품목 전체 목록을 최초로 로딩합니다...");
			const allRes = await fetch("/api/price/getAllProduct");
			const allData = await allRes.json();
			if (allData && Array.isArray(allData.rows)) {
				allProductsCache = allData.rows;
				console.log("✅ 품목 전체 목록 캐싱 완료:", allProductsCache.length);
			} else {
				allProductsCache = [];
			}
		} catch (e) {
			allProductsCache = [];
		}
	}
	loadAllDataForCaching();

	// --------------------------------------------------------------------------
	// 2. 메인 Tabulator 컬럼 정의 
	// --------------------------------------------------------------------------
	if (typeof columns === 'undefined' || typeof rows === 'undefined' || typeof makeTabulator === 'undefined') {
		console.error("❌ 'columns', 'rows', 또는 'makeTabulator' 변수가 정의되지 않았습니다. HTML 파일의 상단 스크립트를 확인하세요.");
		return;
	}

	// generateTabulatorColumns를 사용하여 초기 ALL 탭의 컬럼 구조를 생성합니다.
	let tabulatorColumns = generateTabulatorColumns(defaultVisible, 'ALL');

	// --------------------------------------------------------------------------
	// 3. 메인 단가 테이블 생성 함수 (내부 함수)
	// --------------------------------------------------------------------------
	const makePriceListTabulator = function(rows, tabulatorColumns) {
		const priceSpecificOptions = {
			ajaxURL: "/api/priceList",
			ajaxConfig: "GET"
		};
		// makeTabulator는 외부에서 정의된 Tabulator 초기화 함수로 가정
		return makeTabulator(rows, tabulatorColumns, priceSpecificOptions);
	};

	// --------------------------------------------------------------------------
	// 4. 메인 Tabulator 초기화 및 전역 변수에 할당
	// --------------------------------------------------------------------------
	priceTableInstance = makePriceListTabulator(rows, tabulatorColumns);


	// --------------------------------------------------------------------------
	// 5. 초기 체크박스 상태 설정 및 가시성 적용
	// --------------------------------------------------------------------------
	setCheckboxState(defaultVisible);
	setCheckboxListVisibility(defaultVisible);
	// updateTabulatorVisibility(priceTableInstance, defaultVisible); // 오류 유발 함수 호출 제거

	// --------------------------------------------------------------------------
	// 6. 모달 닫힘 시 초기화 이벤트 리스너 설정
	// --------------------------------------------------------------------------
	const partnerModalElement = document.getElementById('choosePartnerModal');
	const productModalElement = document.getElementById('chooseProductModal');

	if (partnerModalElement) {
		partnerModalElement.addEventListener('hidden.bs.modal', resetChoosePartner);
	}

	if (productModalElement) {
		productModalElement.addEventListener('hidden.bs.modal', resetChooseProduct);
	}
	
	// --------------------------------------------------------------------------
	// 7. 모달 검색/초기화 버튼 이벤트 리스너 설정
	// --------------------------------------------------------------------------
	const searchButtons = document.querySelectorAll('.modalSearchBtn');
	searchButtons.forEach(button => {
	    button.addEventListener('click', filterModalTable);
	});
	
	// '.resetModalSearch' 버튼 클릭 시, '.modalSearchCondition' 입력 필드 초기화
	const resetButtons = document.querySelectorAll('.resetModalSearch');
	resetButtons.forEach(button => {
	    button.addEventListener('click', (event) => {
	        const modalEl = event.currentTarget.closest('.modal');
	        if (!modalEl) return;
	
			// 1. modalSearchCondition 클래스를 가진 모든 input의 값을 초기화합니다.
			const searchInputs = modalEl.querySelectorAll('.modalSearchCondition');
			searchInputs.forEach(input => {
				input.value = '';
			});

	        let tableInstance = null;
	
	        if (modalEl.id === 'choosePartnerModal') {
	            tableInstance = window.partnerTableInstance;
	        } else if (modalEl.id === 'chooseProductModal') {
	            tableInstance = window.productTableInstance;
	        }
	
	        if (tableInstance) {
	            tableInstance.clearFilter();
	            // 필터 초기화 후, 선택된 항목을 상단에 두는 정렬을 다시 적용
	            tableInstance.setSort([{ field: "_sorter", dir: "asc" }]);
	            console.log("✅ 모달 검색 조건 및 필터 초기화 완료.");
	        }
	    });
	});

});
