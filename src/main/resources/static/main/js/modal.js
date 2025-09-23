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
	 // ✅ 추가: 거래처 검색
  partner: {
    url: "/api/partners",               // 서버 API에 맞게
    title: "거래처 검색",
    columns: [
      { title: "거래처코드", field: "partnerCode", width: 140, hozAlign: "center" },
      { title: "거래처명", field: "partnerName", minWidth: 200 },
      { title: "담당자", field: "picName", width: 120 },
      { title: "연락처", field: "tel", width: 140 },
    ],
  },

  // ✅ 추가: 전표 검색
  voucher: {
    url: "/api/statements/lookup",     // 서버 API에 맞게
    title: "전표 검색",
    columns: [
      { title: "전표일자", field: "voucherDate", width: 120, hozAlign: "center",
        formatter:(cell)=>{ const v=cell.getValue(); if(!v) return ""; const d=new Date(v); return isNaN(d)? v : d.toISOString().slice(0,10); }
      },
      { title: "전표번호", field: "voucherNo", width: 130, hozAlign: "center" },
      { title: "유형", field: "type", width: 90, hozAlign: "center",
        formatter:(c)=>({SALES:"매출",BUY:"매입",MONEY:"수금",PAYMENT:"지급"}[c.getValue()]||c.getValue())
      },
      { title: "거래처명", field: "partnerName", minWidth: 180 },
      { title: "적요", field: "remark", minWidth: 200 },
    ],
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
					bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
				}
			}
		],
		ajaxResponse: function(url, params, response) {
			return response;
		}
	});

	// 검색 이벤트 연결
	const searchInput = document.getElementById("commonModalSearch");
	if (searchInput) {
	    searchInput.value = ""; // 초기화
	    searchInput.addEventListener("input", function () {
	        const keyword = this.value.trim();
	        if (keyword) {
	            table.setFilter([
	                [
	                    { field: "empNo", type: "like", value: keyword },
	                    { field: "name", type: "like", value: keyword },
	                    { field: "dept", type: "like", value: keyword },
	                    { field: "grade", type: "like", value: keyword }
	                ]
	            ]);
	        } else {
	            table.clearFilter(); // 검색어 없으면 초기화
	        }
	    });
	}

	const modal = new bootstrap.Modal(document.getElementById("commonModal"));
	modal.show();

}


