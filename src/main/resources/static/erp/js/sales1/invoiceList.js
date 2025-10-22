document.addEventListener("DOMContentLoaded", function() {
	const defaultVisible = ["청구서코드", "등록일자", "청구일자", "청구금액", "수금일자", "거래처명", "담당자", "진행상태"];

	const STATUS_MAP = {
		"미청구": { label: "미청구" },
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"수금완료": { label: "수금완료" },

	};

	const cleanValue = (v) => (v ? Number(String(v).replace(/,/g, "")) : 0);
	const fmt = (n) => (Number(n) || 0).toLocaleString();

	// ===========================================================
	// ✅ Footer 셀 헬퍼 (colspan 구조 대응)
	// ===========================================================
	function getFooterCells() {
		const row = document.querySelector("#itemDetailFooter tr");
		if (!row) return null;
		const tds = Array.from(row.querySelectorAll("td"));
		if (tds.length < 4) return null;
		const [qtyCell, supplyCell, taxCell, finalCell] = tds.slice(-4); // 뒤에서 4칸
		return { qtyCell, supplyCell, taxCell, finalCell };
	}

	// ===========================================================
	// ✅ 탭 필터
	// ===========================================================
	function initTabFiltering() {
		const tabButtons = document.querySelectorAll("#invoiceTab button");
		tabButtons.forEach((btn) => {
			btn.addEventListener("click", function() {
				const type = this.dataset.type;
				tabButtons.forEach((b) => {
					b.classList.remove("btn-primary");
					b.classList.add("btn-outline-primary");
				});
				this.classList.remove("btn-outline-primary");
				this.classList.add("btn-primary");
				applyFilter(type);
			});
		});
	}

	function applyFilter(type) {
		const table = window.invoiceTableInstance;
		if (!table) return;

		const filterField = "진행상태";
		let filterValue = null;

		switch (type) {
			case "ALL": table.clearFilter(); return;
			case "NONINVOICE": filterValue = "미청구"; break;
			case "NONCHECK": filterValue = "미확인"; break;
			case "ONGOING": filterValue = "진행중"; break;
			case "DMND": filterValue = "수금완료"; break;
			case "ACSUCCESS": filterValue = "회계반영완료"; break;
			default: return;
		}
		table.setFilter(filterField, "=", filterValue);
	}


	window.filterSearch = function() {
		// 🔍 검색 조건 수집 (널 안전 처리 포함)
		const partnerName = document.getElementById("partnerName")?.value.trim() || "";
		const quoteDateSearch1 = document.getElementById("quoteDateSearch1")?.value.trim() || "";
		const quoteDateSearch2 = document.getElementById("quoteDateSearch2")?.value.trim() || "";
		const managerSearch = document.getElementById("managerSearch")?.value.trim() || "";

		// 🔧 기본 필터 조건 배열
		const filters = [];
		if (partnerName) filters.push({ field: "거래처명", type: "like", value: partnerName });
		if (managerSearch) filters.push({ field: "담당자", type: "like", value: managerSearch });

		// ✅ 전역 Tabulator 인스턴스 참조
		const table = window.invoiceTableInstance;

		if (table && typeof table.setFilter === "function") {
			table.clearFilter(); // 기존 필터 제거

			// 기본 필터 먼저 적용
			table.setFilter(filters);

			// ✅ 등록일자(quoteDateSearch1 ~ quoteDateSearch2) 범위 필터 추가
			if (quoteDateSearch1 || quoteDateSearch2) {
				table.addFilter((data) => {
					const dateStr = data["등록일자"];
					if (!dateStr) return false; // 등록일자가 없는 데이터는 제외

					const cellDate = new Date(dateStr);
					const startDate = quoteDateSearch1 ? new Date(quoteDateSearch1) : null;
					const endDate = quoteDateSearch2 ? new Date(quoteDateSearch2) : null;

					// 날짜 비교
					if (startDate && cellDate < startDate) return false;
					if (endDate && cellDate > endDate) return false;
					return true;
				});
			}

			console.log("✅ 클라이언트 필터 적용 완료:", filters, quoteDateSearch1, quoteDateSearch2);
		} else {
			console.error("❌ invoiceTable이 초기화되지 않았거나 Tabulator 인스턴스가 아닙니다.", table);
			alert("테이블이 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
		}
	};




	// ===========================================================
	// ✅ 모달 열기 (등록 / 상세)
	// ===========================================================
	window.showDetailModal = async function(mode, invoiceCode = null) {
		const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
		const modalTitle = document.querySelector("#newDetailModal .modal-title");
		const saveBtn = document.querySelector("#newDetailModal .btn-primary");
		const resetBtn = document.querySelector("#newDetailModal .btn-dark");
		const searchShipmentBtn = document.querySelector(".btnSearchShipment");
		const inputs = document.querySelectorAll("#newDetailModal input, #newDetailModal select, #newDetailModal textarea");

		if (mode === "regist") {
			modalTitle.textContent = "청구서 등록";
			saveBtn.style.display = "inline-block";
			resetBtn.style.display = "inline-block";
			searchShipmentBtn.style.display = "inline-block";
			inputs.forEach((el) => el.removeAttribute("readonly"));
			clearInvoiceModal();
		} else if (mode === "detail" && invoiceCode) {
			modalTitle.textContent = "청구서 상세정보";
			saveBtn.style.display = "none";
			resetBtn.style.display = "none";
			searchShipmentBtn.style.display = "none";
			inputs.forEach((el) => el.setAttribute("readonly", true));
			await loadInvoiceDetail(invoiceCode);
		}
		modal.show();
	};

	// ===========================================================
	// ✅ 모달 초기화 (푸터 포함)
	// ===========================================================
	function clearInvoiceModal() {
		document.querySelectorAll("#newDetailModal input").forEach((el) => {
			if (el.type === "checkbox") el.checked = false;
			else el.value = "";
		});
		document.querySelector("#itemDetailBody").innerHTML = "";
		document.querySelector("#supplyAmount").value = "0";
		document.querySelector("#taxAmount").value = "0";
		document.querySelector("#totalAmount").value = "0";

		const cells = getFooterCells();
		if (cells) {
			cells.qtyCell.textContent = "0 개";
			cells.supplyCell.textContent = "0";
			cells.taxCell.textContent = "0";
			cells.finalCell.textContent = "0";
		}
	}

	document.getElementById("newDetailModal").addEventListener("hidden.bs.modal", clearInvoiceModal);

	// ===========================================================
	// ✅ 상세조회
	// ===========================================================
	async function loadInvoiceDetail(invoiceCode) {
		try {
			const res = await fetch(`/api/getDetailInvoice/${invoiceCode}`);
			if (!res.ok) throw new Error("조회 실패");
			const data = await res.json();

			document.querySelector("#partnerCodeModal").value = data.partnerCode || "";
			document.querySelector("#partnerNameModal").value = data.partnerName || "";
			document.querySelector("#manager").value = data.manager || "";
			document.querySelector("#dmndDate").value = data.dmndDate ? data.dmndDate.split("T")[0] : "";

			const tbody = document.querySelector("#itemDetailBody");
			tbody.innerHTML = "";

			if (Array.isArray(data.invoiceDetail)) {
				data.invoiceDetail.forEach((d) => {
					const tr = document.createElement("tr");
					tr.innerHTML = `
						<td class="text-center"><input type="checkbox" class="form-check-input" disabled></td>
						<td><input type="text" class="form-control form-control-sm" name="shipmentCode" value="${d.shipmentCode || ""}" readonly></td>
						<td><input type="date" class="form-control form-control-sm" name="shipmentDate" value="${d.shipmentDate ? d.shipmentDate.split("T")[0] : ""}" readonly></td>
						<td><input type="text" class="form-control form-control-sm text-end" name="totalQuantity" value="${fmt(d.quantity || 0)}" readonly></td>
						<td><input type="text" class="form-control form-control-sm text-end" name="supplyAmount" value="${fmt(d.totalAmount || 0)}" readonly></td>
						<td><input type="text" class="form-control form-control-sm text-end" name="taxAmount" value="${fmt(d.tax || 0)}" readonly></td>
						<td><input type="text" class="form-control form-control-sm text-end fw-bold" name="finalAmount" value="${fmt(d.shipmentInvoiceAmount || 0)}" readonly></td>
					`;
					tbody.appendChild(tr);
				});
			}

			updateTableFooter();
		} catch (err) {
			console.error("🚨 상세조회 오류:", err);
			alert("청구서 상세 정보를 불러오는 중 오류가 발생했습니다.");
		}
	}

	// ===========================================================
	// ✅ 출하지시서 조회
	// ===========================================================
	window.loadShipmentDetailsForInvoice = async function() {
		try {
			const partnerCode = document.querySelector('#newDetailModal input[name="partnerCode"]')?.value?.trim();
			if (!partnerCode) return alert("거래처를 먼저 선택하세요.");

			const res = await fetch(`/api/shipment/completed?partnerCode=${partnerCode}`);
			if (!res.ok) throw new Error("출하지시서 조회 실패");
			const shipments = await res.json();
			if (!Array.isArray(shipments) || shipments.length === 0)
				return alert("출하완료된 출하지시서가 없습니다.");

			const tbody = document.querySelector("#itemDetailBody");
			tbody.innerHTML = "";

			const calcPromises = shipments.map(s =>
				fetch(`/api/invoice/calcAmount?shipmentCode=${encodeURIComponent(s.shipmentCode)}`)
					.then(r => r.ok ? r.json() : { supplyAmount: 0, taxAmount: 0, totalAmount: 0 })
					.catch(() => ({ supplyAmount: 0, taxAmount: 0, totalAmount: 0 }))
			);
			const calcResults = await Promise.all(calcPromises);

			shipments.forEach((s, i) => {
				const c = calcResults[i];
				const tr = document.createElement("tr");
				tr.innerHTML = `
					<td class="text-center"><input type="checkbox" class="form-check-input rowCheck"></td>
					<td><input type="text" name="shipmentCode" class="form-control form-control-sm" value="${s.shipmentCode}" readonly></td>
					<td><input type="date" name="shipmentDate" class="form-control form-control-sm" value="${s.shipmentDate || ''}" readonly></td>
					<td><input type="text" name="totalQuantity" class="form-control form-control-sm text-end" value="${fmt(s.totalQuantity || 0)}" readonly></td>
					<td><input type="text" name="supplyAmount" class="form-control form-control-sm text-end" value="${fmt(c.supplyAmount)}" readonly></td>
					<td><input type="text" name="taxAmount" class="form-control form-control-sm text-end" value="${fmt(c.taxAmount)}" readonly></td>
					<td><input type="text" name="finalAmount" class="form-control form-control-sm text-end fw-bold" value="${fmt(c.totalAmount)}" readonly></td>
				`;
				tbody.appendChild(tr);
			});
			updateTableFooter();
		} catch (err) {
			console.error("🚨 출하지시서 조회 실패:", err);
			alert("출하지시서 데이터를 불러오는 중 오류가 발생했습니다.");
		}
	};

	// ===========================================================
	// ✅ Footer 합계 계산
	// ===========================================================
	function updateTableFooter() {
		const rows = document.querySelectorAll("#itemDetailBody tr");
		let totalQty = 0, sumSupply = 0, sumTax = 0, sumTotal = 0;

		rows.forEach(tr => {
			totalQty += cleanValue(tr.querySelector('input[name="totalQuantity"]')?.value);
			sumSupply += cleanValue(tr.querySelector('input[name="supplyAmount"]')?.value);
			sumTax += cleanValue(tr.querySelector('input[name="taxAmount"]')?.value);
			sumTotal += cleanValue(tr.querySelector('input[name="finalAmount"]')?.value);
		});

		const cells = getFooterCells();
		if (cells) {
			cells.qtyCell.textContent = `${fmt(totalQty)} 개`;
			cells.supplyCell.textContent = fmt(sumSupply);
			cells.taxCell.textContent = fmt(sumTax);
			cells.finalCell.textContent = fmt(sumTotal);
		}
	}

	// ===========================================================
	// ✅ 선택 행 합계 (체크박스)
	// ===========================================================
	function updateSelectedTotals() {
		const checked = document.querySelectorAll("#itemDetailBody tr input.rowCheck:checked");
		let totalQty = 0, totalSupply = 0, totalTax = 0, totalFinal = 0;

		checked.forEach(chk => {
			const tr = chk.closest("tr");
			totalQty += cleanValue(tr.querySelector('input[name="totalQuantity"]').value);
			totalSupply += cleanValue(tr.querySelector('input[name="supplyAmount"]').value);
			totalTax += cleanValue(tr.querySelector('input[name="taxAmount"]').value);
			totalFinal += cleanValue(tr.querySelector('input[name="finalAmount"]').value);
		});

		document.querySelector("#supplyAmount").value = fmt(totalSupply);
		document.querySelector("#taxAmount").value = fmt(totalTax);
		document.querySelector("#totalAmount").value = fmt(totalFinal);

		const cells = getFooterCells();
		if (cells) {
			cells.qtyCell.textContent = `${fmt(totalQty)} 개`;
			cells.supplyCell.textContent = fmt(totalSupply);
			cells.taxCell.textContent = fmt(totalTax);
			cells.finalCell.textContent = fmt(totalFinal);
		}
	}

	document.querySelector("#itemDetailBody").addEventListener("change", (e) => {
		if (e.target.classList.contains("rowCheck")) updateSelectedTotals();
	});

	document.getElementById("newDetailModal").addEventListener("shown.bs.modal", () => {
		const btn = document.querySelector(".btnSearchShipment");
		if (btn) btn.onclick = loadShipmentDetailsForInvoice;
	});




	// ===========================================================
	// ✅ 회계 일괄반영
	// ===========================================================
	window.insertAc = async function() {
		const table = window.invoiceTableInstance;
		if (!table) {
			alert("테이블이 아직 준비되지 않았습니다.");
			return;
		}

		// ✅ 선택된 행 데이터 가져오기
		const selectedRows = table.getSelectedData();
		if (selectedRows.length === 0) {
			return alert("회계반영할 청구서를 선택하세요.");
		}

		// ✅ '수금완료' 상태인 건만 필터링
		const eligibleRows = selectedRows.filter(row => row.진행상태 === "수금완료");
		if (eligibleRows.length === 0) {
			return alert("선택된 청구서 중 '수금완료' 상태인 건만 회계반영할 수 있습니다.");
		}

		// ✅ 사용자 확인
		if (!confirm(`선택된 ${eligibleRows.length}건의 청구서를 회계반영완료로 변경하시겠습니까?`)) {
			return;
		}

		try {
			// ✅ 서버 반영 (일괄 업데이트)
			const updatePayload = eligibleRows.map(row => ({
				invoiceCode: row.청구서코드,
				status: "회계반영완료"
			}));

			const res = await fetch("/api/updateInvoiceStatus", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content,
				},
				body: JSON.stringify(updatePayload)
			});

			if (!res.ok) throw new Error("서버 업데이트 실패");
			alert("회계반영이 완료되었습니다.");

			// ✅ 클라이언트 테이블 상태 갱신
			eligibleRows.forEach(row => {
				const rowComponent = table.getRow(row.청구서코드);
				if (rowComponent) {
					row.진행상태 = "회계반영완료";
					rowComponent.update({ 진행상태: "회계반영완료" });
				}
			});

			table.redraw(true);
		} catch (err) {
			console.error("🚨 회계반영 오류:", err);
			alert("회계반영 처리 중 오류가 발생했습니다.");
		}
	};


	window.updateStatusAPI = function(code, status, selectElement) {
		const row = window.invoiceTableInstance.getRows().find(r => r.getData().청구서코드 === code);
		// API 호출 전 현재 상태를 저장합니다.
		const currentStatus = row?.getData()?.진행상태;

		if (currentStatus === status) {
			console.log(`[출하지시서 ${code}]의 상태는 이미 '${status}'입니다. API 호출을 건너뜁니다.`);
			// 현재 상태와 같더라도 Tabulator가 자동으로 리렌더링하지 않으므로 select 값을 되돌립니다.
			if (selectElement) {
				selectElement.value = currentStatus;
			}
			return;
		}

		// 로딩 상태 등으로 임시 UI 변경을 원할 경우 여기에 로직을 추가할 수 있습니다.

		const url = "/api/updateInvoice";
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
		const csrfToken = document.querySelector('meta[name="_csrf"]').content;

		const data = {
			invoiceCode: code, // 서버에 보낼 견적서 코드
			status: status
		};

		fetch(url, {
			method: "POST",
			headers: {
				'Content-Type': 'application/json',
				[csrfHeader]: csrfToken
			},
			body: JSON.stringify(data)
		})
			.then(res => {
				if (!res.ok) {
					// HTTP 상태 코드가 200번대가 아니면 오류 처리
					return res.json().then(error => {
						throw new Error(error.message || `서버 오류 발생: ${res.status}`);
					});
				}
				return res.json();
			})
			.then(response => {
				if (response.success) { // 서버 응답에 'success: true'가 있다고 가정
					// Tabulator 행 데이터 업데이트 (화면 새로고침 없이)
					if (window.invoiceTableInstance) {
						// 고유 견적서 코드를 기반으로 행을 찾아 '진행상태' 필드를 업데이트합니다.
						// 이 업데이트는 자동으로 Tabulator 셀의 formatter를 다시 호출합니다.
						window.invoiceTableInstance.getRows().find(r => r.getData().청구서코드 === code)?.update({ '진행상태': status });
					}
				} else {
					alert(`상태 변경에 실패했습니다: ${response.message || '알 수 없는 오류'}`);
					// 실패 시 <select> 요소를 원래 상태로 되돌립니다.
					if (selectElement) {
						selectElement.value = currentStatus;
					}
				}
			})
			.catch(err => {
				console.error("상태 변경 API 호출 실패:", err);
				alert(`상태 변경 중 통신 오류가 발생했습니다. 오류: ${err.message}`);
				// 실패 시 <select> 요소를 원래 상태로 되돌립니다.
				if (selectElement) {
					selectElement.value = currentStatus;
				}
			});
	}


	// ===========================================================
	// ✅ 청구서 저장
	// ===========================================================
	window.saveModal = async function() {
		const partnerCode = document.querySelector("#partnerCodeModal").value.trim();
		const partnerName = document.querySelector("#partnerNameModal").value.trim();
		const manager = document.querySelector("#empCode").value.trim();
		const dmndDate = document.querySelector("#dmndDate").value;
		const totalAmount = cleanValue(document.querySelector("#totalAmount").value);

		if (!partnerCode || !manager || !dmndDate) return alert("필수 항목을 모두 입력하세요.");

		const details = [];
		document.querySelectorAll("#itemDetailBody tr input.rowCheck:checked").forEach(chk => {
			const tr = chk.closest("tr");
			const shipmentCode = tr.querySelector("input[name='shipmentCode']").value.trim();
			if (!shipmentCode) return;
			details.push({
				shipmentCode,
				shipmentDate: tr.querySelector("input[name='shipmentDate']").value,
				quantity: cleanValue(tr.querySelector("input[name='totalQuantity']").value),
				totalAmount: cleanValue(tr.querySelector("input[name='supplyAmount']").value),
				tax: cleanValue(tr.querySelector("input[name='taxAmount']").value),
				shipmentInvoiceAmount: cleanValue(tr.querySelector("input[name='finalAmount']").value)
			});
		});

		if (details.length === 0) return alert("출하 항목을 선택하세요.");

		const payload = {
			partnerCode,
			partnerName,
			manager,
			dmndDate,
			dmndAmt: totalAmount,
			status: "미확인",
			unrctBaln: totalAmount,
			invoiceDetail: details,
		};

		try {
			const res = await fetch("/api/registInvoice", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					[document.querySelector('meta[name="_csrf_header"]').content]:
						document.querySelector('meta[name="_csrf"]').content,
				},
				body: JSON.stringify(payload),
			});
			if (!res.ok) throw new Error("등록 실패");
			alert("청구서 등록 완료!");
			bootstrap.Modal.getInstance(document.getElementById("newDetailModal")).hide();
			location.reload();
		} catch (err) {
			console.error("🚨 등록 오류:", err);
			alert("등록 중 오류가 발생했습니다.");
		}
	};

	// ===========================================================
	// ✅ Tabulator
	// ===========================================================
	let tabulatorColumns = [
		{
			formatter: "rowSelection",
			titleFormatter: "rowSelection",
			hozAlign: "center",
			headerHozAlign: "center",
			headerSort: false,
			width: 50,
			frozen: true,
		},
		...columns.map((col) => {
			let def = { title: col, field: col, visible: defaultVisible.includes(col) };
			if (col === "청구서코드") {
				def.formatter = (cell) => {
					const code = cell.getData().청구서코드;
					return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${code}')">${cell.getValue()}</div>`;
				};
			}
			// "진행상태" 컬럼에 HTML Select 요소 적용 (직접 변경 방식)
			if (col === "진행상태") {
				def.field = "진행상태";
				def.formatter = function(cell) {
					const value = cell.getValue(); // 현재 상태 값
					const rowData = cell.getData();
					const code = rowData.청구서코드;

					// 만약 진행상태가 "판매완료"라면 select 대신 input으로 표시
					if (value === "회계반영완료") {
						return `
		<input type="text" 
			class="form-control form-control-sm text-center bg-light" 
			value="${value}" 
			readonly 
			style="font-size:0.75rem; height:auto; min-width:90px; cursor: no-drop;">
	`;
					}

					// 나머지 상태는 select로 표시
					const options = Object.keys(STATUS_MAP).map(key => {
						const itemInfo = STATUS_MAP[key];
						const isSelected = key === value ? 'selected' : '';
						return `<option value="${key}" ${isSelected}>${itemInfo.label}</option>`;
					}).join('');

					return `
			<select class="form-select form-select-sm" 
					onchange="updateStatusAPI('${code}', this.value, this)"
					style="font-size: 0.75rem; padding: 0.25rem 0.5rem; height: auto; min-width: 90px;">
				${options}
			</select>
		`;
				};
			}

			if (col === "청구금액") {
				def.title = "청구금액 (원)"; // 컬럼명 변경
				def.formatter = function(cell) {
					const v = cell.getValue();
					if (v === null || v === undefined || isNaN(v)) return "-";
					return Number(v).toLocaleString('ko-KR');
				};
				def.sorter = "number";
				def.hozAlign = "right";
			}

			return def;
		}),
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.invoiceTableInstance = tableInstance;
	initTabFiltering();
});
