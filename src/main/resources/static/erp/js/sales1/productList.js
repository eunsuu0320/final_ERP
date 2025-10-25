document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "품목그룹", "규격/단위", "입고단가", "출고단가", "창고코드", "이미지", "비고"];

	// 공통코드 로딩
	loadCommonCode('GRP001', 'productGroupSearch', '품목그룹');
	loadCommonCode('GRP003', 'warehouseCodeSearch', '창고');

	// 숫자 입력 포맷터
	window.koKRformatter = function(inputElement) {
		const cleanValue = window.cleanValue || function(val) {
			return Number(String(val).replace(/[^0-9.-]/g, '')) || 0;
		};
		const currentValue = cleanValue(inputElement.value);
		if (!inputElement.value) {
			inputElement.value = '';
			const hiddenInput = document.getElementById(inputElement.id.replace('Display', ''));
			if (hiddenInput) hiddenInput.value = '';
			return;
		}
		inputElement.value = currentValue.toLocaleString('ko-KR');
		const hiddenInput = document.getElementById(inputElement.id.replace('Display', ''));
		if (hiddenInput) hiddenInput.value = currentValue;
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
			Promise.all(commonCodePromises)
				.then(() => {
					console.log("모든 공통 코드(Choices.js) 로드 완료. 상세 데이터 로드 시작.");
					loadDetailData('product', keyword, form)
						.then(responseData => {
							window.lastLoadedProductData = responseData;
							window.lastModalType = modalType;
						});
				})
				.catch(err => {
					console.error("공통 코드 로딩 중 치명적인 오류 발생:", err);
					alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
				});
		}
	};

	// 품목상세모달 저장
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const modalEl = document.getElementById("newDetailModal");
		const formData = new FormData(form);

		if (!checkRequired(form)) return;

		const productCode = formData.get("productCode");
		const isUpdate = productCode && productCode.trim() !== '';
		const url = isUpdate ? "/api/modifyProduct" : "/api/registProduct";

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

				form.reset();

				["unit", "productGroup", "warehouseCode"].forEach(name => {
					const select = form.querySelector(`select[name='${name}']`);
					if (select && select.choicesInstance) select.choicesInstance.setChoiceByValue('');
				});

				bootstrap.Modal.getInstance(modalEl).hide();
			})
			.catch(err => {
				console.error("저장실패 : ", err);
				alert("저장에 실패했습니다.");
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
