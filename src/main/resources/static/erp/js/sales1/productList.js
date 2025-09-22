/*!
    * Start Bootstrap - SB Admin v7.0.7 (https://startbootstrap.com/template/sb-admin)
    * Copyright 2013-2023 Start Bootstrap
    * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-sb-admin/blob/master/LICENSE)
    */
    // 
// Scripts
// 

document.addEventListener("DOMContentLoaded", function() {
    const checkboxes = document.querySelectorAll(".colCheckbox");
    const table = document.getElementById("productTable");

    checkboxes.forEach(cb => {
        cb.addEventListener("change", function() {
            const colName = cb.value.trim(); // 체크박스 value
            const ths = table.querySelectorAll("thead th");

            ths.forEach((th, index) => {
                if (th.textContent.trim() === colName) {
                    // 헤더 표시/숨김
                    th.style.display = cb.checked ? "" : "none";

                    // 해당 컬럼의 모든 td 표시/숨김
                    table.querySelectorAll("tbody tr").forEach(tr => {
                        const tds = tr.querySelectorAll("td");
                        if(tds[index]) {
                            tds[index].style.display = cb.checked ? "" : "none";
                        }
                    });
                }
            });
        });
    });
});



