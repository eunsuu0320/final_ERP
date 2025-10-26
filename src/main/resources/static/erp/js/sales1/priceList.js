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
//  - 탭별 defaultVisible 컬럼만 라벨을 보여주고, 나머지는 숨김 처리
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

// ✅ (추가) 현재 화면에서 "보이는" 컬럼 체크박스만 반환
function getVisibleColumnCheckboxes() {
	const labels = Array.from(document.querySelectorAll('.mb-2 label'));
	return labels
		.map(l => ({ label: l, cb: l.querySelector('.colCheckbox') }))
		.filter(x => x.cb && x.label.style.display !== 'none');
}

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

	// 탭 타입에 따른 추가/제거 컬럼 정의
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
		if (col === "상품규격" || col === "단위") return;

		let def = {
			title: col,
			field: col,
			visible: visibleFields.includes(col) // 탭에 맞는 가시성 설정
		};

		// '단가그룹코드' 클릭 시 상세 모달
		if (col === "단가그룹코드") {
			def.formatter = cell => {
				const val = cell.getValue();
				return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${val}')">${val}</div>`;
			};
		}



		if (col === "비고") {
			def.hozAlign = "left";
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

		// 코드류 정렬/너비
		if (col === "품목코드" || col === "거래처코드") {
			def.width = 120;
			def.hozAlign = "center";
		}

		dynamicColumns.push(def);
	});

	return dynamicColumns.filter(Boolean); // null 제거
}

