document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "품목그룹", "규격/단위", "창고코드", "이미지", "비고"];

	// *주의: loadCommonCode 함수 등은 JSP 페이지 내에 별도로 정의되어 있다고 가정합니다.*
	loadCommonCode('GRP001', 'productGroupSearch', '품목그룹');
	loadCommonCode('GRP003', 'warehouseCodeSearch', '창고');


	// (기존 showDetailModal 및 saveModal 함수 등은 생략하지 않고 아래에 포함)

	// 품목상세모달
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '품목상세정보' : '품목등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("itemForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

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

					loadDetailData('product', keyword, form);
				})
				.catch(err => {
					console.error("공통 코드 로딩 중 치명적인 오류 발생:", err);
					alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
				});
		}
	};


	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const modalEl = document.getElementById("newDetailModal");
		const formData = new FormData(form);

		if (!checkRequired(form)) {
			return;
		};

		const productCode = formData.get("productCode");
		const isUpdate = productCode && productCode.trim() !== '';
		const url = isUpdate ? "/api/modifyProduct" : "/api/registProduct";

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
				const unitSelect = form.querySelector("select[name='unit']");
				if (unitSelect && unitSelect.choicesInstance) {
					unitSelect.choicesInstance.setChoiceByValue('');
				}
				const productGroupSelect = form.querySelector("select[name='productGroup']");
				if (productGroupSelect && productGroupSelect.choicesInstance) {
					productGroupSelect.choicesInstance.setChoiceByValue('');
				}
				const warehouseSelect = form.querySelector("select[name='warehouseCode']");
				if (warehouseSelect && warehouseSelect.choicesInstance) {
					warehouseSelect.choicesInstance.setChoiceByValue('');
				}

				bootstrap.Modal.getInstance(modalEl).hide();

				// ★ 저장 후 테이블 데이터 새로고침 (선택 사항)
				// window.resetSearch(); 
			})
			.catch(err => {
				console.error("저장실패 : ", err);
				alert("저장에 실패했습니다.")
			});
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
				fieldName = '품목코드'; // Product VO의 필드명이라고 가정
				columnDef.field = fieldName;
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`;
				};
			}

			// field가 정의되지 않은 경우를 대비 (실제 VO 필드명으로 수정 필요)
			if (!columnDef.field) {
				columnDef.field = fieldName;
			}

			return columnDef;
		}).filter(c => c !== null)
	];

	// ★ 1. 초기 로딩 시 JSP Model에 담겨온 'rows'로 Tabulator 인스턴스 생성
	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.productTableInstance = tableInstance;


	/**
	 * 데이터를 서버에서 가져와 Tabulator 테이블을 갱신하는 공통 함수
	 * @param {Object} params - 서버로 보낼 검색 조건 객체
	 */
	function loadTableData(params = {}) {
		const queryString = new URLSearchParams(params).toString();
		const url = `/api/product/search?${queryString}`;

		// 로딩 상태 표시
		if (window.productTableInstance) {
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
				if (window.productTableInstance) {
					window.productTableInstance.setData(data);
				}
			})
			.catch(error => {
				console.error('데이터 로딩 중 오류 발생:', error);
				alert('데이터를 가져오는 데 실패했습니다.');

				// 오류 발생 시 빈 배열로 설정하여 테이블 정리
				if (window.productTableInstance) {
					window.productTableInstance.setData([]);
				}
			});
	}


	// ★ 2. 검색 버튼 이벤트 핸들러 (조건에 맞는 목록 조회)
	window.filterSearch = function() {
		const searchParams = getSearchParams('.searchTool');

		console.log("서버로 보낼 검색 조건:", searchParams);

		// 검색 조건이 있는 상태로 데이터 로딩 함수 호출
		loadTableData(searchParams);
	}


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