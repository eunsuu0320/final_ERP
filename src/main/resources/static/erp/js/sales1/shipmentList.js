// ====================================================================
// 전역 헬퍼 함수 정의
// ====================================================================
// 콤마 제거 후 정수만 추출하는 헬퍼 함수 (전역으로 정의하여 모든 함수에서 사용)
window.cleanValue = (val) => parseInt(String(val).replace(/[^0-9]/g, '')) || 0;


document.addEventListener("DOMContentLoaded", function() {
    // 테이블 컬럼을 위한 체크박스의 초기 값.
    const defaultVisible = ["출하지시서코드", "출하예정일자", "거래처명", "창고명", "품목명", "수량합계", "진행상태"];

    // 폼 전체 초기화 (견적서 폼 대신 출하 폼을 초기화)
    window.resetQuote = function() {
        const form = document.getElementById("shipmentForm"); // 출하 폼 ID 사용
        if (form) {
            form.reset();
        }

        // ⭐ 공통함수 window.resetItemGrid 호출 (상세 테이블 초기화 및 입력 행 초기화)
        if (window.resetItemGrid) {
            window.resetItemGrid();
        } else {
            // resetItemGrid가 없을 경우의 대체 로직
            const tbody = document.getElementById('itemDetailBody');
            if (tbody) tbody.innerHTML = '';
            window.addItemRow(); 
        }

        // 합계 초기화 (window.calculateTotal은 아래에 정의됨)
        if (window.calculateTotal) {
             window.calculateTotal();
        }
       
        console.log("출하 모달 전체 초기화 완료.");
    }

    // 이미지모달 (공통 함수 window.showImageModal 사용)
    window.showImageModal = function(url) {
        const modalImg = document.getElementById("modalImg");
        modalImg.src = url;

        const modal = new bootstrap.Modal(document.getElementById('imgModal'));
        modal.show();
    }


    // 출하 상세/등록 모달 
    window.showDetailModal = function(modalType) {
        let modalName = '';
        const modal = new bootstrap.Modal(document.getElementById("newDetailModal"));
        
        if (modalType === 'detail') {
            modalName = '출하 상세정보';
            
        } else if (modalType === 'regist') {
            modalName = '출하 등록';
            window.resetQuote(); // 신규 등록 시 폼 초기화
        }
        
        modal.show();
        document.querySelector("#newDetailModal .modal-title").textContent = modalName;
    };


    // ====================================================================
    // 출하 마스터 + 상세 등록 로직 (saveModal)
    // ====================================================================
    window.saveModal = function() {
        const shipmentForm = document.getElementById("shipmentForm");
        const modalEl = document.getElementById("newDetailModal");

        if (!shipmentForm) {
            alert("저장 오류: 출하 등록 폼을 찾을 수 없습니다.");
            return;
        }
        
        // 1. Shipment 기본 정보 유효성 검사 (공통함수 checkRequired 사용)
        if (!window.checkRequired(shipmentForm)) {
             return;
        }

        // 2. Shipment 상세 정보 수집 (유효성 검사는 collectShipmentDetails에서 처리)
        const detailList = collectShipmentDetails();

        if (detailList.length === 0) {
            alert("출하 상세 항목을 1개 이상 입력해주세요.");
            return;
        }
        
        // 3. 최종 페이로드 구성
        const shipmentData = new FormData(shipmentForm);
        const shipmentDataObject = Object.fromEntries(shipmentData.entries());
        
        const finalPayload = {
            shipmentDate: shipmentDataObject.quoteDate, 
            partnerCode: shipmentDataObject.partnerCode || '',
            partnerName: shipmentDataObject.partnerName,
            warehouse: shipmentDataObject.warehouse || '',
            manager: shipmentDataObject.manager || '',
            postCode: shipmentDataObject.zipcode || '', 
            address: shipmentDataObject.address || '',
            remarks: shipmentDataObject.remarks || '',
            detailList: detailList
        };

        console.log("전송할 최종 출하 데이터:", finalPayload);

        // 4. 서버에 API 호출
        fetch("/api/registShipment", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [document.querySelector('meta[name="_csrf_header"]').content]:
                    document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(finalPayload),
        })
            .then(res => {
                if (!res.ok) {
                    return res.json().then(error => {
                        throw new Error(error.message || `서버 오류 발생: ${res.status}`);
                    });
                }
                return res.json();
            })
            .then(data => {
                console.log("서버 응답 데이터:", data);
                alert("출하 지시가 성공적으로 등록되었습니다. 코드: " + data.id);

                const modalInstance = bootstrap.Modal.getInstance(modalEl);
                if (modalInstance) modalInstance.hide();
                
                // 등록 성공 후, 등록 행을 초기화합니다. (clearInputRowValues 호출)
                const tbody = document.getElementById('itemDetailBody');
                const newRowTemplate = tbody ? tbody.querySelector('tr.new-item-row') : null;
                if (newRowTemplate && window.clearInputRowValues) {
                     window.clearInputRowValues(newRowTemplate);
                     
                     // 초기화 후 합계 재계산
                     if (window.calculateTotal) {
                         window.calculateTotal();
                     }
                }
                
                // 테이블 데이터 리로드 로직 추가 
                // window.priceTableInstance.setData("/api/shipmentList");
            })
            .catch(err => {
                console.error("출하 등록 실패:", err);
                alert(`등록에 실패했습니다. 상세 내용은 콘솔(F12)을 확인하세요. 오류: ${err.message}`)
            });
    };


    // ====================================================================
    // 출하 상세 항목 수집 로직
    // ====================================================================
    /**
     * 출하 상세 테이블의 데이터 행들을 순회하며 ShipmentDetail 엔티티 구조에 맞춰 수집합니다.
     */
    function collectShipmentDetails() {
        const detailList = [];
        const tbody = document.getElementById('itemDetailBody');
        if (!tbody) return detailList;
        
        // cleanValue를 전역에서 가져와 사용
        const cleanValue = window.cleanValue;
        let isRowValid = true;

        // 실제 데이터 행만 순회 (템플릿 행 제외)
        tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {
            
            // 데이터 행에 대해서 유효성 검사 수행 (공통 함수 checkRowRequired 호출)
            const validationResult = window.checkRowRequired(row);
            
            if (!validationResult.isValid) {
                 alert(`[${row.rowIndex}번째 행] ${validationResult.missingFieldName}은(는) 필수 입력 항목입니다.`);
                 isRowValid = false;
                 return;
            }

            // HTML input name 속성에 맞춰서 데이터 구성
            const productCode = row.querySelector('input[name="productCode"]').value || '';
            const quantity = cleanValue(row.querySelector('input[name="quantity"]').value); // cleanValue 사용
            const remarks = row.querySelector('input[name="remarks"]').value || '';
            
            detailList.push({
                productCode: productCode,
                quantity: quantity,
                remarks: remarks,
            });
        });
        
        // 유효성 검사 실패 시 빈 배열 반환
        if (!isRowValid) return [];

        return detailList;
    }


    // ====================================================================
    // 수량 합계 계산 로직 (calculateRow 및 saveModal에서 호출)
    // ====================================================================
    window.calculateTotal = function() {
        let totalQuantity = 0;

        const tbody = document.getElementById('itemDetailBody');
        if (!tbody) return;

        // 모든 데이터 행을 순회하며 합계 계산 (템플릿 행 제외)
        tbody.querySelectorAll('tr:not(.new-item-row)').forEach(row => {
            
            const quantityInput = row.querySelector('input[name="quantity"]');

            if (!quantityInput || !window.cleanValue) return;

            const quantity = window.cleanValue(quantityInput.value); // cleanValue 사용

            totalQuantity += quantity;
        });

        // 총 수량 합계 필드 업데이트 (HTML ID가 'totalQuantity'로 가정)
        const totalQtyElement = document.getElementById('totalQuantity'); 
        if (totalQtyElement) {
            totalQtyElement.textContent = totalQuantity.toLocaleString('ko-KR') + ' 개';
        }
    }


    // ====================================================================
    // 테이블 관련 로직
    // ====================================================================

    // 품목리스트 테이블 컬럼에 대한 정의
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

            let columnDef = {
                title: col,
                field: col,
                visible: defaultVisible.includes(col)
            };

            if (col === "출하지시서코드") {
                columnDef.formatter = function(cell) {
                    const value = cell.getValue();
                    return `<div style="cursor:pointer; color:blue;" onclick="showDetailModal('detail')">${value}</div>`;
                };
            }

            return columnDef;
        }).filter(c => c !== null)
    ];

    // Tabulator 테이블 생성 (공통함수 window.makeTabulator 사용)
    const tableInstance = window.makeTabulator(rows, tabulatorColumns);
    window.priceTableInstance = tableInstance;
});

// ====================================================================
// 전역 스코프 함수 (HTML oninput에서 호출됨)
// ====================================================================

function calculateRow(inputElement) {
    if (window.cleanValue) {
        // cleanValue 헬퍼 함수를 사용하여 콤마 포맷팅 적용
        const cleanValue = window.cleanValue;
        const currentValue = cleanValue(inputElement.value);
        inputElement.value = currentValue.toLocaleString('ko-KR');
        
        // 전체 합계 재계산 (calculateTotal 호출)
        if (window.calculateTotal) { 
             window.calculateTotal();
        }
    }
}