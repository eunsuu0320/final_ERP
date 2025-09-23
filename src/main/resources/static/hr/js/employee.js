const tabled = new Tabulator("#employee-table", {
		layout : "fitColumns",
		pagination : "local",
		paginationSize : 50,
		selectable: true,
		columns : [ {
			  title: "선택",
			  formatter: "rowSelection",  // 체크박스
			  titleFormatter: "rowSelection", // 헤더에도 전체선택 체크박스
			  hozAlign: "center",
			  headerSort: false,
			  width: 44,                 // 기본 폭
			  hozAlign: "center",        // 바디 셀 가로 정렬
			  headerHozAlign: "center",  // 헤더 가로 정렬
			  cellClick: function(e, cell){
			    cell.getRow().toggleSelect();
			  }
			}, {
			title : "사원번호",
			field : "empNo",   // 엔티티 필드명과 매칭
		}, {
			title : "성명",
			field : "name",
		}, {
			title : "부서명",
			field : "dept"
		}, {
			title : "직급",
			field : "grade"
		}, {
			title : "직책",
			field : "position"
		}, {
			title : "전화번호",
			field : "phone"
		}, {
			title : "Email",
			field : "email"
		}, {
			title : "입사일자",
			field : "hireDate",
			sorter : "date",
			hozAlign : "center"
		}, ],
	});

	// 서버에서 전체 사원 조회 (fetch)
	fetch("/selectAllEmp")
		.then(res => res.json())
		.then(data => {
			tabled.setData(data);
		})
		.catch(err => console.error("데이터 불러오기 실패:", err));

	// 검색 버튼
	document.getElementById("btn-search").addEventListener("click", function() {
		const field = document.getElementById("sel-field").value;
		const keyword = document.getElementById("txt-search").value;

		if (keyword.trim() === "") {
			tabled.clearFilter();
		} else {
			tabled.setFilter(field, "like", keyword);
		}
	});

	// 신규 버튼
	document.getElementById("btn-new").addEventListener("click", function() {
 		const modal = new bootstrap.Modal(document.getElementById("empModal"));
  		modal.show();
	});

	// 사원 등록 모달 - 초기화 버튼
	document.getElementById("btn-save-reset").addEventListener("click", function() {
	    const form = document.getElementById("empForm");

	    if (form) {
	        form.reset();  // ✅ form 안 input, select, textarea 값 전부 초기화
	    }

	    // 혹시 readonly 필드나 hidden 값도 같이 지워주고 싶으면 따로 처리
	    document.getElementById("deptCode").value = "";
	    document.getElementById("dept").value = "";
	    document.getElementById("position").value = "";
	    document.getElementById("grade").value = "";
	    document.getElementById("bankCode").value = "";
	});


	// 인쇄 버튼
	document.querySelector(".btn-secondary").addEventListener("click", function() {
	    const selected = tabled.getSelectedData();

	    if (selected.length === 0) {
	        alert("인쇄할 사원을 선택하세요.");
	        return;
	    }

	    // HTML 테이블 문자열로 출력 데이터 만들기
	    let printContents = `
	        <h3>선택된 사원 목록</h3>
	        <table border="1" cellspacing="0" cellpadding="5">
	          <thead>
	            <tr>
	              <th>사원번호</th>
	              <th>성명</th>
	              <th>부서명</th>
	              <th>직급</th>
	              <th>직책</th>
	              <th>전화번호</th>
	              <th>Email</th>
	              <th>입사일자</th>
	            </tr>
	          </thead>
	          <tbody>
	    `;

	    selected.forEach(emp => {
	        printContents += `
	          <tr>
	            <td>${emp.empNo}</td>
	            <td>${emp.name}</td>
	            <td>${emp.dept}</td>
	            <td>${emp.grade}</td>
	            <td>${emp.position}</td>
	            <td>${emp.phone}</td>
	            <td>${emp.email}</td>
	            <td>${emp.hireDate}</td>
	          </tr>
	        `;
	    });

	    printContents += `</tbody></table>`;

	    // 새 창 열고 인쇄 실행
	    const printWindow = window.open("", "_blank");
	    printWindow.document.write(printContents);
	    printWindow.document.close();
	    printWindow.print();
	});
