document.addEventListener("DOMContentLoaded", function() {
	const defaultVisible = ["품목코드", "품목명", "규격/단위", "이미지", "비고"];

	// 전역에서 접근 가능하게 함수 선언
	window.showImageModal = function(url) {
		const modalImg = document.getElementById("modalImg");
		modalImg.src = url;

		// Bootstrap 5 모달 열기
		const modal = new bootstrap.Modal(document.getElementById('imgModal'));
		modal.show();
	}
	
	window.showDetailModal = function(rowData) {
    const contentDiv = document.getElementById("detailContent");

    // 예: 간단히 row 데이터를 JSON 형태로 보여주기
    contentDiv.innerHTML = `
        <p><strong>품목코드:</strong> ${rowData["품목코드"]}</p>
        <p><strong>품목명:</strong> ${rowData["품목명"]}</p>
        <p><strong>규격/단위:</strong> ${rowData["규격/단위"]}</p>
        <p><strong>비고:</strong> ${rowData["비고"]}</p>
    `;

    // Bootstrap 모달 열기
    const modal = new bootstrap.Modal(document.getElementById("detailModal"));
    modal.show();
};

	// Tabulator 컬럼 정의
	let tabulatorColumns = [
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection",
			hozAlign: "center",
			headerSort: false,
			width: 50,
			frozen: true
		},
		...columns.map(col => {
			if (col === "상품규격" || col === "단위") return null;

			let columnDef = {
				title: col === "규격/단위" ? "규격/단위" : col,
				field: col === "규격/단위" ? "규격/단위" : col,
				visible: defaultVisible.includes(col) || col === "규격/단위"
			};

			if (col === "이미지") {
				columnDef.formatter = function(cell) {
					const url = cell.getValue();
					return `<img src="${url}" alt="이미지" style="height:40px; cursor:pointer;" onclick="showImageModal('${url}')">`;
				};
			}
			if (col === "품목코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					const rowData = cell.getData(); // 행 전체 데이터
					return `<div style="cursor:pointer; color:blue;" onclick='showDetailModal(${JSON.stringify(rowData)})'>${value}</div>`;
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
		resizableRows: true
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
