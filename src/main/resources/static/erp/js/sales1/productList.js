document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "품목그룹", "규격/단위", "입고단가", "출고단가", "창고코드", "이미지", "비고"];

	// 공통코드 로딩
	loadCommonCode('GRP001', 'productGroupSearch', '품목그룹');
	loadCommonCode('GRP003', 'warehouseCodeSearch', '창고');

	// 숫자 입력 포맷터 (콤마 표시용)
	window.koKRformatter = function(inputElement) {
		// 1️⃣ 숫자만 추출 (음수, 소수점 허용)
		const cleanValue = String(inputElement.value).replace(/[^0-9.-]/g, '');

		// 2️⃣ 입력값이 비었으면 빈 문자열로 복원
		if (cleanValue === '' || isNaN(cleanValue)) {
			inputElement.value = '';
			return;
		}

		// 3️⃣ 숫자로 변환 후 한글 로케일(3자리 콤마) 적용
		const numericValue = Number(cleanValue);
		inputElement.value = numericValue.toLocaleString('ko-KR');
	};


	// 품목상세모달
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '품목상세정보' : '품목등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("itemForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		window.lastLoadedProductData = null;
		window.lastModalType = null;
		form.reset();

		const commonCodePromises = [
			loadCommonCode('GRP014', 'unit', '단위'),
			loadCommonCode('GRP001', 'productGroup', '품목그룹'),
			loadCommonCode('GRP003', 'warehouseCode', '창고')
		];

		modal.show();

		if (modalType === 'detail' && keyword) {
			// -------------------------------
			// [A] 로딩 오버레이 생성 (모달 내부)
			// -------------------------------
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
						<p class="text-secondary fw-semibold mb-0">데이터를 불러오는 중입니다...</p>
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

			// -------------------------------
			// [B] 실제 데이터 로딩 로직
			// -------------------------------
			Promise.all(commonCodePromises)
				.then(() => {
					console.log("모든 공통 코드(Choices.js) 로드 완료. 상세 데이터 로드 시작.");
					return loadDetailData('product', keyword, form);
				})
				.then(responseData => {
					window.lastLoadedProductData = responseData;
					window.lastModalType = modalType;
				})
				.catch(err => {
					console.error("공통 코드 로딩 중 오류 발생:", err);
					alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
				})
				.finally(() => {
					// -------------------------------
					// [C] 로딩 오버레이 제거
					// -------------------------------
					setTimeout(() => {
						if (overlay) overlay.style.display = "none";
					}, 300); // 부드럽게 사라지도록 약간의 딜레이
				});
		}

	};

	// 품목상세모달 저장
	// 품목상세모달 저장
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const modalEl = document.getElementById("newDetailModal");

		// -------------------------------
		// [A] 모달 오버레이 표시
		// -------------------------------
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

		// -------------------------------
		// [B] FormData 생성 전 콤마 제거
		// -------------------------------
		["inPrice", "outPrice"].forEach(id => {
			const el = document.getElementById(id);
			if (el) el.value = el.value.replace(/,/g, '').trim() || 0;
		});

		const formData = new FormData(form);
		if (!checkRequired(form)) {
			overlay.style.display = "none";
			return;
		}

		const productCode = formData.get("productCode");
		const isUpdate = productCode && productCode.trim() !== '';
		const url = isUpdate ? "/api/modifyProduct" : "/api/registProduct";

		// -------------------------------
		// [C] fetch 전송
		// -------------------------------
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
				try { return JSON.parse(text); }
				catch { throw new Error(text); }
			})
			.then(data => {
				console.log("저장 완료:", data);
				alert("저장되었습니다.");

				form.reset();
				["unit", "productGroup", "warehouseCode"].forEach(name => {
					const select = form.querySelector(`select[name='${name}']`);
					if (select && select.choicesInstance) select.choicesInstance.setChoiceByValue('');
				});

				bootstrap.Modal.getInstance(modalEl).hide();

				// -------------------------------
				// [D] 테이블 영역 오버레이 표시
				// -------------------------------
				const tableArea = document.querySelector("#productTableArea"); // ✅ Tabulator 영역의 부모 div ID로 변경하세요
				if (tableArea) {
					let tableOverlay = tableArea.querySelector(".loading-overlay");
					if (!tableOverlay) {
						tableOverlay = document.createElement("div");
						tableOverlay.className = "loading-overlay";
						tableOverlay.innerHTML = `
							<div class="text-center">
								<div class="spinner-border text-primary mb-3" role="status" style="width:3rem;height:3rem;">
									<span class="visually-hidden">Loading...</span>
								</div>
								<p class="text-secondary fw-semibold mb-0">최신 데이터로 갱신 중입니다...</p>
							</div>
						`;
						Object.assign(tableOverlay.style, {
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
							zIndex: 1500,
							borderRadius: "0.5rem",
						});
						tableArea.style.position = "relative";
						tableArea.appendChild(tableOverlay);
					}
					tableOverlay.style.display = "flex";
				}

				// -------------------------------
				// [E] 페이지 새로고침 (약간의 지연)
				// -------------------------------
				setTimeout(() => {
					window.location.reload();
				}, 800);
			})
			.catch(err => {
				console.error("저장 실패:", err);
				alert("저장에 실패했습니다.\n" + err.message);
			})
			.finally(() => {
				// -------------------------------
				// [F] 모달 오버레이 제거
				// -------------------------------
				setTimeout(() => {
					if (overlay) overlay.style.display = "none";
				}, 300);
			});
	};





	// 품목리스트 테이블 컬럼 정의
	let tabulatorColumns = [
		{
			title: "No",
			formatter: "rownum",
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

			let fieldName = col.replace(/\s/g, '');

			if (col === "이미지") {
				columnDef.formatter = function(cell) {
					const url = cell.getValue();
					if (!url || String(url).trim() === '' || String(url).trim().toLowerCase() === 'undefined') {
						return '<span>이미지 없음</span>';
					}
					return `<img src="${url}" alt="이미지" style="height:30px; cursor:pointer;" onclick="showImageModal('${url}')">`;
				};
			}

			if (col === "품목코드") {
				fieldName = '품목코드';
				columnDef.field = fieldName;
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`;
				};
			}


			if (col === "비고") {
				columnDef.hozAlign = "left";
			}


			if (col === "입고단가" || col === "출고단가") {
				columnDef.title = col + " (원)";
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const formatted = (value !== null && value !== undefined && !isNaN(value))
						? value.toLocaleString('ko-KR')
						: "-";
					const align = (value !== null && value !== undefined && !isNaN(value)) ? "right" : "center";
					return `<div style="text-align:${align}; width:100%;">${formatted}</div>`;
				};
				columnDef.sorter = "number";
			}

			if (!columnDef.field) columnDef.field = fieldName;
			return columnDef;
		}).filter(c => c !== null)
	];

	// ✅ Tabulator 테이블 생성
	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.productTableInstance = tableInstance;

	// ✅ 컬럼 체크박스와 "전체" 선택 로직
	const selectAll = document.getElementById('selectAll');
	const columnCheckboxes = document.querySelectorAll('.colCheckbox');

	if (selectAll && columnCheckboxes.length > 0) {
		selectAll.addEventListener('change', function() {
			const checked = this.checked;
			columnCheckboxes.forEach(chk => {
				chk.checked = checked;
				if (checked) {
					tableInstance.showColumn(chk.value);
				} else {
					tableInstance.hideColumn(chk.value);

				}
				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});

		columnCheckboxes.forEach(chk => {
			chk.addEventListener('change', function() {
				if (this.checked) {
					tableInstance.showColumn(this.value);
				} else {
					tableInstance.hideColumn(this.value);
				}
				const allChecked = Array.from(columnCheckboxes).every(c => c.checked);
				selectAll.checked = allChecked;
				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});
	}

	// ✅ 검색 및 초기화
	function loadTableData(params = {}) {
		const queryString = new URLSearchParams(params).toString();
		const url = `/api/product/search?${queryString}`;

		fetch(url)
			.then(response => {
				if (!response.ok) throw new Error('데이터 요청 실패: ' + response.statusText);
				return response.json();
			})
			.then(data => {
				console.log("검색 결과 데이터:", data);
				if (window.productTableInstance) {
					window.productTableInstance.setData(data);
				}
			})
			.catch(error => {
				console.error('데이터 로딩 중 오류 발생:', error);
				alert('데이터를 가져오는 데 실패했습니다.');
				if (window.productTableInstance) {
					window.productTableInstance.setData([]);
				}
			});
	}

	window.filterSearch = function() {
		const searchParams = getSearchParams('.searchTool');
		console.log("서버로 보낼 검색 조건:", searchParams);
		loadTableData(searchParams);
	};

	window.resetSearch = function() {
		const searchTool = document.querySelector('.searchTool');
		searchTool.querySelectorAll('input[type=text], select').forEach(el => {
			if (el.tagName === 'SELECT' && el.choicesInstance) {
				el.choicesInstance.setChoiceByValue('');
			} else {
				el.value = '';
			}
		});
		window.filterSearch();
	};

	// ✅ 모달 초기화
	window.resetModal = function() {
		const form = document.getElementById("itemForm");
		if (form) form.reset();

		if (window.lastModalType === 'detail' && window.lastLoadedProductData) {
			console.log("상세 모드: 이전에 불러온 데이터를 다시 바인딩합니다.");
			bindDataToForm(window.lastLoadedProductData, form);
		}
		console.log("견적 모달 전체 초기화 완료.");
	};

});
