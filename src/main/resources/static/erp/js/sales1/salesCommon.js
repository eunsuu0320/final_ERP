
// 콤보박스
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


// 필수 항목 체크하는 유효성 검사
window.checkRequired = function(formElement) {
	if (!formElement) {
		console.error("checkRequired: 검사할 form 요소가 전달되지 않았습니다.");
		return false;
	}

	const labels = formElement.querySelectorAll("label");

	for (let label of labels) {
		const text = label.textContent.trim();
		if (text.endsWith("*")) {
			const inputWrapper = label.closest(".row");
			const input = inputWrapper ? inputWrapper.querySelector("input, select, textarea") : null;

			if (input) {
				const value =
					input.type === "radio" || input.type === "checkbox"
						? formElement.querySelector(`input[name="${input.name}"]:checked`)
						: input.value.trim();

				if (!value) {
					alert(`${text.replace("*", "")}은(는) 필수 입력항목입니다!`);
					if (input.tagName !== "SELECT" || !input.choicesInstance) {
						input.focus();
					}
					return false;
				}
			}
		}
	}
	return true;
}






// Tabulator 테이블 생성
window.makeTabulator = function(rows, tabulatorColumns) {
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
			const column = table.getColumn(colName);
			if (column) {
				column.toggle(cb.checked);
			}
		});
	});
	return table;
}




// 데이터 삭제
window.deleteData = function(tableInstance, url) {
	const deleteBtn = document.getElementById("deleteSelected");
	deleteBtn.addEventListener("click", function() {
		const selectedRows = tableInstance.getSelectedRows();
		selectedRows.forEach(row => row.delete());

		fetch(url, {
			method: "POST",
			body: formData,
			headers: {
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content,
			},
		})
			.then((res) => res.json())
			.then((data) => {
				console.log(`삭제할 데이터: `, data);
				alert("삭제되었습니다.");
				bootstrap.Modal.getInstance(modal).hide();
			})
			.catch((err) => {
				console.error(`삭제 실패: `, err);
				alert("삭제되었습니다.");
			});
	});
}




// 이미지 모달
window.showImageModal = function(url) {
	// 이미지모달에 이미지 경로를 지정.
	const modalImg = document.getElementById("modalImg");
	modalImg.src = url;

	// 모달 열기
	const modal = new bootstrap.Modal(document.getElementById('imgModal'));
	modal.show();
}




// 이력조회
window.getHirtory = function(historyType) {
	const url = "api/" + historyType + "/history";
	
	fetch(url, {
		method: "POST",
		body: formData,
		headers: {
			[document.querySelector('meta[name="_csrf_header"]').content]:
				document.querySelector('meta[name="_csrf"]').content,
		},
	})
		.then((res) => res.json())
		.then((data) => {
			console.log(`조회할 데이터: `, data);
		})
		.catch((err) => {
			console.error(`조회 실패: `, err);
		});
}
