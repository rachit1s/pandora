// JScript source code
    
var SAVING_OPTIONS_UNSUCCESSFUL = "Your option settings could not saved because of a database error.";

// Request ID field should be present in the Search Results Display header.
var REQUEST_ID_MANDATORY_IN_DISPLAY_HEADER = "The Request ID. field is mandatory and cannot be removed.";

// Confirmation whether user really wants to revert to default settings.
var REVERT_BACK_TO_DEFAULT_SETTINGS = "Reverting to default settings will clear your custom settings on this page. Continue?";

var obj = window.dialogArguments;

function onLoadBody()
{
	sort(document.getElementById("resultHeader"));
	var isIE = (document.all) ? true : false;
	if(isIE == false)
	{
	   mytimer = setTimeout('fcsOnMe()', 50);
	}
}

function fcsOnMe()
{
 if (!skipcycle)
  {
     window.focus();
  }
  mytimer = setTimeout('fcsOnMe()', 50);
}

function onKeyDownBody(e)
{
    var event = (document.all) ? window.event : e;
    var keyCode = (document.all) ? event.keyCode : e.which;
	if (keyCode == 13)
	{
		onSubmit();
		return false;
	}
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

function onSubmit()
{
	var systemId = document.getElementById("systemId").value;
	var resultHdr = new String();
	var displayHdr = new String();
	var sortColumn = new String();
	var sortOrder = 1;
	var dateFormat = new String();
	var sysPrefix = new String();
	var enableVE = new String();
	// var myRequestsFilter = new String();
    
	var ctrl = document.getElementById("resultHeader");
	var length = ctrl.options.length;
	if (length != 0) resultHdr = ctrl.options[0].value;
	for (var i = 1; i< length; i++)
	{
		resultHdr = resultHdr + "," + ctrl.options[i].value;
	}
    
	ctrl = document.getElementById("displayHeader");
	length = ctrl.options.length;
	if (length != 0) displayHdr = ctrl.options[0].value;
	for (var i = 1; i< length; i++)
	{
		displayHdr = displayHdr + "," + ctrl.options[i].value;
	}
    
	sortColumn = document.getElementById("sortColumn").value;
	sortOrder = document.getElementById("sortOrder").value;
	dateFormat = document.getElementById("dateFormat").value;
	sysPrefix = document.getElementById("sysPrefix").value;

	// myRequestsFilter = document.getElementById("myRequestsFilter").value;
	enableVE = document.getElementById("enableVE").checked;
	if(enableVE == true) enableVE = "true";
	else                 enableVE = "";
       
	var url = getRelativePath(window.location, "/configure-search/") + sysPrefix + "?systemId=" + systemId + 
	          "&resultHeader=" + resultHdr + "&displayHeader=" + displayHdr + "&sortColumn=" + sortColumn + 
		      "&sortOrder=" + sortOrder + "&dateFormat=" + dateFormat +"&sysPrefix=" + sysPrefix + 
		      "&enableVE=" + enableVE + "&now=" + getCurrentTime();
        
	var output = executeHttpRequest("POST", url, false);
    
	if(output.indexOf("true") >= 0)
	{
		if (isIE == true)
		{
		    if (obj != null) obj.returnValue = true;
		}
		else
		{
		    this.opener.onSaveConfigureHandler();
		}
		window.close();
	}
	else
	{
		alert(SAVING_OPTIONS_UNSUCCESSFUL);
		if (obj != null) obj.returnValue = false;
		return;
	}
}

function onCancel()
{
	// Action Cancelled
	window.close();
    if (obj != null) obj.returnValue = false;
}

function onAdd()
{
	transfer(document.getElementById("resultHeader"), document.getElementById("displayHeader"));
	updateSortColumn();
	sort(document.getElementById("resultHeader"));
}

function onRemove()
{
	transfer(document.getElementById("displayHeader"), document.getElementById("resultHeader"));
	updateSortColumn();
	sort(document.getElementById("resultHeader"));
}

function transfer(left, right)
{
	var lenRight = right.options.length;
	var lenLeft = left.options.length;
    
	var finalValueArray = new Array();
	var finalTextArray = new Array();
	var ctr = 0;
	for(var i=0;i<lenLeft;i++)
	{
		// An unselected item remains in the list,
		// whereas a selected item moves to the end of other list.
		if (left.options[i].selected == false)
		{
			finalValueArray[ctr] = left.options[i].value;
			finalTextArray[ctr] = left.options[i].text;
			ctr++;
		}
		else
		{
			if(left.options[i].value == "request_id")
			{
				alert(REQUEST_ID_MANDATORY_IN_DISPLAY_HEADER.replace("<RequestId>", left.options[i].text));
				finalValueArray[ctr] = left.options[i].value;
				finalTextArray[ctr] = left.options[i].text;
				ctr++;
				continue;
			}
			right.options.length = lenRight + 1;
			right.options[lenRight].value = left.options[i].value;
			right.options[lenRight].text = left.options[i].text;
			lenRight++;
		}
	}
    
	left.options.length = 0;
	lenLeft = 0;
	for (var i = 0;i<ctr;i++)
	{
		left.options.length = lenLeft + 1;
		left.options[i].value = finalValueArray[i];
		left.options[i].text = finalTextArray[i];
		lenLeft++;
	}
	return;
}

function updateSortColumn()
{
	var dctrl = document.getElementById("displayHeader");
	var sctrl = document.getElementById("sortColumn");
    
	var sortValue = document.getElementById("sortColumn").value;
    
	var len = dctrl.options.length;
	sctrl.options.length = 0;
	for(var i = 0; i < len; i++)
	{
		sctrl.options.length = i + 1;
		sctrl.options[i].value = dctrl.options[i].value;
		sctrl.options[i].text = dctrl.options[i].text;
	}
    
	sctrl.value = sortValue;
	if (sctrl.value == "")
		sctrl.value = "request_id";
	return;
}

function onUp()
{
	var ctrl = document.getElementById("displayHeader");
	var len = ctrl.options.length;
    
	var tempValue, tempText, tempSelected;
    
	for(var i = len-1; i > 0; i--) 
	{
		if (ctrl.options[i].selected == true)
		{
			var startSelection = i;
			while (i >= 0 && ctrl.options[i].selected == true ) 
			{
				i--;
			}
			if(i < 0) break;
			var endSelection = i;
			for (var j=i;j < startSelection && j < len;j++)
			{
				swap(ctrl, j, j+1);
			}
		}
	}
}

function onDown()
{
	var ctrl = document.getElementById("displayHeader");
	var len = ctrl.options.length;
    
	var tempValue, tempText, tempSelected;
    
	for(var i = 0; i < len - 1; i++) 
	{
		if (ctrl.options[i].selected == true)
		{
			var startSelection = i;
			while (i < len && ctrl.options[i].selected == true) 
			{
				i++;
			}
			if(i == len) break;
			var endSelection = i;
			for (var j=i;j>startSelection && j > 0;j--)
			{
				swap(ctrl, j, j-1);
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


function onRevert()
{
	var result = confirm(REVERT_BACK_TO_DEFAULT_SETTINGS);
	if (result == false)
	{
		return false;
	}

	var systemId = document.getElementById("systemId").value;
	var sysPrefix = document.getElementById("sysPrefix").value;
	document.getElementById("action").value = "default";
	var url = getRelativePath(window.location, "/configure-search/") + sysPrefix + 
	    "?systemId=" + systemId +
		"&action=default"+"&sysPrefix=" + sysPrefix ;
    
    var output = executeHttpRequest("POST", url, false);
    
	if(output.indexOf("true") >= 0)
	{
		// Action Success.
		window.close();
		if (obj != null) obj.returnValue = true;
	}
	else
	{
		alert(SAVING_OPTIONS_UNSUCCESSFUL);
		if (obj != null) obj.returnValue = false;
		return;
	}
}
