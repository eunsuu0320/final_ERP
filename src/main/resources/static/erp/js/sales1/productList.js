document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["품목코드", "품목명", "규격/단위", "이미지", "비고"];

	// 이미지모달
	window.showImageModal = function(url) {
		// 이미지모달에 이미지 경로를 지정.
		const modalImg = document.getElementById("modalImg");
		modalImg.src = url;

		// 모달 열기
		const modal = new bootstrap.Modal(document.getElementById('imgModal'));
		modal.show();
	}

	// 품목상세모달
	window.showDetailModal = function(modalType) {
		let modalName = '';
		// 모달 열기
		if (modalType === 'detail') {
			modalName = '품목상세정보'

		} else if (modalType === 'regist') {
			modalName = '품목등록'
		}
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;


	};

	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const formData = new FormData(form);
		let modalName = document.querySelector('#newDetailModal .modal-title').innerHTML;

		if (modalName === 'detail') {
			// AJAX 요청
			fetch("/api/registProduct", {
				method: "POST",
				body: formData
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목저장 데이터 : ", data);
					alert("저장되었습니다.");

					const modal = bootstrap.Modal.getInstance(document.getElementById("newDetailModal"));
					modal.hide();
				})
				.catch(err => {
					console.error("품목저장실패 : ", err);
					alert("저장에 실패했습니다.")
				})

		} else if (modalName === 'regist') {
			// AJAX 요청
			fetch("/api/modifyProduct", {
				method: "POST",
				body: formData
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목수정 데이터 : ", data);
					alert("저장되었습니다.");

					const modal = bootstrap.Modal.getInstance(document.getElementById("newDetailModal"));
					modal.hide();
				})
				.catch(err => {
					console.error("품목수정실패 : ", err);
					alert("저장에 실패했습니다.")
				})
		}
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
					// cell.getValue() : 셀에 들어있는 데이터 값을 반환.
					// cell.setValue(value) : 셀 데이터 값을 변경
					// cell.getData() : 행 전체 데이터 객체 반환
					// cell.getRow() : 셀이 속한 행 반환
					// cell.getField() : 컬럼의 필드값 반환
					const url = cell.getValue();
					return `<img src="${url}" alt="이미지" style="height:30px; cursor:pointer;" onclick="showImageModal('${url}')">`;
				};
			}
			if (col === "품목코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					// ajax 호출!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
				};
			}

			return columnDef;
		}).filter(c => c !== null)
	];

	// Tabulator 테이블 생성
	const table = new Tabulator("#productTable", {
		data: rows,
		layout: "fitColumns",
		height: "100%",
		columns: tabulatorColumns,
		placeholder: "데이터가 없습니다.",
		movableColumns: true,
		resizableRows: false
	});

	// 컬럼 토글 체크박스
	const checkboxes = document.querySelectorAll(".colCheckbox");
	checkboxes.forEach(cb => {
		cb.addEventListener("change", function() {
			const colName = cb.value.trim();
			tabulatorColumns = tabulatorColumns.map(col => {
				if (col.field === colName) col.visible = cb.checked;
				return col;
			});
			table.setColumns(tabulatorColumns);
		});
	});

	// 선택된 행 삭제 버튼
	const deleteBtn = document.getElementById("deleteSelected");
	deleteBtn.addEventListener("click", function() {
		const selectedRows = table.getSelectedRows();
		selectedRows.forEach(row => row.delete());
	});
});
