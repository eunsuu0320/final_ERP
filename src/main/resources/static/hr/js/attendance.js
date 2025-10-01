const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

// 공통코드 불러오기
async function loadCommonCode(groupName) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupName}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {

	const USE_YN = await loadCommonCode("GRP007");

	// 근태 항목
	// tabulator
	const attendanceTable = new Tabulator(document.getElementById("att-table"), {
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
			{ title: "근태코드", field: "attId", editor: false },
			{ title: "근태유형", field: "attType", editor: "input" },
			{
				title: "사용여부",
				field: "attIs",
				editor: "list",
				editorParams: { values: USE_YN },
				formatter: (cell) => USE_YN[cell.getValue()] || cell.getValue()
			},
			{ title: "비고", field: "note", editor: "input" },
		],
	});

	// 데이터 로드 함수
	async function loadAttendances() {
		const res = await fetch(`/attendance?companyCode=${manager}`);
		const data = await res.json();
		attendanceTable.setData([...data, { companyCode: manager }]);
	}

	const token = document.querySelector('meta[name="_csrf"]').content;

	// 저장 button
	document.getElementById("att-save").addEventListener("click", async () => {
		const rows = attendanceTable.getData(); // Tabulator 같은 데서 데이터 가져온다고 가정
		console.log("보낼 데이터:", rows);

		try {
			const res = await fetch(`/attendance/saveAll?companyCode=${manager}`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					"X-CSRF-Token": token
				},
				body: JSON.stringify(rows)
			});

			const result = await res.text();
			if (result === "success") {
				alert("근태 항목 저장을 완료하였습니다.");
				loadAttendances();
			} else {
				alert("근태 항목 저장을 실패하였습니다. 잠시 후 다시 시도해 주세요.");
			}
		} catch (err) {
			alert("서버에 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.");
			console.error(err);
		}
	});

	// 상태 변경 공통 함수
	async function updateStatus(url, codes, status, successMsg, failMsg, reloadFn) {
		try {
			const res = await fetch(url, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					"X-CSRF-Token": token   // 스프링 시큐리티 CSRF 쓰면 필요
				},
				body: JSON.stringify({ codes, status }) // codes와 status를 JSON으로 넘김
			});

			if (res.ok) {
				alert(successMsg);
				if (typeof reloadFn === "function") reloadFn(manager); // 성공 후 테이블 리로드
			} else {
				alert(failMsg);
			}
		} catch (err) {
			console.error("updateStatus 오류:", err);
			alert("서버 오류가 발생했습니다.");
		}
	}


	// 사용중단 버튼
	document.getElementById("att-stop").addEventListener("click", async () => {
		const codes = attendanceTable.getSelectedData().map(r => r.attId);
		if (codes.length) {
			await updateStatus(
				`/attendance/updateStatus?companyCode=${manager}`,
				codes,
				"N",
				"사용중단이 완료되었습니다.",
				"사용중단에 실패하였습니다. 잠시 후 다시 시도해주세요.",
				loadAttendances
			);
		}
	});

	// 재사용 버튼
	document.getElementById("att-reStart").addEventListener("click", async () => {
		const codes = attendanceTable.getSelectedData().map(r => r.attId);
		if (codes.length) {
			await updateStatus(
				`/attendance/updateStatus?companyCode=${manager}`,
				codes,
				"Y",
				"재사용이 완료되었습니다.",
				"사용중단에 실패하였습니다. 잠시 후 다시 시도해주세요.",
				loadAttendances
			);
		}
	});

	// 공통코드 휴가
	const HOLY = await loadCommonCode("GRP008");

	// 사원 근태 등록
	const empAttInsertTable = new Tabulator(document.getElementById("empAttInsert-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
		columns: [
			{
				title: "근태일자",
				field: "workDate",
				editor: "date",   // 달력 에디터
				editorParams: {
					format: "yyyy-MM-dd",  // 날짜 형식
				},
				hozAlign: "center"
			},
			{ title: "사원", field: "name" },  // 화면엔 이름
			{ title: "사원코드", field: "empCode", visible: false }, // DB용 (숨김)
			{ title: "근태", field: "attType" },
			{ title: "근태코드", field: "attId", visible: false },
			{
				title: "휴가",
				field: "holyIs",
				editor: "list",
				editorParams: { values: HOLY },
				formatter: (cell) => USE_YN[cell.getValue()] || cell.getValue()
			},
			{ title: "근태(일/시간)", field: "workTime", editor: "input" },
			{ title: "비고", field: "note", editor: "textarea" }
		],
		data: [{ companyCode: manager }]
	});

	// 셀 더블클릭 이벤트 등록
	empAttInsertTable.on("cellDblClick", function(e, cell) {
		const field = cell.getField();
		const row = cell.getRow();

		if (field === "name") {
			// 사원 선택 모달 열기
			openModal("employee", (selected) => {
				row.update({
					empCode: selected.empCode, // DB 코드
					name: selected.name        // 화면 표시
				});
			});
		} else if (field === "attType") {
			// 근태코드 선택 모달 열기
			openModal("attendance", (selected) => {
				row.update({
					attId: selected.attId,     // DB 코드
					attType: selected.attType  // 화면 표시
				});
			});
		}
	});

	// 사원 근태 조회
	const empAttSelectTable = new Tabulator(document.getElementById("empAttSelect-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
		placeholder: "조회된 사원의 근태 목록이 없습니다.",
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
			{ title: "근태번호", field: "empAttId" },
			{ title: "사원", field: "empCode" }, // 사원이름으로 교체 해야함
			{ title: "근태", field: "attId" }, // 사원이름으로 교체 해야함
			{ title: "휴가여부", field: "holyIs" },
			{ title: "근태(일/시간)", field: "holyIs" },
			{ title: "근태일자", field: "workDate" },
			{ title: "비고", field: "note" }
		],
	});

	// 데이터 로드 함수
	async function loadEmpAttendances() {
		const res = await fetch(`/empAttendance?companyCode=${manager}`);
		const data = await res.json();
		empAttSelectTable.setData([...data]);
	}

	// 선택된 행 가져오기
	function getSelectedRows() {
		return empAttSelectTable.getSelectedData();
	}

	// 선택된 데이터 → HTML 테이블 변환
	function buildTableHTML(rows) {
		if (!rows || rows.length === 0) return "<p>선택된 데이터가 없습니다.</p>";

		let html = `
      <h2 style="text-align:center;">근태전표</h2>
      <p style="text-align:right;">전표일자 : ${new Date().toISOString().slice(0, 10)}</p>
      <table border="1" cellspacing="0" cellpadding="5"
             style="width:100%; border-collapse:collapse; text-align:center;">
        <thead>
          <tr>
            <th>사원</th>
            <th>근태</th>
            <th>휴가여부</th>
            <th>근태(일/시간)</th>
            <th>근태일자</th>
            <th>비고</th>
          </tr>
        </thead>
        <tbody>
    `;

		rows.forEach(r => {
			html += `
        <tr>
          <td>${r.empCode ?? ""}</td>
          <td>${r.attId ?? ""}</td>
          <td>${r.holyIs ?? ""}</td>
          <td>${r.holyIs ?? ""}</td>
          <td>${r.workDate ?? ""}</td>
          <td>${r.note ?? ""}</td>
        </tr>
      `;
		});

		html += "</tbody></table>";
		return html;
	}

	// PDF 다운로드
	document.getElementById("empAtt-print").addEventListener("click", function() {
		const rows = getSelectedRows();
		const exportDiv = document.getElementById("empAtt-export");
		exportDiv.innerHTML = buildTableHTML(rows);

		const opt = {
			margin: 10,
			filename: '근태전표.pdf',
			image: { type: 'jpeg', quality: 0.98 },
			html2canvas: { scale: 2 },
			jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
		};
		html2pdf().set(opt).from(exportDiv).save();
	});

	// Excel 다운로드
	document.getElementById("empAtt-excel").addEventListener("click", function() {
		const rows = getSelectedRows();
		if (rows.length === 0) {
			alert("선택된 데이터가 없습니다.");
			return;
		}

		// JSON → SheetJS로 변환
		const ws = XLSX.utils.json_to_sheet(rows);
		const wb = XLSX.utils.book_new();
		XLSX.utils.book_append_sheet(wb, ws, "근태전표");
		XLSX.writeFile(wb, "근태전표.xlsx");
	});


	// 초기 로드
	loadAttendances();
	loadEmpAttendances();
});
