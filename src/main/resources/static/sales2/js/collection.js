
document.addEventListener("DOMContentLoaded", function() {

	// ================================
    // 📌 메인 목록 테이블
    // ================================
    let table = new Tabulator("#sales-table", {
        layout: "fitColumns",
        height: "500px",
        placeholder: "데이터가 없습니다.",
        ajaxURL: "/api/receivable/list",  // 👉 백엔드 API 엔드포인트 맞게 수정
        columns: [
            { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", headerSort: false },
            { title: "거래처명", field: "customerName", hozAlign: "center" },
            { title: "총 매출", field: "totalSales", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
            { title: "총 수금", field: "totalCollected", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
            { title: "미수금(잔액)", field: "outstanding", hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
            { title: "미수건수", field: "arrearsCount", hozAlign: "center" }
        ],
        ajaxResponse: function (url, params, response) {
            // 👉 총 거래처 수 표시
            document.querySelector("#total-count span").textContent = response.length + "건";
            return response;
        }
    });

    // ================================
    // 📌 검색 버튼(거래처모달)
    // ================================
    document.getElementById("btn-search").addEventListener("click", function () {
        const keyword = document.getElementById("partnerName").value.trim();
        if (partnerTable) {
            if (keyword) {
                partnerTable.setFilter("PARTNERNAME", "like", keyword);
            } else {
                partnerTable.clearFilter();
            }
        }
    });
    
    // ================================
    // 📌 Enter 키 검색
    // ================================
    document.getElementById("partnerName").addEventListener("keyup", function (e) {
        if (e.key === "Enter") {
            document.getElementById("btn-search").click();
        }
    });   
    
    // ================================
    // 📌 Excel 내보내기
    // ================================
    document.getElementById("btn-excel").addEventListener("click", function () {
        table.download("xlsx", "수금관리.xlsx", { sheetName: "수금관리" });
    });

	





    // ================================
    // 📌 신규 버튼
    // ================================
    document.getElementById("btn-new").addEventListener("click", function () {
        alert("신규 등록 모달 열기");
        // 👉 bootstrap modal 띄우거나 페이지 이동 연결
    });


});

