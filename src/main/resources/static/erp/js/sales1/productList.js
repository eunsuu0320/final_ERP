document.addEventListener("DOMContentLoaded", function() {

	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "규격/단위", "이미지", "비고"];

	// 품목상세모달
	window.showDetailModal = function(modalType) {
		const modalName = modalType === 'detail' ? '품목상세정보' : '품목등록';
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		modal.show();
	};




	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const formData = new FormData(form);

		if (!checkRequired(form)) {
			return;
		};

		const productCode = formData.get("productCode");
		const isUpdate = productCode && productCode.trim() !== '';
		const url = isUpdate ? "/api/modifyProduct" : "/api/registProduct";

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
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}
			return columnDef;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;

});