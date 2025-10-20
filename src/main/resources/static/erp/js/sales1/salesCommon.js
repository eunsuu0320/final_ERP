

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
			const inputWrapper = label.closest(".col-6, .row");
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


// 테이블의 유효성 검사
window.checkRowRequired = function(rowElement) {
	// 1. 테이블 전체를 찾아 헤더를 추출합니다.
	const table = rowElement.closest("table");
	if (!table) {
		console.error("checkRowRequired: 행의 상위 테이블을 찾을 수 없습니다.");
		return { isValid: false, missingFieldName: null };
	}

	const headers = table.querySelectorAll("thead th");
	let missingFieldName = null;
	let isValid = true;

	// 2. 각 헤더(<th>)를 순회하며 * 필수 항목을 확인합니다.
	for (let i = 0; i < headers.length; i++) {
		const headerText = headers[i].textContent.trim();

		// *이 포함된 필수 항목만 검사합니다.
		if (headerText.includes("*")) {
			// 해당 열(컬럼)의 입력 필드를 찾습니다.
			// <td>의 i+1 번째 자식 요소를 찾습니다.
			// (i=0은 보통 체크박스 <th>이므로, i=1부터 데이터 필드로 가정)

			// rowElement의 (i)번째 <td>를 찾습니다.
			const inputContainer = rowElement.children[i];

			if (inputContainer) {
				// 해당 <td> 내의 첫 번째 입력 필드를 가져옵니다.
				const input = inputContainer.querySelector("input, select, textarea");

				if (input) {
					let value;
					if (input.tagName === "SELECT") {
						// Select는 빈 값이 ''입니다.
						value = input.value.trim();
					} else if (input.type === "number") {
						// 수량, 단가 등 숫자 필드는 0이 아닌지 확인 (0도 유효할 수 있으나 일반적으로 입력 요구)
						value = input.value.trim();
						if (value === '0' || value === '') value = null; // 0이거나 빈 값은 미입력으로 간주
					} else {
						// Text, Textarea 등
						value = input.value.trim();
					}

					if (!value) {
						isValid = false;
						missingFieldName = headerText.replace("*", "");
						// 첫 번째 필수 누락 필드를 찾으면 즉시 중단합니다.
						break;
					}
				}
			}
		}
	}

	return { isValid: isValid, missingFieldName: missingFieldName };
};




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

		// (Choices.js 인스턴스 파괴 및 재설정 로직 유지) ...
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

		// 💡 1. 초기화를 위해 Native Select에 빈 옵션을 추가
		select.innerHTML = `<option value="" selected disabled hidden>${defaultPlaceholderText}</option>`;


		// 2. Choices.js 인스턴스 생성 (removeItemButton: false 유지)
		choicesInstance = new Choices(select, {
			removeItemButton: false,
			searchEnabled: true,
			placeholder: true,
			placeholderValue: defaultPlaceholderText,
			allowHTML: true,
			shouldSort: false
		});
		select.choicesInstance = choicesInstance;

		// 3. 데이터 로드 및 setChoices 호출
		const codeChoices = codes.map(code => ({
			value: code.codeName,
			label: code.codeName,
		}));

		choicesInstance.setChoices(
			codeChoices,
			'value',
			'label',
			false // replaceChoices: false (초기 옵션 유지)
		);

		// 4. 💡 최종 해결: 로드 후 강제로 선택 상태를 초기화하여 진한 표시 제거
		// '원자재'나 플레이스홀더가 진하게 선택되는 것을 즉시 해제합니다.
		choicesInstance.removeActiveItems();
		choicesInstance.setChoiceByValue('');


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
		paginationSize: 12,
		headerHozAlign: "center" // ✅ 모든 헤더 가운데 정렬

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



