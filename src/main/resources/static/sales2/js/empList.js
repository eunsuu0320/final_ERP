// /sales2/js/empList.js
let empTable;
let planTable;

document.addEventListener("DOMContentLoaded", function () {
  // ================================
  // 📌 왼쪽 사원 테이블 (페이지네이션 추가)
  // ================================
  empTable = new Tabulator("#empPlanList-table", {
    height: "610px",
    layout: "fitColumns",
    columnDefaults: { vertAlign: "middle", headerHozAlign: "center" },
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/sales/empPlanList?planYear=2025",

    // ✅ 페이지네이션 옵션 (local)
    pagination: "local",
    paginationSize: 20,                        // 기본 10개씩 보기
    paginationCounter: "rows",                 // "1-10 of 128" 형태 카운터

    columns: [
      { title: "사원명", field: "EMPNAME", width: 150, widthGrow: 0.3 },
      { title: "기존 거래처수", field: "CUSTOMERCOUNT", hozAlign: "center", widthGrow: 0.3 },
      { title: "작년 매출액(원)", field: "LASTYEARSALES", hozAlign: "right", formatter: "money", formatterParams: { thousand: ",", precision: 0 }, widthGrow: 0.4 },
      { title: "작년 매입단가(원)", field: "LASTYEARCOST", hozAlign: "right", formatter: "money", formatterParams: { thousand: ",", precision: 0 }, widthGrow: 0.4 },
      { title: "작년 영업이익(원)", field: "LASTYEARPROFIT", hozAlign: "right", formatter: "money", formatterParams: { thousand: ",", precision: 0 }, widthGrow: 0.4 }
    ]
  });

  // ================================
  // 📌 오른쪽 영업계획 등록 테이블
  // ================================
  planTable = new Tabulator("#plan-table", {
    layout: "fitColumns",
    reactiveData: true,
    columnDefaults: { vertAlign: "middle", headerHozAlign: "center" },
    columns: [
      { title: "분기", field: "qtr", hozAlign: "center", editor: false, widthGrow: 0.4 },
      { title: "올해 총 매출액(원)", field: "purpSales", hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ",", precision: 0 } },
      { title: "올해 총 영업이익(원)", field: "purpProfitAmt", hozAlign: "right", editor: "number", formatter: "money", formatterParams: { thousand: ",", precision: 0 } },
      { title: "신규 거래처수", field: "newVendCnt", editor: "number", hozAlign: "center"  }
    ],
    data: [
      { 분기: "1분기" },
      { 분기: "2분기" },
      { 분기: "3분기" },
      { 분기: "4분기" }
    ]
  });

  // ================================
  // 📌 저장 버튼 → 서버 전송 (오른쪽 패널)
  //  - 로딩 오버레이(emp-save-loading) 표시 + 중복 방지
  // ================================
  const mainSaveBtn   = document.querySelector('.table-box.right #btn-update-sales');
  const mainResetBtn  = document.querySelector('.table-box.right #btn-cancel-update');
  const mainOverlayEl = document.getElementById('emp-save-loading');

  if (mainSaveBtn) {
    mainSaveBtn.addEventListener("click", async () => {
      const btn = mainSaveBtn;
      if (btn.dataset.loading === "1") return;

      const empCode = document.getElementById("employCode").value;
      if (!empCode) {
        alert("사원을 먼저 선택해주세요");
        return;
      }

      // UI 잠그기
      btn.dataset.loading = "1";
      const originalHtml = btn.innerHTML;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>로딩 중…`;
      btn.disabled = true;
      if (mainResetBtn) mainResetBtn.disabled = true;
      if (mainOverlayEl) mainOverlayEl.classList.remove("d-none");

      try {
        const data = planTable.getData();

        // payload를 객체 구조로 감싸기
        const payload = {
          empCode: empCode,
          espCode: document.getElementById("espCode").value,
          detailPlans: data.map(row => ({
            espCode: row.espCode,
            esdpCode: row.esdpCode,
            qtr: row.qtr,
            purpSales: row.purpSales || 0,
            purpProfitAmt: row.purpProfitAmt || 0,
            newVendCnt: row.newVendCnt || 0
          }))
        };

        const res = await fetch("/api/sales/insertPlanWithDetails", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            [document.querySelector("meta[name='_csrf_header']").content]:
              document.querySelector("meta[name='_csrf']").content
          },
          body: JSON.stringify(payload)
        });

        const result = await res.text();
        if (!res.ok) throw new Error(result || `HTTP ${res.status}`);

        console.log("등록 성공:", result);
        alert("분기별 영업계획이 저장되었습니다!");
      } catch (err) {
        console.error("등록 실패:", err);
        alert("등록 중 오류 발생: " + err.message);
      } finally {
        // UI 해제
        btn.innerHTML = originalHtml;
        btn.disabled = false;
        if (mainResetBtn) mainResetBtn.disabled = false;
        if (mainOverlayEl) mainOverlayEl.classList.add("d-none");
        btn.dataset.loading = "0";
      }
    });
  }

  // ================================
  // 📌 사원 클릭 시 강조 + 계획 테이블 채우기
  // ================================
  let selectedRow = null;
  empTable.on("rowClick", function (e, row) {
    const data = row.getData();
    console.log("선택된 사원:", data);

    // 이전 강조 해제
    if (selectedRow) {
      selectedRow.getElement().style.fontWeight = "normal";
      selectedRow.getElement().style.backgroundColor = "";
    }
    // 현재 강조
    row.getElement().style.fontWeight = "bold";
    row.getElement().style.backgroundColor = "#f0f0f0";
    selectedRow = row;

    // hidden input 값 세팅
    document.getElementById("employCode").value = data.EMP_CODE;
    // document.getElementById("employeeName").value = data.EMPNAME; // 자동 입력 제거
    document.getElementById("espCode").value = data.ESPCODE;

    // 오른쪽 제목 업데이트
    document.getElementById("plan-title").innerText = data.EMPNAME + "님의 영업계획";

    //fetch 함수
    fetch("/api/slaes/empDeatilPlan?espCode=" + data.ESPCODE)
      .then(response => {
        if (!response.ok) {
          throw new Error("HTTP error! Status: " + response.status);
        }
        return response.json();
      })
      .then(data => {
        planTable.replaceData(data);
        console.log("응답 데이터:", data);
      })
      .catch(error => {
        console.error("에러 발생:", error);
      });
  });

  // ================================
  // 📌 검색 버튼(사원모달)
  // ================================
  document.getElementById("btn-search").addEventListener("click", function () {
    const keyword = document.getElementById("employeeName").value.trim();
    if (empTable) {
      if (keyword) {
        empTable.setFilter("EMPNAME", "like", keyword);
      } else {
        empTable.clearFilter();
      }
    }
  });

  // ================================
  // 📌 Enter 키 검색
  // ================================
  document.getElementById("employeeName").addEventListener("keyup", function (e) {
    if (e.key === "Enter") {
      document.getElementById("btn-search").click();
    }
  });

  // ================================
  // 📌 초기화 버튼
  // ================================
  document.getElementById("btn-cancel-update").addEventListener("click", () => {
    planTable.replaceData([
      { 분기: "1분기" },
      { 분기: "2분기" },
      { 분기: "3분기" },
      { 분기: "4분기" }
    ]);
    alert("계획 테이블이 초기화되었습니다!");
  });

  // ================================
  // 📌 수정(모달) 저장 — 로딩 표시 + 중복 방지
  //  - 모달 버튼은 같은 id가 중복되므로 #modifySalesModal 영역으로 한정
  // ================================
  const modalSaveBtn   = document.querySelector('#modifySalesModal #btn-update-sales');
  const modalCancelBtn = document.querySelector('#modifySalesModal #btn-cancel-update');
  const modalCloseBtn  = document.querySelector('#modifySalesModal .btn-close');
  const modalOverlayEl = document.getElementById('emp-update-loading');

  if (modalSaveBtn) {
    modalSaveBtn.addEventListener("click", async () => {
      const btn = modalSaveBtn;
      if (btn.dataset.loading === "1") return;

      // UI 잠그기
      btn.dataset.loading = "1";
      const originalHtml = btn.innerHTML;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>로딩 중…`;
      btn.disabled = true;
      if (modalCancelBtn) modalCancelBtn.disabled = true;
      if (modalCloseBtn)  modalCloseBtn.disabled  = true;
      if (modalOverlayEl) modalOverlayEl.classList.remove("d-none");

      try {
        // 🔧 실제 수정 저장 로직을 여기에 작성
        // 예: 모달 내 empListTable(or 편집 그리드)에서 데이터 수집 후 전송
        // const payload = ...
        // const res = await fetch("/api/sales/updatePlanWithDetails", { method:"PUT", headers:{...}, body: JSON.stringify(payload) });
        // if (!res.ok) throw new Error(await res.text());

        alert("수정이 완료되었습니다.");
        const modalEl = document.getElementById("modifySalesModal");
        bootstrap.Modal.getInstance(modalEl)?.hide();
      } catch (err) {
        console.error("수정 실패:", err);
        alert("수정 중 오류 발생: " + err.message);
      } finally {
        // UI 해제
        btn.innerHTML = originalHtml;
        btn.disabled = false;
        if (modalCancelBtn) modalCancelBtn.disabled = false;
        if (modalCloseBtn)  modalCloseBtn.disabled  = false;
        if (modalOverlayEl) modalOverlayEl.classList.add("d-none");
        btn.dataset.loading = "0";
      }
    });
  }
});
