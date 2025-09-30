document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값. (메인 테이블용)
	const defaultVisible = ["거래처코드", "거래처명", "거래처유형", "전화번호", "이메일", "비고"];

	let bankSelectOptions = {}; // 은행 코드 옵션을 저장할 객체

	// 탭 전환 (Partner, LoanPrice, Payment)
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

				Object.values(tabContents).forEach(div => {
					if (div) div.classList.add('d-none');
				});

				tabButtons.forEach(b => {
					b.classList.remove('btn-primary');
					b.classList.add('btn-outline-primary');
				});

				if (tabContents[type]) {
					tabContents[type].classList.remove('d-none');
				}

				this.classList.remove('btn-outline-primary');
				this.classList.add('btn-primary');
			});
		});

		const defaultButton = document.querySelector('#partnerTab button[data-type="PARTNER"]');
		if (defaultButton) {
			tabButtons.forEach(b => {
				b.classList.remove('btn-primary');
				b.classList.add('btn-outline-primary');
			});

			defaultButton.classList.remove('btn-outline-primary');
			defaultButton.classList.add('btn-primary');

			if (tabContents.PARTNER) tabContents.PARTNER.classList.remove('d-none');
			if (tabContents.LOANPRICE) tabContents.LOANPRICE.classList.add('d-none');
			if (tabContents.PAYMENT) tabContents.PAYMENT.classList.add('d-none');
		}
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

	/**
	 * 공통 코드 로드 후 은행 코드 옵션을 bankSelectOptions 변수에 저장
	 */
	async function loadBankCodes() {
		let bankCodes = [];
		try {
			// window.getCommonCode 함수는 외부에서 제공된다고 가정
			bankCodes = await window.getCommonCode('BANK'); 
		} catch (e) {
			console.error("은행 코드 로딩 중 오류 발생:", e);
		}

		// 은행 코드를 { 'CODE - NAME': 'CODE - NAME' } 형태로 변환
		bankSelectOptions = bankCodes.reduce((options, item) => {
			const combinedValue = `${item.codeId} - ${item.codeName}`;
			options[combinedValue] = combinedValue;
			return options;
		}, { "": "선택하세요" });
	}

	// 거래처 상세/등록 모달
	window.showDetailModal = function(modalType, keyword) {
		const modalName = modalType === 'detail' ? '거래처 상세정보' : '거래처 등록';
		const modalEl = document.getElementById("newDetailModal");
		const modal = new bootstrap.Modal(modalEl);
		const form = document.getElementById("partnerForm"); 

		document.querySelector("#newDetailModal .modal-title").textContent = modalName;

		form.reset();

		initTabSwitching();

		// 결제 정보 테이블 초기화 및 은행 코드 로드 후 그리드 초기화
		loadBankCodes().then(() => {
			initPaymentGrid();
		});

		const commonCodePromises = [
			loadCommonCode('BUSINESSTYPE', 'businessType', '업태'),
			loadCommonCode('INDUSTRY', 'businessSector', '업종'),
			loadCommonCode('EMAILDOMAIN', 'emailDomain', '이메일도메인')
		];

		modal.show();

		if (modalType === 'detail' && keyword) {
			Promise.all(commonCodePromises)
				.then(() => {
					// loadDetailData 함수는 외부에서 제공된다고 가정
					loadDetailData('partner', keyword, form);
				})
				.catch(err => {
					console.error("공통 코드 로딩 중 치명적인 오류 발생:", err);
					alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
				});
		} else {
			// 신규 등록 시 결제 정보 초기화
			resetPaymentGrid(); 
		}
	};

	// 모달 저장
	window.saveModal = function() {
		const partnerForm = document.getElementById("partnerForm");
		const modalEl = document.getElementById("newDetailModal");

		if (!partnerForm) {
			console.error("Error: partnerForm element (ID: partnerForm) not found.");
			alert("저장 오류: 거래처 등록 폼을 찾을 수 없습니다.");
			return;
		}

		// 거래처 탭 데이터 수집
		const partnerData = new FormData(partnerForm);
		const partnerDataObject = Object.fromEntries(partnerData.entries());

		// 이메일 필드 처리
		if (partnerDataObject.emailId || partnerDataObject.emailDomain) {
			partnerDataObject.email = `${partnerDataObject.emailId || ''}@${partnerDataObject.emailDomain || ''}`;
		}
		delete partnerDataObject.emailId;
		delete partnerDataObject.emailDomain;


		// 여신/단가 탭 데이터 수집
		const loanForm = document.getElementById("loanForm");
		let loanPriceDataObject = {};
		if(loanForm) {
			const loanPriceData = new FormData(loanForm);
			loanPriceDataObject = Object.fromEntries(loanPriceData.entries());
		}
		

		// 결제정보 탭 데이터 수집 (일반 DOM 사용)
		let paymentData = collectPaymentData();

		// 모든 탭의 정보
		const finalPayload = {
			partnerData: partnerDataObject,
			loanPriceData: loanPriceDataObject,
			paymentData: paymentData
		};

		console.log("전송할 최종 데이터:", finalPayload);

		// 서버에 단일 API 호출
		fetch("api/registFullPartner", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				[document.querySelector('meta[name="_csrf_header"]').content]:
					document.querySelector('meta[name="_csrf"]').content
			},
			body: JSON.stringify(finalPayload),
		})
			.then(res => {
				if (!res.ok) {
					return res.json().then(error => { throw new Error(error.message || `서버 오류 발생: ${res.status}`); });
				}
				return res.json();
			})
			.then(data => {
				console.log("서버 응답 데이터 : ", data);
				alert("저장되었습니다.");

				partnerForm.reset();   
				
				// Choices.js 인스턴스 초기화 (기존 로직 유지)
				const businessTypeSelect = partnerForm.querySelector("select[name='businessType']");
				if (businessTypeSelect && businessTypeSelect.choicesInstance) {
					businessTypeSelect.choicesInstance.setChoiceByValue('');
				}
				const businessSectorSelect = partnerForm.querySelector("select[name='businessSector']");
				if (businessSectorSelect && businessSectorSelect.choicesInstance) { 
					businessSectorSelect.choicesInstance.setChoiceByValue('');
				}
				const emailDomainSelect = partnerForm.querySelector("select[name='emailDomain']");
				if (emailDomainSelect && emailDomainSelect.choicesInstance) { 
					emailDomainSelect.choicesInstance.setChoiceByValue('');
				}

				bootstrap.Modal.getInstance(modalEl).hide();
				
				// 등록 후 거래처 목록 새로고침 (메인 테이블 새로고침)
				window.priceTableInstance.redraw(); 
			})
			.catch(err => {
				console.error("저장실패 : ", err);
				alert("저장에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요.")
			});
	};


	// 💡 [유지] 메인 테이블 Tabulator 생성 로직 
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

			if (col === "거래처코드") {
				columnDef.formatter = function(cell) {
					const value = cell.getValue();
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`;
				};
			}
			return columnDef;
		}).filter(c => c !== null)
	];
	
	// rows와 columns 변수는 이 파일 외부에 있다고 가정합니다.
	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.priceTableInstance = tableInstance;


	/**
	 * 💡 결제정보 테이블 초기화 (Tabulator 제거)
	 * 은행 코드 드롭다운 생성
	 */
	function initPaymentGrid() {
		const tbody = document.getElementById('paymentDetailBody');
		if (!tbody) return;

		// 은행 코드 옵션 설정
		const bankSelects = tbody.querySelectorAll('select[name="bankCombined"]');

		bankSelects.forEach(selectEl => {
			selectEl.innerHTML = ''; 
			for (const value in bankSelectOptions) {
				const option = document.createElement('option');
				option.value = value;
				option.textContent = bankSelectOptions[value];
				selectEl.appendChild(option);
			}
		});
		
		// 초기 입력 행의 체크박스를 비활성화 상태로 유지
		const newRowCheckbox = tbody.querySelector('tr.new-payment-row input[type="checkbox"]');
		if (newRowCheckbox) {
			newRowCheckbox.checked = false;
			newRowCheckbox.disabled = true;
		}
	}

	/**
	 * 💡 결제 정보 테이블 데이터 수집 (일반 DOM 사용)
	 */
	function collectPaymentData() {
		const paymentData = [];
		// 새 입력 행 제외한 나머지 데이터 행만 선택
		const rows = document.getElementById('paymentDetailBody').querySelectorAll('tr:not(.new-payment-row)'); 

		rows.forEach(row => {
			
			// 필수 값 확인 (은행, 계좌번호, 예금주명)
			const bankCombined = row.querySelector('select[name="bankCombined"]').value;
			const accountNumber = row.querySelector('input[name="accountNumber"]').value;
			const accountHolder = row.querySelector('input[name="accountHolder"]').value;
			
			if (bankCombined && accountNumber && accountHolder) {
				
				// Tabulator에서 하던 것처럼 bankCombined를 파싱
				const [bankCode, ...bankNameParts] = bankCombined.split(' - ');

				paymentData.push({
					bankCode: bankCode ? bankCode.trim() : null,
					bankName: bankNameParts.join(' - ').trim(),
					accountNo: accountNumber, 
					depositorName: accountHolder, 
					isDefault: row.querySelector('select[name="isDefault"]').value,
					usageStatus: row.querySelector('select[name="usageType"]').value,
				});
			}
		});
		return paymentData;
	}


	/**
	 * 💡 [수정] 결제정보 행추가 (새로운 행의 select 값 유지 및 입력 행만 초기화)
	 */
	window.addPaymentRow = function() {
		const tbody = document.getElementById('paymentDetailBody');
		const newRowTemplate = tbody.querySelector('tr.new-payment-row');
		
		if (!newRowTemplate) return;

		// 1. 현재 입력된 새 행의 데이터를 데이터 행으로 등록할지 확인
		const bankCombined = newRowTemplate.querySelector('select[name="bankCombined"]').value;
		const accountNumber = newRowTemplate.querySelector('input[name="accountNumber"]').value;
		const accountHolder = newRowTemplate.querySelector('input[name="accountHolder"]').value;
		
		if (bankCombined && accountNumber && accountHolder) {
			
			// 입력이 있다면 데이터 행으로 복사하여 추가
			const dataRow = newRowTemplate.cloneNode(true);
			dataRow.classList.remove('new-payment-row', 'bg-light');
			dataRow.removeAttribute('data-row-id');
			dataRow.setAttribute('data-row-id', Date.now()); // 고유 ID 부여

			// 💡 [핵심 수정] 복사된 행의 Select 요소들에 현재 선택된 값을 유지
            const newRowSelects = newRowTemplate.querySelectorAll('select.payment-input');
            const dataRowSelects = dataRow.querySelectorAll('select.payment-input');
            
            newRowSelects.forEach((selectEl, index) => {
                dataRowSelects[index].value = selectEl.value;
            });


			// 데이터 행의 체크박스를 활성화하고 클래스 부여
			const dataRowCheckbox = dataRow.querySelector('td:first-child input[type="checkbox"]');
			if (dataRowCheckbox) {
			    dataRowCheckbox.classList.add('payment-checkbox');
			    dataRowCheckbox.disabled = false;
			    dataRowCheckbox.checked = false;
			}
			
			tbody.appendChild(dataRow);
		} else if (tbody.querySelectorAll('tr:not(.new-payment-row)').length === 0) {
             alert("은행, 계좌번호, 예금주명 중 하나 이상 입력 후 추가해주세요.");
             return;
        }

		// 2. 새 입력 행 (new-payment-row)만 초기화
		newRowTemplate.querySelector('select[name="bankCombined"]').value = ''; // 은행 초기화
		newRowTemplate.querySelector('input[name="accountNumber"]').value = '';
		newRowTemplate.querySelector('input[name="accountHolder"]').value = '';
		newRowTemplate.querySelector('select[name="isDefault"]').value = 'N'; // 기본여부 초기화
		newRowTemplate.querySelector('select[name="usageType"]').value = 'Y'; // 사용구분 초기화
		
		// 초기 행의 체크박스 상태 리셋 (비활성화 상태 유지)
		const newRowCheckbox = newRowTemplate.querySelector('input[type="checkbox"]');
		if (newRowCheckbox) {
			newRowCheckbox.checked = false;
			newRowCheckbox.disabled = true;
		}

		console.log("새로운 결제 정보 행 추가 완료.");
	}
	
	/**
	 * 💡 선택된 결제정보 행 삭제
	 */
	window.deleteSelectedPaymentRows = function() {
		const tbody = document.getElementById('paymentDetailBody');
		// payment-checkbox 클래스를 가진 체크박스만 선택
		const selectedRows = tbody.querySelectorAll('input.payment-checkbox:checked'); 
		
		if (selectedRows.length === 0) {
			alert("삭제할 행을 선택해주세요.");
			return;
		}

		if (confirm(`${selectedRows.length}개의 결제 정보를 삭제하시겠습니까?`)) {
			selectedRows.forEach(checkbox => {
				const row = checkbox.closest('tr');
				if (row) {
					row.remove();
				}
			});
			console.log("선택된 결제 정보 행 삭제 완료.");
		}
	}


	/**
	 * 💡 결제정보 탭 초기화
	 */
	window.resetPaymentGrid = function() {
		const tbody = document.getElementById('paymentDetailBody');
		const allRows = tbody.querySelectorAll('tr:not(.new-payment-row)');
		
		// 등록된 모든 행 삭제
		allRows.forEach(row => row.remove());

		// 새 입력 행 초기화
		const newRowTemplate = tbody.querySelector('tr.new-payment-row');
		if (newRowTemplate) {
			newRowTemplate.querySelector('select[name="bankCombined"]').value = '';
			newRowTemplate.querySelector('input[name="accountNumber"]').value = '';
			newRowTemplate.querySelector('input[name="accountHolder"]').value = '';
			newRowTemplate.querySelector('select[name="isDefault"]').value = 'N';
			newRowTemplate.querySelector('select[name="usageType"]').value = 'Y';
			
			// 초기 행의 체크박스 상태 리셋 (비활성화 상태 유지)
			const newRowCheckbox = newRowTemplate.querySelector('input[type="checkbox"]');
			if (newRowCheckbox) {
				newRowCheckbox.checked = false;
				newRowCheckbox.disabled = true;
			}
		}
		
		console.log("결제 정보 테이블 초기화 완료.");
	}

});