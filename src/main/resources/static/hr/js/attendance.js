const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

// 공통코드 불러오기
async function loadCommonCode(groupName) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupName}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

// 사원 목록 불러오기 (사원 근태 조회 이름 변경 위함)
async function loadMapEmployees() {
	const res = await fetch(`/employees?companyCode=${manager}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.empCode, it.name]));
}

// 근태 목록 불러오기 (사원 근태 조회 이름 변경 위함)
async function loadMapAttendances() {
	const res = await fetch(`/attendance?companyCode=${manager}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.attId, it.attType]));
}

document.addEventListener("DOMContentLoaded", async () => {

	const USE_YN = await loadCommonCode("GRP007");

	// 근태 항목
	const attendanceTable = new Tabulator(document.getElementById("att-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 20,
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
				hozAlign: "center",
				editor: "list",
				editorParams: {
					// 키는 저장값(Y/N), 값은 표시 라벨
					values: { "Y": "사용함", "N": "사용안함" }
				},
				formatter: function(cell) {
					var v = cell.getValue();
					var sz = "13px";
					var pad = "0.35em 0.6em";
					if (v === "Y" || v === "y" || v === "사용함") {
						return '<span class="badge bg-success" style="font-size:' + sz + ';padding:' + pad + ';">사용함</span>';
					}
					return '<span class="badge bg-danger" style="font-size:' + sz + ';padding:' + pad + ';">사용안함</span>';
				},
				// 혹시 '사용함/사용안함' 문자열이 들어올 때 Y/N로 정규화
				mutatorEdit: function(value) {
					if (value === "사용함") return "Y";
					if (value === "사용안함") return "N";
					return (value ?? "").toString().toUpperCase();
				},
				accessorDownload: function(value) {
					return value === "Y" ? "사용함" : "사용안함";
				}
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
		paginationSize: 20,
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
				title: "휴가여부",
				field: "holyIs",
				editor: "list",
				editorParams: { values: Object.keys(HOLY).map(code => ({ value: code, label: HOLY[code] })) },
				formatter: (cell) => HOLY[cell.getValue()] ?? cell.getValue(), // 화면엔 라벨 표시
			},
			{ title: "근태시간", field: "workTime", editor: "input" },
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
				console.log("선택된 근태:", selected);
				row.update({
					attId: selected.attId,     // DB 코드
					attType: selected.attType  // 화면 표시
				});
			});
		}
	});

	// 등록 버튼 클릭 이벤트
	document.getElementById("empAtt-insert-save").addEventListener("click", async () => {
		try {
			// 1. 테이블 데이터 가져오기
			const rows = empAttInsertTable.getData();

			if (!rows || rows.length === 0) {
				alert("저장할 데이터가 없습니다.");
				return;
			}

			// 2) 행 단위 검증 (필수값 및 형식)
			const errors = [];
			const dateRe = /^\d{4}-\d{2}-\d{2}$/;

			for (let i = 0; i < rows.length; i++) {
				const r = rows[i];
				const no = i + 1;

				if (!r || !r.workDate || typeof r.workDate !== "string" || !dateRe.test(r.workDate)) {
					errors.push(`[${no}행] 근태일자(yyyy-MM-dd)`);
				}
				if (!r || !r.empCode) {
					errors.push(`[${no}행] 사원코드(empCode)`);
				}
				if (!r || !r.attId) {
					errors.push(`[${no}행] 근태코드(attId)`);
				}
				const wt = (r && r.workTime !== "" && r.workTime != null) ? Number(r.workTime) : NaN;
				if (!Number.isFinite(wt) || wt < 0) {
					errors.push(`[${no}행] 근태(일/시간)(workTime)은 0 이상 숫자`);
				}
				if (r && r.holyIs != null && r.holyIs !== "" && !Object.prototype.hasOwnProperty.call(HOLY, r.holyIs)) {
					errors.push(`[${no}행] 휴가여부(holyIs)는 목록에서 선택`);
				}
			}

			if (errors.length) {
				alert("먼저 값을 올바르게 입력하세요.\n\n확인 필요 항목:\n" + errors.join("\n"));
				return;
			}

			// 3. payload 만들기
			const payload = rows.filter(r => r.empCode && r.attId).map(r => ({
				...r,
				companyCode: undefined // ❌ companyCode는 body에 넣을 필요 없음
			}));
			console.log("저장 전 payload:", JSON.stringify(payload, null, 2));
			console.log("저장 데이터:", payload);

			// 3. 서버에 전송
			const res = await fetch(`/empAttendance/saveAll?companyCode=${manager}`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					"X-CSRF-Token": token
				},
				body: JSON.stringify(payload) // 배열 그대로 보냄
			});

			if (!res.ok) {
				throw new Error("서버 오류: " + res.status);
			}

			const result = await res.text();
			if (result === "success") {
				alert("사원 근태 등록을 완료하였습니다.");
				empAttInsertTable.clearData();
				loadEmpAttendances();
			} else {
				alert("사원 근태 등록에 실패하였습니다. 잠시 후 다시 시도해주세요.");
				console.log(result);
			}

		} catch (err) {
			console.error("저장 중 오류:", err);
			alert("에러 발생: " + err.message);
		}
	});

	const EMP_MAP = await loadMapEmployees();
	const ATT_MAP = await loadMapAttendances();

	// 사원 근태 조회
	const empAttSelectTable = new Tabulator(document.getElementById("empAttSelect-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 20,
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
			{
				title: "사원",
				field: "empCode",
				formatter: (cell) => EMP_MAP[cell.getValue()] || cell.getValue()
			},
			{
				title: "근태",
				field: "attId",
				formatter: (cell) => ATT_MAP[cell.getValue()] || cell.getValue()
			},
			{
				title: "휴가여부",
				field: "holyIs",
				formatter: (cell) => HOLY[cell.getValue()] || cell.getValue()
			},
			{ title: "근태(일/시간)", field: "workTime" },
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

	// 인쇄
	const printBtn = document.getElementById("empAtt-print");
	if (printBtn) {
		printBtn.addEventListener("click", () => {
			// ✅ 사원 근태 조회 테이블에서 선택 행 가져오기
			const selected = (typeof empAttSelectTable !== "undefined" && empAttSelectTable.getSelectedData)
				? empAttSelectTable.getSelectedData()
				: [];

			if (selected.length === 0) { alert("인쇄할 근태 데이터를 선택하세요."); return; }

			// (선택) 전표일자 표기를 위해 날짜 포맷
			const fmtDateYMD = (v) => {
				if (!v) return "";
				const d = new Date(v);
				if (!isNaN(d)) {
					const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, "0"), dd = String(d.getDate()).padStart(2, "0");
					return `${y}/${m}/${dd}`;
				}
				const m = String(v).match(/(\d{4})[.\-\/](\d{1,2})[.\-\/](\d{1,2})/);
				return m ? `${m[1]}/${String(m[2]).padStart(2, "0")}/${String(m[3]).padStart(2, "0")}` : v;
			};

			// ✅ 행 렌더링(사원명 / 근태명칭 / 근태(일/시간) / 적요)
			const rows = selected.map(r => {
				const empName = (typeof EMP_MAP !== "undefined" && EMP_MAP?.[r.empCode]) ? EMP_MAP[r.empCode] : (r.name ?? r.empCode ?? "");
				const attName = (typeof ATT_MAP !== "undefined" && ATT_MAP?.[r.attId]) ? ATT_MAP[r.attId] : (r.attName ?? r.attId ?? "");
				const timeVal = r.workTime ?? r.time ?? r.days ?? "";
				const noteVal = r.note ?? r.remark ?? "";
				return `
        <tr>
          <td>${empName}</td>
          <td style="text-align:left; padding-left:10px;">${attName}</td>
          <td>${timeVal}</td>
          <td style="text-align:left; padding-left:10px;">${noteVal}</td>
        </tr>`;
			}).join("");

			// (선택) 상단 표기 값
			const now = new Date().toISOString().slice(0, 16).replace("T", " ");
			const count = selected.length;
			const slipDate = fmtDateYMD(selected[0]?.workDate) || fmtDateYMD(new Date());

			const html = `
			<!doctype html>
			<html>
			<head>
			  <meta charset="utf-8">
			  <title>근태전표</title>
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
			    <div class="print-header"><h3 class="m-0">근태전표</h3></div>
			    <div class="meta"><div>전표일자 : ${slipDate}</div><div>선택건수: ${count} / 생성일시: ${now}</div></div>
			    <table class="print">
			      <colgroup>
			        <col class="emp"><col class="att"><col class="time"><col class="note">
			      </colgroup>
			      <thead>
			        <tr>
			          <th>사원명</th>
			          <th>근태명칭</th>
			          <th>근태(일/시간)</th>
			          <th>적요</th>
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

	// EXCEL 버튼 (교체)
	const excelBtn = document.getElementById("empAtt-excel");
	if (excelBtn) {
		excelBtn.addEventListener("click", function() {
			// 선택 행 안전하게 가져오기: empAttSelectTable 우선
			const rows = (typeof empAttSelectTable !== "undefined" && empAttSelectTable?.getSelectedData)
				? empAttSelectTable.getSelectedData()
				: (typeof table !== "undefined" && table?.getSelectedData ? table.getSelectedData() : []);

			if (!rows || rows.length === 0) {
				alert("선택된 데이터가 없습니다.");
				return;
			}

			if (typeof XLSX === "undefined") {
				alert("엑셀 라이브러리(XLSX)가 로드되지 않았습니다.");
				return;
			}

			// 사원별 그룹핑
			const grouped = rows.reduce((acc, r) => {
				(acc[r.empCode] ??= []).push(r);
				return acc;
			}, {});

			const wb = XLSX.utils.book_new();

			Object.keys(grouped).forEach((empCode) => {
				const list = grouped[empCode];

				const empRows = list.map((r) => ({
					사원: (typeof EMP_MAP !== "undefined" && EMP_MAP?.[r.empCode]) ? EMP_MAP[r.empCode] : (r.name ?? r.empCode ?? ""),
					근태: (typeof ATT_MAP !== "undefined" && ATT_MAP?.[r.attId]) ? ATT_MAP[r.attId] : (r.attName ?? r.attId ?? ""),
					휴가여부: (typeof HOLY !== "undefined" && HOLY?.[r.holyIs]) ? HOLY[r.holyIs] : (r.holyIs ?? ""),
					"근태(일/시간)": r.workTime ?? r.time ?? r.days ?? "",
					근태일자: r.workDate ?? "",
					적요: r.note ?? r.remark ?? ""
				}));

				const ws = XLSX.utils.json_to_sheet(empRows);

				// (선택) 기본 열 너비
				ws["!cols"] = [
					{ wch: 12 }, // 사원
					{ wch: 16 }, // 근태
					{ wch: 10 }, // 휴가여부
					{ wch: 12 }, // 근태(일/시간)
					{ wch: 12 }, // 근태일자
					{ wch: 30 }, // 적요
				];

				const sheetName = (typeof EMP_MAP !== "undefined" && EMP_MAP?.[empCode]) ? EMP_MAP[empCode] : (empCode || "사원");
				XLSX.utils.book_append_sheet(wb, ws, String(sheetName).slice(0, 31)); // 시트명 31자 제한
			});

			XLSX.writeFile(wb, "근태전표.xlsx");
		});
	}

	// 초기 로드
	loadAttendances();
	loadEmpAttendances();
});
