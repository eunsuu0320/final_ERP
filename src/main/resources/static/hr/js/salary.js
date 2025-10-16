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
					var d = cell.getRow().getData();
					var isConfirmed = d && (d.confirmIs === "Y" || d.confirmIs === "y");
					if (isConfirmed) {
						return '<button type="button"'
							+ ' class="btn btn-secondary btn-sm py-1 px-2" disabled'
							+ ' style="font-size:13px; line-height:1.2; min-width:72px;"'
							+ ' title="확정된 건은 계산할 수 없습니다.">계산</button>';
					} else {
						return '<button type="button"'
							+ ' class="btn btn-outline-primary btn-sm py-1 px-2"'
							+ ' style="font-size:13px; line-height:1.2; min-width:72px;"'
							+ ' title="해당 대장을 계산합니다.">계산</button>';
					}
				},
				cellClick: function(e, cell) {
					var d = cell.getRow().getData();
					if (!d) return;
					// 확정 건 클릭 시 아무 반응 없음(비활성 버튼이라 기본적으로 클릭 안 됨)
					if (d.confirmIs === "Y" || d.confirmIs === "y") return;
					// 미확정 건만 계산 로직 실행 (기존 salaryCalc 사용)
					salaryCalc(e, cell);
				},
			},
			{
				title: "명세서",
				field: "payload",
				hozAlign: "center",
				formatter: function() {
					return '<button type="button" class="btn btn-outline-primary btn-sm py-1 px-2" style="font-size:13px; line-height:1.2; min-width:64px;">조회</button>';
				},
				cellClick: function(e, cell) {
					var btn = e.target.closest('button');
					if (!btn) return; // 버튼 클릭일 때만
					showSalaryPayModalFromRow(cell.getRow(), btn);
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

	// 확정
	const fixBtn = document.getElementById("btn-fix");
	if (fixBtn) {
		fixBtn.addEventListener("click", async () => {
			try {
				const selectedRows = salaryTable.getSelectedRows();
				if (!selectedRows || selectedRows.length === 0) {
					alert("확정할 급여대장을 선택하세요.");
					return;
				}
				// 선택 행들의 salaryId 수집
				const ids = selectedRows
					.map(r => {
						const d = r.getData();
						return (d && typeof d.salaryId !== "undefined") ? d.salaryId : null;
					})
					.filter(v => v !== null);

				if (ids.length === 0) {
					alert("선택된 대장에 유효한 ID가 없습니다.");
					return;
				}

				// 서버 확정 요청 (기존에 사용하던 엔드포인트가 있다면 그걸로 교체)
				const res = await fetch("/api/payroll/confirm", {
					method: "POST",
					headers: {
						"Content-Type": "application/json",
						"X-CSRF-Token": token
					},
					body: JSON.stringify({
						companyCode: manager,
						salaryIds: ids
					})
				});

				if (!res.ok) {
					const txt = await res.text();
					alert("확정 처리 실패\n" + txt);
					return;
				}
				// 응답을 사용해도 되고(인원수/총액 등), 최소한 확정 플래그는 Y로 반영
				let result = null;
				try { result = await res.json(); } catch (_) { }

				// 선택된 모든 행을 로컬에서도 즉시 Y로 업데이트 → calc 버튼 자동 비활성화
				for (let i = 0; i < selectedRows.length; i++) {
					const d = selectedRows[i].getData();
					if (d && ids.includes(d.salaryId)) {
						selectedRows[i].update({ confirmIs: "Y" });
					}
				}
				alert("확정 처리 완료되었습니다.");
				await loadSalaries();

			} catch (err) {
				console.error(err);
				alert("확정 처리 중 오류가 발생했습니다.");
			}
		});
	}

	// 명세서 조회 클릭
	async function showSalaryPayModalFromRow(row, btn) {
		// 버튼 클릭 아닐 때 대비
		if (btn) {
			var original = btn.innerHTML;
			btn.disabled = true;
			btn.innerHTML = "조회 중...";
		}

		// 행 데이터에서 salaryId 추출
		var d = row ? row.getData() : null;
		var salaryId = d && d.salaryId ? String(d.salaryId) : "";
		console.log(salaryId);

		if (!salaryId) {
			alert("대상 급여대장 ID(salaryId)가 없습니다.");
			if (btn) { btn.disabled = false; btn.innerHTML = original; }
			return;
		}

		try {
			// 프래그먼트 GET (salaryId를 반드시 쿼리스트링으로 전달)
			var res = await fetch("/salaryPay?salaryId=" + encodeURIComponent(salaryId), {
				method: "GET",
				cache: "no-store"
			});

			if (!res.ok) {
				var txt = await res.text();
				alert("명세서 조회 실패: " + txt);
				return;
			}

			// HTML 주입
			var html = await res.text();
			var host = document.getElementById("salaryPayModalHost");
			if (!host) {
				host = document.createElement("div");
				host.id = "salaryPayModalHost";
				document.body.appendChild(host);
			}
			host.innerHTML = html;

			// 모달 표시
			var modalEl = document.getElementById("payDetailModal");
			if (!modalEl) {
				alert("모달 엘리먼트를 찾을 수 없습니다.");
				return;
			}
			var instance = bootstrap.Modal.getOrCreateInstance(modalEl);
			instance.show();
		} catch (err) {
			console.error(err);
			alert("명세서 조회 중 오류가 발생했습니다.");
		} finally {
			if (btn) {
				btn.disabled = false;
				btn.innerHTML = original;
			}
		}
	}

	// 검색
	const selField = document.getElementById("sel-field");
	const txtSearch = document.getElementById("txt-search");
	const btnSearch = document.getElementById("btn-search");

	// 필드 선택에 따라 placeholder 안내
	function updatePlaceholder() {
		if (!txtSearch) return;
		if (selField?.value === "payPeriod") {
			txtSearch.placeholder = "예) 2025-08";
		} else {
			txtSearch.placeholder = "대장명칭 입력";
		}
	}
	selField?.addEventListener("change", updatePlaceholder);
	updatePlaceholder();

	// 유틸: 문자열 → 날짜 파싱 (YYYY-MM-DD | YYYY-MM)
	function tryParseDate(s) {
		if (!s) return null;
		const t = s.trim();
		// YYYY-MM-DD
		let m = t.match(/^(\d{4})-(\d{2})-(\d{2})$/);
		if (m) {
			const d = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]));
			return isNaN(d.getTime()) ? null : d;
		}
		// YYYY-MM (월의 1일로 처리)
		m = t.match(/^(\d{4})-(\d{2})$/);
		if (m) {
			const d = new Date(Number(m[1]), Number(m[2]) - 1, 1);
			return isNaN(d.getTime()) ? null : d;
		}
		return null;
	}

	// 유틸: 행의 payPeriod 값을 Date로
	function getRowPeriodDate(rowData) {
		// 우선순위: payPeriod -> payDate -> payYm
		const v = rowData?.payPeriod || rowData?.payDate || rowData?.payYm || "";
		// payYm(YYYYMM) 같은 형식이면 변환
		if (/^\d{6}$/.test(v)) {
			const y = Number(v.slice(0, 4));
			const m = Number(v.slice(4, 6)) - 1;
			const d = new Date(y, m, 1);
			return isNaN(d.getTime()) ? null : d;
		}
		// 일반적인 문자열(YYYY-MM[-DD]) 처리
		return tryParseDate(v);
	}

	// 검색 실행
	async function runSearch() {
		const field = selField?.value || "payPeriod";
		const keywordRaw = (txtSearch?.value || "").trim();

		// 입력 없으면 필터 제거 + 전체 재조회
		if (!keywordRaw) {
			salaryTable.clearFilter(true);
			// 원래 전체 목록을 서버에서 받아오던 흐름 유지
			await loadSalaries();
			return;
		}

		// 클라이언트 필터링
		if (field === "payName") {
			// 대장명칭 부분일치 (대소문자 무시)
			const kw = keywordRaw.toLowerCase();
			salaryTable.setFilter(function(data) {
				const v = (data?.payName || "").toString().toLowerCase();
				return v.includes(kw);
			});
		} else {
			// payPeriod: 날짜/연월 검색
			// 포맷 1) "YYYY-MM-DD ~ YYYY-MM-DD"
			// 포맷 2) "YYYY-MM ~ YYYY-MM"
			// 포맷 3) 단일 "YYYY-MM" 또는 "YYYY-MM-DD"
			const parts = keywordRaw.split("~").map(s => s.trim()).filter(Boolean);

			// 범위 검색
			if (parts.length === 2) {
				const d1 = tryParseDate(parts[0]);
				const d2 = tryParseDate(parts[1]);
				if (!d1 || !d2) {
					alert("날짜 형식이 올바르지 않습니다. 예) 2025-08-01 ~ 2025-08-31 또는 2025-08 ~ 2025-09");
					return;
				}
				const from = d1 < d2 ? d1 : d2;
				const to = d1 < d2 ? d2 : d1;
				// 날짜 상한 보정(하루 끝으로) – 월 단위 입력이라도 안전하게 포함
				const toEnd = new Date(to.getFullYear(), to.getMonth(), to.getDate() || 1, 23, 59, 59, 999);

				salaryTable.setFilter(function(data) {
					const rd = getRowPeriodDate(data);
					if (!rd) return false;
					return rd >= from && rd <= toEnd;
				});
			} else {
				// 단일 날짜/연월
				const d = tryParseDate(parts[0]);
				if (d) {
					// 연월만 입력했다면 같은 달 레코드 매칭
					const isMonthOnly = /^\d{4}-\d{2}$/.test(parts[0]);
					salaryTable.setFilter(function(data) {
						const rd = getRowPeriodDate(data);
						if (!rd) return false;
						if (isMonthOnly) {
							return rd.getFullYear() === d.getFullYear() && rd.getMonth() === d.getMonth();
						}
						// 일자까지 입력 시 같은 날짜인지 비교
						return rd.getFullYear() === d.getFullYear() &&
							rd.getMonth() === d.getMonth() &&
							rd.getDate() === d.getDate();
					});
				} else {
					// 날짜 형식 아니면 문자열 포함으로 fallback
					const kw = parts[0].toLowerCase();
					salaryTable.setFilter(function(data) {
						const v = (data?.payPeriod || data?.payDate || data?.payYm || "").toString().toLowerCase();
						return v.includes(kw);
					});
				}
			}
		}
	}

	// 버튼/엔터 키 바인딩
	btnSearch?.addEventListener("click", (e) => {
		e.preventDefault();
		runSearch();
	});
	txtSearch?.addEventListener("keydown", (e) => {
		if (e.key === "Enter") {
			e.preventDefault();
			runSearch();
		}
	});


	// 최초 데이터 로드
	loadSalaries();
});
