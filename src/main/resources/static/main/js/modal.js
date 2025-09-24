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

function openModal(type, onSelect, commonGroup) {
    const config = modalConfigs[type];
    if (!config) {
        console.error(`modalConfigs[${type}] 설정이 없습니다.`);
        return;
    }

    // 제목 설정
    document.getElementById("commonModalLabel").textContent = config.title;

    // Tabulator 초기화
    if (table) {
        table.destroy();
    }

    table = new Tabulator("#commonModalTable", {
        ajaxURL: config.url,
        ajaxParams: commonGroup ? { commonGroup: commonGroup } : {}, // 공통그룹 파라미터
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
                    if (onSelect) {
                        onSelect(selected);
                    }
                    bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
                }
            }
        ],
        ajaxResponse: function (url, params, response) {
            return response; // 필요 시 response.data 로 변경
        }
    });

    // 검색 이벤트 연결
    const searchInput = document.getElementById("commonModalSearch");
    if (searchInput) {
        searchInput.value = ""; // 초기화
        searchInput.oninput = function () {
            const keyword = this.value.trim();
            if (keyword) {
                const allFields = table.getColumns()
                    .map(col => col.getField())
                    .filter(f => f && f !== ""); // field 없는 버튼 컬럼 제외

                const filters = allFields.map(f => ({
                    field: f,
                    type: "like",
                    value: keyword
                }));

                table.setFilter([filters]);
            } else {
                table.clearFilter();
            }
        };
    }

    const modal = new bootstrap.Modal(document.getElementById("commonModal"));
    modal.show();
}

// === Excel Export ===
function exportExcel() {
    if (table) {
        table.download("xlsx", "table-data.xlsx", { sheetName: "Data" });
    } else {
        alert("테이블이 초기화되지 않았습니다.");
    }
}

// === PDF Export ===
function exportPDF() {
  if (!window.jspdf || !window.jspdf.jsPDF) {
    alert("🚨 jsPDF가 아직 로드되지 않았습니다.");
    return;
  }

  const { jsPDF } = window.jspdf;
  const doc = new jsPDF("landscape");

  // 현재 모달 제목 가져오기
  const title = document.getElementById("commonModalLabel").textContent || "검색 결과";

  // 한글 폰트 등록
  if (typeof window.registerNanumGothic === "function") {
    window.registerNanumGothic(doc);
  }
  doc.setFont("NanumGothic-Regular");

  const data = table.getData();
  const columns = table.getColumnDefinitions()
    .filter(col => col.field)
    .map(col => ({ header: col.title, dataKey: col.field }));

  doc.autoTable({
    columns: columns,
    body: data,
    styles: { font: "NanumGothic-Regular", fontSize: 9 },
    headStyles: { font: "NanumGothic-Regular", fontSize: 10, fillColor: [200, 200, 200] },
    margin: { top: 30 },
    didDrawPage: function () {
      // ✅ 페이지 상단 제목
      doc.setFontSize(16);
      doc.text(title, doc.internal.pageSize.getWidth() / 2, 15, { align: "center" });
    }
  });

  doc.save("table-data.pdf");
}
