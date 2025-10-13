const manager = document.getElementById("companyCode").value; // 회사코드
const token = document.querySelector('meta[name="_csrf"]')?.content || ""; // 토큰

// 공통코드 불러오기
async function loadCommonCode(groupName) {
	const res = await fetch(`/api/modal/commonCode?commonGroup=${groupName}`);
	const list = await res.json();
	return Object.fromEntries(list.map(it => [it.codeId, it.codeName]));
}

document.addEventListener("DOMContentLoaded", async () => {
	// 급여대장 테이블 초기화
	const salaryTable = new Tabulator(document.getElementById("pay-table"), {
		layout: "fitColumns",
		pagination: "local",
		paginationSize: 20,
		placeholder: "조회된 급여대장이 없습니다.",
		columns: [
			{ title: "귀속날짜", field: "payPeriod", width: 120 },
			{ title: "지급구분", field: "payType", width: 90 },
			{ title: "대장명칭", field: "payName", width: 300 },
			{ title: "지급일", field: "payDate" },
			{ title: "지급연월", field: "payYm" },
			/*{
				title: "근무 확정",
				headerHozAlign: "center",
				hozAlign: "center",
				formatter: function(cell) {
					var d = cell.getRow().getData();
					var isConfirmed = d.confirmIs === "Y";
					return '<button type="button"'
						+ ' class="btn ' + (isConfirmed ? 'btn-info' : 'btn-success') + ' btn-sm py-1 px-2"' // 적당한 패딩
						+ (isConfirmed ? ' disabled' : '')
						+ ' style="font-size:13px; line-height:1.2; min-width:64px;">'
						+ (isConfirmed ? '확정완료' : '확정')
						+ '</button>';
				},
				cellClick: handlePayrollConfirmClick,
			},*/
			{
				title: "급여계산",
				field: "payCount",
				hozAlign: "center",
				formatter: function(cell) {
					var v = cell.getValue();
					var cnt = (v != null ? v : 0);
					return '<button type="button"'
						+ 'id="payCount"'
						+ ' class="btn btn-outline-primary btn-sm py-1 px-2"'
						+ ' style="font-size:13px; line-height:1.2; min-width:64px;">'
						+ '계산</button>';
				},
				cellClick: function(e, cell) {
					// 기존 핸들러 유지 (예: 모달 띄우기)
					// handlePayrollCalcClick(e, cell);  // 사용 중이면 이걸로 대체
					alert("급여계산");
				}
			},
			{
				title: "명세서",
				field: "payload",
				hozAlign: "center",
				formatter: function(cell) {
					var v = cell.getValue();
					var cnt = (v != null ? v : 0);
					return '<button type="button"'
						+ ' class="btn btn-outline-primary btn-sm py-1 px-2"'
						+ ' style="font-size:13px; line-height:1.2; min-width:64px;">'
						+ '조회</button>';
				},
				cellClick: function(e, cell) {
					// 기존 핸들러 유지 (예: 모달 띄우기)
					// handlePayrollCalcClick(e, cell);  // 사용 중이면 이걸로 대체
					alert("급여계산");
				}
			},
			{ title: "인원수", field: "payCount", hozAlign: "right" },
			{ title: "지급총액", field: "payTotal", hozAlign: "right" },
			{
				title: "확정여부",
				field: "confirmIs",
				hozAlign: "center",
				formatter: function(cell) {
					var v = cell.getValue();
					var sz = '13px';                  // 기존 대비 +1~2px
					var pad = '0.35em 0.6em';         // 약간 여유
					if (v === "Y" || v === "y") {
						return '<span class="badge bg-primary" style="font-size:' + sz + '; padding:' + pad + ';">확정</span>';
					}
					if (v === "N" || v === "n" || v == null) {
						return '<span class="badge bg-secondary" style="font-size:' + sz + '; padding:' + pad + ';">미확정</span>';
					}
					return String(v);
				}
			}
		],
	});

	// 데이터 로드 함수
	async function loadSalaries() {
		try {
			const res = await fetch(`/salaryMaster?companyCode=${manager}`);
			if (!res.ok) throw new Error(await res.text());

			const data = await res.json();
			salaryTable.setData(data);
		} catch (err) {
			console.error("급여대장 불러오기 실패:", err);
			salaryTable.setData([]);
		}
	}

	// 신규 버튼 클릭 → 모달 열기
	const newBtn = document.getElementById("btn-new");
	if (newBtn) {
		newBtn.addEventListener("click", () => {
			// 폼 초기화
			const form = document.getElementById("salaryForm");
			if (form) form.reset();

			// 모달 열기
			const modalEl = document.getElementById("salaryModal");
			if (modalEl) {
				const modal = new bootstrap.Modal(modalEl);
				modal.show();
			}
		});
	}

	// 최초 데이터 로드
	loadSalaries();
});
