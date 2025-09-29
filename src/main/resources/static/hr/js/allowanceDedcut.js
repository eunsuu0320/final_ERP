manager = document.getElementById("companyCode").value;

// 공통코드 불러오기
async function loadCommonCode(groupId) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupId}`);
	const list = await res.json();

	// [{codeId:"Y", codeName:"사용함"}, {codeId:"N", codeName:"사용안함"}]
	// → {Y:"사용함", N:"사용안함"} 으로 변환
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {
	const tableEl = document.getElementById("allowance-table");
	if (!tableEl) return;

	// 공통코드 가져오기
	const USE_YN = await loadCommonCode("USE_YN");

	const allowanceTable = new Tabulator(tableEl, {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
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
			{ title: "수당코드", field: "allId", editor: false }, // PK는 수정 불가
			{ title: "수당항목", field: "allName", editor: "input" },
			{ title: "계산식", field: "formula", editor: "input" },
			{ title: "산출 방법", field: "calcNote", editor: "input" },
			{
				title: "사용여부",
				field: "allIs",
				editor: "select",
				editorParams: { values: USE_YN },   // 🔹 select 옵션 = {Y:"사용함", N:"사용안함"}
				formatter: (cell) => USE_YN[cell.getValue()] || cell.getValue() // 보기용
			}
		],
	});

	// 데이터 로드
	async function loadAllowances(companyCode) {
		const res = await fetch(`/allowance?companyCode=${companyCode}`);
		const data = await res.json();

		// 항상 마지막에 입력용 빈 행 추가
		const newRow = { companyCode: companyCode, allIs: "Y" };

		if (data.length === 0) {
			allowanceTable.setData([newRow]);
		} else {
			allowanceTable.setData([...data, newRow]);
		}
	}


	// 저장 버튼 클릭 시 → 전체 데이터 서버로 저장
	const header = document.querySelector('meta[name="_csrf_header"]').content;
	const token = document.querySelector('meta[name="_csrf"]').content;

	const saveBtn = document.getElementById("allDec-save");
	if (saveBtn) {
		saveBtn.addEventListener("click", async () => {
			const rows = allowanceTable.getData();
			console.log("저장할 데이터:", rows);

			try {
				const res = await fetch(`/allowance/saveAll?companyCode=${manager}`, {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						'X-CSRF-Token': token
					},
					body: JSON.stringify(rows)
				});

				const msg = await res.text();  // 문자열 응답
				if (res.ok && msg === "success") {
					alert("수당이 정상적으로 등록되었습니다.");
					loadAllowances(manager);
				} else {
					alert("수당 등록 실패: " + msg);
				}
			} catch (err) {
				console.error("fetch 예외:", err);
				alert("네트워크 오류: " + err.message);
			}
		});
	}

		// 사용중단 버튼
	const stopBtn = document.getElementById("allDec-stop");
	if (stopBtn) {
		stopBtn.addEventListener("click", async () => {
			const selected = allowanceTable.getSelectedData();
			if (selected.length === 0) {
				alert("대상을 선택하세요.");
				return;
			}
			const codes = selected.map(row => row.allId);

			try {
				const res = await fetch(`/allowance/updateStatus?companyCode=${manager}`, {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						'X-CSRF-Token': token
					},
					body: JSON.stringify({ codes: codes, status: "N" })
				});

				if (res.ok) {
					alert("선택 항목이 사용중단 처리되었습니다.");
					loadAllowances(manager);
				} else {
					alert("사용중단 실패");
				}
			} catch (err) {
				console.error("fetch 예외:", err);
				alert("네트워크 오류: " + err.message);
			}
		});
	}

	// 재사용 버튼
	const restartBtn = document.getElementById("allDec-reStart");
	if (restartBtn) {
		restartBtn.addEventListener("click", async () => {
			const selected = allowanceTable.getSelectedData();
			if (selected.length === 0) {
				alert("대상을 선택하세요.");
				return;
			}
			const codes = selected.map(row => row.allId);

			try {
				const res = await fetch(`/allowance/updateStatus?companyCode=${manager}`, {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						'X-CSRF-Token': token
					},
					body: JSON.stringify({ codes: codes, status: "Y" })
				});

				if (res.ok) {
					alert("선택 항목이 재사용 처리되었습니다.");
					loadAllowances(manager);
				} else {
					alert("재사용 실패");
				}
			} catch (err) {
				console.error("fetch 예외:", err);
				alert("네트워크 오류: " + err.message);
			}
		});
	}


	// 페이지 로드시 회사코드 기준 조회
	loadAllowances(manager);
});
