(function() {
	const pad = n => String(n).padStart(2, '0');

	function lastDayOfMonth(y, m) { return new Date(y, m, 0).getDate(); } // m: 1~12

	function fillYears(select, centerYear, spanBefore = 1, spanAfter = 1) {
		if (!select) return;
		select.innerHTML = '';
		for (let y = centerYear - spanBefore; y <= centerYear + spanAfter; y++) {
			const opt = document.createElement('option');
			opt.value = String(y);
			opt.textContent = String(y);
			select.appendChild(opt);
		}
	}
	function fillMonths(select) {
		if (!select) return;
		select.innerHTML = '';
		for (let m = 1; m <= 12; m++) {
			const opt = document.createElement('option');
			opt.value = pad(m);
			opt.textContent = pad(m);
			select.appendChild(opt);
		}
	}
	function fillDays(select, y, m) {
		if (!select) return;
		const max = lastDayOfMonth(Number(y), Number(m));
		const hold = select.value;
		select.innerHTML = '';
		for (let d = 1; d <= max; d++) {
			const opt = document.createElement('option');
			opt.value = pad(d);
			opt.textContent = pad(d);
			select.appendChild(opt);
		}
		if (hold && Number(hold) <= max) select.value = hold;
	}
	function setSelect(select, value) {
		if (!select) return;
		const v = String(value);
		const found = Array.from(select.options).find(o => o.value === v);
		if (found) select.value = v;
	}
	function attachYMDHandlers(ySel, mSel, dSel) {
		if (!ySel || !mSel || !dSel) return;
		ySel.addEventListener('change', () => fillDays(dSel, ySel.value, mSel.value));
		mSel.addEventListener('change', () => fillDays(dSel, ySel.value, mSel.value));
	}

	function initSalaryModal(root = document) {
		const get = (id) => root.getElementById
			? root.getElementById(id)           // document인 경우
			: (root.querySelector ? root.querySelector('#' + id) : null); // Element인 경우

		const payYear = get('payYear');
		const payMonth = get('payMonth');

		const startYear = get('startYear');
		const startMonth = get('startMonth');
		const startDay = get('startDay');
		const endYear = get('endYear');
		const endMonth = get('endMonth');
		const endDay = get('endDay');

		const tStartYear = get('targetStartYear');
		const tStartMonth = get('targetStartMonth');
		const tStartDay = get('targetStartDay');
		const tEndYear = get('targetEndYear');
		const tEndMonth = get('targetEndMonth');
		const tEndDay = get('targetEndDay');

		const payDate = get('payDate');
		const payYear2 = get('payYear2');
		const payMonth2 = get('payMonth2');

		// 루트에 해당 요소가 없으면 스킵 (다른 모달 열릴 때 대비)
		if (!payYear || !payMonth) return;

		const today = new Date();
		const CY = today.getFullYear();
		const CM = today.getMonth() + 1; // 1~12
		const LD = lastDayOfMonth(CY, CM);

		// 연/월 select 채우기
		[payYear, startYear, endYear, tStartYear, tEndYear, payYear2].forEach(sel => fillYears(sel, CY, 1, 1));
		[payMonth, startMonth, endMonth, tStartMonth, tEndMonth, payMonth2].forEach(fillMonths);

		// 기본값: 이번 달
		setSelect(payYear, CY);
		setSelect(payMonth, pad(CM));

		setSelect(startYear, CY);
		setSelect(startMonth, pad(CM));
		fillDays(startDay, CY, CM);
		setSelect(startDay, '01');

		setSelect(endYear, CY);
		setSelect(endMonth, pad(CM));
		fillDays(endDay, CY, CM);
		setSelect(endDay, pad(LD));

		setSelect(tStartYear, CY);
		setSelect(tStartMonth, pad(CM));
		fillDays(tStartDay, CY, CM);
		setSelect(tStartDay, '01');

		setSelect(tEndYear, CY);
		setSelect(tEndMonth, pad(CM));
		fillDays(tEndDay, CY, CM);
		setSelect(tEndDay, pad(LD));

		// 지급일 = 매달 10일
		if (payDate) {
			var mm = CM < 10 ? ('0' + CM) : String(CM);
			payDate.value = CY + '-' + mm + '-10';
		}

		// 지급일 → 지급연월 동기화
		function syncPayYearMonth2() {
			if (!payDate || !payYear2 || !payMonth2 || !payDate.value) return;
			const d = new Date(payDate.value + 'T00:00:00');
			const y = d.getFullYear();
			const m = d.getMonth() + 1;
			setSelect(payYear2, y);
			setSelect(payMonth2, pad(m));
		}
		syncPayYearMonth2();

		attachYMDHandlers(startYear, startMonth, startDay);
		attachYMDHandlers(endYear, endMonth, endDay);
		attachYMDHandlers(tStartYear, tStartMonth, tStartDay);
		attachYMDHandlers(tEndYear, tEndMonth, tEndDay);

		if (payDate) payDate.addEventListener('change', syncPayYearMonth2);

		// 초기화 버튼(같은 루트 기준 재초기화)
		const resetBtn = get('btn-save-reset');
		if (resetBtn) {
			resetBtn.addEventListener('click', function(e) {
				e.preventDefault();
				initSalaryModal(root);
			});
		}
	}

	// DOM 로드 후 1회
	document.addEventListener('DOMContentLoaded', function() {
		initSalaryModal(document);
	});

	// 부트스트랩 모달이 열릴 때마다 해당 모달 컨테이너 기준으로 재초기화
	document.addEventListener('shown.bs.modal', function(ev) {
		const modal = ev.target;              // Element
		if (modal && modal.id === 'salaryModal') {
			initSalaryModal(modal);
		}
	});
})();

// 저장
document.getElementById("btn-save-emp").addEventListener("click", function(e) {
	e.preventDefault();

	var f = document.getElementById("salaryForm");
	var token = document.querySelector('meta[name="_csrf"]')?.content || "";

	var payload = {
		payPeriod: f.payYear.value + "-" + f.payMonth.value,
		payType: f.payType.value,
		payName: document.getElementById("payName").value,
		payDate: f.payDate.value,
		payYm: f.payYear2.value + "-" + f.payMonth2.value
	};

	fetch("/salaryMaster/save", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"X-CSRF-Token": token
		},
		body: JSON.stringify(payload)
	})
		.then(r => r.text())
		.then(txt => {
			if (!txt.startsWith("success")) return alert("저장 실패: " + txt);

			// 모달 닫기
			var modalEl = document.getElementById('salaryModal');
			if (modalEl) (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).hide();

			// 페이지 새로고침
			location.reload();
			alert("저장 완료되었습니다.");
		})
		.catch(err => alert("저장 실패: " + err.message));
});

