document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값. (메인 테이블용)
	const defaultVisible = ["거래처코드", "거래처명", "거래처유형", "전화번호", "이메일", "업종", "업태", "사업자번호", "담당자", "비고"];

	let bankSelectOptions = {}; // 은행 코드 옵션을 저장할 객체

	// ------------------------------
	// 탭 전환 (Partner, LoanPrice, Payment)
	// ------------------------------
	function initTabSwitching() {
		const tabButtons = document.querySelectorAll('#partnerTab button');
		const tabContents = {
			PARTNER: document.getElementById('tab-partner'),
			LOANPRICE: document.getElementById('tab-loanprice'),
			PAYMENT: document.getElementById('tab-payment')
		};

		tabButtons.forEach(btn => {
			btn.addEventListener('click', function() {
				const type = this.dataset.type;
				Object.values(tabContents).forEach(div => { if (div) div.classList.add('d-none'); });
				tabButtons.forEach(b => { b.classList.remove('btn-primary'); b.classList.add('btn-outline-primary'); });
				if (tabContents[type]) tabContents[type].classList.remove('d-none');
				this.classList.remove('btn-outline-primary');
				this.classList.add('btn-primary');
			});
		});

		const defaultButton = document.querySelector('#partnerTab button[data-type="PARTNER"]');
		if (defaultButton) {
			tabButtons.forEach(b => { b.classList.remove('btn-primary'); b.classList.add('btn-outline-primary'); });
			defaultButton.classList.remove('btn-outline-primary');
			defaultButton.classList.add('btn-primary');
			if (tabContents.PARTNER) tabContents.PARTNER.classList.remove('d-none');
			if (tabContents.LOANPRICE) tabContents.LOANPRICE.classList.add('d-none');
			if (tabContents.PAYMENT) tabContents.PAYMENT.classList.add('d-none');
		}
	}

	// ------------------------------
	// 은행 코드 로딩
	// ------------------------------
	async function loadBankCodes() {
		let bankCodes = [];
		try { bankCodes = await window.getCommonCode('GRP015'); }
		catch (e) { console.error("은행 코드 로딩 중 오류 발생:", e); }

		bankSelectOptions = bankCodes.reduce((options, item) => {
			const combinedValue = `${item.codeId} - ${item.codeName}`;
			options[combinedValue] = combinedValue;
			return options;
		}, { "": "선택하세요" });
	}

	// ------------------------------
	// 거래처 상세/등록 모달
	// ------------------------------
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '거래처 상세정보' : '거래처 등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("partnerForm");

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		form.reset();
		window.resetPartner();

		initTabSwitching();
		loadBankCodes().then(() => { initPaymentGrid(); });

		const commonCodePromises = [
			loadCommonCode('GRP006', 'businessType', '업태'),
			loadCommonCode('GRP005', 'businessSector', '업종'),
			loadCommonCode('GRP004', 'emailDomain', '이메일 도메인')
		];

		modal.show();

		if (modalType === 'detail' && keyword) {
			Promise.all(commonCodePromises)
				.then(() => { loadDetailData('partner', keyword, form); })
				.catch(err => { console.error("공통 코드 로딩 중 치명적인 오류 발생:", err); alert("필수 데이터 로딩 중 오류가 발생했습니다."); });
		} else {
			resetItemGrid();
		}
	};




	// === 모달 ===
	window.resetPartner = function() {
		const partnerForm = document.getElementById("partnerForm");
		const loanForm = document.getElementById("loanForm");

		if (partnerForm) {
			partnerForm.reset();
		}
		if (loanForm) {
			loanForm.reset();
		}
		initPaymentGrid();



	};

	// ------------------------------
	// 모달 저장
	// ------------------------------
	window.saveModal = function() {
		const partnerForm = document.getElementById("partnerForm");
		const modalEl = document.getElementById("newDetailModal");
		if (!partnerForm) { alert("저장 오류: 폼을 찾을 수 없습니다."); return; }

		const partnerData = Object.fromEntries(new FormData(partnerForm).entries());
		if (partnerData.emailId || partnerData.emailDomain) partnerData.email = `${partnerData.emailId || ''}@${partnerData.emailDomain || ''}`;
		delete partnerData.emailId; delete partnerData.emailDomain;

		const loanForm = document.getElementById("loanForm");
		let loanPriceDataObject = loanForm ? Object.fromEntries(new FormData(loanForm).entries()) : {};

		const paymentData = collectPaymentData();
		const finalPayload = { partnerData, loanPriceData: loanPriceDataObject, paymentData };

		console.log("전송 데이터:", finalPayload);

		fetch("api/registFullPartner", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]: document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload)
		})
			.then(res => !res.ok ? res.json().then(error => { throw new Error(error.message || `서버 오류: ${res.status}`); }) : res.json())
			.then(data => {
				alert("저장되었습니다.");
				partnerForm.reset();
				['businessType', 'businessSector', 'emailDomain'].forEach(name => {
					const select = partnerForm.querySelector(`select[name='${name}']`);
					if (select && select.choicesInstance) select.choicesInstance.setChoiceByValue('');
				});
				bootstrap.Modal.getInstance(modalEl).hide();
				window.partnerTableInstance.redraw();
			})
			.catch(err => { console.error("저장 실패:", err); alert("저장 실패. 콘솔 확인."); });
	};

	// ------------------------------
	// Tabulator 테이블 생성
	// ------------------------------
	let tabulatorColumns = [
		{ title: "No", formatter: "rownum", hozAlign: "center", headerHozAlign: "center", headerSort: false, width: 50, frozen: true },
		...columns.map(col => {
			let def = { title: col, field: col, visible: defaultVisible.includes(col) };

			if (col === "상품규격" || col === "단위") return null;
			if (col === "비고") {
				def.hozAlign = "left";
			}

			return def;
		}).filter(c => c !== null)
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.partnerTableInstance = tableInstance;

	// ------------------------------
	// ✅ 컬럼 체크박스 제어 (PRODUCTLIST.js 스타일)
	// ------------------------------
	const selectAll = document.getElementById('selectAllColumns');
	const columnCheckboxes = document.querySelectorAll('.colCheckbox');

	if (selectAll && columnCheckboxes.length > 0) {
		// 전체 선택 체크박스
		selectAll.addEventListener('change', function() {
			const checked = this.checked;
			columnCheckboxes.forEach(chk => {
				chk.checked = checked;
				if (checked) tableInstance.showColumn(chk.value);
				else tableInstance.hideColumn(chk.value);

				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});

		// 개별 체크박스
		columnCheckboxes.forEach(chk => {
			chk.addEventListener('change', function() {
				if (this.checked) tableInstance.showColumn(this.value);
				else tableInstance.hideColumn(this.value);

				// 전체 선택 상태 반영
				selectAll.checked = Array.from(columnCheckboxes).every(c => c.checked);

				tableInstance.redraw(true); // true: 열 너비 자동 조정
			});
		});
	}

	window.addPaymentRow = function() {
		const tbody = document.getElementById('itemDetailBody');
		const newRowTemplate = tbody.querySelector('tr.new-item-row');

		if (!newRowTemplate) return;

		// 1. 필수 입력 검증
		const validationResult = window.checkRowRequired(newRowTemplate);

		if (validationResult.isValid) {
			// 2. 데이터 행 복사 및 구성
			const dataRow = newRowTemplate.cloneNode(true);
			dataRow.classList.remove('new-item-row', 'bg-light');
			dataRow.removeAttribute('data-row-id');
			dataRow.setAttribute('data-row-id', Date.now());

			// 3. 복제된 행의 체크박스 활성화
			const dataRowCheckbox = dataRow.querySelector('td:first-child input[type="checkbox"]');
			if (dataRowCheckbox) {
				dataRowCheckbox.classList.add('item-checkbox');
				dataRowCheckbox.disabled = false;
				dataRowCheckbox.checked = false;
			}

			// 4. select 값 유지 (은행, 기본여부, 사용구분 등)
			const newRowSelects = newRowTemplate.querySelectorAll('select.item-input');
			const dataRowSelects = dataRow.querySelectorAll('select.item-input');
			newRowSelects.forEach((selectEl, i) => {
				dataRowSelects[i].value = selectEl.value;
			});

			// 5. tbody에 새 행 추가
			tbody.appendChild(dataRow);
			console.log("💾 결제정보 행 추가 완료");

			// 6. 기존 입력행(new-item-row) 초기화
			newRowTemplate.querySelectorAll('input.item-input, select.item-input').forEach(el => {
				if (el.tagName === 'SELECT') {
					if (el.name === 'bankCombined') {
						// placeholder 추가
						if (![...el.options].some(o => o.value === '')) {
							const opt = document.createElement('option');
							opt.value = '';
							opt.textContent = '은행을 선택하세요';
							el.insertBefore(opt, el.firstChild);
						}
						el.value = '';
					} else if (el.name === 'isDefault') {
						el.value = 'N';
					} else if (el.name === 'usageType') {
						el.value = 'Y';
					} else {
						el.selectedIndex = 0;
					}
				} else {
					el.value = '';
				}
			});

		} else {
			// 7. 필수 입력 누락 시 처리
			if (tbody.querySelectorAll('tr:not(.new-item-row)').length === 0) {
				const missingField = validationResult.missingFieldName;
				alert(`${missingField}은(는) 필수 입력 항목입니다.`);
				const missingInput = newRowTemplate.querySelector(`[name="${missingField}"]`);
				if (missingInput) missingInput.focus();
				return;
			}
		}

		// 8. 은행코드 다시 로드 (선택지 최신화)
		initPaymentGrid();

		console.log("✅ 새로운 결제 정보 행 추가 및 입력 행 초기화 완료");
	};


	// ------------------------------
	// 결제정보 테이블 초기화
	// ------------------------------
	function initPaymentGrid() {
		const tbody = document.getElementById('itemDetailBody');
		if (!tbody) return;

		// 1. 은행 select 다시 초기화
		tbody.querySelectorAll('select[name="bankCombined"]').forEach(selectEl => {
			selectEl.innerHTML = '';

			// placeholder 추가
			const placeholder = document.createElement('option');
			placeholder.value = '';
			placeholder.textContent = '은행을 선택하세요';
			selectEl.appendChild(placeholder);

			// 실제 옵션 추가
			for (const value in bankSelectOptions) {
				const option = document.createElement('option');
				option.value = value;
				option.textContent = bankSelectOptions[value];
				selectEl.appendChild(option);
			}

			selectEl.value = ''; // 선택 해제
		});

		// 2. 입력행의 체크박스 초기화
		const newRowCheckbox = tbody.querySelector('tr.new-item-row input[type="checkbox"]');
		if (newRowCheckbox) {
			newRowCheckbox.checked = false;
			newRowCheckbox.disabled = true;
		}

		// 3. 다른 입력 필드도 함께 초기화
		tbody.querySelectorAll('tr.new-item-row input.item-input').forEach(input => {
			if (input.type !== 'checkbox') input.value = '';
		});

		tbody.querySelectorAll('tr.new-item-row select.item-input').forEach(select => {
			if (select.name !== 'bankCombined') {
				if (select.name === 'isDefault') select.value = 'N';
				else if (select.name === 'usageType') select.value = 'Y';
				else select.selectedIndex = 0;
			}
		});
	}


	// ------------------------------
	// 결제정보 수집
	// ------------------------------
	function collectPaymentData() {
		const paymentData = [];
		const rows = document.getElementById('itemDetailBody').querySelectorAll('tr:not(.new-payment-row)');
		rows.forEach(row => {
			const bankCombined = row.querySelector('select[name="bankCombined"]').value;
			const accountNo = row.querySelector('input[name="accountNo"]').value;
			const depositorName = row.querySelector('input[name="depositorName"]').value;
			if (bankCombined && accountNo && depositorName) {
				const [bankCode, ...bankNameParts] = bankCombined.split(' - ');
				paymentData.push({
					bankCode: bankCode ? bankCode.trim() : null,
					bankName: bankNameParts.join(' - ').trim(),
					accountNo, depositorName,
					isDefault: row.querySelector('select[name="isDefault"]').value,
					usageStatus: row.querySelector('select[name="usageType"]').value
				});
			}
		});
		return paymentData;
	}

	// ------------------------------
	// 검색 및 초기화
	// ------------------------------
	function loadTableData(params = {}) {
		const queryString = new URLSearchParams(params).toString();
		fetch(`/api/partner/search?${queryString}`)
			.then(res => res.ok ? res.json() : Promise.reject('데이터 요청 실패'))
			.then(data => { if (window.partnerTableInstance) window.partnerTableInstance.setData(data); })
			.catch(err => { console.error(err); if (window.partnerTableInstance) window.partnerTableInstance.setData([]); });
	}

	window.filterSearch = function() {
		const table = window.partnerTableInstance;
		if (!table) return;

		const partnerCode = document.getElementById('partnerCodeSearch').value;
		const partnerType = document.getElementById('partnerTypeSearch').value;
		const partnerName = document.getElementById('partnerNameSearch').value.trim();


		table.clearFilter(true);

		table.setFilter((data) => {
			if (partnerCode && !String(data['거래처코드'] || '').includes(partnerCode)) return false;
			if (partnerType && !String(data['거래처유형'] || '').includes(partnerType)) return false;
			if (partnerName && !String(data['거래처명'] || '').includes(partnerName)) return false;
			return true;
		});
	};
	window.resetSearch = function() {
		const searchTool = document.querySelector('.searchTool');
		searchTool.querySelectorAll('input[type=text], select').forEach(el => {
			if (el.tagName === 'SELECT' && el.choicesInstance) el.choicesInstance.setChoiceByValue('');
			else el.value = '';
		});
		loadTableData({});
	}

});
