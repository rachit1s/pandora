
function getData(){
	var bgwindow = chrome.extension.getBackgroundPage(); //counter++ + ""; //  new Date().toString();
	var data = bgwindow.getData();
	console.log("data to be printed : " + data);
	return data;
}

document.addEventListener('DOMContentLoaded', function () {
	var p =  document.getElementById('text') ;
	p.innerHTML = getData();
});
