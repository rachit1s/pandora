
var counter = 0;
function getData(){
	return counter++ + ""; //  new Date().toString();
}

document.addEventListener('DOMContentLoaded', function () {
	var p =  document.getElementById('text') ;
	p.innerHTML = getData();
});
