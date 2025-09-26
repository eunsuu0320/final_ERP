document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["단가그룹코드", "단가그룹명", "단가", "사용구분", "거래처설정", "품목설정"];

	// 등록/수정모달
	window.showDetailModal = function(modalType) {
		const modalName = modalType === 'detail' ? '단가상세정보' : '단가등록';
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;
	};


	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const formData = new FormData(form);

		if (!checkRequired(form)) {
			return;
		};

		const priceGroupCode = formData.get("priceGroupCode");
		const isUpdate = priceGroupCode && priceGroupCode.trim() !== '';
		const url = isUpdate ? "/api/modifyPrice" : "/api/registPrice";

		if (!url) {
			alert("API 경로를 결정할 수 없습니다.");
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
				bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
			})
			.catch(err => {
				console.error("저장실패 : ", err);
				alert("저장에 실패했습니다.")
			});
	};



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

			if (col === "단가그룹코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}
			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;

});