// --------------------------------------------------------------------------
// [함수] 모달 테이블 검색 처리 (filterModalTable) - 전역
// --------------------------------------------------------------------------
function filterModalTable(event) {
	const button = event.currentTarget;
	const inputGroup = button.closest('.input-group');
	if (!inputGroup) return console.error("Could not find input-group for search button.");

	const searchInput = inputGroup.querySelector('input[type="text"]');
	const searchValue = searchInput ? searchInput.value.trim() : '';

	const modalEl = button.closest('.modal');
	if (!modalEl) return console.error("Could not find parent modal for search button.");

	let tableInstance = null;
	let searchField = '';

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

	if (searchValue) {
		tableInstance.setFilter(searchField, 'like', searchValue);
	} else {
		tableInstance.clearFilter();
	}

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
			width: 50,
			headerSort: false,
			cellClick: (e, cell) => cell.getRow().toggleSelect(),
		},
		{ title: "거래처명", field: "거래처명" },
		{ title: "거래처코드", field: PARTNER_CODE_FIELD },
		{ title: "유형", field: "거래처유형" },
	];

	const instance = new Tabulator(`#${tableContainerId}`, {
		data: formattedData,
		layout: "fitColumns",
		height: "320px",
		index: PARTNER_CODE_FIELD,
		selectable: true,
		initialSort: [{ field: "_sorter", dir: "asc" }],
		columns,
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
		// ✅ 체크박스 선택 컬럼 추가
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection", // 헤더 전체 선택 박스
			hozAlign: "center",
			headerHozAlign: "center",
			width: 50,
			headerSort: false,
			cellClick: (e, cell) => cell.getRow().toggleSelect(),
		},
		{ title: "품목명", field: "품목명" },
		{ title: "품목코드", field: PRODUCT_CODE_FIELD, hozAlign: "center" },
		{ title: "품목그룹", field: "품목그룹", hozAlign: "center" },
		{ title: "규격/단위", field: "규격/단위", hozAlign: "center" },
	];

	const instance = new Tabulator(`#${tableContainerId}`, {
		data: formattedData,
		layout: "fitColumns",
		height: "320px",
		index: PRODUCT_CODE_FIELD,
		selectable: true,
		initialSort: [{ field: "_sorter", dir: "asc" }],
		columns,
		placeholder: "검색된 데이터가 없습니다.",
	});

	// ✅ 자동 선택 처리
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

	// 1) 새 컬럼 정의 생성 및 적용
	const newColumns = generateTabulatorColumns(defaultVisible, type);
	table.setColumns(newColumns);

	// 2) 체크박스 UI를 탭별 목록만 보이도록 동기화 + 상태 동기화
	setCheckboxListVisibility(defaultVisible);
	setCheckboxState(defaultVisible);

	// 2-1) 전체선택 체크박스 상태도 "보이는 항목"만 기준으로 갱신
	const selectAll = document.getElementById('selectAllColumns');
	if (selectAll) {
		const visibles = getVisibleColumnCheckboxes().map(x => x.cb);
		selectAll.checked = visibles.length > 0 && visibles.every(c => c.checked);
	}

	// 3) API 호출 및 데이터 설정
	const loadingOverlay = document.getElementById('loading-overlay');
	if (loadingOverlay) loadingOverlay.style.display = 'flex';

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
		return;
	}

	let modal = bootstrap.Modal.getInstance(modalEl);
	if (!modal) {
		try {
			modal = new bootstrap.Modal(modalEl);
		} catch (e) {
			console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
			return;
		}
	}

	// ✅ 기존 오버레이 제거: 메인 영역의 overlayToControl 제거
	const mainOverlay = document.getElementById('loading-overlay');
	if (mainOverlay) mainOverlay.style.display = 'none';

	// ✅ [A] 모달 내부 오버레이 표시
	let overlayContainer = modalEl.querySelector(".modal-content");
	let overlay = overlayContainer.querySelector(".loading-overlay");
	if (!overlay) {
		overlay = document.createElement("div");
		overlay.className = "loading-overlay";
		overlay.innerHTML = `
			<div class="text-center">
				<div class="spinner-border text-primary mb-3" role="status" style="width:3rem;height:3rem;">
					<span class="visually-hidden">Loading...</span>
				</div>
				<p class="text-secondary fw-semibold mb-0">거래처 정보를 불러오는 중...</p>
			</div>
		`;
		Object.assign(overlay.style, {
			position: "absolute",
			top: 0,
			left: 0,
			width: "100%",
			height: "100%",
			display: "flex",
			flexDirection: "column",
			justifyContent: "center",
			alignItems: "center",
			backgroundColor: "rgba(255,255,255,0.9)",
			zIndex: 1056,
			borderRadius: "0.5rem",
		});
		overlayContainer.style.position = "relative";
		overlayContainer.appendChild(overlay);
	}

	overlay.style.display = "flex";

	const saveButton = modalEl.querySelector('.savePartnerButton');
	if (saveButton) {
		saveButton.setAttribute('data-price-unique-code', priceUniqueCode);
	} else {
		console.warn("📌 경고: 'savePartnerButton' 클래스를 가진 저장 버튼을 찾을 수 없습니다.");
	}

	try {
		// ✅ 거래처 목록 조회
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
		// ✅ [B] 오버레이 숨기기
		setTimeout(() => {
			if (overlay) overlay.style.display = "none";
		}, 400);
	}
}


