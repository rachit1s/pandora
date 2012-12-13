// JScript source code

function onLoadBody()
{
	myOptionForm.focus();
}

function onKeyDownBody()
{
	if (event.keyCode == 13) 
		document.getElementById("submit").click()
}

function onSubmit()
{
	//var select = document.all.sRight;
	//var length = select.options.length;
	var csList = document.getElementById("analystBAList").value;
	var strList = csList.split(',');
	var vacation = "";
	var notify = "";
	for (var i = 0; i < strList.length ;i++)
	{
	  var currentCB = document.getElementById(strList[i] + "_vac");
	  var currentNB = document.getElementById(strList[i] + "_notify");
	  if (currentCB != null)
	   {
		  if (currentCB.checked == true)
		  {
		     vacation = vacation + strList[i] + ",";
		  }
	   }
	   if (currentNB != null)
	   {
		  if (currentNB.checked == true)
		  {
		     notify = notify + strList[i] + ",";
		  }
	   }
	}
	vacation = vacation.replace(/([0-9]*),$/, "$1");
	notify = notify.replace(/([0-9]*),$/, "$1");
	document.getElementById("vacation").value = vacation;
	document.getElementById("notify").value = notify;
	return true;
}

function onRevert()
{
	var result = confirm(REVERT_BACK_TO_DEFAULT_SETTINGS);
	if (result == true)
	{
		document.getElementById("action").value='default';
		//document.getElementById("myOptionForm").action = getRelativePath(window.location,"options")+"/"+document.getElementById("sysPrefix").value;
		//document.getElementById("myOptionForm").submit();
		//document.getElementById("myOptionForm").submit();
		document.getElementById("submit").click()
		//myOptionForm.submit();
		//return true;
	}
	else
	{
		//return false;
	}
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

function viewCatProfile()
{
	var select;
	if (document.getElementById("sLeft").options.length != 0) 
		select = document.all.sLeft;
	else if (document.all.sRight.options.length != 0) 
		select = document.all.sLeft;
	else
		return;
		
	var selIndex = 0;
	if (select.options.selectedIndex < 0) 
		selIndex = 0;
	else 
		selIndex = select.options.selectedIndex;
	var baName = select.options[selIndex].text;
	var index = baName.indexOf("[");
	var sysPrefix = baName.substring(index + 1, baName.lastIndexOf("]"));
	var features = "width=560px, resizable= 0, height= 560px, status= 0, help= 0, scrollbars= yes";
	var redirection = "/analyst-info/" + sysPrefix;
	var child = window.open(redirection + "?date=" + (new Date()), "analystInfo", features, true);
}

function viewCatProfile()
{
	var select;
	var sysPrefix = document.getElementById("sysPrefix").value;
	var features = "width=560px, resizable= 0, height= 570px, status= 0, help= 0, scrollbars= yes";
	var redirection = YAHOO.transbit.tbits.contextPath + "analyst-profile/" + sysPrefix;
	var child = window.open(redirection, "analystProfile", features, true);
}

function getAbsoluteTop(ctrl)
{
	var eT = 0;
	for(var p=ctrl; p&&p.tagName!='BODY'; p=p.offsetParent)
	{
		eT+=p.offsetTop;
	}
	return eT;
}
