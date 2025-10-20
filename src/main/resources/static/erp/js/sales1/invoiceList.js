document.addEventListener("DOMContentLoaded", function() {
	const defaultVisible = ["청구서코드", "등록일자", "청구일자", "청구금액", "수금일자", "거래처명", "진행상태"];

	const STATUS_MAP = {
		"미청구": { label: "미청구" },
		"미확인": { label: "미확인" },
		"진행중": { label: "진행중" },
		"수금완료": { label: "수금완료" },
		"회계반영완료": { label: "회계반영완료" },
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
			if (col === "진행상태") {
				def.formatter = (cell) => {
					const value = cell.getValue();
					const code = cell.getData().청구서코드;
					const options = Object.keys(STATUS_MAP)
						.map((k) => `<option value="${k}" ${k === value ? "selected" : ""}>${STATUS_MAP[k].label}</option>`)
						.join("");
					return `<select class="form-select form-select-sm"
							onchange="updateStatusAPI('${code}', this.value, this)"
							style="font-size:.75rem; padding:.25rem .5rem; height:auto; min-width:90px;">
							${options}</select>`;
				};
			}
			return def;
		}),
	];

	const tableInstance = makeTabulator(rows, tabulatorColumns);
	window.invoiceTableInstance = tableInstance;
	initTabFiltering();
});
