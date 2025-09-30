const manager = document.getElementById("companyCode").value; // 회사코드 가져오기

// 공통코드 불러오기
async function loadCommonCode(groupId) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupId}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {

	// 급여대장 테이블
	const salaryTable = new Tabulator(document.getElementById("pay-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 10,
		selectable: true,
		placeholder: "조회된 급여대장이 없습니다.",
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
			{ title: "귀속날짜", field: "payPeriod" },
			{ title: "지급구분", field: "payType" },
			{ title: "대장명칭", field: "payName" },
			{ title: "지급일", field: "payDate" },
			{ title: "지급연월", field: "payYm" },
			{ title: "인원수", field: "payCount" },
			{ title: "지급총액", field: "payTotal" },
			{ title: "확정여부", field: "confirmIs" },
		],
	});

	// 데이터 로드
	async function loadSalaries() {
		const res = await fetch(`salaryMaster?companyCode=${manager}`);
		const data = await res.json();
		salaryTable.setData([...data])
	}


	// 신규
	document.getElementById("btn-new").addEventListener("click", () => {
		alert("신규버튼");
	});

	// 데이터 로드
	loadSalaries();
});
