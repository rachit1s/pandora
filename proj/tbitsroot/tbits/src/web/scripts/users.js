// JScript source code

// Get the arguments object passed to this modal dialog from the parent window.
var myParent = window.dialogArguments;
var lastActiveSelect ;
var lastMatchedIndex = 0;
var returnValue = 0;

/*
	* This method is called when the page is loaded. Adds the users if any in the parent, to the 
	* right side <select>
	*/
function init()
{
	if(myParent.invocationField == "subscribers")
		lastActiveSelect = document.all.subscribers;
	else 
		lastActiveSelect = document.all.ccs;
	document.all.login.focus();
	
	if (myParent.subDisabled == true)
	{
		document.all.addSub.disabled = true;
		document.all.remSub.disabled = true;
		document.all.subscribers.disabled = true;
	}
	if (myParent.ccDisabled == true)
	{
		document.all.addCc.disabled = true;
		document.all.remCc.disabled = true;
		document.all.ccs.disabled = true;
	}
	if (myParent.ccDisabled == true && myParent.subDisabled == true)
	{
		document.all.login.disabled = true;
		document.all.btnSubmit.disabled = true;
	}
}

function addIfPresent(user, sLeft, sRight)
{
	
	var lLen = sLeft.length;
	var rLen = sRight.length;
	for(i = 0; i < lLen; i++) {
		var value = new String(sLeft.options[i].value);
		if(value == user) {
			var anOption = document.createElement("OPTION");
			sRight.options.add(anOption);
			anOption.text = sLeft.options[i].text;
			anOption.value= sLeft.options[i].value;
			break;
		}
	}
}

function callRemove(select)
{
	if (myParent.ccDisabled == true && myParent.subDisabled == true)
	 return;
	 if (myParent.ccDisabled == true && lastActiveSelect.name == 'ccs')
	 return;
	 if (myParent.subDisabled == true && lastActiveSelect.name == 'subscribers')
	 return;
	var values = new Array();
	var text = new Array();
	var lLen = select.length;
	var ctr = 0;
	for(var i=0; i<lLen;i++)
	{
		if(select.options[i].selected == true) {
			
			select.options[i].text = "";
			select.options[i].value= "";
		}
		else {
			values[ctr] = select.options[i].value;
			text[ctr] = select.options[i].text;
			ctr++;
		}
	}
	for(var i=0;i<ctr;i++) {
		select.options[i].text = text[i];
		select.options[i].value = values[i];
		select.options[i].selected = false;
	}
	select.length = ctr;
}

function submitAndClose(subscribers, ccs)
{
	var len = subscribers.length;
	var strSubscribers = "";
	for(i = 0; i < len; i++)
		strSubscribers = strSubscribers + subscribers.options[i].value + ",";
	strSubscribers = strSubscribers.substr(0, strSubscribers.length - 1);
	myParent.subscribers = strSubscribers;

	var len = ccs.length;
	var strccs = "";
	for(i = 0; i < len; i++)
		strccs = strccs + ccs.options[i].value + ",";
	strccs = strccs.substr(0, strccs.length - 1);
	myParent.cc = strccs;
	
	returnValue = 1;
	myParent.returnValue = 1;
	window.close();
}

function toRight(sLeft, sRight)
{
	var lLen = sLeft.length;
	var rLen = sRight.length;

	//Check if any data is present in the text field.
	var login = document.all.login.value;
	
	if(login != "") {
		// Check if this is the one selected in the SLeft <Select>
		if (login == sLeft.value) {
	
			//check if it is already present on the right side.
			for (i = 0; i< rLen;i++) 
			{
				var value = sRight.options[i].value;
				if(value == login) {
					document.all.login.value = "";
					return;
				}
			}
		
			for(i=0; i<lLen;i++)
			{
				if(sLeft.options[i].value == login) {
					var anOption = document.createElement("OPTION");
					sRight.options.add(anOption);
					anOption.text = sLeft.options[i].text;
					anOption.value= sLeft.options[i].value;
					document.all.login.value = "";
					return;
				}
			}
		}
		else {
			document.all.login.value = "";
		}
	}
		
	for(i=0; i<lLen;i++)
	{
		if(sLeft.options[i].selected) {
			//Check if it is present in the right select.
			var assignee = sLeft.options[i].value;
			var insert = 1;
			for(j = 0;j < rLen; j++)
			{
				var value = sRight.options[j].value;
				if(value == assignee) 
				{
					insert = 0;
				}
			}
			if(insert != 0)
			{
				var anOption = document.createElement("OPTION");
				sRight.options.add(anOption);
				anOption.text = sLeft.options[i].text;
				anOption.value= sLeft.options[i].value;
			}
		}
	}
	setMeAsActive(sRight);
}

