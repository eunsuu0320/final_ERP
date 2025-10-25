// ===== 유틸 =====
function pad2(n){ return ('0' + n).slice(-2); }
function lastDayOfMonth(y, m1to12){ return new Date(y, m1to12, 0).getDate(); }
function setDateInput(input, y, m, d){
  if (!input) return;
  input.value = y + '-' + pad2(m) + '-' + pad2(d);
}
function setHiddenYmd(yEl, mEl, dEl, y, m, d){
  if (yEl) yEl.value = String(y);
  if (mEl) mEl.value = pad2(m);
  if (dEl) dEl.value = pad2(d);
}

// ===== 기본값 일괄 적용 =====
function applyDefaults(){
  // 요소들
  var payDate     = document.getElementById('payDate');
  var payYear2    = document.getElementById('payYear2');
  var payMonth2   = document.getElementById('payMonth2');

  var settleStart = document.getElementById('settleStartDate');
  var settleEnd   = document.getElementById('settleEndDate');
  var startYear   = document.getElementById('startYear');
  var startMonth  = document.getElementById('startMonth');
  var startDay    = document.getElementById('startDay');
  var endYear     = document.getElementById('endYear');
  var endMonth    = document.getElementById('endMonth');
  var endDay      = document.getElementById('endDay');

  var targetStart = document.getElementById('targetStartDate');
  var targetEnd   = document.getElementById('targetEndDate');
  var tStartYear  = document.getElementById('targetStartYear');
  var tStartMonth = document.getElementById('targetStartMonth');
  var tStartDay   = document.getElementById('targetStartDay');
  var tEndYear    = document.getElementById('targetEndYear');
  var tEndMonth   = document.getElementById('targetEndMonth');
  var tEndDay     = document.getElementById('targetEndDay');

  // 오늘 기준 (이번 달)
  var now = new Date();
  var y = now.getFullYear();
  var m = now.getMonth() + 1; // 1..12
  var ld = lastDayOfMonth(y, m); // 말일

  // 지급연월 드롭다운 채우기/세팅 (전년도~내년도)
  if (payYear2) {
    // options 없으면 채우기
    if (!payYear2.options || payYear2.options.length === 0){
      for (var yy = y - 1; yy <= y + 1; yy++) {
        var opt = document.createElement('option');
        opt.value = '' + yy;
        opt.textContent = '' + yy;
        payYear2.appendChild(opt);
      }
    }
    payYear2.value = '' + y;
  }
  if (payMonth2) {
    if (!payMonth2.options || payMonth2.options.length === 0){
      for (var i = 1; i <= 12; i++) {
        var im = pad2(i);
        var optm = document.createElement('option');
        optm.value = im;
        optm.textContent = im;
        payMonth2.appendChild(optm);
      }
    }
    payMonth2.value = pad2(m);
  }

  // ---- 화면에 보이는 기본값 채우기 ----
  // 지급일 = 10일
  setDateInput(payDate, y, m, 10);

  // 정산기간 = 이번 달 1일 ~ 말일
  setDateInput(settleStart, y, m, 1);
  setDateInput(settleEnd,   y, m, ld);

  // 대상기간 = 이번 달 1일 ~ 말일
  setDateInput(targetStart, y, m, 1);
  setDateInput(targetEnd,   y, m, ld);

  // ---- 숨김 필드 동기화 ----
  setHiddenYmd(startYear, startMonth, startDay, y, m, 1);
  setHiddenYmd(endYear,   endMonth,   endDay,   y, m, ld);

  setHiddenYmd(tStartYear, tStartMonth, tStartDay, y, m, 1);
  setHiddenYmd(tEndYear,   tEndMonth,   tEndDay,   y, m, ld);
}

