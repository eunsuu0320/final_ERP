

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


// 단위 드롭다운 공통코드에서 가져오기
async function loadCommonCode(value, selectName, name) {
	try {
		const codes = await window.getCommonCode(value);
		const select = document.querySelector(`select[name='${selectName}']`);

		if (!select) {
			console.warn(`선택 요소(select[name='${selectName}']) 없음.`);
			return;
		}

		let choicesInstance = select.choicesInstance;

		if (choicesInstance && typeof choicesInstance.destroy === 'function') {
			choicesInstance.destroy();
			select.choicesInstance = undefined;
		}

		const wrapper = select.closest('.choices');
		if (wrapper) {
			wrapper.parentNode.insertBefore(select, wrapper);
			wrapper.remove();
		}

		select.removeAttribute('data-choice');
		select.removeAttribute('data-choice-id');
		select.style.display = '';
		select.innerHTML = '';

		const defaultPlaceholderText = `${name}을 선택하세요`;

		choicesInstance = new Choices(select, {
			removeItemButton: true,
			searchEnabled: true,
			placeholder: true,
			placeholderValue: defaultPlaceholderText,
			allowHTML: true
		});
		select.choicesInstance = choicesInstance;

		const choiceData = codes.map(code => ({
			value: code.codeName,
			label: code.codeName,
		}));

		const updatedChoices = choiceData;

		choicesInstance.setChoices(
			updatedChoices,
			'value',
			'label',
			true
		);

		return;

	} catch (err) {
		console.error(`${value} 코드 불러오기 실패:`, err);
		throw err;
	}
};



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
		body: historyType,
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



// 공통코드 목록 조회
window.getCommonCode = async function(commonGroup) {
	const url = `/api/modal/commonCode?commonGroup=${encodeURIComponent(commonGroup)}`;
	try {
		const res = await fetch(url, {
			method: "GET",
			headers: {
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content,
			},
		});
		return await res.json();
	} catch (err) {
		console.error(`조회 실패:`, err);
		return [];
	}
}



// 데이터를 폼 요소에 매핑
function bindDataToForm(data, form) {
	const allInputs = form.querySelectorAll('input, select, textarea');
	const codeFields = ['productCode', 'partnerCode', 'estimateCode', 'priceGroupCode'];

	allInputs.forEach(input => {
		if (input.type !== 'hidden') {
			input.readOnly = false;
		}
	});

	const displayElement = form.querySelector('.display-code-field');
	if (displayElement) {
		displayElement.placeholder = "자동 생성";
		displayElement.value = "";
	}

	// 데이터 바인딩 시작
	for (const key in data) {
		if (data.hasOwnProperty(key)) {
			const elements = form.querySelectorAll(`[name="${key}"]`);

			if (elements.length > 0) {
				const value = data[key] === null ? '' : String(data[key]); 

				if (elements[0].type === 'radio' || elements[0].type === 'checkbox') {
					elements.forEach(element => {
						if (element.value === value) {
							element.checked = true;
						} else if (element.type === 'radio') {
							element.checked = false;
						}
					});

				} else {
					const element = elements[0];
					element.value = value;

					if (codeFields.includes(key)) {
						element.readOnly = true;

						if (displayElement) {
							displayElement.value = value;
							displayElement.readOnly = true;
						}
					}

					// Choices.js (comboSelect) 처리
					if (element.classList.contains('comboSelect') && element.choicesInstance) {
						element.choicesInstance.setChoiceByValue(value);
					}
				}
			}
		}
	}
}

// 상세 정보 데이터 로딩 함수
function loadDetailData(domain, keyword, form) {
	if (!keyword || !domain) return;

	const url = `/api/${domain}/getDetail?keyword=${encodeURIComponent(keyword)}`;

	// CSRF 토큰을 가져옵니다.
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
	const csrfToken = document.querySelector('meta[name="_csrf"]').content;

	fetch(url, {
		method: "GET",
		headers: {
			[csrfHeader]: csrfToken
		}
	})
		.then(res => {
			if (!res.ok) {
				throw new Error(`HTTP error! status: ${res.status}`);
			}
			return res.json();
		})
		.then(data => {
			console.log("상세 품목 정보 수신:", data);

			bindDataToForm(data, form);

		})
		.catch(err => {
			console.error("상세 정보 로딩 실패:", err);
			alert(`상세 정보를 불러오는 데 실패했습니다: ${err.message}`);
		});
}
