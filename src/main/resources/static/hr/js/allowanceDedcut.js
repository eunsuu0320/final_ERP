const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

// 공통코드 불러오기
async function loadCommonCode(groupName) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupName}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {
	const allowanceEl = document.getElementById("allowance-table");
	const deductionEl = document.getElementById("deduction-table");
	if (!allowanceEl || !deductionEl) return;

	const USE_YN = await loadCommonCode("USE_YN");

	// 수당 테이블
	const allowanceTable = new Tabulator(allowanceEl, {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
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
			},
			{ title: "수당코드", field: "allId", editor: false },
			{ title: "수당항목", field: "allName", editor: "input" },
			{ title: "계산식", field: "formula", editor: "input" },
			{ title: "산출 방법", field: "calcNote", editor: "input" },
			{
				title: "사용여부",
				field: "allIs",
				editor: "select",
				editorParams: { values: USE_YN },
				formatter: (cell) => USE_YN[cell.getValue()] || cell.getValue()
			}
		],
	});

	// 공제 테이블
	const deductionTable = new Tabulator(deductionEl, {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
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
			},
			{ title: "공제코드", field: "dedId", editor: false },
			{ title: "공제항목", field: "dedName", editor: "input" },
			{ title: "계산식", field: "formula", editor: "input" },
			{ title: "산출 방법", field: "calcNote", editor: "input" },
			{
				title: "사용여부",
				field: "allIs",
				editor: "select",
				editorParams: { values: USE_YN },
				formatter: (cell) => USE_YN[cell.getValue()] || cell.getValue()
			}
		],
	});

	// 데이터 로드 함수
	async function loadAllowances() {
		const res = await fetch(`/allowance?companyCode=${manager}`);
		const data = await res.json();
		allowanceTable.setData([...data, { companyCode: manager, allIs: "Y" }]);
	}

	async function loadDeductions() {
		const res = await fetch(`/dedcut?companyCode=${manager}`);
		const data = await res.json();
		deductionTable.setData([...data, { companyCode: manager, allIs: "Y" }]);
	}

	// 공통 저장 함수
	async function saveData(url, rows, successMsg, failMsg, reloadFn) {
		try {
			const res = await fetch(url, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					"X-CSRF-Token": token
				},
				body: JSON.stringify(rows)
			});
			const msg = await res.text();
			if (res.ok && msg === "success") {
				alert(successMsg);
				reloadFn();
			} else {
				alert(failMsg + ": " + msg);
			}
		} catch (err) {
			alert("네트워크 오류: " + err.message);
		}
	}

	// 공통 상태변경 함수
	async function updateStatus(url, codes, status, successMsg, failMsg, reloadFn) {
		try {
			const res = await fetch(url, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					"X-CSRF-Token": token
				},
				body: JSON.stringify({ codes, status })
			});
			if (res.ok) {
				alert(successMsg);
				reloadFn();
			} else {
				alert(failMsg);
			}
		} catch (err) {
			alert("네트워크 오류: " + err.message);
		}
	}

	// CSRF
	const token = document.querySelector('meta[name="_csrf"]').content;

	// 현재 활성 탭 추적
	let activeTab = "allow"; // 기본은 수당
	document.getElementById("allow-tab").addEventListener("click", () => activeTab = "allow");
	document.getElementById("deduct-tab").addEventListener("click", () => activeTab = "deduct");

	// 버튼 이벤트 (공용)
	document.getElementById("allDec-save").addEventListener("click", async () => {
		if (activeTab === "allow") {
			const rows = allowanceTable.getData().filter(r => r.allName);
			await saveData(`/allowance/saveAll?companyCode=${manager}`, rows,
				"수당 등록 완료되었습니다", "수당 등록 실패하였습니다. 잠시 후 다시 시도해주세요.", loadAllowances);
		} else {
			const rows = deductionTable.getData().filter(r => r.dedName);
			await saveData(`/dedcut/saveAll?companyCode=${manager}`, rows,
				"공제 등록 완료되었습니다", "공제 등록 실패하였습니다. 잠시 후 다시 시도해주세요.", loadDeductions);
		}
	});

	document.getElementById("allDec-stop").addEventListener("click", async () => {
		if (activeTab === "allow") {
			const codes = allowanceTable.getSelectedData().map(r => r.allId);
			if (codes.length) await updateStatus(`/allowance/updateStatus?companyCode=${manager}`, codes, "N",
				"수당 사용중단 완료되었습니다.", "수당 사용중단 실패하였습니다. 잠시 후 다시 시도해주세요.", loadAllowances);
		} else {
			const codes = deductionTable.getSelectedData().map(r => r.dedId);
			if (codes.length) await updateStatus(`/dedcut/updateStatus?companyCode=${manager}`, codes, "N",
				"공제 사용중단 완료되었습니다.", "공제 사용중단 실패하였습니다. 잠시 후 다시 시도해주세요.", loadDeductions);
		}
	});

	document.getElementById("allDec-reStart").addEventListener("click", async () => {
		if (activeTab === "allow") {
			const codes = allowanceTable.getSelectedData().map(r => r.allId);
			if (codes.length) await updateStatus(`/allowance/updateStatus?companyCode=${manager}`, codes, "Y",
				"수당 재사용 완료되었습니다.", "수당 재사용 실패하였습니다. 잠시 후 다시 시도해주세요.", loadAllowances);
		} else {
			const codes = deductionTable.getSelectedData().map(r => r.dedId);
			if (codes.length) await updateStatus(`/dedcut/updateStatus?companyCode=${manager}`, codes, "Y",
				"공제 재사용 완료되었습니다.", "공제 재사용 실패하였습니다. 잠시 후 다시 시도해주세요.", loadDeductions);
		}
	});

	// 초기 로드
	loadAllowances();
	loadDeductions();
});