function sort(select)
{
	var len = select.length;
	for(i=0;i<len;i++) {
		for(j=0;j<len;j++) {
			if(select.options[i].text < select.options[j].text) {
				var tempText  = select.options[i].text;
				var tempValue = select.options[i].value;
				select.options[i].text = select.options[j].text;
				select.options[i].value= select.options[j].value; 
				select.options[j].text = tempText;
				select.options[j].value = tempValue;
			}
		}
	}
}

function match(select, login)
{
	var str = new String(login.value);
	str = str.toLowerCase();
	var len = select.length;
	if(lastMatchedIndex != 0)
		select.options[lastMatchedIndex].selected = false;
	for(i = 0; i < len; i++)
	{
		var value = new String(select.options[i].text);
		value = value.toLowerCase();
		select.options[i].selected = false;
		if(value.indexOf(str) == 0) 
		{
			select.options[i].selected = true;
			lastMatchedIndex = i;
			return;
		}
	}
}

function trim(str)
{
	var result = str;
	while (result.charAt(0) == ' ')
		result = result.substr(1, result.length - 1);
	while (result.charAt(result.length - 1) == ' ')
		result = result.substr(0, result.length - 1);
	return result;
}

function onKeyUp()
{
	if (event.keyCode == 13)submitAndClose(subscribers, ccs);
	if (event.keyCode == 27)
	{
		returnValue = 0;
		myParent.returnValue = 0;
		window.close();
	}
}

function setMeAsActive(select)
{
	lastActiveSelect = select;
}

function moveToActiveSelect()
{
	if (myParent.ccDisabled == true && myParent.subDisabled == true)
	 return;
	 if (myParent.ccDisabled == true && lastActiveSelect.name == 'ccs')
	 return;
	 if (myParent.subDisabled == true && lastActiveSelect.name == 'subscribers')
	 return;
	document.all.login.value = "";
	toRight(sLeft, lastActiveSelect);
}

function onKeyUpLogin()
{
	if ( event.keyCode == 27 ) 
	{
		returnValue = 0;
		myParent.returnValue = 0;
		window.close(); 
	}
	else if (event.keyCode == 13)
	{
		if (document.all.login.value == "")
		{
			submitAndClose(document.all.subscribers, document.all.ccs);
			return;
		}
		if (
			(lastActiveSelect == document.all.ccs && document.all.addCc.disabled == false) ||
			(lastActiveSelect == document.all.subscribers && document.all.addSub.disabled == false)
		   )
			toRight(document.all.sLeft, lastActiveSelect);
		event.cancelBubble = true;
	}
	else 
		match(document.all.sLeft, document.all.login);
}

function onChangeUserFilter()
{
	var len = subscribers.length;
	var strSubscribers = "";
	for(i = 0; i < len; i++)
		strSubscribers = strSubscribers + subscribers.options[i].value + ",";
	strSubscribers = strSubscribers.substr(0, strSubscribers.length - 1);
	myParent.subscribers = strSubscribers;

	var len = ccs.length;
	var strccs = "";
	for(i = 0; i < len; i++)
		strccs = strccs + ccs.options[i].value + ",";
	strccs = strccs.substr(0, strccs.length - 1);
	myParent.cc = strccs;
	returnValue = 2;
	myParent.returnValue = 2;
	myParent.userFilter = document.all.userFilter.value;
	window.close();
}

function onUnloadBody()
{
	myParent.returnValue = returnValue;
}
