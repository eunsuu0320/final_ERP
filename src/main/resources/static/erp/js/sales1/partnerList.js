document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["거래처코드", "거래처명", "거래처유형", "전화번호", "이메일", "비고"];

	// 품목상세모달
	window.showDetailModal = function(modalType) {
		let modalName = '';
		// 모달 열기
		if (modalType === 'detail') {
			modalName = '거래처 상세정보'

		} else if (modalType === 'regist') {
			modalName = '거래처등록'
		}
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		modal.show();
		document.querySelector("#newDetailModal .modal-title").textContent = modalName;


	};

	window.applyComboSelect = function(selector) {
		const selects = document.querySelectorAll(selector);

		selects.forEach(select => {
			if (select) {
				if (select.choicesInstance) {
					select.choicesInstance.destroy();
				}

				const choices = new Choices(select, {
					removeItemButton: false,
					duplicateItemsAllowed: false,
					addItems: true,
					searchEnabled: true,
					shouldSort: false
				});

				select.choicesInstance = choices;
			}
		});
	}
	applyComboSelect('.comboSelect');

	// 품목상세모달의 저장버튼 이벤트 -> 신규 등록 / 수정
	window.saveModal = function() {
		const form = document.getElementById("itemForm");
		const formData = new FormData(form);
		let modalName = document.querySelector('#newDetailModal .modal-title').innerHTML;

		if (!formData.get("productName") || !formData.get("unit") || !formData.get("productSize") || !formData.get("productGroup")) {
			alert("필수항목을 입력하세요!");
			return;
		}


		if (modalName === '거래처 상세정보') {
			fetch("/api/modifyPartner", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("거래처수정 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("거래처수정실패 : ", err);
					alert("저장에 실패했습니다.")
				});

		} else if (modalName === '거래처등록') {
			fetch("/api/registPartner", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("거래처등록 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("거래처등록실패 : ", err);
					alert("저장에 실패했습니다.")
				});
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

			let columnDef = {
				title: col,
				field: col,
				visible: defaultVisible.includes(col)
			};

			if (col === "거래처코드") {
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
		resizableRows: false,
		pagination: "local",
		paginationSize: 10
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
