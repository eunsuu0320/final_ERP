document.addEventListener("DOMContentLoaded", function() {

  //최근 5년 드롭다운
  const yearSelect = document.getElementById("yearSelectProduct");
  const quarterSelect = document.getElementById("quarterSelectProduct");

  if (yearSelect && yearSelect.options.length === 0) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear; y > currentYear - 5; y--) {
      const option = document.createElement("option");
      option.value = y;
      option.textContent = y;
      yearSelect.appendChild(option);
    }
    yearSelect.value = currentYear;
  }

  function getFilterParams() {
    let year = yearSelect?.value ?? null;
    let quarter = quarterSelect?.value ?? null;
    let keyword = document.getElementById("productName")?.value ?? null;

    year = (year ?? "").trim() || null;
    quarter = (quarter ?? "").trim() || null;
    if (keyword != null) {
      keyword = keyword.trim() || null;
    }
    return { year, quarter, keyword };
  }

  const salesTable = new Tabulator("#sales-table", {
    layout: "fitDataStretch",
    height: "408px",
    pagination: "local",
    paginationSize: 10,
    placeholder: "데이터가 없습니다.",
    ajaxURL: "/api/sales/profit-list",
    ajaxConfig: "GET",
    ajaxParams: getFilterParams(),
    ajaxURLGenerator: function(url, config, params) {
      const { year, quarter, keyword } = getFilterParams();
      const qs = new URLSearchParams();
      if (year) qs.append("year", year);
      if (quarter) qs.append("quarter", quarter);
      if (keyword) qs.append("keyword", keyword);
      qs.append("_", Date.now());
      return url + (qs.toString() ? `?${qs.toString()}` : "");
    },
    columns: [
      { title: "품목코드", field: "PRODUCTCODE", hozAlign: "center", width: 80 },
      { title: "품목명",   field: "PRODUCTNAME", hozAlign: "center", width: 120 },
      { title: "판매", columns: [
        { title: "수량(개)",  field: "QTY",        hozAlign: "center", width: 70 },
        { title: "단가(원)",  field: "SALEPRICE",  hozAlign: "right", formatter: "money", width: 90,  formatterParams:{precision:0} },
        { title: "금액(원)",  field: "SALEAMT",    hozAlign: "right", formatter: "money", width: 100, formatterParams:{precision:0} },
      ]},
      { title: "원가(원)", columns: [
        { title: "단가(원)",  field: "COSTPRICE",  hozAlign: "right", formatter: "money", width: 90,  formatterParams:{precision:0} },
        { title: "금액(원)",  field: "COSTAMT",    hozAlign: "right", formatter: "money", width: 100, formatterParams:{precision:0} },
      ]},
      { title: "판매부대비", columns: [
        { title: "금액(원)",  field: "EXPAMT",     hozAlign: "right", formatter: "money", width: 100, formatterParams:{precision:0} },
      ]},
      { title: "이익", columns: [
        {
          title: "이익률",
          field: "PROFITRATE",
          hozAlign: "center",
          formatter: function(cell) {
            const v = cell.getValue();
            return (v ?? v === 0) ? String(v).replace('%','') + '%' : '-';
          },
          width: 60
        }
      ]}
    ],
    ajaxResponse: function(url, params, response) {
      updateSummary(response);
      updateQuarterTop5ChartWithFetch(); // ← 테이블 응답 후 차트도 현재 조건으로 재조회
      return response;
    }
  });

  function reloadTableData() {
    salesTable.setData().catch(err => console.error("데이터 로드 실패:", err));
  }

  document.getElementById("btn-search").addEventListener("click", reloadTableData);
  document.getElementById("productName")?.addEventListener("keypress", e => {
    if (e.key === "Enter") reloadTableData();
  });

  // ✅ KPI 카드 업데이트 (summary-box 없이 카드만 채워도 동작)
  function updateSummary(raw) {
    const data = Array.isArray(raw) ? raw : (Array.isArray(raw?.data) ? raw.data : []);
    const totals = data.reduce((acc, item) => {
      const qty     = Number(item.QTY ?? 0);
      const saleAmt = Number(item.SALEAMT ?? 0); // 공급가액
      acc.totalSalesCount += qty;
      acc.totalSupply     += saleAmt;
      return acc;
    }, { totalSalesCount: 0, totalSupply: 0 });

    const totalTax    = Math.floor(totals.totalSupply * 0.1);
    const totalAmount = totals.totalSupply + totalTax;

    // (혹시 남아있으면) 예전 summary-box 스팬도 같이 채움 — 없어도 에러 X
    const tCount  = document.getElementById("totalSalesCount");
    const tSupply = document.getElementById("totalSupply");
    const tTax    = document.getElementById("totalTax");
    const tAmount = document.getElementById("totalAmount");
    if (tCount)  tCount.textContent  = totals.totalSalesCount.toLocaleString();
    if (tSupply) tSupply.textContent = totals.totalSupply.toLocaleString();
    if (tTax)    tTax.textContent    = totalTax.toLocaleString();
    if (tAmount) tAmount.textContent = totalAmount.toLocaleString();

    // ✅ 새 KPI 카드 3곳 채우기
    const kUnshipped = document.getElementById("kpi-unshipped");
    const kAccounted = document.getElementById("kpi-accounted");
    const kEstimate  = document.getElementById("kpi-estimate-open");
    if (kUnshipped) kUnshipped.textContent = totals.totalSalesCount.toLocaleString();
    if (kAccounted) kAccounted.textContent = totals.totalSupply.toLocaleString();
    if (kEstimate)  kEstimate.textContent  = totalAmount.toLocaleString();
  }

  // 탭 이동
  const btnProduct = document.getElementById("btnProductView");
  const btnEmployee = document.getElementById("btnEmployeeView");
  if (btnProduct) {
    btnProduct.classList.add("active");
    btnEmployee?.classList.remove("active");
    btnEmployee?.addEventListener("click", () => { window.location.href = "/employeeProfits"; });
  }

  /* ============================
   *  차트 베이스 + 안전한 툴팁
   * ============================ */
  (function () {
    if (window.__profitChartBooted) return; window.__profitChartBooted = true;

    const canvas = document.getElementById("profitChart");
    if (!canvas) return;
    if ((canvas.getBoundingClientRect().height | 0) === 0) canvas.style.height = "360px";

    const isV2 = C => !!(C && C.defaults && C.defaults.global);

    function buildBase(ChartLib){
      const ctx = canvas.getContext("2d");
      if (window.__profitChart?.destroy) { try{window.__profitChart.destroy();}catch(e){} }

      if (isV2(ChartLib)){
        window.__profitChart = new ChartLib(ctx, {
          type: "bar",
          data: { labels: [], datasets: [] },
          options: {
            responsive:true, maintainAspectRatio:false,
            legend:{ position:"bottom" },
            tooltips:{ callbacks:{ label: t => `${t.datasetLabel}: ${t.yLabel != null ? t.yLabel : "-"}%` } },
            scales:{ yAxes:[{ ticks:{ beginAtZero:true, callback:v=>v+"%" } }] },
            title:{ display:true, text:"분기별 이익률 Top5 (품목)" }
          }
        });
      } else {
        window.__profitChart = new ChartLib(ctx, {
          type: "bar",
          data: { labels: [], datasets: [] },
          options: {
            responsive:true, maintainAspectRatio:false,
            scales:{ y:{ beginAtZero:true, ticks:{ callback:v=>v+"%" } } },
            plugins:{
              legend:{ position:"bottom" },
              tooltip:{ callbacks:{ label: c => `${c.dataset?.label ?? ""}: ${c.parsed?.y ?? "-"}%` } },
              title:{ display:true, text:"분기별 이익률 Top5 (품목)" }
            },
            datasets:{ bar:{ barThickness:22, maxBarThickness:28, borderWidth:0 } }
          }
        });
      }
    }

    if (window.Chart) buildBase(window.Chart);
    else {
      const s = document.createElement("script");
      s.src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js";
      s.onload = () => buildBase(window.Chart);
      document.head.appendChild(s);
    }
  })();

  /* =========================================================
   *  🔁 차트 조회: 년도만 vs 분기 선택(직전 4개 분기) 로직 반영
   * ========================================================= */
  async function updateQuarterTop5ChartWithFetch(){
    const { year, quarter, keyword } = getFilterParams();
    try {
      const qs = new URLSearchParams();
      if (year) qs.append("year", year);
      if (quarter) qs.append("quarter", quarter);
      if (keyword) qs.append("keyword", keyword);
      const res = await fetch(`/api/sales/profit-by-quarter-smart?${qs.toString()}`, { method:"GET" });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const rows = await res.json();
      renderQuarterTop5Chart(rows);
    } catch(e) {
      console.error("[chart] 조회 실패:", e);
    }
  }

  const makeColor = (i, a = 0.85) => `hsla(${(i*63)%360}, 70%, 50%, ${a})`;

  function renderQuarterTop5Chart(raw){
    const data = Array.isArray(raw) ? raw : [];
    if (!data.length || !window.__profitChart) return;

    const rows = data.map(r => ({
      year: Number(r.YEAR ?? r.year),
      q: Number(String(r.QUARTER ?? r.quarter).replace(/[^\d]/g,'')),
      product: r.PRODUCTNAME ?? r.productname ?? r.productName ?? "-",
      rate: Number(r.PROFITRATE ?? r.profitrate ?? r.profitRate ?? 0)
    })).filter(r => r.year && r.q >=1 && r.q <=4);

    const key = r => `${r.year}-${r.q}`;
    const sorted = rows.slice().sort((a,b)=> a.year!==b.year ? a.year-b.year : a.q-b.q);
    const labelsKey = Array.from(new Set(sorted.map(key)));
    const multiYear = new Set(sorted.map(r=>r.year)).size > 1;
    const labelMap = new Map();
    labelsKey.forEach(k=>{
      const [Y, Q] = k.split('-');
      labelMap.set(k, multiYear ? `${String(Y).slice(-2)}년 ${Q}분기` : `${Q}분기`);
    });

    const byProduct = new Map();
    for (const r of rows){
      const e = byProduct.get(r.product) || {sum:0,cnt:0};
      e.sum += r.rate; e.cnt += 1;
      byProduct.set(r.product, e);
    }
    const topProducts = Array.from(byProduct.entries())
      .map(([product,{sum,cnt}]) => ({ product, avg: sum/cnt }))
      .sort((a,b)=> b.avg - a.avg)
      .slice(0,5)
      .map(x=>x.product);

    const cell = new Map();
    for (const r of rows){
      if (!topProducts.includes(r.product)) continue;
      const k = `${r.product}|${r.year}-${r.q}`;
      const e = cell.get(k) || {sum:0,cnt:0};
      e.sum += r.rate; e.cnt += 1;
      cell.set(k,e);
    }

    const datasets = topProducts.map((p, idx)=>{
      const color = makeColor(idx);
      const data = labelsKey.map(LK=>{
        const e = cell.get(`${p}|${LK}`);
        return e ? Math.round((e.sum/e.cnt)*10)/10 : null;
      });
      return {
        label: p,
        data,
        backgroundColor: color,
        borderColor: color,
        borderWidth: 1,
        barPercentage: 0.9,
        categoryPercentage: 0.8
      };
    });

    const ch = window.__profitChart;
    ch.data.labels = labelsKey.map(k => labelMap.get(k));
    ch.data.datasets = datasets;
    ch.update();
  }

  // 초기 차트 로드
  updateQuarterTop5ChartWithFetch();
});
