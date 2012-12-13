// JavaScript Document
function optionsList (elemId){
	var list = [];
	var selectEl = document.getElementById (elemId);
	if (selectEl.options.length > 0)	
		for (var i=0; i<selectEl.options.length; i++){
			var temp = selectEl.options[i].value; 
			if ((temp != null) || (temp != "") || (temp != " "))
				list.push (temp);
		}
	return list; 
}	
