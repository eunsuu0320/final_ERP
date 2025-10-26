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
		if (!partnerForm) {
			alert("저장 오류: 폼을 찾을 수 없습니다.");
			return;
		}

		// -------------------------------
		// [A] 로딩 오버레이 생성
		// -------------------------------
		// -------------------------------
		// [A] 로딩 오버레이 생성 (흰색 배경 + 파란 스피너)
		// -------------------------------
		let overlayContainer = modalEl.querySelector(".modal-content");
		if (!overlayContainer) overlayContainer = modalEl;

		let overlay = overlayContainer.querySelector(".loading-overlay");
		if (!overlay) {
			overlay = document.createElement("div");
			overlay.className = "loading-overlay";
			overlay.innerHTML = `
				<div class="text-center">
					<div class="spinner-border text-primary mb-3" role="status" style="width:3rem;height:3rem;">
						<span class="visually-hidden">Loading...</span>
					</div>
					<p class="text-secondary fw-semibold mb-0">저장 중입니다...</p>
				</div>
			`;
			Object.assign(overlay.style, {
				position: "absolute",
				top: 0,
				left: 0,
				width: "100%",
				height: "100%",
				display: "flex",
				flexDirection: "column",
				justifyContent: "center",
				alignItems: "center",
				backgroundColor: "rgba(255,255,255,0.9)", // ✅ 흰색 반투명 배경
				zIndex: 1056,
				borderRadius: "0.5rem",
			});
			overlayContainer.style.position = "relative";
			overlayContainer.appendChild(overlay);
		}
		overlay.style.display = "flex";



		// -------------------------------
		// [1] partnerForm 데이터 수집
		// -------------------------------
		const partnerData = Object.fromEntries(new FormData(partnerForm).entries());

		// -------------------------------
		// [2] 이메일 조합
		// -------------------------------
		if (partnerData.emailId || partnerData.emailDomain) {
			partnerData.email = `${partnerData.emailId || ''}@${partnerData.emailDomain || ''}`;
		}
		delete partnerData.emailId;
		delete partnerData.emailDomain;

		// -------------------------------
		// [3] 전화번호 조합 (phone1 + phone2 + phone3 → 010-1234-5678)
		// -------------------------------
		const phone1 = document.getElementById("phone1")?.value.trim() || "";
		const phone2 = document.getElementById("phone2")?.value.trim() || "";
		const phone3 = document.getElementById("phone3")?.value.trim() || "";

		if (phone1 && phone2 && phone3) {
			const cleanPhone = (phone1 + phone2 + phone3).replace(/[^0-9]/g, "");
			const formattedPhone = cleanPhone.replace(/(\d{3})(\d{3,4})(\d{4})/, "$1-$2-$3");
			partnerData.partnerPhone = formattedPhone;
			document.getElementById("partnerPhone").value = formattedPhone;
		} else {
			alert("연락처를 모두 입력해주세요.");
			overlay.style.display = "none";
			return;
		}

		// -------------------------------
		// [4] 사업자등록번호 조합 (biz1 + biz2 + biz3 → 123-45-67890)
		// -------------------------------
		const biz1 = document.getElementById("biz1")?.value.trim() || "";
		const biz2 = document.getElementById("biz2")?.value.trim() || "";
		const biz3 = document.getElementById("biz3")?.value.trim() || "";

		if (biz1 && biz2 && biz3) {
			const cleanBiz = (biz1 + biz2 + biz3).replace(/[^0-9]/g, "");
			const formattedBiz = cleanBiz.replace(/(\d{3})(\d{2})(\d{5})/, "$1-$2-$3");
			partnerData.businessNo = formattedBiz;
			document.getElementById("businessNo").value = formattedBiz;
		} else {
			alert("사업자등록번호를 모두 입력해주세요.");
			overlay.style.display = "none";
			return;
		}

		// -------------------------------
		// [5] 기타 폼 데이터 결합
		// -------------------------------
		const loanForm = document.getElementById("loanForm");
		let loanPriceDataObject = loanForm ? Object.fromEntries(new FormData(loanForm).entries()) : {};

		// 여신한도 콤마 제거
		if (loanPriceDataObject.loanLimit) {
			loanPriceDataObject.loanLimit = loanPriceDataObject.loanLimit.replace(/,/g, "");
		}

		const paymentData = collectPaymentData();

		const finalPayload = {
			partnerData,
			loanPriceData: loanPriceDataObject,
			paymentData
		};

		console.log("전송 데이터:", finalPayload);

		// -------------------------------
		// [6] 서버 전송
		// -------------------------------
		fetch("api/registFullPartner", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload)
		})
			.then(res =>
				!res.ok
					? res.json().then(error => {
						throw new Error(error.message || `서버 오류: ${res.status}`);
					})
					: res.json()
			)
			.then(async data => {
				alert("저장되었습니다.");
				partnerForm.reset();

				// select 초기화
				["businessType", "businessSector", "emailDomain"].forEach(name => {
					const select = partnerForm.querySelector(`select[name='${name}']`);
					if (select && select.choicesInstance) select.choicesInstance.setChoiceByValue("");
				});

				// ✅ 모달 닫기
				const modalInstance = bootstrap.Modal.getInstance(modalEl);
				modalInstance.hide();

				// ✅ 테이블 새로고침 (API 재요청)
				if (window.partnerTableInstance) {
					try {
						console.log("📡 거래처 목록 새로고침 중...");
						await window.partnerTableInstance.setData("/api/partnerList");
						console.log("✅ 거래처 목록 새로고침 완료");
					} catch (reloadErr) {
						console.error("❌ 거래처 목록 새로고침 실패:", reloadErr);
					}
				} else if (typeof window.loadPartnerList === "function") {
					console.log("📡 loadPartnerList() 호출");
					await window.loadPartnerList();
				}

				// ✅ 스크롤 맨 위로 이동 (UI 자연스럽게)
				window.scrollTo({ top: 0, behavior: "smooth" });
			})
			.catch(err => {
				console.error("저장 실패:", err);
				alert("저장 실패. 콘솔 확인.");
			})
			.finally(() => {
				// 로딩 오버레이 제거
				overlay.style.display = "none";
			});

	};


	window.formatLoanLimit = function(input) {
		let value = input.value.replace(/[^0-9]/g, "");
		input.value = value ? Number(value).toLocaleString() : "";
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

		// ✅ 1. 입력행(new-item-row)만 선택
		const newRow = tbody.querySelector('tr.new-item-row');
		if (!newRow) return;

		// ✅ 2. 입력행의 은행 select만 초기화
		const bankSelect = newRow.querySelector('select[name="bankCombined"]');
		if (bankSelect) {
			bankSelect.innerHTML = '';

			// placeholder 추가
			const placeholder = document.createElement('option');
			placeholder.value = '';
			placeholder.textContent = '은행을 선택하세요';
			bankSelect.appendChild(placeholder);

			// 실제 옵션 추가
			for (const value in bankSelectOptions) {
				const option = document.createElement('option');
				option.value = value;
				option.textContent = bankSelectOptions[value];
				bankSelect.appendChild(option);
			}

			bankSelect.value = ''; // 선택 해제
		}

		// ✅ 3. 입력행의 체크박스 초기화
		const newRowCheckbox = newRow.querySelector('input[type="checkbox"]');
		if (newRowCheckbox) {
			newRowCheckbox.checked = false;
			newRowCheckbox.disabled = true;
		}

		// ✅ 4. 입력행의 나머지 input/select 초기화
		newRow.querySelectorAll('input.item-input').forEach(input => {
			if (input.type !== 'checkbox') input.value = '';
		});

		newRow.querySelectorAll('select.item-input').forEach(select => {
			if (select.name !== 'bankCombined') {
				if (select.name === 'isDefault') select.value = 'N';
				else if (select.name === 'usageType') select.value = 'Y';
				else select.selectedIndex = 0;
			}
		});

		console.log("✅ 입력행(new-item-row)만 초기화 완료");
	}



	// ------------------------------
	// 결제정보 수집
	// ------------------------------
	function collectPaymentData() {
		const paymentRows = document.querySelectorAll("#paymentTable tbody tr");
		const paymentList = [];

		paymentRows.forEach(row => {
			// 입력 요소 수집
			const bankSelect = row.querySelector("select[name='bankCombined']");
			const accountInput = row.querySelector("input[name='accountNo']");
			const depositorInput = row.querySelector("input[name='depositorName']");
			const isDefaultSelect = row.querySelector("select[name='isDefault']");
			const usageSelect = row.querySelector("select[name='usageType']");

			// 값 추출
			const bankCombined = bankSelect?.value?.trim();
			const accountNo = accountInput?.value?.trim();
			const depositorName = depositorInput?.value?.trim();
			const isDefault = isDefaultSelect?.value || "N";
			const usageType = usageSelect?.value || "Y";

			// ✅ 은행코드/은행명 분리 처리
			let bankCode = "";
			let bankName = "";
			if (bankCombined) {
				const parts = bankCombined.split("-").map(s => s.trim()); // 양쪽 여백 제거
				bankCode = parts[0] || "";
				bankName = parts[1] || "";
			}

			// ✅ 유효한 입력만 수집
			if (bankCode && accountNo) {
				paymentList.push({
					bankCode,
					bankName,
					accountNo,
					depositorName,
					isDefault,
					usageType
				});
			}
		});

		console.log("💾 수집된 결제정보:", paymentList);
		return paymentList;
	}


	
	
	// 📞 연락처 자동 탭 이동 함수
	window.autoTab = function(currentInput, nextInputId) {
		// 입력된 값의 길이
		const currentLength = currentInput.value.length;
		// 현재 input의 maxlength 속성값 (HTML에 반드시 지정되어 있어야 함)
		const maxLength = currentInput.getAttribute("maxlength");

		// ✅ maxLength만큼 입력되면 다음 input으로 포커스 이동
		if (maxLength && currentLength >= parseInt(maxLength)) {
			const nextInput = document.getElementById(nextInputId);
			if (nextInput) {
				nextInput.focus();
			}
		}
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