// --------------------------------------------------------------------------
// [함수] 품목 설정 모달 열기 (chooseProduct) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// [함수] 품목 설정 모달 열기 (chooseProduct) - HTML onclick에서 직접 호출됨
// --------------------------------------------------------------------------
async function chooseProduct(priceUniqueCode) {
	const modalEl = document.getElementById("chooseProductModal");
	if (!modalEl) {
		console.error("❌ Modal Element not found: #chooseProductModal");
		return;
	}

	let modal = bootstrap.Modal.getInstance(modalEl);
	if (!modal) {
		try {
			modal = new bootstrap.Modal(modalEl);
		} catch (e) {
			console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
			return;
		}
	}

	// ✅ 저장 버튼에 priceUniqueCode 바인딩
	const saveButton = modalEl.querySelector(".saveProductButton");
	if (saveButton) {
		saveButton.setAttribute("data-price-unique-code", priceUniqueCode);
		console.log("priceUniqueCode:", priceUniqueCode);
	} else {
		console.warn("📌 경고: '.saveProductButton' 클래스를 가진 저장 버튼을 찾을 수 없습니다. HTML을 확인하세요.");
	}

	// ✅ 모달 내부 오버레이 (modal-content 위에 표시)
	const overlayContainer = modalEl.querySelector(".modal-content") || modalEl;
	let overlay = overlayContainer.querySelector(".loading-overlay");
	if (!overlay) {
		overlay = document.createElement("div");
		overlay.className = "loading-overlay";
		overlay.innerHTML = `
      <div class="text-center">
        <div class="spinner-border text-primary mb-3" role="status" style="width:3rem;height:3rem;">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="text-secondary fw-semibold mb-0">품목 정보를 불러오는 중...</p>
      </div>
    `;
		Object.assign(overlay.style, {
			position: "absolute",
			inset: "0",
			width: "100%",
			height: "100%",
			display: "flex",
			flexDirection: "column",
			justifyContent: "center",
			alignItems: "center",
			backgroundColor: "rgba(255,255,255,0.9)",
			zIndex: 1056,
			borderRadius: "0.5rem",
		});
		// 부모가 relative 아니면 오버레이가 안 보일 수 있음
		if (getComputedStyle(overlayContainer).position === "static") {
			overlayContainer.style.position = "relative";
		}
		overlayContainer.appendChild(overlay);
	}
	overlay.style.display = "flex";

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
		// 에러 메시지를 오버레이 텍스트로 잠깐 보여주고 싶다면:
		// overlay.querySelector("p").textContent = "목록을 불러오지 못했습니다.";
	} finally {
		setTimeout(() => {
			if (overlay) overlay.style.display = "none";
		}, 300);
	}
}


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

function showTableLoading(show = true) {
	const overlay = document.getElementById("loading-overlay");
	if (overlay) overlay.style.display = show ? "flex" : "none";
};

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
		if (typeof loadDetailData === 'function') {
			loadDetailData('price', keyword, form);
		} else {
			console.warn("loadDetailData 함수가 정의되지 않았습니다. 상세 데이터 로드를 건너킵니다.");
		}
	}
};