function bindDataToForm(data, form) {
	console.log("바인딩할 데이터:", data);
	console.log("바인딩할 폼:", form);
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

	// --- [1] 마스터 데이터 바인딩 ---
	for (const key in data) {
		if (data.hasOwnProperty(key) && key !== 'detailList') {
			const elements = form.querySelectorAll(`[name="${key}"]`);
			if (elements.length > 0) {
				const value = data[key] === null ? '' : String(data[key]);

				if (elements[0].type === 'radio' || elements[0].type === 'checkbox') {
					elements.forEach(element => {
						element.checked = (element.value === value);
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

			const partnerNameEl = document.getElementById("partnerName");
			if (partnerNameEl) partnerNameEl.readOnly = true;

			const partnerModalBtn = document.getElementById("partnerModalBtn");
			if (partnerModalBtn) partnerModalBtn.disabled = true;



		}
	}

	// --- [2] 디테일 데이터(detailList) 바인딩 ---
	if (data.detailList && Array.isArray(data.detailList) && data.detailList.length > 0) {
		console.log("디테일 리스트 바인딩 시작:", data.detailList);
		const detailTbody = document.getElementById('itemDetailBody');
		if (!detailTbody) {
			console.warn("itemDetailBody를 찾을 수 없습니다. detailList 바인딩을 건너뜁니다.");
			return;
		}

		// 기존 행 초기화
		detailTbody.querySelectorAll('tr:not(.new-item-row)').forEach(tr => tr.remove());

		data.detailList.forEach(detail => {
			// 템플릿 행 복제
			const newRowTemplate = detailTbody.querySelector('tr.new-item-row');
			if (!newRowTemplate) {
				console.log("newRowTemplate 없음.");
				return;
			}
			const dataRow = newRowTemplate.cloneNode(true);
			dataRow.classList.remove('new-item-row', 'bg-light');
			dataRow.removeAttribute('data-row-id');
			dataRow.setAttribute('data-row-id', Date.now());

			dataRow.querySelectorAll('.btn-outline-secondary').forEach(btn => btn.remove());

			// 각 key에 해당하는 id를 찾아 값 매핑
			for (const key in detail) {
				if (!detail.hasOwnProperty(key)) continue;
				console.log("detail의 key는: ", key);
				const value = detail[key] ?? '';
				const targetInput = dataRow.querySelector(`#${key}`);
				if (targetInput) {
					targetInput.value = value;

					// 숫자 계산용 필드에만 calculateRow 적용
					const numericFields = ['price', 'quantity', 'supplyAmount', 'taxAmount', 'finalAmount'];
					if (numericFields.includes(key)) {
						calculateRow(targetInput);
					}
				}
			}

			// productSpec = productSize + ' ' + unit
			const productSize = detail.productSize ?? '';
			const unit = detail.unit ?? '';
			const specValue = [productSize, unit].filter(v => v && v.trim() !== '').join(' ');
			const specInput = dataRow.querySelector('#productSpec');
			if (specInput) {
				specInput.value = specValue;
			}

			// 첫 번째 셀 체크박스 활성화
			const checkbox = dataRow.querySelector('input[type="checkbox"]');
			if (checkbox) {
				checkbox.classList.add('item-checkbox');
				checkbox.disabled = false;
				checkbox.checked = false;
			}

			detailTbody.appendChild(dataRow);
		});

		// 모든 상세 항목 바인딩 후 총합 계산
		if (typeof window.calculateTotal === 'function') {
			window.calculateTotal();
		}
	}
}


window.showLoading = function() {
	const overlay = document.getElementById("loadingOverlay");
	if (overlay) overlay.style.display = "flex";
}

window.hideLoading = function() {
	const overlay = document.getElementById("loadingOverlay");
	if (overlay) overlay.style.display = "none";
}




// 상세 정보 데이터 로딩 함수
function loadDetailData(domain, keyword, form) {
	if (!keyword || !domain) return Promise.reject("keyword 또는 domain 누락");

	console.log("keyword: ", keyword, "      domain: ", domain);
	const url = `/api/${domain}/getDetail?keyword=${encodeURIComponent(keyword)}`;

	// CSRF 토큰 설정
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
	const csrfToken = document.querySelector('meta[name="_csrf"]').content;

	// ✅ Promise 반환
	return fetch(url, {
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

			// ✅ 폼에 데이터 바인딩
			bindDataToForm(data, form);

			// ✅ 다음 then에서 사용할 수 있도록 데이터 반환
			return data;
		})
		.catch(err => {
			console.error("상세 정보 로딩 실패:", err);
			alert(`상세 정보를 불러오는 데 실패했습니다: ${err.message}`);
			throw err; // 에러를 상위로 전달 (필요 시 catch 가능)
		});
}



// 주소 API
window.execDaumPostcode = function() {
	new daum.Postcode({
		oncomplete: function(data) {
			document.getElementById("zipcode").value = data.zonecode;
			document.getElementById("address").value = data.roadAddress || data.jibunAddress;
		}
	}).open();
}


window.clearInputRowValues = function(containerElement) {
	if (!containerElement) return;

	const inputs = containerElement.querySelectorAll('input, textarea, select');

	inputs.forEach(input => {
		const type = input.type;

		if (type === 'checkbox' || type === 'radio') {
			// 체크박스와 라디오 버튼은 체크 해제
			input.checked = false;
			input.disabled = true; // (특정 로직에 따라)
		} else if (input.tagName === 'SELECT') {
			// select 박스는 첫 번째 옵션이나 빈 문자열 값으로 설정
			input.value = '';
		} else {
			// 텍스트, 숫자, textarea 등은 빈 문자열로 설정
			input.value = '';
		}
	});

	console.log("입력 행 값 수동 초기화 완료:", containerElement.id || containerElement.className);
};


// 모달의 테이블 초기화
window.resetItemGrid = function() {
	const tbody = document.getElementById('itemDetailBody');

	// 1. 등록된 모든 행 삭제
	const allRows = tbody.querySelectorAll('tr:not(.new-item-row)');
	allRows.forEach(row => row.remove());

	// 2. 새 입력 행 템플릿 찾기
	const newRowTemplate = tbody.querySelector('tr.new-item-row');

	// 3. [공통 함수 호출] 새 입력 행 초기화
	if (newRowTemplate) {
		window.clearInputRowValues(newRowTemplate);
	}

	console.log("결제 정보 테이블 초기화 완료.");
}












// 행 추가 버튼 이벤트
// 파일: partnerList.js (결제 정보 테이블 로직)

window.addItemRow = function() {
	const tbody = document.getElementById('itemDetailBody');
	const newRowTemplate = tbody.querySelector('tr.new-item-row');

	if (!newRowTemplate) return;

	// 1. 필수 입력 검증
	const validationResult = window.checkRowRequired(newRowTemplate);

	if (validationResult.isValid) {
		// 입력이 있다면 데이터 행으로 복사
		const dataRow = newRowTemplate.cloneNode(true);
		dataRow.classList.remove('new-item-row', 'bg-light');
		dataRow.removeAttribute('data-row-id');
		dataRow.setAttribute('data-row-id', Date.now());

		// 💡 복제된 행에서 검색 버튼 제거
		const searchBtn = dataRow.querySelector('.btn-outline-secondary');
		if (searchBtn) searchBtn.remove();

		// select 값 유지
		const newRowSelects = newRowTemplate.querySelectorAll('select.item-input');
		const dataRowSelects = dataRow.querySelectorAll('select.item-input');

		newRowSelects.forEach((selectEl, index) => {
			dataRowSelects[index].value = selectEl.value;
		});

		// 체크박스 활성화
		const dataRowCheckbox = dataRow.querySelector('td:first-child input[type="checkbox"]');
		if (dataRowCheckbox) {
			dataRowCheckbox.classList.add('item-checkbox');
			dataRowCheckbox.disabled = false;
			dataRowCheckbox.checked = false;
		}

		tbody.appendChild(dataRow);
		window.calculateTotal();

	} else {
		if (tbody.querySelectorAll('tr:not(.new-item-row)').length === 0) {
			const missingField = validationResult.missingFieldName;
			alert(`${missingField}은(는) 필수 입력 항목입니다.`);
			const missingInput = newRowTemplate.querySelector(`[name="${missingField}"]`);
			if (missingInput) missingInput.focus();
			return;
		}
	}

	window.clearInputRowValues(newRowTemplate);
	console.log("새로운 결제 정보 행 추가 완료 및 입력 행 초기화.");
};



window.deleteSelectedItemRows = function() {
	const tbody = document.getElementById('itemDetailBody');
	const selectedRows = tbody.querySelectorAll('input.item-checkbox:checked');

	if (selectedRows.length === 0) {
		alert("삭제할 행을 선택해주세요.");
		return;
	}

	if (confirm(`${selectedRows.length}개의 항목을 삭제하시겠습니까?`)) {
		selectedRows.forEach(checkbox => {
			const row = checkbox.closest('tr');
			if (row) {
				row.remove();
			}
		});

		// ✨ [수정 위치] 행 삭제 작업이 완료된 후, 이 블록 내부에서 호출해야 합니다.
		if (window.calculateTotal) {
			window.calculateTotal();
			console.log("선택된 행 삭제 완료 및 합계 재계산 완료.");
		} else {
			console.error("calculateTotal 함수를 찾을 수 없습니다.");
			console.log("선택된 행 삭제 완료.");
		}

	} else {
		// 사용자가 '취소'를 누르면 아무것도 하지 않습니다.
	}
}









// 검색 파라미터 설정 함수
function getSearchParams(containerSelector) {
	if (!containerSelector) {
		console.error("Error: containerSelector is required for getSearchParams.");
		return {};
	}

	const searchParams = {};
	const container = document.querySelector(containerSelector);

	if (!container) {
		console.error(`Error: Search container '${containerSelector}' not found.`);
		return searchParams;
	}

	const fields = container.querySelectorAll('input, select, textarea');

	fields.forEach(field => {
		let key = field.name || field.id;

		// 1. 유효한 키(key)가 없으면 다음 필드로 넘어갑니다.
		if (!key) {
			return;
		}

		let value = field.value;

		// 2. 값 정리 및 빈 값(Empty Value) 검증
		if (value) {
			value = value.trim();
		}

		// 3. 값이 없거나, Select의 기본 플레이스홀더 값이면 객체에 추가하지 않습니다.
		// 예를 들어, 값이 ""이거나 "도메인 선택" 등의 문자열이면 제외합니다.
		if (!value || value === "") {
			return;
		}

		// 4. 키 이름 변환 (Search 접미사 제거)
		if (key.endsWith('Search')) {
			// key에서 'Search' (길이 6) 제거
			key = key.substring(0, key.length - 6);
		}

		// 5. 💡 최종적으로 객체에 값을 담습니다. (이 부분이 빠져 있었습니다)
		searchParams[key] = value;
	});

	return searchParams;
}






// 검색 필터 초기화
function filterReset(containerSelector) {
	if (!containerSelector) {
		console.error("Error: containerSelector is required for resetSearchParams.");
		return;
	}

	const container = document.querySelector(containerSelector);

	if (!container) {
		console.error(`Error: Search container '${containerSelector}' not found.`);
		return;
	}

	const fields = container.querySelectorAll('input, select, textarea');

	fields.forEach(field => {
		const type = field.type;

		if (type === 'checkbox' || type === 'radio') {
			field.checked = false;
		} else if (field.tagName === 'SELECT') {

			// 💡 Choices.js 인스턴스 확인 및 강제 초기화
			if (field.choicesInstance) {
				const choices = field.choicesInstance;

				// 1. 💡 핵심: 현재 선택된 모든 항목을 UI에서 강제로 제거합니다.
				// 이 방법은 단일 선택 필드의 경우 선택 상태를 확실히 '선택 안 됨'으로 만듭니다.
				choices.removeActiveItems();

				// 2. Native Select와 Choices.js 내부 상태를 빈 값으로 동기화
				// 이 명령이 UI를 플레이스홀더 텍스트로 되돌려 놓습니다.
				choices.setChoiceByValue('');

				// 3. Native Select도 초기화
				field.selectedIndex = 0;

				// Note: 만약 1, 2번이 실패할 경우, options의 selected 속성을 조작 후 refresh()가 필요할 수 있으나,
				// 이 방법을 사용하면 대부분의 Choices.js 문제가 해결됩니다.
			} else {
				// 일반 <select> 필드 초기화
				field.selectedIndex = 0;
				field.value = "";
			}

		} else {
			// input[type=text], textarea 등 초기화
			field.value = '';
		}
	});

	console.log(`Container '${containerSelector}' search fields reset successfully.`);
}