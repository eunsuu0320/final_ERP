// 회사코드 가져오기
const manager = document.getElementById("companyCode").value;

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
      { title: "인원수", field: "payCount", hozAlign: "right" },
      { title: "지급총액", field: "payTotal", hozAlign: "right" },
      { title: "확정여부", field: "confirmIs", hozAlign: "center" },
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
