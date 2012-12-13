function addUser(textCtrlId, selectCtrlId, elemSet){	
	var textCtrl = document.getElementById (textCtrlId);
	var userLogin = textCtrl.value;
	if (!isExistsInAnyUserList (userLogin, elemSet)){
		if (userLogin != ""){
			var elem = document.createElement('Option');
			elem.value = userLogin;	
			elem.setAttribute('id',textCtrl.value);
			elem.innerHTML = textCtrl.value;				
			var selectCtrl= document.getElementById(selectCtrlId);
			selectCtrl.appendChild(elem);
		}
	}	
	else{
		alert ("User: " + "\"" + userLogin + "\" already exists in include/exclude lists");
	}			
	textCtrl.value = "";
}	// JavaScript Document

function isExistsInAnyUserList (loginName, elemSet){
	if (elemSet>0)
		return (isExists (loginName, "includeUsers"+elemSet) || isExists (loginName, "excludeUsers"+elemSet));
	else 
		return (isExists (loginName, "includeUsers") || isExists (loginName, "excludeUsers"));
}

function isExists (loginName, ctrlId){
	var selectEl = document.getElementById (ctrlId);		
	if (selectEl.options.length > 0){	
		for (var i=0; i < selectEl.options.length; i++){
			if (selectEl.options[i].value == loginName)
				return true;
			else 
				continue;
		} 
	}
	else{
		return false;
	}
}

function removeUser(selectCtrlId){
	var selectCtrl = document.getElementById (selectCtrlId);
	var optionElem = selectCtrl.options[selectCtrl.options.selectedIndex];
	selectCtrl.removeChild (optionElem);
}