// --------------------------------------------------------------------------
// [함수] 모달 저장 (saveModal) - HTML onclick에서 직접 호출됨
function saveModal() {
	const form = document.getElementById("itemForm");
	const modalEl = document.getElementById("newDetailModal");

	// ✅ 유효성 검사
	if (typeof checkRequired === 'function' && !checkRequired(form)) return;
	if (typeof checkRequired !== 'function')
		console.warn("checkRequired 함수가 정의되지 않아 유효성 검사를 건너뜁니다.");

	const formData = new FormData(form);
	const priceGroupCode = formData.get("priceGroupCode");
	const isUpdate = priceGroupCode && priceGroupCode.trim() !== '';
	const url = isUpdate ? "/api/modifyPrice" : "/api/registPrice";

	// ✅ [A] 모달 오버레이 표시
	let overlayContainer = modalEl ? modalEl.querySelector(".modal-content") : document.body;
	let overlay = overlayContainer.querySelector(".loading-overlay");
	if (!overlay) {
		overlay = document.createElement("div");
		overlay.className = "loading-overlay";
		overlay.innerHTML = `
			<div class="text-center">
				<div class="spinner-border text-primary mb-3" role="status" style="width:3rem;height:3rem;">
					<span class="visually-hidden">Loading...</span>
				</div>
				<p class="text-secondary fw-semibold mb-0">저장 중입니다...</p>
			</div>
		`;
		Object.assign(overlay.style, {
			position: "absolute",
			top: 0,
			left: 0,
			width: "100%",
			height: "100%",
			display: "flex",
			flexDirection: "column",
			justifyContent: "center",
			alignItems: "center",
			backgroundColor: "rgba(255,255,255,0.9)",
			zIndex: 2000,
			borderRadius: "0.5rem",
		});
		overlayContainer.style.position = "relative";
		overlayContainer.appendChild(overlay);
	}
	overlay.style.display = "flex";

	// ✅ [1] 할인율 변환 (예: 30 → 0.3)
	let discountValue = formData.get("discountPct");
	if (discountValue !== null && discountValue !== "") {
		let parsed = parseFloat(discountValue);
		if (!isNaN(parsed)) {
			const converted = parsed / 100; // ← 0.3으로 변환
			formData.set("discountPct", converted);
			document.getElementById("discountPct").value = converted;
			console.log(`📦 할인율 변환 완료: ${parsed}% → ${converted}`);
		}
	}

	// ✅ [2] fetch 전송
	fetch(url, {
		method: "POST",
		body: formData,
		headers: {
			[document.querySelector('meta[name="_csrf_header"]').content]:
				document.querySelector('meta[name="_csrf"]').content
		}
	})
		.then(async res => {
			const text = await res.text();
			let data;
			try {
				data = JSON.parse(text);
			} catch (err) {
				console.error("⚠️ JSON 파싱 실패. 서버 응답 원문:", text);
				throw new Error("서버 응답이 유효한 JSON 형식이 아닙니다.");
			}

			if (!res.ok) throw new Error(data.message || `서버 오류: ${res.status}`);
			return data;
		})
		.then(() => {
			console.log("✅ 저장 성공");
			form.reset();
			bootstrap.Modal.getInstance(modalEl).hide();

			if (typeof reloadMainTableData === 'function') reloadMainTableData();
		})
		.catch(err => {
			console.error("저장 실패:", err);
			alert("저장 실패. 콘솔을 확인하세요.");
		})
		.finally(() => {
			// ✅ [B] 저장 완료 후 오버레이 제거
			setTimeout(() => {
				if (overlay) overlay.style.display = "none";
			}, 400);
		});
}




