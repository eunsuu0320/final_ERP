document.addEventListener("DOMContentLoaded", () => {
    // Tabulator 테이블 생성
    const table = new Tabulator("#sales-table", {
        height: "600px", // 테이블 높이
        layout: "fitColumns", // 컬럼 너비를 테이블 너비에 맞게 조정
        placeholder: "데이터가 없습니다.", // 데이터가 없을 때 표시할 메시지
        
        // 데이터 가져오기 설정
        ajaxURL: "/api/sales/stats", // 데이터를 가져올 API URL
        ajaxResponse: function(url, params, response){
            let processedData = [];
            let prevCount = null;

            // 데이터를 순회하며 재거래율을 계산
            response.forEach(item => {
                const currentCount = item.correspondentCount;
                let retradeRate = 0;

                if (prevCount !== null) {
                    // 재거래율 계산 (올해 거래처 수 - 작년 거래처 수) / 작년 거래처 수 * 100
                    retradeRate = ((currentCount - prevCount) / prevCount) * 100;
                }

                // 계산된 재거래율을 소수점 2자리까지 반올림
                item.retradeRate = retradeRate.toFixed(2) + "%"; 
                processedData.push(item);
                prevCount = currentCount; // 다음 순회를 위해 현재 값을 저장
            });

            // 가공된 데이터를 Tabulator에 반환
            return processedData; 
        },

        // 컬럼 정의
        columns: [
            // 체크박스 컬럼 추가
            {formatter:"rowSelection", titleFormatter:"rowSelection", hozAlign:"center", headerSort:false, cellClick:function(e, cell){
                cell.getRow().toggleSelect();
            }},
            // 오라클 DB에서 반환되는 대문자 키에 맞게 field를 수정합니다.
            {title:"년도", field:"salesYear", hozAlign:"center", sorter:"number"},
            {title:"총 매출액", field:"totalSalesAmount", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"총 영업이익", field:"totalProfitAmount", hozAlign:"center", sorter:"number", formatter:"money", formatterParams:{
                precision:0
            }},
            {title:"신규 거래처수", field:"correspondentCount", hozAlign:"center", sorter:"number"},
            // 재거래율 컬럼은 이제 계산된 값을 사용
            {title:"재거래율", field:"retradeRate", hozAlign:"center"}
        ],
    });
});
