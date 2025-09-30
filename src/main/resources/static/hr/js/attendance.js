const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

// 공통코드 불러오기
async function loadCommonCode(groupId) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupId}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {

const USE_YN = await loadCommonCode("USE_YN");

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
				editor: "select",
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

	// 저장 button
	document.getElementById("att-save").addEventListener("click", () => {
		alert("아라르랔아")
	});

	// 초기 로드
	loadAttendances();
});
