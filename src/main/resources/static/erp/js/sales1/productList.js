document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "규격/단위", "이미지", "비고"];

	loadCommonCode('GRP001', 'productGroupSearch', '품목그룹');
	loadCommonCode('GRP003', 'warehouseCodeSearch', '창고');





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

			if (col === "이미지") {
				columnDef.formatter = function(cell) {
					const url = cell.getValue();
					return `<img src="${url}" alt="이미지" style="height:30px; cursor:pointer;" onclick="showImageModal('${url}')">`;
				};
			}
			if (col === "품목코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`;
				};
			}
			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;









	window.filterSearch = function() {
		const searchParams = getSearchParams('.searchTool');

		console.log("서버로 보낼 검색 조건:", searchParams);


		const queryString = new URLSearchParams(searchParams).toString();
		const url = `/api/product/search?${queryString}`;

		// fetch API를 사용한 요청
		fetch(url)
			.then(response => {
				if (!response.ok) {
					throw new Error('검색 요청 실패');
				}
				return response.json();
			})
			.then(data => {
				console.log("검색 결과 데이터:", data);
				// 3. (옵션) Tabulator 테이블 등 화면에 검색 결과를 반영하는 로직
				// myTable.setData(data);
			})
			.catch(error => {
				console.error('검색 중 오류 발생:', error);
				alert('데이터를 가져오는 데 실패했습니다.');
			});
	}







});