// --------------------------------------------------------------------------
// [함수] 거래처 설정 모달 초기화 (resetChoosePartner) - 모달 닫힘 시 호출
// --------------------------------------------------------------------------
function resetChoosePartner() {
	const tableInstance = window.partnerTableInstance;
	if (tableInstance) {
		tableInstance.deselectRow();
		document.getElementById("partnerName").value = "";
		document.getElementById("partnerLevelNameDisplay").value = "";
		document.getElementById("partnerCode").value = "";
		document.getElementById("partnerLevel").value = "";

		console.log("✅ 거래처 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

		if (allPartnersCache) {
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
		document.getElementById("productCodeSearch").value = "";
		document.getElementById("productNameSearch").value = "";
		document.getElementById("productGroupCode").value = "";
		document.getElementById("productGroupNameDisplay").value = "";

		console.log("✅ 품목 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

		if (allProductsCache) {
			tableInstance.clearFilter();
			tableInstance.setData(allProductsCache.map(p => ({
				...p,
				_selected: false,
				_sorter: 1
			})));
		} else {
			console.warn("전체 품목 캐시 데이터(allProductsCache)가 없어 테이블 데이터 초기화는 건너뜁니다.");
		}
	}
}

// ==========================================================================
// DOMContentLoaded: 초기화 및 Tabulator 생성 로직 (단 한번만 실행됨)
// ==========================================================================
document.addEventListener("DOMContentLoaded", async function() {

	// ✅ 체크박스 제어 로직 (기존 함수들 유지, 이벤트만 보이는 항목 대상으로 동작)
	function initColumnCheckboxes(tableInstance) {
		const selectAll = document.getElementById('selectAllColumns');

		if (!tableInstance) {
			console.error("❌ tableInstance가 필요합니다.");
			return;
		}

		// 전체 선택
		if (selectAll) {
			// 기존 이벤트 누적 방지: 새로 바인딩 전 기존 listener 초기화가 필요하면 구현 (생략)
			selectAll.addEventListener('change', function() {
				const checked = this.checked;
				const visibleCbs = getVisibleColumnCheckboxes().map(x => x.cb);

				visibleCbs.forEach(chk => {
					chk.checked = checked;
					if (checked) tableInstance.showColumn(chk.value);
					else tableInstance.hideColumn(chk.value);
				});

				tableInstance.redraw(true);
			});
		}

		// 개별 체크박스
		const allCbs = document.querySelectorAll('.colCheckbox');
		allCbs.forEach(chk => {
			chk.addEventListener('change', function() {
				// 보이는 체크박스만 동작
				const label = this.closest('label');
				if (label && label.style.display === 'none') return;

				if (this.checked) tableInstance.showColumn(this.value);
				else tableInstance.hideColumn(this.value);

				// 전체 선택 상태는 보이는 체크박스 기준
				if (selectAll) {
					const visibleCbs = getVisibleColumnCheckboxes().map(x => x.cb);
					selectAll.checked = visibleCbs.length > 0 && visibleCbs.every(c => c.checked);
				}

				tableInstance.redraw(true);
			});
		});

		// 초기 진입 시에도 전체선택 상태 동기화
		if (selectAll) {
			const visibleCbs = getVisibleColumnCheckboxes().map(x => x.cb);
			selectAll.checked = visibleCbs.length > 0 && visibleCbs.every(c => c.checked);
		}
	}

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
		return makeTabulator(rows, tabulatorColumns, priceSpecificOptions);
	};

	// --------------------------------------------------------------------------
	// 4. 메인 Tabulator 초기화 및 전역 변수에 할당
	// --------------------------------------------------------------------------
	priceTableInstance = makePriceListTabulator(rows, tabulatorColumns);
	window.priceTableInstance = priceTableInstance;

	// --------------------------------------------------------------------------
	// 5. 초기 체크박스 상태/표시 적용 + 이벤트 바인딩
	// --------------------------------------------------------------------------
	setCheckboxListVisibility(defaultVisible);
	setCheckboxState(defaultVisible);
	initColumnCheckboxes(window.priceTableInstance);

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

	const resetButtons = document.querySelectorAll('.resetModalSearch');
	resetButtons.forEach(button => {
		button.addEventListener('click', (event) => {
			const modalEl = event.currentTarget.closest('.modal');
			if (!modalEl) return;

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
				tableInstance.setSort([{ field: "_sorter", dir: "asc" }]);
				console.log("✅ 모달 검색 조건 및 필터 초기화 완료.");
			}
		});
	});

	// --------------------------------------------------------------------------
	// 8. 검색/초기화 (툴바) - 전역 함수를 그대로 노출
	// --------------------------------------------------------------------------
	window.filterSearch = function() {
		const groupCode = document.getElementById("priceGroupCodeSearch").value.trim();
		const groupName = document.getElementById("priceGroupNameSearch").value.trim();
		const validDate = document.getElementById("validDateSearch").value.trim();

		const filters = [];
		if (groupCode) filters.push({ field: "단가그룹코드", type: "like", value: groupCode });
		if (groupName) filters.push({ field: "단가그룹명", type: "like", value: groupName });
		if (validDate) {
			filters.push({ field: "단가적용시작일", type: "<=", value: validDate });
			filters.push({ field: "단가적용종료일", type: ">=", value: validDate });
		}

		const table = window.priceTableInstance;

		if (table) {
			table.clearFilter();
			table.setFilter(filters);
			console.log("✅ 클라이언트 필터 적용 완료:", filters);
		} else {
			console.error("❌ priceTableInstance가 초기화되지 않았습니다.");
			alert("테이블이 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
		}
	};

	window.resetSearch = function() {
		const searchTool = document.querySelector('.searchTool');
		searchTool.querySelectorAll('input[type=text], input[type=date]').forEach(el => el.value = '');

		const table = Tabulator.findTable("#priceTable")[0];
		if (table) {
			table.clearFilter();
			console.log("✅ 검색조건 및 필터 초기화 완료");
		}
	};

});
