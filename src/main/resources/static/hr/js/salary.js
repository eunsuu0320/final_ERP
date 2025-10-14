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
				cellClick: salaryCalc,
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
					alert("명세서 조회");
				}
			},
			{ title: "인원수", field: "payCount", hozAlign: "center" },
			{
				title: "지급총액",
				field: "payTotal",          // 실제 필드명에 맞게
				hozAlign: "center",
				sorter: "number",
				formatter: "money",
				formatterParams: {
					thousand: ",",
					decimal: ".",
					symbol: "원",
					symbolAfter: true,        // 금액 뒤에 '원'
					precision: false          // 소수점 표시 안 함
				}
			},
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

	// 모달 실행 대상 보관용 (행 참조)
	let calcTargetRow = null;

	// 행 마다 급여계산 버튼 클릭 → 모달 오픈
	async function salaryCalc(e, cell) {
		// 선택된 행 보관
		calcTargetRow = cell.getRow();
		const d = calcTargetRow.getData();

		// 모달 내용 바인딩
		const elName = document.getElementById("calc-payName");
		const elYm = document.getElementById("calc-payYm");
		if (elName) elName.textContent = d?.payName ?? "-";
		if (elYm) elYm.textContent = d?.payYm ?? "-";

		// 모달 오픈
		const modalEl = document.getElementById("salaryCalcModal");
		if (modalEl) new bootstrap.Modal(modalEl).show();
	}

	// 모달의 "계산 실행" 버튼 → 프로시저 호출
	(function bindCalcRunOnce() {
		const btnCalcRun = document.getElementById("btn-calc-run");
		const modalEl = document.getElementById("salaryCalcModal");
		if (!btnCalcRun || !modalEl) return;

		// 모달 닫히면 대상 초기화
		modalEl.addEventListener("hidden.bs.modal", () => { calcTargetRow = null; });

		btnCalcRun.addEventListener("click", async () => {
			if (!calcTargetRow) return;

			// 버튼 잠금 & 진행표시
			btnCalcRun.disabled = true;
			const originalText = btnCalcRun.textContent;
			btnCalcRun.textContent = "계산 중...";

			try {
				const d = calcTargetRow.getData();

				// 프로시저 호출용 페이로드 (백엔드 Map<String,Object> 기준)
				const payload = {
					companyCode: manager,
					salaryId: (typeof d.salaryId !== "undefined") ? d.salaryId : null,
					payYm: d.payYm ?? null,
					payType: d.payType ?? null
				};

				const res = await fetch("/api/calculate", {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						"X-CSRF-Token": token
					},
					body: JSON.stringify(payload)
				});

				if (!res.ok) {
					const txt = await res.text();
					alert("급여계산 실패\n" + txt);
					return;
				}

				// 응답(JSON)이 있으면 해당 행만 부분 업데이트, 없으면 전체 리로드
				let result = null;
				try { result = await res.json(); } catch (_) { }

				if (result && (typeof result.updatedCount !== "undefined" || typeof result.totalAmount !== "undefined")) {
					const patch = {};
					if (typeof result.updatedCount !== "undefined") patch.payCount = result.updatedCount;
					if (typeof result.totalAmount !== "undefined") patch.payTotal = result.totalAmount;
					// 프로시저에서 확정 처리까지 했다면 확정 플래그도 갱신
					if (typeof result.confirmIs !== "undefined") patch.confirmIs = result.confirmIs;
					calcTargetRow.update(patch);
				} else {
					// 전부 갱신
					await loadSalaries();
				}

				alert("급여계산이 완료되었습니다.");
			} catch (err) {
				console.error(err);
				alert("급여계산 중 오류가 발생했습니다.");
			} finally {
				// 버튼/모달 복구
				btnCalcRun.disabled = false;
				btnCalcRun.textContent = originalText;
				bootstrap.Modal.getInstance(modalEl)?.hide();
				calcTargetRow = null;
			}
		}, { once: false }); // 한 번만 바인딩할 거면 true로 바꿔도 OK
	})();

	// 최초 데이터 로드
	loadSalaries();
});
