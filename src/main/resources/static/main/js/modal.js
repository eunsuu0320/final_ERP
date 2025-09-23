// modal.js (Tabulator 적용)

// === 공통 설정 ===
const modalConfigs = {
	employee: {
		url: "/api/modal/employee",
		title: "직원 검색",
		columns: [
			{ title: "사원번호", field: "empNo" },
			{ title: "성명", field: "name" },
			{ title: "부서", field: "dept" },
			{ title: "직급", field: "grade" }
		]
	},
	commonCode: {
		url: "/api/modal/commonCode",
		title: "공통코드 검색",
		columns: [
			{ title: "코드", field: "codeId" },
			{ title: "코드명", field: "codeName" },
		]
	}
};

let table; // Tabulator 인스턴스 전역 변수

// === 모달 열기 함수 ===
window.openModal = function(type, onSelect, commonGroup) {
  const config = modalConfigs[type];
  if (!config) {
    console.error(`modalConfigs[${type}] 설정이 없습니다.`);
    return;
  }

  const labelEl = document.getElementById("commonModalLabel");
  if (labelEl) labelEl.textContent = config.title;

  if (table) table.destroy();

  let tabulatorOptions = {
    ajaxURL: config.url,
    layout: "fitColumns",
    pagination: "local",
    paginationSize: 10,
    movableColumns: true,
    columns: [
      ...config.columns,
      {
        title: "선택",
        formatter: () => "<button class='btn btn-sm btn-primary'>선택</button>",
        width: 100,
        hozAlign: "center",
        cellClick: (e, cell) => {
          const selected = cell.getRow().getData();
          if (onSelect) onSelect(selected);
          bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
        }
      }
    ],
    ajaxResponse: (url, params, response) => response
  };

  // 공통그룹 필요할 때만 파라미터 추가
  if (commonGroup) {
    tabulatorOptions.ajaxParams = { commonGroup: commonGroup };
  }

  table = new Tabulator("#commonModalTable", tabulatorOptions);

  const modal = new bootstrap.Modal(document.getElementById("commonModal"));
  modal.show();
}


