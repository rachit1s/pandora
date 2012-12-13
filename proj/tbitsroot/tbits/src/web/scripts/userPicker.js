// JScript source code

var myParent;
var returnValue = 0;
function onLoadBody()
{
	if(isIE == true)
	{
	   myParent = window.dialogArguments;
	}
	else
	{
	   myParent = this.opener;
	}
	document.getElementById("userLogin").focus();
	if (myParent.disabled == true)
	{
		document.getElementById("userLogin").disabled = true;
		document.getElementById("left").disabled = true; 
		document.getElementById("right").disabled = true;
		document.getElementById("btnSubmit").disabled = true;
	}
}

/*
  * Function called when user Presses any key.
  */
function onKeyDownBody(e)
{
	// On <Enter>
    var event = (!document.all) ? e : window.event;
    var keyCode = (!document.all) ? e.which : event.keyCode;
	if(keyCode == 13) 
	{
		onSubmit();
		return false;
	}
	// On <Esc>
	else if (keyCode == 27) 
	{
		onCancel();
		return false;
	}
	else 
	{
		return true;
	}
}

function onCancel()
{
	returnValue = 0;

	  myParent.returnValue = 0;
	  window.close();
}


function onSubmit()
{
	if (document.getElementById("userLogin").value != "")
	{
		var cList = document.getElementById("userList");
		var sList = document.getElementById("selected");
		var index = cList.options.selectedIndex;
		if (index >= 0)
		{
			if (isPresent(sList, cList.options[index].value) == false) 
			{
				var ctr = sList.options.length;
				sList.options.length = ctr + 1;
				sList.options[ctr].value = cList.options[index].value;
				sList.options[ctr].text = cList.options[index].text;
				document.getElementById("userLogin").value = "";
				sort(sList);
			}
			else 
			{
				document.getElementById("userLogin").value = "";
			}
		}
	}
	else 
	{
		onSelect();
	}
	var list = "";
	var sList = document.getElementById("selected");
	if (sList.options.length == 0) 
	{

		   myParent.userList = "";
	}
	else 
	{
		list = sList.options[0].value;
		for(var i = 1;i < sList.options.length;i++)
		{
			list = list + "," + sList.options[i].value;
		}
		myParent.userList = list;
	}
	returnValue = 1;
	myParent.returnValue = 1;
	if(isIE == false)
	{
	   myParent.userField.value = list;
	}
	window.close();
}

/* function onChangeUserLogin()
{
	var value = document.getElementById("userLogin").value;
	var cList = document.getElementById("userList");
	var index = cList.options.selectedIndex;
	if (index >= 0) cList.options[index].selected = false;

	for(var i=0; i< cList.options.length; i++)
	{
		if (value == "") 
		{
			cList.options[i].selected = true;
			cList.options[i].selected = false;
			break;
		}
		var login = new String(cList.options[i].text);
		if(login.toLowerCase().indexOf(value.toLowerCase()) == 0)
		{
			alert("here");
			cList.options[i].selected = true;
			break;
		}
	}
} */

function onKeyDownUserLogin(e)
{
	 var event = (!document.all) ? e : window.event;
     var keyCode = (!document.all) ? e.which : event.keyCode;
	if ((keyCode == 13) && (document.getElementById("userLogin").value != ""))
	{
		var cList = document.getElementById("userList");
		var sList = document.getElementById("selected");
		var index = cList.options.selectedIndex;
		if (index >= 0)
		{
			if (isPresent(sList, cList.options[index].value) == false) 
			{
				var ctr = sList.options.length;
				sList.options.length = ctr + 1;
				sList.options[ctr].value = cList.options[index].value;
				sList.options[ctr].text = cList.options[index].text;
				document.getElementById("userLogin").value = "";
				sort(sList);
			}
			else 
			{
				document.getElementById("userLogin").value = "";
			}
		}
		event.cancelBubble = true;
		return false;
	}
	else
	{
	   	var value = document.getElementById("userLogin").value;
	    var cList = document.getElementById("userList");
	    var index = cList.options.selectedIndex;
	    if (index >= 0) cList.options[index].selected = false;

	    for(var i=0; i< cList.options.length; i++)
	    {
		   if (value == "") 
		   {
			   cList.options[i].selected = true;
			   cList.options[i].selected = false;
			   break;
		   }
		   var login = new String(cList.options[i].text);
		   if(login.toLowerCase().indexOf(value.toLowerCase()) == 0)
		   {
			   cList.options[i].selected = true;
			   break;
		   }
	    } 
	}
	return true;
}

function sort(ctrl)
{
	for(var i=0; i < ctrl.options.length; i++)
	{
		for(var j=0; j < ctrl.options.length; j++)
		{
			if (ctrl.options[i].value < ctrl.options[j].value)
			{
				swap(ctrl, i, j);
			}
		}
	}
}

function swap(ctrl, i, j)
{
	tempValue = ctrl.options[i].value;
	tempText = ctrl.options[i].text;
	tempSelected = ctrl.options[i].selected;
	
	ctrl.options[i].value = ctrl.options[j].value;
	ctrl.options[i].text = ctrl.options[j].text;
	ctrl.options[i].selected = ctrl.options[j].selected;
	
	ctrl.options[j].value = tempValue;
	ctrl.options[j].text = tempText;
	ctrl.options[j].selected = tempSelected;
}


function isPresent(ctrl, value)
{
	for (var i=0;i<ctrl.options.length;i++)
	{
		if(ctrl.options[i].value.toLowerCase() == value.toLowerCase()) return true;
	}
	return false;
}

function onSelect()
{
	if (myParent.disabled == true)
		return;
	var cList = document.getElementById("userList");
	var sList = document.getElementById("selected");
	var index = cList.options.selectedIndex;
	if (index >= 0)
	{
		var ctr = sList.options.length;
		for(var i=0;i<cList.options.length;i++)
		{
			if (cList.options[i].selected ==true)
			{
				if (isPresent(sList, cList.options[i].value)== false)
				{
					sList.options.length = ctr + 1;
					sList.options[ctr].value = cList.options[i].value;
					sList.options[ctr].text = cList.options[i].text;
					ctr = ctr + 1;
				}
			}
		}
		sort(sList);
	}
}

function onDeSelect()
{
	if (myParent.disabled == true)
		return;
	var valueArray = new Array();
	var textArray = new Array();
	var sList = document.getElementById("selected");
	if (sList.options.selectedIndex >= 0)
	{
		var ctr = 0;
		for(var i=0;i<sList.options.length;i++)
		{
			if (sList.options[i].selected == false)
			{
				valueArray[ctr] = sList.options[i].value;
				textArray[ctr] = sList.options[i].text;
				ctr = ctr + 1;
			}
		}
		sList.options.length = 0;
		for (var i = 0; i < ctr; i++)
		{
			sList.options.length = i + 1;
			sList.options[i].value = valueArray[i];
			sList.options[i].text = textArray[i];
		}
		sort(sList);
	}
}

function onChangeUserFilter()
{
	var list = "";
	var sList = document.getElementById("selected");
	if (sList.options.length == 0) 
	{
		myParent.userList = "";
	}
	else 
	{
		list = sList.options[0].value;
		for(var i = 1;i < sList.options.length;i++)
		{
			list = list + "," + sList.options[i].value;
		}
		myParent.userList = list;
	}
	returnValue = 2;
	myParent.returnValue = 2;
	myParent.userFilter = document.getElementById("userFilter").value;
	window.close();
}


function onUnloadBody()
{
	myParent.returnValue = returnValue;
}
