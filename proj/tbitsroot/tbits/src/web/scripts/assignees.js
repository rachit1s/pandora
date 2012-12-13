// JScript source code
// Get the arguments object passed to this modal dialog from the parent window.
var myParent;
var isIE = (document.all) ? true : false;
if(isIE == true)
{ 
   myParent = window.dialogArguments;
}
else
{
   myParent = this.opener;
}
var lastMatchedIndex = 0;
var returnValue = 0;
myParent.returnValue = 0;

/*
	* This method is called when the page is loaded. Adds the assignees if any in the parent, to the 
	* right side <select> and removes them from the left side <select>
	*/
function init(sLeft, sRight)
{
	document.getElementById("login").focus();
	if (myParent.disabled == true) 
	{
		document.getElementById("left").disabled = true;
		document.getElementById("right").disabled = true;
		document.getElementById("login").disabled = true;
		document.getElementById("btnAssign").disabled = true;
	}
}

function addIfPresent(assignee, sLeft, sRight)
{
	var lLen = sLeft.length;
	var rLen = sRight.length;
	for(i = 0; i < lLen; i++) 
	{
		var value = new String(sLeft.options[i].value);
		if(value == assignee) {
			var anOption = document.createElement("OPTION");
			sRight.options.add(anOption);
			anOption.text = sLeft.options[i].text;
			anOption.value= sLeft.options[i].value;
			return value;
		}
	}
	
	return assignee;
}

// This method gets a comma separated list of items in the right <select>.
function assign(sRight)
{
	var len = sRight.length;
	var strAssignees = "";
	for(i = 0; i < len; i++)
		strAssignees = strAssignees + sRight.options[i].value + ",";
	
	// Remove the trailing comma.
	strAssignees = strAssignees.substr(0, strAssignees.length - 1);
	
	// assign the value to the object which will be returned back to the parent.
	myParent.assignee = strAssignees;
	if(isIE == false)
	{
	   myParent.assigneeFldName.value = strAssignees;
	}
	
	// Close this popup.
	returnValue = 1;
	myParent.returnValue = 1;
	window.close();
}

function onClickCancel()
{
	returnValue = 0;
	myParent.returnValue = 0;
	window.close();
}

function match(select, login)
{
	var str = new String(login.value);
	str = str.toLowerCase();
	var len = select.length;
	if(lastMatchedIndex >= 0)
	{
		select.options[lastMatchedIndex].selected = false;
	}
	if (str == "") return;
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

function toRight(sLeft, sRight)
{
	if (myParent.disabled == true) return false;
	var lLen = sLeft.length;
	var rLen = sRight.length;

	var insert = 1;
	var selectionCount = 0;
	var nonInsertCount = 0;
	for(i=0; i<lLen;i++)
	{
		if(sLeft.options[i].selected) 
		{
			selectionCount++;
			//Check if it is present in the right select.
			var assignee = sLeft.options[i].value;
			insert = 1;
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
			else
			{
				nonInsertCount++;
			}
		}
	}
	if (insert == 0 && selectionCount == nonInsertCount) return false;
	sort(sRight);
	return true;
}

function removeFromList(select)
{
	if (myParent.disabled == true) return;
	var values = new Array();
	var text = new Array();
	var lLen = select.length;
	var ctr = 0;
	for(var i=0; i<lLen;i++)
	{
		if(select.options[i].selected) {
			select.options[i].text = "";
			select.options[i].value= "";
			select.options[i].selected = false;
		}
		else {
			values[ctr] = select.options[i].value;
			text[ctr] = select.options[i].text;
			ctr++;
		}
	}
	//select.length = 0;
	for(var i=0;i<ctr;i++) {
		select.options[i].text = text[i];
		select.options[i].value = values[i];
		select.options[i].selected = false;
	}
	select.length = ctr;
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

function trim(str)
{
	var result = str;
	while (result.charAt(0) == ' ')
		result = result.substr(1, result.length - 1);
	while (result.charAt(result.length - 1) == ' ')
		result = result.substr(0, result.length - 1);
	return result;
}

function onKeyDown()
{
	if (event.keyCode == 27)
	{
		returnValue = 0;
		myParent.returnValue = 0;
		window.close();
		return true;
	}
	if (event.keyCode == 13)
	{
		if (document.getElementById("login").value == "") 
		{
			assign(document.getElementById("sRight"));
		}
		return true;
	}
}

function onKeyDownLeft()
{
	if (event.keyCode == 13)
	{
		event.cancelBubble = true;
		if(document.getElementById("sLeft").value == "")
		{
			assign(document.getElementById("sRight"));
		}
		else
		{
			var result = toRight(document.getElementById("sLeft"), document.getElementById("sRight"));
			if (result == false)
			{
				assign(document.getElementById("sRight"));
			}
		}
		return false;
	}
}

function onKeyUpLogin(e)
{
	// On <Enter>
    var event = (!document.all) ? e : window.event;
    var keyCode = (!document.all) ? e.which : event.keyCode;
    
	if ( event.keyCode == 27 ) 
	{
		returnValue = 0;
		myParent.returnValue = 0;
		window.close(); 
	}
	else if ( event.keyCode == 13 )
	{
		if (document.getElementById("login").value == "") 
		{
			assign(document.getElementById("sRight"));
			return;
		}
		var result = toRight(document.getElementById("sLeft"), document.getElementById("sRight"));
		document.getElementById("login").value = "";
		document.getElementById("login").focus();
	}
	else 
	{
		match(document.getElementById("sLeft"), document.getElementById("login"))
	}
}

function onChangeUserFilter()
{
	var len = document.getElementById("sRight").length;
	var strAssignees = "";
	for(i = 0; i < len; i++)
		strAssignees = strAssignees + document.getElementById("sRight").options[i].value + ",";
	
	// Remove the trailing comma.
	strAssignees = strAssignees.substr(0, strAssignees.length - 1);
	
	// assign the value to the object which will be returned back to the parent.
	myParent.assignee = strAssignees;
	
	returnValue = 2;
	myParent.returnValue = 2;
	myParent.userFilter = document.getElementById("userFilter").value;
	window.close();
}

function onUnloadBody()
{
	myParent.returnValue = returnValue;
}
