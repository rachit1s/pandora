function displayJsonWindowInfo(){
	chrome.runtime.getBackgroundPage(function (bgWindow){
		bgWindow.getJsonWindowInfo(function displayInfo(infoJson){
			document.body.innerHTML = "<pre>" + infoJson + "</pre>";
		});
	}
	 ) ;
}

function addButton(){
	var button = document.createElement("button");
	button.onclick = displayJsonWindowInfo;
	button.innerHTML = "click me";
	document.body.appendChild(button);
}
document.addEventListener("DOMContentLoaded", addButton);
