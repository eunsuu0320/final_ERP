const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

async function loadCodeMaps(groups = ["GRP011", "GRP013", "GRP010", "GRP015"]) {
	const maps = {};
	await Promise.all(groups.map(async (g) => {
		const res = await fetch(`/api/modal/commonCode?commonGroup=${g}`);
		const list = await res.json();
		maps[g] = Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
	}));
	return maps;
}

document.addEventListener("DOMContentLoaded", async () => {
	const tableEl = document.getElementById("employee-table");0.
	if (!tableEl) {
		console.error("#employee-table 엘리먼트를 찾을 수 없습니다.");
		return;
	}

	// ▼ 조회/등록/수정 모드 토글 헬퍼
	function setModalMode(mode) {
		const modalEl = document.getElementById("empModal");
		if (!modalEl) return;
		modalEl.dataset.mode = mode;

		const titleEl = document.getElementById("empModalTitle") || modalEl.querySelector(".modal-title");
		if (titleEl) {
			if (mode === "view") titleEl.textContent = "사원 조회";
			else if (mode === "create") titleEl.textContent = "사원 등록";
			else if (mode === "edit") titleEl.textContent = "사원 수정";
		}

		// 입력 토글 (empForm 전체)
		const formEls = modalEl.querySelectorAll("#empForm input, #empForm select, #empForm textarea");
		formEls.forEach(el => {
			if (mode === "view") {
				el.setAttribute("readonly", "readonly");
				if (el.tagName === "SELECT" || el.type === "checkbox" || el.type === "radio" || el.type === "file") {
					el.disabled = true;
				}
			} else {
				el.removeAttribute("readonly");
				if (el.tagName === "SELECT" || el.type === "checkbox" || el.type === "radio" || el.type === "file") {
					el.disabled = false;
				}
			}
		});

		// 사번은 공통적으로 수정 불가
		const empCodeInput = document.getElementById("empCode");
		if (empCodeInput) empCodeInput.readOnly = true;

		// 계약 섹션 / 버튼들
		const contractSection = document.getElementById("contract-section");
		const btnPdfPick = document.getElementById("btn-pdf-pick");
		const pdfFile = document.getElementById("pdfFile");
		const agreeChk = document.getElementById("agreeContract");
		const btnSave = document.getElementById("btn-save-emp");
		const resetBtn = document.getElementById("btn-save-reset");

		if (mode === "view") {
			contractSection?.classList.add("d-none");
			if (btnPdfPick) btnPdfPick.disabled = true;
			if (pdfFile) pdfFile.disabled = true;
			if (agreeChk) agreeChk.disabled = true;
			if (btnSave) {
				btnSave.disabled = true;
				btnSave.title = "조회 모드에서는 저장할 수 없습니다.";
			}
			if (resetBtn) {
				resetBtn.disabled = true;
				resetBtn.title = "조회 모드에서는 초기화할 수 없습니다.";
			}
		}

		if (mode === "create") {
			contractSection?.classList.remove("d-none");
			if (btnPdfPick) btnPdfPick.disabled = false;
			if (pdfFile) pdfFile.disabled = false;
			if (agreeChk) agreeChk.disabled = false;
			if (btnSave) {
				btnSave.disabled = false;
				btnSave.title = "";
			}
			if (resetBtn) {
				resetBtn.disabled = false;
				resetBtn.title = "";
			}
		}

		if (mode === "edit") {
			contractSection?.classList.remove("d-none");
			if (btnPdfPick) btnPdfPick.disabled = false;
			if (pdfFile) pdfFile.disabled = false;
			if (agreeChk) agreeChk.disabled = false;
			if (btnSave) {
				btnSave.disabled = false;
				btnSave.title = "";
			}
			if (resetBtn) {
				resetBtn.disabled = false;
				resetBtn.title = "";
			}
			if (empCodeInput) empCodeInput.readOnly = true;
		}
	}

	const CODE = await loadCodeMaps(["GRP011", "GRP013", "GRP010", "GRP015"]);

	const table = new Tabulator(tableEl, {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 22,
		placeholder: "조회된 사원이 없습니다.",
		selectable: true,
		columns: [
			{
				title: "선택",
				formatter: "rowSelection",
				titleFormatter: "rowSelection",
				headerSort: false,
				width: 44,
				hozAlign: "center",
				headerHozAlign: "center",
				cellClick: (e, cell) => cell.getRow().toggleSelect(),
			},
			{ title: "사원번호", field: "empCode" },
			{ title: "성명", field: "name" },
			{ title: "부서명", field: "deptCode.codeName" },
			{ title: "직급", field: "gradeCode.codeName" },
			{ title: "직책", field: "positionCode.codeName" },
			{ title: "전화번호", field: "phone" },
			{ title: "Email", field: "email" },
			{ title: "입사일자", field: "hireDate", sorter: "date", hozAlign: "center" },
		],
	});

	window.tabled = table;

	// ▼ 더블클릭: 단건 조회
	window.tabled?.on("rowDblClick", async (e, row) => {
		const r = row.getData();
		if (!r?.empCode) return;

		try {
			const res = await fetch(`/api/employees/${encodeURIComponent(r.empCode)}`);
			if (!res.ok) throw new Error(await res.text());
			const emp = await res.json();

			const modalEl = document.getElementById("empModal");
			if (!modalEl) return;

			setModalMode("view");

			const setVal = (id, v) => { const el = document.getElementById(id); if (el) el.value = v ?? ""; };
			setVal("empCode", emp.empCode);
			setVal("name", emp.name);
			setVal("phone", emp.phone);
			setVal("birth", (emp.birth || "").toString().slice(0, 10));
			setVal("email", emp.email);
			setVal("dept", emp.dept);
			setVal("position", emp.position);
			setVal("grade", emp.grade);
			setVal("salary", emp.salary);
			setVal("hireDate", (emp.hireDate || "").toString().slice(0, 10));
			setVal("resignDate", (emp.resignDate || "").toString().slice(0, 10));
			setVal("holyDays", emp.holyDays);
			setVal("depCnt", emp.depCnt);
			setVal("resignReason", emp.resignReason);
			setVal("bankCode", emp.bankCode);
			setVal("bankCodeName", CODE.GRP015[emp.bankCode]);
			setVal("accHolder", emp.accHolder);
			setVal("accNo", emp.accNo);
			setVal("postalCode", emp.postalCode);
			setVal("address", emp.address);

			new bootstrap.Modal(modalEl).show();
		} catch (err) {
			console.error(err);
			alert("단건 조회에 실패했습니다.");
		}
	});

	// 단 건 수정
	document.getElementById("btn-update")?.addEventListener("click", async () => {
		const selected = table.getSelectedData();
		if (selected.length !== 1) {
			alert("수정할 사원 한 명을 선택하세요.");
			return;
		}

		const empCode = selected[0].empCode;
		try {
			const res = await fetch(`/api/employees/${encodeURIComponent(empCode)}`);
			if (!res.ok) throw new Error(await res.text());
			const emp = await res.json();

			const modalEl = document.getElementById("empModal");
			if (!modalEl) return;

			setModalMode("edit");

			const setVal = (id, v) => { const el = document.getElementById(id); if (el) el.value = v ?? ""; };
			setVal("empCode", emp.empCode);
			setVal("name", emp.name);
			setVal("phone", emp.phone);
			setVal("birth", (emp.birth || "").toString().slice(0, 10));
			setVal("email", emp.email);
			setVal("dept", emp.dept);
			setVal("position", emp.position);
			setVal("grade", emp.grade);
			setVal("salary", emp.salary);
			setVal("hireDate", (emp.hireDate || "").toString().slice(0, 10));
			setVal("resignDate", (emp.resignDate || "").toString().slice(0, 10));
			setVal("holyDays", emp.holyDays);
			setVal("depCnt", emp.depCnt);
			setVal("resignReason", emp.resignReason);
			setVal("bankCode", emp.bankCode);
			setVal("bankCodeName", CODE.GRP015[emp.bankCode]);
			setVal("accHolder", emp.accHolder);
			setVal("accNo", emp.accNo);
			setVal("postalCode", emp.postalCode);
			setVal("address", emp.address);

			new bootstrap.Modal(modalEl).show();
		} catch (err) {
			console.error(err);
			alert("수정할 사원 데이터를 불러오지 못했습니다.");
		}
	});

	// 신규 버튼
	document.getElementById("btn-new")?.addEventListener("click", () => {
		const modalEl = document.getElementById("empModal");
		if (!modalEl) return;

		document.getElementById("empForm")?.reset();
		document.getElementById("contractForm")?.reset();
		const pdfLabel = document.getElementById("pdfFileName");
		if (pdfLabel) pdfLabel.textContent = "";

		setModalMode("create");
		const titleEl = document.getElementById("empModalTitle") || modalEl.querySelector(".modal-title");
		if (titleEl) titleEl.textContent = "사원 등록";

		new bootstrap.Modal(modalEl).show();
	});

	// ✅ 데이터 주입 (회사코드 반영)
	table.on("tableBuilt", async () => {
		try {
			const companyCode = document.getElementById("companyCode")?.value;
			if (!companyCode) {
				console.error("companyCode를 찾을 수 없습니다.");
				return;
			}
			const res = await fetch(`/employees?companyCode=${manager}`);
			if (!res.ok) throw new Error(await res.text());

			const data = await res.json();
			table.setData(data);
		} catch (e) {
			console.error("데이터 불러오기 실패:", e);
		}
	});

	// ===== 검색 =====
	const FIELD_MAP = { empCode: "empCode", name: "name", dept: "dept" };
	function debounce(fn, delay = 250) { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), delay); }; }

	function applySearch() {
		const rawField = document.getElementById("sel-field").value;
		const keyword = (document.getElementById("txt-search").value || "").trim();
		const field = FIELD_MAP[rawField] || rawField;

		table.clearFilter();
		if (!keyword) return;

		if (field === "dept") {
			const kw = keyword.toLowerCase();
			table.setFilter((data) => {
				const code = (data.dept || "").toString().toLowerCase();
				const name = (CODE.DEPT?.[data.dept] || "").toString().toLowerCase();
				return code.includes(kw) || name.includes(kw);
			});
			return;
		}
		table.setFilter(field, "like", keyword);
	}

	document.getElementById("btn-search").addEventListener("click", applySearch);
	document.getElementById("txt-search").addEventListener("keydown", (e) => {
		if (e.key === "Enter") applySearch();
	});
	document.getElementById("txt-search").addEventListener("input", debounce(applySearch, 300));

	// 초기화
	const resetBtn = document.getElementById("btn-save-reset");
	if (resetBtn) {
		resetBtn.addEventListener("click", () => {
			const form = document.getElementById("empForm");
			if (form) form.reset();
		});
	}

	// 인쇄
	const printBtn = document.querySelector(".btn-secondary");
	if (printBtn) {
		printBtn.addEventListener("click", () => {
			const selected = table.getSelectedData();
			if (selected.length === 0) { alert("인쇄할 사원을 선택하세요."); return; }

			const toName = (group, code) => (CODE?.[group]?.[code] ?? code) || "";
			const rows = selected.map(emp => `
      <tr>
        <td>${emp.empCode ?? ""}</td>
        <td>${emp.name ?? ""}</td>
        <td>${toName("GRP011", emp.dept)}</td>
        <td>${toName("GRP013", emp.grade)}</td>
        <td>${toName("GRP010", emp.position)}</td>
        <td>${emp.phone ?? ""}</td>
        <td>${emp.email ?? ""}</td>
        <td>${emp.hireDate ?? ""}</td>
      </tr>`).join("");

			const now = new Date().toISOString().slice(0, 16).replace("T", " ");
			const count = selected.length;

			const html = `
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>선택된 사원 목록</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    @page { size: A4 portrait; margin: 8mm; }
    html, body { height: 100%; }
    body { margin: 0; font-size: 12px; }
    .print-wrap { width: 100%; padding: 0; }
    .print-header { text-align: center; margin: 0 0 8px 0; }
    .meta { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; padding: 0 2mm; }
    table.print { width: 100%; table-layout: fixed; border-collapse: collapse; }
    table.print th, table.print td {
      border: 1px solid #dee2e6; padding: 6px 8px; vertical-align: middle;
      overflow: hidden; text-overflow: ellipsis; white-space: nowrap; word-break: keep-all;
    }
    thead th { background: #f8f9fa; text-align: center; font-weight: 600; }
    tbody tr:nth-child(even) td { background: #fcfcfc; }
    thead { display: table-header-group; }
    tfoot { display: table-footer-group; }
    tr { page-break-inside: avoid; }
    col.empcode{width:13%} col.name{width:12%} col.dept{width:13%} col.grade{width:10%}
    col.position{width:10%} col.phone{width:15%} col.email{width:17%} col.hire{width:10%}
  </style>
</head>
<body>
  <div class="print-wrap">
    <div class="print-header"><h3 class="m-0">선택된 사원 목록</h3></div>
    <div class="meta"><div>총 <strong>${count}</strong>명</div><div>생성일시: ${now}</div></div>
    <table class="print">
      <colgroup>
        <col class="empcode"><col class="name"><col class="dept"><col class="grade">
        <col class="position"><col class="phone"><col class="email"><col class="hire">
      </colgroup>
      <thead>
        <tr>
          <th>사원번호</th><th>성명</th><th>부서명</th><th>직급</th>
          <th>직책</th><th>전화번호</th><th>Email</th><th>입사일자</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  </div>
  <script>window.addEventListener('load', () => window.print());</script>
</body>
</html>`;

			const w = window.open("", "_blank");
			if (!w) { alert("팝업이 차단되었습니다. 팝업 허용 후 다시 시도하세요."); return; }
			w.document.open();
			w.document.write(html);
			w.document.close();
		});
	}
});
