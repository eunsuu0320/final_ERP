// modal.js (Tabulator 적용)

// === 공통 설정 ===
const modalConfigs = {
	productDetail: {
		url: "/api/modal/productDetail",
		title: "품목 상세",
	}
};

let table; // Tabulator 인스턴스 전역 변수

// === 모달 열기 함수 ===
// === 모달 열기 함수 ===
function openModal(type, onSelect) {
	const config = modalConfigs[type];
	if (!config) {
		console.error(`modalConfigs[${type}] 설정이 없습니다.`);
		return;
	}

	// 제목 설정
	document.getElementById("ModalLabel").textContent = config.title;

	// Tabulator 초기화
	if (table) {
		table.destroy();
	}

	table = new Tabulator("#ModalTable", {
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

					// onSelect 콜백에 데이터 넘기기
					if (onSelect) {
						onSelect(selected);
					}

					bootstrap.Modal.getInstance(document.getElementById("commonModal")).hide();
				}
			}
		],
		ajaxResponse: function(url, params, response) {
			return response;
		}
	});

	const modal = new bootstrap.Modal(document.getElementById("commonModal"));
	modal.show();
}

