document.addEventListener("DOMContentLoaded", async function() {

	// --------------------------------------------------------------------------
	// 0. 로딩 오버레이 시작 (메인 테이블 로딩)
	// --------------------------------------------------------------------------
	// 💡 초기에는 이 오버레이를 닫고, 모달에서 다시 열 것입니다.
	const mainLoadingOverlay = document.getElementById('main-table-loading-overlay');
	if (mainLoadingOverlay) {
		mainLoadingOverlay.style.display = 'none'; // 초기에는 숨김 (모달 클릭 시 사용)
	}

	// --------------------------------------------------------------------------
	// 1. 테이블 컬럼의 초기 가시성을 정의하는 배열
	// --------------------------------------------------------------------------
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
	// [함수] HTML 체크박스 상태 설정
	// --------------------------------------------------------------------------
	const setCheckboxState = function(visibleColumns) {
		const checkboxes = document.querySelectorAll('.colCheckbox');
		checkboxes.forEach(checkbox => {
			const colValue = checkbox.value;
			checkbox.checked = visibleColumns.includes(colValue);
		});
	};

	// --------------------------------------------------------------------------
	// [함수] Tabulator 컬럼 가시성 업데이트
	// --------------------------------------------------------------------------
	const updateTabulatorVisibility = function(tableInstance, visibleColumns) {
		const allColumns = tableInstance.getColumns();
		allColumns.forEach(column => {
			const field = column.getField();
			if (field) {
				if (visibleColumns.includes(field)) column.show();
				else column.hide();
			}
		});
	};

	// --------------------------------------------------------------------------
	// [함수] 체크박스 목록 표시 제어
	// --------------------------------------------------------------------------
	const setCheckboxListVisibility = function(columnsToShow) {
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
	// 2. 거래처 및 품목 전체 목록 캐시 (초기엔 비어 있음)
	// --------------------------------------------------------------------------
	let allPartnersCache = [];
	let allProductsCache = [];

	// --------------------------------------------------------------------------
	// 3. 메인 Tabulator 컬럼 정의 
	// --------------------------------------------------------------------------
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
			let def = { title: col, field: col, visible: defaultVisible.includes(col) };

			if (col === "단가그룹코드") {
				def.formatter = cell => {
					const val = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${val}')">${val}</div>`;
				};
			}

			if (col === "단가") {
				def.formatter = "money";
				def.formatterParams = { decimal: ".", thousand: ",", symbol: "₩", precision: false };
			}

			if (col === "거래처설정") {
				def.formatter = cell => {
					const row = cell.getRow().getData();
					if (row["단가유형"] === "거래처단가") {
						return `<button onclick="choosePartner('${row["단가고유코드"]}')" class="btn btn-sm btn-outline-secondary">거래처설정</button>`;
					}
					return "";
				};
			}

			if (col === "품목설정") {
				def.formatter = cell => {
					const row = cell.getRow().getData();
					if (row["단가유형"] === "품목단가") {
						return `<button onclick="chooseProduct('${row["단가고유코드"]}')" class="btn btn-sm btn-outline-secondary">품목설정</button>`;
					}
					return "";
				};
			}

			return def;
		}).filter(Boolean)
	];

	// --------------------------------------------------------------------------
	// 4. 메인 단가 테이블 생성 함수
	// --------------------------------------------------------------------------
	const makePriceListTabulator = function(rows, tabulatorColumns) {
		const priceSpecificOptions = {
			ajaxURL: "/api/priceList",
			ajaxConfig: "GET"
		};
		return makeTabulator(rows, tabulatorColumns, priceSpecificOptions);
	};

	// --------------------------------------------------------------------------
	// 5. 메인 Tabulator 초기화
	// --------------------------------------------------------------------------
	const tableInstance = makePriceListTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;

	// --------------------------------------------------------------------------
	// 6. 초기 체크박스 상태 설정 및 가시성 적용
	// --------------------------------------------------------------------------
	setCheckboxState(defaultVisible);
	setCheckboxListVisibility(defaultVisible);
	updateTabulatorVisibility(tableInstance, defaultVisible);

	// --------------------------------------------------------------------------
	// 7. 탭 전환 함수
	// --------------------------------------------------------------------------
	window.changeTabData = function(type, clickedButton) {
		const tabButtons = document.querySelectorAll('#priceTab button');
		tabButtons.forEach(btn => {
			btn.classList.remove('btn-primary');
			btn.classList.add('btn-outline-primary');
		});
		clickedButton.classList.remove('btn-outline-primary');
		clickedButton.classList.add('btn-primary');

		const table = window.priceTableInstance;
		if (!table) return console.error("Tabulator instance not initialized");

		let apiUrl = "/api/priceList";

		switch (type) {
			case 'ALL':
				apiUrl = "/api/priceList";
				defaultVisible = [
					"단가그룹코드", "단가그룹명", "단가유형", "할인율", "비고 ",
					"단가적용시작일", "단가적용종료일", "사용구분", "거래처설정", "품목설정"
				];
				break;
			case 'PRODUCT':
				apiUrl = "/api/pricetabProduct";
				defaultVisible = [
					"품목코드", "품목명", "품목그룹", "단가그룹코드", "단가그룹명",
					"단가유형", "할인율", "비고 ", "단가적용시작일", "단가적용종료일", "사용구분"
				];
				break;
			case 'PARTNER':
				apiUrl = "/api/pricetabPartner";
				defaultVisible = [
					"거래처코드", "거래처명", "거래처유형", "단가그룹코드", "단가그룹명",
					"단가유형", "할인율", "비고 ", "단가적용시작일", "단가적용종료일", "사용구분"
				];
				break;
		}

		setCheckboxListVisibility(defaultVisible);
		setCheckboxState(defaultVisible);
		updateTabulatorVisibility(table, defaultVisible);

		// 탭 전환 시에는 모달 로딩 오버레이 사용 (원래 로직 유지)
		const loadingOverlay = document.getElementById('loading-overlay');
		if (loadingOverlay) loadingOverlay.style.display = 'flex';

		fetch(apiUrl)
			.then(res => res.json())
			.then(data => table.setData(data.rows || []))
			.finally(() => {
				if (loadingOverlay) loadingOverlay.style.display = 'none';
			});
	};

	// --------------------------------------------------------------------------
	// 8. 거래처 및 품목 모달용 Tabulator 렌더링
	// --------------------------------------------------------------------------
	function renderPartnerTable(selectedPartners = []) {
		const tableContainerId = "partnerListTable";

		let current = Tabulator.findTable(`#${tableContainerId}`)[0];
		if (current) current.destroy();

		const PARTNER_CODE_FIELD = "거래처코드";
		const rowsToDisplay = allPartnersCache || [];
		const selectedCodes = new Set(selectedPartners);

		const formattedData = rowsToDisplay.map((p) => ({
			...p,
			_selected: selectedCodes.has(p[PARTNER_CODE_FIELD]),
			_sorter: selectedCodes.has(p[PARTNER_CODE_FIELD]) ? 0 : 1,
		}));

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
			{ title: "거래처코드", field: PARTNER_CODE_FIELD, width: 120 },
			{ title: "유형", field: "거래처유형", width: 100 },
			{ title: "등급", field: "등급", width: 80, hozAlign: "center" },
		];

		const instance = new Tabulator(`#${tableContainerId}`, {
			data: formattedData,
			layout: "fitColumns",
			height: "320px",
			index: PARTNER_CODE_FIELD,
			selectable: true,
			initialSort: [{ field: "_sorter", dir: "asc" }],
			columns,
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

		window.partnerTableInstance = instance;
	}


	function renderProductTable(selectedProducts = []) {
		const tableContainerId = "productListTable";

		// 기존 테이블 제거
		let current = Tabulator.findTable(`#${tableContainerId}`)[0];
		if (current) current.destroy();

		const PRODUCT_CODE_FIELD = "품목코드";
		const rowsToDisplay = allProductsCache || [];
		const selectedCodes = new Set(selectedProducts);

		// 선택된 항목을 먼저, 선택되지 않은 항목을 뒤로
		const formattedData = [
			...rowsToDisplay.filter(p => selectedCodes.has(p[PRODUCT_CODE_FIELD])),
			...rowsToDisplay.filter(p => !selectedCodes.has(p[PRODUCT_CODE_FIELD]))
		].map(p => ({ ...p, _selected: selectedCodes.has(p[PRODUCT_CODE_FIELD]) }));

		// 컬럼 정의
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
			{ title: "품목명", field: "품목명" },
			{ title: "품목코드", field: PRODUCT_CODE_FIELD, width: 120 },
			{ title: "품목그룹", field: "품목그룹", width: 100 },
			{ title: "규격/단위", field: "규격/단위", width: 80, hozAlign: "center" },
		];

		// Tabulator 생성
		const instance = new Tabulator(`#${tableContainerId}`, {
			data: formattedData,
			layout: "fitColumns",
			height: "320px",
			index: PRODUCT_CODE_FIELD,
			selectable: true,
			columns,
		});

		// 렌더 완료 후 체크된 항목 선택
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

		window.productTableInstance = instance;
	}


	// --------------------------------------------------------------------------
	// 9. 거래처 설정 모달 열기 (Modal 오류 방어 로직 추가)
	// --------------------------------------------------------------------------
	window.choosePartner = async function(priceUniqueCode) {
		const modalEl = document.getElementById("choosePartnerModal");

		// 🚨 Modal Element 존재 확인 및 인스턴스 재사용
		if (!modalEl) {
			console.error("❌ Modal Element not found: #choosePartnerModal");
			const overlayToControl = document.getElementById('main-table-loading-overlay');
			if (overlayToControl) overlayToControl.style.display = 'none';
			return;
		}

		let modal = bootstrap.Modal.getInstance(modalEl);
		if (!modal) {
			try {
				modal = new bootstrap.Modal(modalEl);
			} catch (e) {
				console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
				const overlayToControl = document.getElementById('main-table-loading-overlay');
				if (overlayToControl) overlayToControl.style.display = 'none';
				return;
			}
		}

		const overlayToControl = document.getElementById('main-table-loading-overlay');
		
		
		// HTML에서 제공된 클래스 이름 'savePartnerButton'을 사용하여 버튼을 찾습니다.
		const saveButton = modalEl.querySelector('.savePartnerButton'); 
		if (saveButton) {
			saveButton.setAttribute('data-price-unique-code', priceUniqueCode);
		} else {
			console.warn("📌 경고: 'savePartnerButton' 클래스를 가진 저장 버튼을 찾을 수 없습니다. HTML을 확인하세요.");
		}
		// ⭐⭐ 끝 ⭐⭐

		modal.show();

		// ✅ 로딩 시작: 모달 클릭 시 메인 오버레이를 표시합니다.
		if (overlayToControl) overlayToControl.style.display = 'flex';

		try {
			// ✅ 거래처 전체 목록이 비어 있으면 이때만 로딩
			if (allPartnersCache.length === 0) {
				console.log("📦 거래처 전체 목록을 최초로 로딩합니다...");
				const allRes = await fetch("/api/price/getAllPartner");
				const allData = await allRes.json();
				if (allData && Array.isArray(allData.rows)) {
					allPartnersCache = allData.rows;
					console.log("✅ 거래처 전체 목록 캐싱 완료:", allPartnersCache.length);
				} else {
					console.error("❌ 거래처 전체 목록 로드 실패:", allData);
					allPartnersCache = [];
				}
			}

			// 선택된 거래처 목록
			const res = await fetch(`/api/price/getPartner?priceUniqueCode=${encodeURIComponent(priceUniqueCode)}`);
			const partners = await res.json();
			console.log("📋 서버에서 받은 거래처 목록:", partners);

			if (Array.isArray(partners)) {
				renderPartnerTable(partners);
			} else {
				renderPartnerTable([]);
			}
		} catch (err) {
			console.error("❌ 거래처 설정 로드 실패:", err);
			renderPartnerTable([]);
		} finally {
			// ✅ 로딩 완료: 데이터 로드 후 오버레이를 숨깁니다.
			if (overlayToControl) overlayToControl.style.display = 'none';
		}
	};


	// 🚨🚨🚨 품목 설정 모달 열기 (chooseProduct) 함수 수정 (Modal 오류 방어 로직 추가)
	window.chooseProduct = async function(priceUniqueCode) {
		const modalEl = document.getElementById("chooseProductModal");

		// 🚨 Modal Element 존재 확인 및 인스턴스 재사용
		if (!modalEl) {
			console.error("❌ Modal Element not found: #chooseProductModal");
			const overlayToControl = document.getElementById('main-table-loading-overlay');
			if (overlayToControl) overlayToControl.style.display = 'none';
			return;
		}

		let modal = bootstrap.Modal.getInstance(modalEl);
		if (!modal) {
			try {
				modal = new bootstrap.Modal(modalEl);
			} catch (e) {
				console.error("❌ Bootstrap Modal 초기화 중 오류 발생:", e);
				const overlayToControl = document.getElementById('main-table-loading-overlay');
				if (overlayToControl) overlayToControl.style.display = 'none';
				return;
			}
		}

		const overlayToControl = document.getElementById('main-table-loading-overlay');

		
		// ID 선택자로 가정 (HTML에 'saveProductButton' 클래스를 사용하시려면 이 부분을 수정해야 합니다.)
		const saveButton = modalEl.querySelector('#saveProductButton'); 
		if (saveButton) {
			saveButton.setAttribute('data-price-unique-code', priceUniqueCode);
		} else {
			console.warn("📌 경고: 'saveProductButton' ID를 가진 저장 버튼을 찾을 수 없습니다. HTML을 확인하세요.");
		}
		// ⭐⭐ 끝 ⭐⭐

		modal.show(); // priceUniqueCode를 show에 넘기는 것은 Bootstrap 5에서는 의미가 없습니다.

		// ✅ 로딩 시작: 모달 클릭 시 메인 오버레이를 표시합니다.
		if (overlayToControl) overlayToControl.style.display = 'flex';

		try {
			// 🚨 품목 전체 목록이 비어 있으면 이때만 로딩 (allProductsCache 사용)
			if (allProductsCache.length === 0) {
				console.log("📦 품목 전체 목록을 최초로 로딩합니다...");
				const allRes = await fetch("/api/price/getAllProduct");
				const allData = await allRes.json();
				if (allData && Array.isArray(allData.rows)) {
					allProductsCache = allData.rows; // 🚨 allPartnersCache 대신 allProductsCache에 저장
					console.log("✅ 품목 전체 목록 캐싱 완료:", allProductsCache.length);
				} else {
					console.error("❌ 품목 전체 목록 로드 실패:", allData);
					allProductsCache = []; // 🚨 allPartnersCache 대신 allProductsCache 초기화
				}
			}

			// 선택된 품목 목록
			const res = await fetch(`/api/price/getProduct?priceUniqueCode=${encodeURIComponent(priceUniqueCode)}`);
			const products = await res.json();
			console.log("📋 서버에서 받은 품목 목록:", products);

			if (Array.isArray(products)) {
				renderProductTable(products); // 🚨 renderProductTable 호출
			} else {
				renderProductTable([]); // 🚨 renderProductTable 호출
			}
		} catch (err) {
			console.error("❌ 품목 설정 로드 실패:", err);
			renderProductTable([]); // 🚨 renderProductTable 호출
		} finally {
			// ✅ 로딩 완료: 데이터 로드 후 오버레이를 숨깁니다.
			if (overlayToControl) overlayToControl.style.display = 'none';
		}
	};


	// --------------------------------------------------------------------------
	// [함수] 거래처 설정 저장 (saveChoosePartner) - 인자 사용하도록 수정
	// --------------------------------------------------------------------------
	window.saveChoosePartner = function(priceUniqueCode) {
		const tableInstance = window.partnerTableInstance;
		const modalEl = document.getElementById("choosePartnerModal");

		if (!tableInstance) {
			console.error("❌ 거래처 목록 Tabulator 인스턴스를 찾을 수 없습니다.");
			alert("데이터 처리 준비가 완료되지 않았습니다.");
			return;
		}

		// 1. 선택된 거래처 데이터 가져오기
		const selectedRows = tableInstance.getSelectedData();

		// 2. 단가그룹코드 사용 (인자로 받은 값)
		if (!priceUniqueCode) {
			alert("저장할 단가그룹 코드를 찾을 수 없습니다. 모달을 다시 열어주세요.");
			return;
		}

		// 3. 선택된 거래처 코드만 추출 ("거래처코드" 필드 사용)
		const selectedPartnerCodes = selectedRows.map(row => row["거래처코드"]);
		console.log(`저장할 단가그룹: ${priceUniqueCode}, 선택된 거래처 코드:`, selectedPartnerCodes);

		// 4. API 호출을 위한 데이터 구성
		const payload = {
			priceUniqueCode: priceUniqueCode, // 인자로 받은 priceUniqueCode 사용
			partnerCodes: selectedPartnerCodes
		};

		// 5. CSRF 토큰 설정 및 API 호출 (가상의 API 경로: /api/price/savePartners)
		const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
		const csrfTokenEl = document.querySelector('meta[name="_csrf"]');

		if (!csrfHeaderEl || !csrfTokenEl) {
			console.error("❌ CSRF 토큰 메타 태그를 찾을 수 없습니다.");
			alert("보안 토큰이 없어 저장이 불가능합니다.");
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
				alert("거래처 설정이 성공적으로 저장되었습니다.");
				// 모달 닫기
				bootstrap.Modal.getInstance(modalEl).hide();
				// 필요하다면 메인 테이블의 데이터를 새로 로드합니다.
				window.priceTableInstance?.replaceData();
			})
			.catch(err => {
				console.error("❌ 거래처 설정 저장 실패:", err);
				alert("거래처 설정 저장에 실패했습니다. 서버 로그를 확인해주세요.");
			});
	}


	// --------------------------------------------------------------------------
	// [함수] 품목 설정 저장 (saveChooseProduct) - 추가
	// --------------------------------------------------------------------------
	window.saveChooseProduct = function(priceUniqueCode) {
		const tableInstance = window.productTableInstance;
		const modalEl = document.getElementById("chooseProductModal");

		if (!tableInstance) {
			console.error("❌ 품목 목록 Tabulator 인스턴스를 찾을 수 없습니다.");
			alert("데이터 처리 준비가 완료되지 않았습니다.");
			return;
		}

		// 1. 선택된 품목 데이터 가져오기
		const selectedRows = tableInstance.getSelectedData();

		if (!priceUniqueCode) {
			alert("저장할 단가그룹 코드를 찾을 수 없습니다. 모달을 다시 열어주세요.");
			return;
		}

		// 2. 선택된 품목 코드만 추출 ("품목코드" 필드 사용)
		const selectedProductCodes = selectedRows.map(row => row["품목코드"]);
		console.log(`저장할 단가그룹: ${priceUniqueCode}, 선택된 품목 코드:`, selectedProductCodes);

		// 3. API 호출을 위한 데이터 구성
		const payload = {
			priceUniqueCode: priceUniqueCode,
			productCodes: selectedProductCodes
		};

		// 4. CSRF 토큰 설정 및 API 호출 (가상의 API 경로: /api/price/saveProducts)
		const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
		const csrfTokenEl = document.querySelector('meta[name="_csrf"]');

		if (!csrfHeaderEl || !csrfTokenEl) {
			console.error("❌ CSRF 토큰 메타 태그를 찾을 수 없습니다.");
			alert("보안 토큰이 없어 저장이 불가능합니다.");
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
				alert("품목 설정이 성공적으로 저장되었습니다.");
				// 모달 닫기
				bootstrap.Modal.getInstance(modalEl).hide();
				// 메인 테이블의 데이터를 새로 로드
				window.priceTableInstance?.replaceData();
			})
			.catch(err => {
				console.error("❌ 품목 설정 저장 실패:", err);
				alert("품목 설정 저장에 실패했습니다. 서버 로그를 확인해주세요.");
			});
	}



	// --------------------------------------------------------------------------
	// 거래처 설정 모달 초기화 (resetChoosePartner)
	// --------------------------------------------------------------------------
	window.resetChoosePartner = function() {
		const tableInstance = window.partnerTableInstance;
		if (tableInstance) {
			// 1. 테이블 선택 초기화
			tableInstance.deselectRow();

			// 2. 검색 필드 초기화
			document.getElementById("partnerCode").value = "";
			document.getElementById("partnerName").value = "";
			document.getElementById("partnerLevel").value = "";
			document.getElementById("partnerLevelNameDisplay").value = "";

			// 3. 필터가 적용되어 있다면 필터도 초기화 (옵션)
			// tableInstance.clearFilter(); 

			console.log("✅ 거래처 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

			// 필터 초기화 후 테이블 데이터가 캐시된 전체 목록으로 복원되도록 처리
			if (window.allPartnersCache) {
				// _sorter 필드를 기준으로 다시 정렬하고 데이터를 재설정하여 초기 상태로 돌아갑니다.
				tableInstance.setData(window.allPartnersCache.map(p => ({
					...p,
					_selected: false,
					_sorter: 1 // 선택 안됨 상태로 초기화
				})));
			} else {
				console.warn("전체 거래처 캐시 데이터(allPartnersCache)가 없어 테이블 데이터 초기화는 건너뜁니다.");
			}
		}
	}


	// --------------------------------------------------------------------------
	// 품목 설정 모달 초기화 (resetChooseProduct)
	// --------------------------------------------------------------------------
	window.resetChooseProduct = function() {
		const tableInstance = window.productTableInstance;
		if (tableInstance) {
			// 1. 테이블 선택 초기화
			tableInstance.deselectRow();

			// 2. 검색 필드 초기화 (HTML에 맞게 ID 수정 필요)
			document.getElementById("productCodeSearch").value = "";
			document.getElementById("productNameSearch").value = "";
			document.getElementById("productGroupCode").value = "";
			document.getElementById("productGroupNameDisplay").value = "";

			console.log("✅ 품목 설정 모달의 선택 및 검색 필드가 초기화되었습니다.");

			// 필터 초기화 후 테이블 데이터가 캐시된 전체 목록으로 복원되도록 처리
			if (window.allProductsCache) {
				// _sorter 필드를 기준으로 다시 정렬하고 데이터를 재설정하여 초기 상태로 돌아갑니다.
				tableInstance.setData(window.allProductsCache.map(p => ({
					...p,
					_selected: false,
					_sorter: 1 // 선택 안됨 상태로 초기화
				})));
			} else {
				console.warn("전체 품목 캐시 데이터(allProductsCache)가 없어 테이블 데이터 초기화는 건너킵니다.");
			}
		}
	}


	// --------------------------------------------------------------------------
	// 10. 단가 상세 모달 등 기존 로직
	// --------------------------------------------------------------------------
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '단가상세정보' : '단가등록';
		const modalEl = document.getElementById("newDetailModal");

		// 🚨 Modal Element 존재 확인 및 인스턴스 재사용
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
			loadDetailData('price', keyword, form);
		}
	};

	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const modalEl = document.getElementById("newDetailModal");
		const formData = new FormData(form);

		if (!checkRequired(form)) return;

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
				alert("저장되었습니다.");
				form.reset();
				bootstrap.Modal.getInstance(modalEl).hide();
				location.reload();
			})
			.catch(err => {
				console.error("저장 실패:", err);
				alert("저장에 실패했습니다.");
			});
	};


});
