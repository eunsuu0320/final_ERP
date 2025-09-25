document.addEventListener("DOMContentLoaded", function() {
	// 테이블 컬럼을 위한 체크박스의 초기 값.
	const defaultVisible = ["거래처코드", "거래처명", "거래처유형", "전화번호", "이메일", "비고"];
	
		const tabButtons = document.querySelectorAll('#partnerTab button');
	const tabContents = {
		partner: document.getElementById('tab-partner'),
		loan: document.getElementById('tab-loan'),
		payment: document.getElementById('tab-payment')
	};

	tabButtons.forEach(btn => {
		btn.addEventListener('click', function () {
			// 1. 모든 콘텐츠 숨기기
			Object.values(tabContents).forEach(div => div.classList.add('d-none'));

			// 2. 모든 버튼 비활성화
			tabButtons.forEach(b => b.classList.remove('btn-primary'));
			tabButtons.forEach(b => b.classList.add('btn-outline-primary'));

			// 3. 선택된 콘텐츠 보여주기
			const type = this.dataset.type;
			if (tabContents[type]) {
				tabContents[type].classList.remove('d-none');
			}

			// 4. 버튼 활성화 상태 표시
			this.classList.remove('btn-outline-primary');
			this.classList.add('btn-primary');
		});
	});
	
	window.execDaumPostcode = function() {
		const elementLayer = document.getElementById('postcodeLayer');

		new daum.Postcode({
			oncomplete: function(data) {
				document.getElementById("zipcode").value = data.zonecode;
				document.getElementById("address").value = data.roadAddress || data.jibunAddress;
				elementLayer.style.display = 'none';
			},
			width: '100%',
			height: '100%'
		}).embed(elementLayer);

		elementLayer.style.display = 'block';

		// 💡 주소창 내부 iframe 강제 스타일 조정
		setTimeout(() => {
			const iframe = elementLayer.querySelector('iframe');
			if (iframe) {
				iframe.style.position = 'absolute';
				iframe.style.bottom = '0';
				iframe.style.right = '0';
			}
		}, 100); // iframe 렌더링까지 약간 대기 필요
	}

	// 품목상세모달
	window.showDetailModal = function(modalType) {
		let modalName = '';
		// 모달 열기
		if (modalType === 'detail') {
			modalName = '견적서 상세정보'

		} else if (modalType === 'regist') {
			modalName = '견적서등록'
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
		const form = document.getElementById("partnerForm");
		const formData = new FormData(form);
		let modalName = document.querySelector('#newDetailModal .modal-title').innerHTML;

		if (!formData.get("productName") || !formData.get("unit") || !formData.get("productSize") || !formData.get("productGroup")) {
			alert("필수항목을 입력하세요!");
			return;
		}


		if (modalName === '품목상세정보') {
			fetch("/api/modifyProduct", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목수정 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("품목수정실패 : ", err);
					alert("저장에 실패했습니다.")
				});

		} else if (modalName === '품목등록') {
			fetch("/api/registProduct", {
				method: "POST",
				body: formData,
				headers: {
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content
				}
			})
				.then(res => res.json())
				.then(data => {
					console.log("품목등록 데이터 : ", data);
					alert("저장되었습니다.");
					bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
				})
				.catch(err => {
					console.error("품목등록실패 : ", err);
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
