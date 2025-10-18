document.addEventListener("DOMContentLoaded", function() {

	//최근 5년 드롭다운
	const yearSelect = document.getElementById("yearSelectProduct");
	const quarterSelect = document.getElementById("quarterSelectProduct");

	// 이미 옵션이 있다면 다시 안 만들기
	if (yearSelect && yearSelect.options.length === 0) {
		const currentYear = new Date().getFullYear();
		for (let y = currentYear; y > currentYear - 5; y--) {
			const option = document.createElement("option");
			option.value = y;
			option.textContent = y;
			yearSelect.appendChild(option);
		}
		yearSelect.value = currentYear; // 현재 연도로 기본 선택 지정
	}


	// 필터 파라미터 가져오는 함수
	function getFilterParams() {
		let year = yearSelect?.value ?? null;
		let quarter = quarterSelect?.value ?? null;
		let keyword = document.getElementById("productName")?.value ?? null;

		if (year === "") year = null;
		if (quarter === "") quarter = null;
		if (keyword) {
			keyword = keyword.trim();
			if (keyword === "") keyword = null;
		}

		const params = { year, quarter, keyword };
		console.log("스냅샷:", JSON.stringify(params)); // 참조 이슈 방지용
		return params;
	}

	// Tabulator 테이블 생성
	const salesTable = new Tabulator("#sales-table", {
		layout: "fitDataStretch",
		height: "408px",
		pagination: "local",
		paginationSize: 10,
		placeholder: "데이터가 없습니다.",
		ajaxURL: "/api/sales/profit-list",
		ajaxConfig: "GET",
		ajaxParams: getFilterParams(), // 초기 로딩 시 조건
		ajaxURLGenerator: function(url, config, params) {
			const { year, quarter, keyword } = getFilterParams();
			const qs = new URLSearchParams();
			if (year) qs.append("year", year);
			if (quarter) qs.append("quarter", quarter);
			if (keyword) qs.append("keyword", keyword);
			qs.append("_", Date.now()); // 캐시 방지
			const full = url + (qs.toString() ? `?${qs.toString()}` : "");
			console.log("ajaxURLGenerator:", full);
			return full;
		},

		columns: [
			{ title: "품목코드", field: "PRODUCTCODE", hozAlign: "center", width: 150 },
			{ title: "품목명", field: "PRODUCTNAME", hozAlign: "center", width: 150 },
			{
				title: "판매", columns: [
					{ title: "수량", field: "QTY", hozAlign: "right", width: 150},
					{ title: "단가", field: "SALEPRICE", hozAlign: "right", formatter: "money", width: 150},
					{ title: "금액", field: "SALEAMT", hozAlign: "right", formatter: "money" , width: 150},
				]
			},
			{
				title: "원가", columns: [
					{ title: "단가", field: "COSTPRICE", hozAlign: "right", formatter: "money" , width: 150},
					{ title: "금액", field: "COSTAMT", hozAlign: "right", formatter: "money", width: 150 },
				]
			},
			{
				title: "판매부대비", columns: [
					{ title: "금액", field: "EXPAMT", hozAlign: "right", formatter: "money", width: 150 },
				]
			},
			{
				title: "이익", columns: [
					{
						title: "이익률",
						field: "PROFITRATE",
						hozAlign: "center",
						formatter: function(cell) {
							const value = cell.getValue();
							return value !== null && value !== undefined
								? value.toString().replace('%', '') + '%'
								: '-';
						},
						width: 120
					}
				]
			}
		],
		ajaxResponse: function(url, params, response) {
			console.log("서버 응답 데이터:", response);
			updateSummary(response);
			return response;
		}
	});

	// 테이블 데이터 새로고침 함수 (salesTable 생성 이후에 둬야 함)
	function reloadTableData() {
		const snap = getFilterParams();
		console.log("재조회 스냅샷:", snap);
		salesTable.setData()                 // ajaxURLGenerator가 자동으로 최신 URL 생성
			.catch(err => console.error("데이터 로드 실패:", err));
	}

	// 검색버튼
	document.getElementById("btn-search").addEventListener("click", reloadTableData);

	const searchInput = document.getElementById("productName");
	searchInput.addEventListener("keypress", e => {
		if (e.key === "Enter") reloadTableData();
	});


	// 요약 박스 갱신
function updateSummary(raw) {
  // 1) Tabulator ajaxResponse가 배열만 넘기는 게 아닐 수도 있으니 방어
  const data = Array.isArray(raw)
    ? raw
    : Array.isArray(raw?.data)
      ? raw.data
      : [];

  // 2) 합계 계산 (대문자/소문자 필드 모두 지원)
  const totals = data.reduce((acc, item) => {
    const qty =
      item.QTY ?? item.qty ?? item.Quantity ?? 0;
    const saleAmt =
      item.SALEAMT ?? item.saleAmt ?? item.SALE_AMT ?? 0;

    acc.totalSalesCount += Number(qty) || 0;
    acc.totalSupply += Number(saleAmt) || 0;
    return acc;
  }, { totalSalesCount: 0, totalSupply: 0 });

  const totalTax = Math.floor(totals.totalSupply * 0.1); // 필요시 반올림 방식 조정
  const totalAmount = totals.totalSupply + totalTax;

  // 3) DOM 갱신 (요소가 없을 때도 안전하게)
  const tCount = document.getElementById("totalSalesCount");
  const tSupply = document.getElementById("totalSupply");
  const tTax = document.getElementById("totalTax");
  const tAmount = document.getElementById("totalAmount");

  if (tCount)  tCount.textContent  = totals.totalSalesCount.toLocaleString();
  if (tSupply) tSupply.textContent = totals.totalSupply.toLocaleString();
  if (tTax)    tTax.textContent    = totalTax.toLocaleString();
  if (tAmount) tAmount.textContent = totalAmount.toLocaleString();
}

 // 탭 이동
  const btnProduct = document.getElementById("btnProductView");
  const btnEmployee = document.getElementById("btnEmployeeView");

  if (btnProduct) {
    // 현재 페이지가 품목별: 활성 표시
    btnProduct.classList.add("active");
    btnEmployee?.classList.remove("active");

    // 사원별로 이동
    btnEmployee?.addEventListener("click", () => {
      window.location.href = "/employeeProfits";
    });
  }


});


