/*!
* Start Bootstrap - Personal v1.0.1 (https://startbootstrap.com/template-overviews/personal)
* Copyright 2013-2023 Start Bootstrap
* Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-personal/blob/master/LICENSE)
*/
// This file is intentionally blank
// Use this file to add JavaScript to your project

/*function getCode(GroupName, selector) {
	fetch(`/api/modal/commonCode?commonGroup=${GroupName}`)
		.then(res => res.json())
		.then(data => {
			const position = document.querySelector(`#${selector}`);
			for (const option of data) {
				position.insertAdjacentHTML('beforeend', `<option value="${option.codeId}">${option.codeName}</option>`)
			}
		})
}*/

async function getCode(groupName, selectId, selectedValue) {
  const sel = document.getElementById(selectId);
  if (!sel) return;

  // 옵션 초기화
  sel.innerHTML = '<option value="">선택</option>';

  // 목록 조회
  const res = await fetch(`/api/modal/commonCode?commonGroup=${encodeURIComponent(groupName)}`);
  if (!res.ok) throw new Error(await res.text());
  const data = await res.json();

  // 옵션 채우기
  for (const it of data) {
    const opt = document.createElement('option');
    opt.value = String(it.codeId);
    opt.textContent = it.codeName;
    sel.appendChild(opt);
  }

  // 선택 적용
  if (selectedValue != null && selectedValue !== '') {
    sel.value = String(selectedValue);
  }
  // 필요하면 change 이벤트 발생
  sel.dispatchEvent(new Event('change'));
}
