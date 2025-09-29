document.addEventListener("DOMContentLoaded", function() {
    // 테이블 컬럼을 위한 체크박스의 초기 값.
    const defaultVisible = ["거래처코드", "거래처명", "거래처유형", "전화번호", "이메일", "비고"];

    // 💡 [변경] AG Grid 인스턴스 및 옵션을 저장할 변수
    window.paymentGridOptions = {};
    window.paymentGridApi = null;

    // =========================================================
    // 탭 전환 기능 초기화 함수
    // =========================================================
    function initTabSwitching() {
        const tabButtons = document.querySelectorAll('#partnerTab button');

        const tabContents = {
            PARTNER: document.getElementById('tab-partner'),
            LOANPRICE: document.getElementById('tab-loanprice'),
            PAYMENT: document.getElementById('tab-payment')
        };

        tabButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const type = this.dataset.type;

                // 1. 모든 콘텐츠 숨기기
                Object.values(tabContents).forEach(div => {
                    if (div) div.classList.add('d-none');
                });

                // 2. 모든 버튼 비활성화 (스타일 초기화)
                tabButtons.forEach(b => {
                    b.classList.remove('btn-primary');
                    b.classList.add('btn-outline-primary');
                });

                // 3. 선택된 콘텐츠 보여주기
                if (tabContents[type]) {
                    tabContents[type].classList.remove('d-none');

                    // 💡 [변경] PAYMENT 탭이 선택될 때 AG Grid 초기화
                    if (type === 'PAYMENT' && !window.paymentGridApi) {
                        initPaymentGrid();
                    }
                }

                // 4. 버튼 활성화 상태 표시
                this.classList.remove('btn-outline-primary');
                this.classList.add('btn-primary');
            });
        });

        // 모달이 열릴 때 기본 탭(PARTNER)을 활성화합니다.
        const defaultButton = document.querySelector('#partnerTab button[data-type="PARTNER"]');
        if (defaultButton) {
            defaultButton.classList.remove('btn-outline-primary');
            defaultButton.classList.add('btn-primary');

            if (tabContents.PARTNER) tabContents.PARTNER.classList.remove('d-none');
            if (tabContents.LOANPRICE) tabContents.LOANPRICE.classList.add('d-none');
            if (tabContents.PAYMENT) tabContents.PAYMENT.classList.add('d-none');
        }
    }

    // =========================================================
    // 주소 API 레이어
    // =========================================================
    window.execDaumPostcode = function() {
        new daum.Postcode({
            oncomplete: function(data) {
                document.getElementById("zipcode").value = data.zonecode;
                document.getElementById("address").value = data.roadAddress || data.jibunAddress;
            }
        }).open();
    }

    // =========================================================
    // 거래처 상세/등록 모달
    // =========================================================
    window.showDetailModal = function(modalType, keyword) {
        const modalName = modalType === 'detail' ? '거래처 상세정보' : '거래처 등록';
        const modalEl = document.getElementById("newDetailModal");
        const modal = new bootstrap.Modal(modalEl);
        const form = document.getElementById("partnerForm");

        document.querySelector("#newDetailModal .modal-title").textContent = modalName;

        form.reset();

        initTabSwitching();

        // 💡 [변경] AG Grid 초기화 (모달 열릴 때)
        initPaymentGrid();

        const commonCodePromises = [
            loadCommonCode('BUSINESSTYPE', 'businessType', '업태'),
            loadCommonCode('INDUSTRY', 'industry', '업종'),
            loadCommonCode('EMAILDOMAIN', 'emailDomain', '이메일도메인')
        ];

        modal.show();

        if (modalType === 'detail' && keyword) {
            Promise.all(commonCodePromises)
                .then(() => {
                    console.log("모든 공통 코드(Choices.js) 로드 완료. 상세 데이터 로드 시작.");
                    loadDetailData('partner', keyword, form);

                    // 💡 상세 조회 시 결제정보 데이터 로드
                    loadPaymentData(keyword);
                })
                .catch(err => {
                    console.error("공통 코드 로딩 중 치명적인 오류 발생:", err);
                    alert("필수 데이터 로딩 중 오류가 발생했습니다. 관리자에게 문의하세요.");
                });
        } else {
            // 등록 모드일 때 입력 행 초기화
            resetPaymentGrid();
        }
    };

    // =========================================================
    // 모달 저장 버튼 이벤트
    // =========================================================
    window.saveModal = function() {
        // ... (기존 저장 로직 유지) ...
        // [참고] 저장 로직 내에서 결제정보 탭의 데이터도 함께 저장하는 로직이 필요합니다.
        // if (window.paymentGridApi) { savePayment(); }
    }


    // =========================================================
    // 거래처 목록 Tabulator 초기화 (기존 로직 유지)
    // =========================================================
    // ... (거래처 목록 Tabulator 컬럼 정의 및 인스턴스 생성 로직 유지) ...
    let tabulatorColumns = [
        {
            formatter: "rowSelection",
            titleFormatter: "rowSelection",
            hozAlign: "center",
            headerHozAlign: "center",
            headerSort: false,
            width: 50,
            frozen: true
        },
        ...columns.map(col => {
            if (col === "상품규격" || col === "단위") return null;

            let columnDef = {
                title: col,
                field: col,
                visible: defaultVisible.includes(col)
            };

            if (col === "이미지") { 
                columnDef.formatter = function(cell) {
                    const url = cell.getValue();
                    return `<img src="${url}" alt="이미지" style="height:30px; cursor:pointer;" onclick="showImageModal('${url}')">`;
                };
            }
            
            if (col === "거래처코드") { 
                columnDef.formatter = function(cell) {
                    const value = cell.getValue();
                    return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail', '${value}')">${value}</div>`; 
                };
            }
            return columnDef;
        }).filter(c => c !== null)
    ];

    const tableInstance = makeTabulator(rows, tabulatorColumns);
    window.priceTableInstance = tableInstance;


    // =========================================================
    // 💡 [AG GRID] 결제정보 AG Grid 초기화
    // =========================================================
    const getPaymentColumnDefs = () => {
        return [
            // 체크박스 컬럼 (선택 삭제를 위해)
            { 
                headerName: "", 
                checkboxSelection: true, 
                headerCheckboxSelection: true, 
                width: 50, 
                suppressMovable: true,
                // Pinned Row에는 체크박스 비활성화 (선택 대상이 아님)
                headerCheckboxSelectionFilteredOnly: true, 
            },
            // 모든 목록 필드는 Pinned Row에서 입력 가능하도록 'editable: true' 설정
            { headerName: "은행코드", field: "bankCode", editable: true, width: 100 },
            { headerName: "은행", field: "bankName", editable: true, minWidth: 150 },
            { headerName: "계좌번호", field: "accountNumber", editable: true, minWidth: 150 },
            { headerName: "예금주명", field: "accountHolder", editable: true, minWidth: 100 },
            { 
                headerName: "기본여부", 
                field: "isDefault", 
                editable: true,
                width: 80, 
                cellEditor: 'agSelectCellEditor',
                cellEditorParams: { values: ["Y", "N"] },
                valueFormatter: params => {
                    const map = { "Y": "기본", "N": "선택" };
                    return map[params.value] || params.value;
                }
            },
            { 
                headerName: "사용구분", 
                field: "usageType", 
                editable: true,
                width: 100,
                cellEditor: 'agSelectCellEditor',
                cellEditorParams: { values: ["", "Y", "N"] },
                valueFormatter: params => {
                    const map = { "": "선택", "Y": "사용", "N": "미사용" };
                    return map[params.value] || params.value;
                }
            },
        ];
    };

    function initPaymentGrid() {
        // 💡 [변경] Tabulator 컨테이너 대신 AG Grid 컨테이너를 사용한다고 가정
        const gridDiv = document.getElementById("paymentTabulator"); 
        
        // 이미 인스턴스가 존재하면 파괴합니다.
        if (window.paymentGridApi) {
            window.paymentGridApi.destroy();
        }

        window.paymentGridOptions = {
            columnDefs: getPaymentColumnDefs(),
            rowData: [], // 목록 데이터
            
            // 💡 핵심: Pinned Top Row를 빈 객체로 설정하여 입력 행을 만듭니다.
            pinnedTopRowData: [{}], 
            
            domLayout: 'normal',
            height: '300px', // 높이 설정
            pagination: true, // 페이지네이션 활성화
            paginationPageSize: 5, // 페이지당 5개 행
            paginationPageSizeSelector: [5, 10, 20],
            rowSelection: 'multiple', // 행 다중 선택 허용
            animateRows: true,
            
            // Pinned Row 스타일 (입력 행임을 명확히 표시)
            getRowStyle: params => {
                if (params.node.rowPinned === 'top') {
                    return { 'font-weight': 'bold', 'background-color': '#f0f0f0' };
                }
            }
        };

        // AG Grid 인스턴스 생성
        window.paymentGridApi = agGrid.createGrid(gridDiv, window.paymentGridOptions);
    }

    // =========================================================
    // 💡 [AG GRID] 상세 조회 시 결제정보 데이터 로드
    // =========================================================
    function loadPaymentData(partnerCode) {
        if (!window.paymentGridApi) return;
        
        // API 호출을 통해 목록 데이터를 가져옵니다.
        const dummyRowData = [
            { bankCode: "004", bankName: "KB국민은행", accountNumber: "123-45-67890", accountHolder: "홍길동", isDefault: "Y", usageType: "Y" },
            { bankCode: "020", bankName: "우리은행", accountNumber: "111-22-33333", accountHolder: "홍길동", isDefault: "N", usageType: "N" },
        ];
        
        // RowData(목록) 설정 (Pinned Top Row는 영향을 받지 않습니다)
        window.paymentGridApi.setGridOption('rowData', dummyRowData);
    }

    // =========================================================
    // 💡 [AG GRID] 저장 기능: 등록 행과 목록 데이터를 분리하여 처리
    // =========================================================
    window.savePayment = function() {
        if (!window.paymentGridApi) return;
        
        // 1. Pinned Top Row (입력 행) 데이터 가져오기
        const pinnedRows = window.paymentGridApi.getPinnedTopRowData();
        const newPaymentData = pinnedRows && pinnedRows.length > 0 ? pinnedRows[0] : null;
        
        // 2. 일반 Row (목록) 데이터 가져오기
        const listPaymentData = [];
        window.paymentGridApi.forEachNode(node => {
            if (!node.rowPinned) { // Pinned Row가 아닌 경우만 목록에 추가
                listPaymentData.push(node.data);
            }
        });

        // 3. 등록 데이터 유효성 검사 및 처리
        if (newPaymentData && newPaymentData.bankCode && newPaymentData.bankCode.trim() !== '') {
            console.log("등록할 새 결제정보:", newPaymentData);
            // 서버 등록 API 호출 로직...
            resetPaymentGrid(); // 등록 성공 후 입력 행 초기화
        }
        
        // 4. 목록 데이터 처리 (수정된 데이터 포함)
        console.log("수정/기존 결제정보 목록:", listPaymentData);
        // 서버 목록 저장/수정 API 호출 로직...

        alert("결제정보 저장을 시도합니다. (데이터 콘솔 확인)");
    }

    // =========================================================
    // 💡 [AG GRID] 입력 초기화 기능
    // =========================================================
    window.resetPaymentGrid = function() {
        if (window.paymentGridApi) {
            // Pinned Row의 입력 데이터를 빈 객체로 초기화합니다.
            window.paymentGridApi.setGridOption('pinnedTopRowData', [{}]);
            
            // 목록의 선택 해제
            window.paymentGridApi.deselectAll();
        }
    }

    // =========================================================
    // 💡 [AG GRID] 선택 삭제 기능
    // =========================================================
    window.deleteSelectedPaymentRows = function() {
        if (!window.paymentGridApi) return;
        
        const selectedNodes = window.paymentGridApi.getSelectedNodes();
        // Pinned Row 데이터는 자동으로 제외되지만, 안전을 위해 필터링
        const selectedData = selectedNodes
            .filter(node => !node.rowPinned)
            .map(node => node.data);
        
        if (selectedData.length === 0) {
            alert("삭제할 행을 선택해주세요.");
            return;
        }
        
        // 트랜잭션을 사용하여 삭제를 요청합니다.
        window.paymentGridApi.applyTransaction({ remove: selectedData });
        
        console.log("삭제 요청 데이터:", selectedData);
        // 서버에 삭제 API 호출 로직 추가
    }
});