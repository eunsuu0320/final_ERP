

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

		// 기존 Choices 인스턴스 정리
		let choicesInstance = select.choicesInstance;
		if (choicesInstance && typeof choicesInstance.destroy === 'function') {
			choicesInstance.destroy();
			select.choicesInstance = undefined;
		}
		const oldWrapper = select.closest('.choices');
		if (oldWrapper) {
			oldWrapper.parentNode.insertBefore(select, oldWrapper);
			oldWrapper.remove();
		}
		select.removeAttribute('data-choice');
		select.removeAttribute('data-choice-id');
		select.style.display = ''; // native select 보이게
		select.innerHTML = '';

		const defaultPlaceholderText = `${name}을 선택하세요`;
		// placeholder
		select.innerHTML = `<option value="" selected disabled hidden>${defaultPlaceholderText}</option>`;

		// 새 Choices 인스턴스
		choicesInstance = new Choices(select, {
			removeItemButton: false,
			searchEnabled: true,
			placeholder: true,
			placeholderValue: defaultPlaceholderText,
			allowHTML: true,
			shouldSort: false,
		});
		select.choicesInstance = choicesInstance;

		// 데이터 주입
		const codeChoices = codes.map(code => ({
			value: code.codeName,
			label: code.codeName,
		}));
		choicesInstance.setChoices(codeChoices, 'value', 'label', false);

		// 선택 초기화
		choicesInstance.removeActiveItems();
		choicesInstance.setChoiceByValue('');

		// ✅ 여기서부터 "보이는 박스(.choices)"에 직접 크기 적용
		const wrapper = select.closest('.choices'); // Choices가 만든 래퍼
		if (wrapper) {
			// 원하는 width 값 분기
			let targetWidth = '500px';
			if (selectName === 'businessType') targetWidth = '300px';
			if (selectName === 'businessSector') targetWidth = '300px';
			if (selectName === 'emailDomain') targetWidth = '250px';
			if (selectName === 'warehouse') targetWidth = '250px';

			wrapper.style.width = targetWidth;    // 폭 고정
			wrapper.style.maxWidth = 'none';      // 최대폭 제한 해제
			wrapper.style.flex = `0 0 ${targetWidth}`; // flex 컨테이너에서 줄어들지 않도록
		}

		return;
	} catch (err) {
		console.error(`${value} 코드 불러오기 실패:`, err);
		throw err;
	}
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

	// 모든 입력 필드 잠금 해제
	allInputs.forEach(input => {
		if (input.type !== 'hidden') {
			input.readOnly = false;
		}
	});

	// 자동 생성 placeholder
	const displayElement = form.querySelector('.display-code-field');
	if (displayElement) {
		displayElement.placeholder = "자동 생성";
		displayElement.value = "";
	}

	// ===============================
	// [1] 일반 필드 바인딩
	// ===============================
	for (const key in data) {
		if (data.hasOwnProperty(key) && key !== 'detailList') {
			const elements = form.querySelectorAll(`[name="${key}"]`);
			if (elements.length > 0) {
				let value = data[key] === null ? '' : data[key];

				// discountPct 변환 (0.1 → 10)
				if (key === 'discountPct' && value !== '') {
					if (value < 1) value = (Number(value) * 100).toFixed(2);
				}

				value = String(value);

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

					// comboSelect (Choices.js)
					if (element.classList.contains('comboSelect') && element.choicesInstance) {
						element.choicesInstance.setChoiceByValue(value);
					}
				}
			}
		}
	}

	// 거래처명/모달 버튼 비활성화
	const partnerNameEl = document.getElementById("partnerName");
	if (partnerNameEl) partnerNameEl.readOnly = true;

	const partnerModalBtn = document.getElementById("partnerModalBtn");
	if (partnerModalBtn) partnerModalBtn.disabled = true;

	// ===============================
	// [2] detailList 바인딩
	// ===============================
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
			const newRowTemplate = detailTbody.querySelector('tr.new-item-row');
			if (!newRowTemplate) return;

			const dataRow = newRowTemplate.cloneNode(true);
			dataRow.classList.remove('new-item-row', 'bg-light');
			dataRow.removeAttribute('data-row-id');
			dataRow.setAttribute('data-row-id', Date.now());
			dataRow.querySelectorAll('.btn-outline-secondary').forEach(btn => btn.remove());

			// key 기반 값 매핑
			for (const key in detail) {
				if (!detail.hasOwnProperty(key)) continue;
				let value = detail[key] ?? '';

				// discountAmount 쉼표 포맷팅
				if (key === 'discountAmount' && value !== '' && !isNaN(value)) {
					value = Number(value).toLocaleString('ko-KR');
				}

				const targetInput = dataRow.querySelector(`#${key}`);
				if (targetInput) {
					targetInput.value = value;

					// 숫자 계산 필드 자동 계산
					const numericFields = ['price', 'quantity', 'supplyAmount', 'taxAmount', 'finalAmount'];
					if (numericFields.includes(key)) calculateRow(targetInput);
				}
			}

			// 규격/단위 결합
			const productSize = detail.productSize ?? '';
			const unit = detail.unit ?? '';
			const specValue = [productSize, unit].filter(v => v && v.trim() !== '').join(' ');
			const specInput = dataRow.querySelector('#productSpec');
			if (specInput) specInput.value = specValue;

			// 체크박스 활성화
			const checkbox = dataRow.querySelector('input[type="checkbox"]');
			if (checkbox) {
				checkbox.classList.add('item-checkbox');
				checkbox.disabled = false;
				checkbox.checked = false;
			}

			detailTbody.appendChild(dataRow);
		});
	}

	// ===============================
	// [3] 금액 (tfoot) 필드 바인딩
	// ===============================
	const partnerDiscountEl = document.getElementById("partnerDiscountAmount");
	const totalEstimateEl = document.getElementById("totalEstimateAmount");
	const totalDiscountEl = document.getElementById("totalDiscountAmount");

	if (partnerDiscountEl && data.partnerDiscountAmount != null) {
		partnerDiscountEl.textContent = Number(data.partnerDiscountAmount).toLocaleString("ko-KR") + " 원";
	}
	if (totalEstimateEl && data.totalEstimateAmount != null) {
		totalEstimateEl.textContent = Number(data.totalEstimateAmount).toLocaleString("ko-KR") + " 원";
	}
	if (totalDiscountEl && data.totalDiscountAmount != null) {
		totalDiscountEl.textContent = Number(data.totalDiscountAmount).toLocaleString("ko-KR") + " 원";
	}

	// ===============================
	// [4] 총합 계산 (거래처할인 적용)
	// ===============================
	if (typeof window.calculateTotal === 'function') {
		window.calculateTotal();
		// calculateTotal이 덮어쓰기 전에 다시 반영
		if (partnerDiscountEl && data.partnerDiscountAmount != null) {
			const formatted = Number(data.partnerDiscountAmount).toLocaleString('ko-KR');
			partnerDiscountEl.textContent = `${formatted} 원`;
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

		// ✅ 추가: 기존 입력 행 초기화 (reset)
		const inputs = newRowTemplate.querySelectorAll('input.item-input, select.item-input');
		inputs.forEach(el => {
			if (el.tagName === 'SELECT') {
				el.selectedIndex = 0; // 첫 옵션으로 초기화
			} else if (el.type === 'checkbox' || el.type === 'radio') {
				el.checked = false;
			} else {
				el.value = ''; // 일반 input 초기화
			}
		});

	} else {
		if (tbody.querySelectorAll('tr:not(.new-item-row)').length === 0) {
			const missingField = validationResult.missingFieldName;
			alert(`${missingField}은(는) 필수 입력 항목입니다.`);
			const missingInput = newRowTemplate.querySelector(`[name="${missingField}"]`);
			if (missingInput) missingInput.focus();
			return;
		}
	}


	// 기존 함수 호출 (혹시 커스텀 초기화 로직이 있는 경우)
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