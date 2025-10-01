/*!
* Start Bootstrap - Personal v1.0.1 (https://startbootstrap.com/template-overviews/personal)
* Copyright 2013-2023 Start Bootstrap
* Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-personal/blob/master/LICENSE)
*/
// This file is intentionally blank
// Use this file to add JavaScript to your project

function getCode(GroupName, selector) {
	fetch(`/api/modal/commonCode?commonGroup=${GroupName}`)
		.then(res => res.json())
		.then(data => {
			const position = document.querySelector(`#${selector}`);
			for (const option of data) {
				position.insertAdjacentHTML('beforeend', `<option value="${option.codeId}">${option.codeName}</opiton>`)
			}
		})
}