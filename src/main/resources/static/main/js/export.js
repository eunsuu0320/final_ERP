const ExportHelper = {
  exportExcel: function(table, title = "검색 결과") {
    if (!table) {
      alert("테이블이 초기화되지 않았습니다.");
      return;
    }

    const data = table.getData();
    const columns = table.getColumnDefinitions()
      .filter(col => col.field)
      .map(col => col.title);

    const wb = XLSX.utils.book_new();
    const sheetData = [
      [title],
      [],
      columns,
      ...data.map(row => columns.map(c => row[
        table.getColumnDefinitions().find(col => col.title === c).field
      ]))
    ];

    const ws = XLSX.utils.aoa_to_sheet(sheetData);
    ws['!cols'] = columns.map(() => ({ wch: 20 }));

    XLSX.utils.book_append_sheet(wb, ws, "Data");
    XLSX.writeFile(wb, `${title}.xlsx`);
  },

  exportPDF: function(table, title = "검색 결과") {
    if (!window.jspdf || !window.jspdf.jsPDF) {
      alert("jsPDF가 아직 로드되지 않았습니다.");
      return;
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF("landscape");

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
        doc.setFontSize(16);
        doc.text(title, doc.internal.pageSize.getWidth() / 2, 15, { align: "center" });
      }
    });

    doc.save(`${title}.pdf`);
  }
};