// ===== 변경 시 동기화 핸들러 =====
function bindChangeHandlers(){
  var payDate     = document.getElementById('payDate');
  var payYear2    = document.getElementById('payYear2');
  var payMonth2   = document.getElementById('payMonth2');

  var settleStart = document.getElementById('settleStartDate');
  var settleEnd   = document.getElementById('settleEndDate');
  var startYear   = document.getElementById('startYear');
  var startMonth  = document.getElementById('startMonth');
  var startDay    = document.getElementById('startDay');
  var endYear     = document.getElementById('endYear');
  var endMonth    = document.getElementById('endMonth');
  var endDay      = document.getElementById('endDay');

  var targetStart = document.getElementById('targetStartDate');
  var targetEnd   = document.getElementById('targetEndDate');
  var tStartYear  = document.getElementById('targetStartYear');
  var tStartMonth = document.getElementById('targetStartMonth');
  var tStartDay   = document.getElementById('targetStartDay');
  var tEndYear    = document.getElementById('targetEndYear');
  var tEndMonth   = document.getElementById('targetEndMonth');
  var tEndDay     = document.getElementById('targetEndDay');

  // 지급일 → 지급연월 자동 동기화
  if (payDate && !payDate._bound) {
    payDate.addEventListener('change', function(){
      if (payDate.value && payDate.value.length >= 7) {
        var vy = payDate.value.slice(0,4);
        var vm = payDate.value.slice(5,7);
        if (payYear2)  payYear2.value  = vy;
        if (payMonth2) payMonth2.value = vm;
      }
    }, { passive: true });
    payDate._bound = true;
  }

  // 정산 시작일 → 숨김필드, 종료<시작 보정
  if (settleStart && !settleStart._bound) {
    settleStart.addEventListener('change', function(){
      var v = settleStart.value; // YYYY-MM-DD
      if (v && v.length === 10) {
        setHiddenYmd(startYear, startMonth, startDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      } else {
        setHiddenYmd(startYear, startMonth, startDay, '', '', '');
      }
      if (settleEnd && settleEnd.value && settleEnd.value < v) {
        settleEnd.value = v;
        setHiddenYmd(endYear, endMonth, endDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      }
    }, { passive: true });
    settleStart._bound = true;
  }

  // 정산 종료일 → 숨김필드, 시작>종료 보정
  if (settleEnd && !settleEnd._bound) {
    settleEnd.addEventListener('change', function(){
      var v = settleEnd.value;
      if (v && v.length === 10) {
        setHiddenYmd(endYear, endMonth, endDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      } else {
        setHiddenYmd(endYear, endMonth, endDay, '', '', '');
      }
      if (settleStart && settleStart.value && settleStart.value > v) {
        settleStart.value = v;
        setHiddenYmd(startYear, startMonth, startDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      }
    }, { passive: true });
    settleEnd._bound = true;
  }

  // 대상 시작일 → 숨김필드, 종료<시작 보정
  if (targetStart && !targetStart._bound) {
    targetStart.addEventListener('change', function(){
      var v = targetStart.value;
      if (v && v.length === 10) {
        setHiddenYmd(tStartYear, tStartMonth, tStartDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      } else {
        setHiddenYmd(tStartYear, tStartMonth, tStartDay, '', '', '');
      }
      if (targetEnd && targetEnd.value && targetEnd.value < v) {
        targetEnd.value = v;
        setHiddenYmd(tEndYear, tEndMonth, tEndDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      }
    }, { passive: true });
    targetStart._bound = true;
  }

  // 대상 종료일 → 숨김필드, 시작>종료 보정
  if (targetEnd && !targetEnd._bound) {
    targetEnd.addEventListener('change', function(){
      var v = targetEnd.value;
      if (v && v.length === 10) {
        setHiddenYmd(tEndYear, tEndMonth, tEndDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      } else {
        setHiddenYmd(tEndYear, tEndMonth, tEndDay, '', '', '');
      }
      if (targetStart && targetStart.value && targetStart.value > v) {
        targetStart.value = v;
        setHiddenYmd(tStartYear, tStartMonth, tStartDay, v.slice(0,4), v.slice(5,7), v.slice(8,10));
      }
    }, { passive: true });
    targetEnd._bound = true;
  }

  // 초기화 버튼 → 기본값 재적용
  var resetBtn = document.getElementById('btn-save-reset');
  if (resetBtn && !resetBtn._bound) {
    resetBtn.addEventListener('click', function(e){
      e.preventDefault();
      applyDefaults();
    });
    resetBtn._bound = true;
  }
}

// ===== 로드/모달 오픈 시 기본값 보이게 반영 =====
document.addEventListener('DOMContentLoaded', function(){
  applyDefaults();     // 페이지 로드 직후 UI에 바로 보이게
  bindChangeHandlers();
});
document.addEventListener('shown.bs.modal', function(ev){
  if (ev.target && ev.target.id === 'salaryModal') {
    applyDefaults();   // 모달 열릴 때도 다시 보이게 반영
    bindChangeHandlers();
  }
}, { passive: true });

// ===== 저장 (payPeriod = YYYY-MM) =====
var saveBtn = document.getElementById("btn-save-emp");
if (saveBtn && !saveBtn._bound) {
  saveBtn.addEventListener("click", function (e) {
    e.preventDefault();

    var f = document.getElementById("salaryForm");
    if (!f) return;

    var token = document.querySelector('meta[name="_csrf"]')?.content || "";

    // payPeriod: YYYY-MM (우선순위: 정산기간 시작연월 → 지급연월 → 지급일의 YYYY-MM)
    var payPeriod = "";
    if (f.startYear && f.startYear.value && f.startMonth && f.startMonth.value) {
      payPeriod = f.startYear.value + "-" + f.startMonth.value;
    } else if (document.getElementById('payYear2')?.value &&
               document.getElementById('payMonth2')?.value) {
      payPeriod = document.getElementById('payYear2').value + "-" +
                  document.getElementById('payMonth2').value;
    } else if (f.payDate && f.payDate.value && f.payDate.value.length >= 7) {
      payPeriod = f.payDate.value.slice(0, 7);
    }

    var payload = {
      payPeriod: payPeriod, // YYYY-MM
      payType: f.payType.value,
      payName: document.getElementById("payName").value || '',
      payDate: f.payDate.value || '',
      payYm: (document.getElementById('payYear2')?.value || '') + '-' +
             (document.getElementById('payMonth2')?.value || '')
    };

    fetch("/salaryMaster/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-CSRF-Token": token
      },
      body: JSON.stringify(payload)
    })
    .then(function (r) { return r.text(); })
    .then(function (txt) {
      if (!txt.startsWith("success")) {
        alert("저장 실패: " + txt);
        return;
      }
      var modalEl = document.getElementById('salaryModal');
      if (modalEl) (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).hide();

      alert("저장 완료되었습니다.");
      location.reload();
    })
    .catch(function (err) {
      alert("저장 실패: " + err.message);
    });
  });
  saveBtn._bound = true;
}
