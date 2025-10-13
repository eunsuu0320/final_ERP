document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["단가그룹코드", "단가그룹명", "단가", "사용구분", "거래처설정", "품목설정"];
	

	function initTabFiltering() {

	    const tabButtons = document.querySelectorAll('#priceTab button');
	    
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
	    const table = window.priceTableInstance; 
	    if (!table) {
	        console.error("Tabulator instance is not initialized.");
	        return;
	    }

	    // 데이터에서 단가 유형을 구분하는 필드 이름. 
        // 서버에서 오는 데이터 필드명이 'priceType'이라고 가정합니다.
	    const filterField = "단가유형"; 
	    let filterValue = null;

	    // HTML 탭 타입(data-type)과 서버 데이터 값(Modal의 Radio Value)을 매핑
	    switch (type) {
	        case 'ALL':
	            // 'ALL' 탭은 모든 필터를 지웁니다.
	            table.clearFilter();
	            return;
	        case 'STANDARD':
	            filterValue = "기준단가"; // 모달의 priceType1 value와 일치
	            break;
	        case 'SPECIAL':
                // '특별단가'는 모달 라디오 버튼에 명시되어 있지 않아, 
                // 임시로 '품목별단가' (priceType3 value)로 가정합니다. 실제 값으로 변경하세요.
	            filterValue = "품목별단가"; 
	            break;
	        case 'PARTNER':
	            filterValue = "거래처별단가"; // 모달의 priceType2 value와 일치
	            break;
	        default:
	            return;
	    }
	    
	    // 필터 적용: setFilter(필드 이름, 비교 연산자, 값)
	    if (filterValue) {
	        table.setFilter(filterField, "=", filterValue);
	    }
	}
	
	
	// 단가상세모달
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

	// 단가상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
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
				    // 가정: 테이블 데이터를 다시 불러오는 함수 (salesCommon.js에 정의되어 있을 수 있음)
				    // tableInstance.setData("/api/priceList"); 
				    // 또는 페이지 새로고침
				    location.reload(); 
				}
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
			
			return columnDef;
		}).filter(c => c !== null)
	];

	// Tabulator 인스턴스 생성
	// makeTabulator 함수는 salesCommon.js에 정의되어 있다고 가정합니다.
	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;
	
	// Tabulator 초기화 완료 후 탭 필터링 이벤트 리스너를 연결합니다.
	initTabFiltering();
});
