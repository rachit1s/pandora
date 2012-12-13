// True if the browser is IE.
var isIE = true;
var usersSet = false;
var controlName = "";


detectBrowser();

setCookie("ClientOffset", (new Date()).getTimezoneOffset());

/**
 * This method detects the type of the browser and creates an XMLHttpRequest object.
 */


function detectBrowser()
{
    var agent = navigator.userAgent.toLowerCase();
    // 
    // In case of Internet Explorer, agent would contain something like this:
    //      mozilla/4.0 (compatible; msie 6.0; windows nt 5.1; .net clr 1.0.3075; .net clr 1.1.4322; 
    // 
    // In case of Mozilla/firefox, agent would contain something like this:
    //      mozilla/5.0 (windows; u; windows nt 5.1; rv: 1.7.3) gecko/20041001 firefox/0.10.1
    //
    
    // Our XML Http Control depends on the browser. 
    // We would be using 'Microsoft.XMLHTTP' if the browser is Iexplorer.
    // and 'Msxml2.XMLHTTP' in case of Non-Iexplorer browsers.
    
    var index = agent.indexOf("msie");
    if (index < 0)
    {
        controlName = "Msxml2.XMLHTTP";
        isIE = false;
        try
        {
            var objXmlHttp = new XMLHttpRequest();
        }
        catch (e)
        {
            document.writeln("This browser does not support ActiveXControls.&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;");
        }
    }
    else
    {
        controlName = "Microsoft.XMLHTTP";
        isIE = true;
        try
        {
            var objXmlHttp = new ActiveXObject(controlName);
        }
        catch (e)
        {
            document.writeln("This browser does not support ActiveXControls.&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;");
        }
    }
}

function getXMLHttpObject()
{
    var obj;
    if (isIE == true)
    {
        obj = new ActiveXObject(controlName);
    }
    else
    {
        obj = new XMLHttpRequest();
    }
    return obj;
}



/**
 * This method removes the leading and trailing spaces in the given string.
 */
function trim(str)
{
	str = str.replace(/^\s+/, "");
	str = str.replace(/\s+$/, "");
	return str;
}

/**
 * This method removes the whitespaces in the given string.
 */
function replaceWS(str)
{
	str = str.replace(/\s+/g, "");
	return str;
}

/**
 * This method executes an XMLHttpRequest with the given parameters.
 */
function executeHttpRequest(method, url, async)
{
    var response = new String();
    var objXmlHttp = getXMLHttpObject();
    try
    {
        objXmlHttp.open(method, url, false);
        objXmlHttp.send(null);
        
        // Check if the call is successful.
        if (objXmlHttp.status == 200)
        {
            response = objXmlHttp.responseText;
        }
        else
        {
            // alert("The server returned an error status: \n" + objXmlHttp.status + ": " + objXmlHttp.statusText);
        }
    }
    catch (e)
    {
        var errMsg = "";
        for(var i in e)
        {
            errMsg = i + ": " + e[i] + "\n";
        }
        // alert("Could not send request to " + url);
    }
    return response;
}

/**
 * This method returns the current time as a long value.
 */
function getCurrentTime()
{
    return (new Date()).getTime();
}

/*
 * This method returns the relative path.
 */
function getRelativePath(location, target)
{
	if(target.charAt(0) == '/')
	{
		target = target.substring(1);
	}
	var action = YAHOO.transbit.tbits.contextPath + target;
	return action;
}

/**
 * this method validates if the given shortcut name is valid.
 */
function validateSCName(name)
{
    var str = new String(name);
    var match = str.match(/^[a-zA-Z0-9_\-\. ]*$/); // Only alphanumeric, underscore, hyphen, dot and space are allowed.
    if (match == null)
    {
        return false;
    }
    return true;
}

/*
 * This method executes an asynchronous get request to the server.
 *
 * @param url     URL to be requested.
 * @param handler Handler to be invoked when the state of the XML Http Object changes.
 */
function executeAsyncGetRequest(url, handler, paramHash)
{
    var objXmlHttp = getXMLHttpObject();
    try
    {
        objXmlHttp.open("GET", url, true);
        objXmlHttp.onreadystatechange = function (){handler(objXmlHttp, paramHash);};
        objXmlHttp.send(null);
    }
    catch (e)
    {
        throw e;
    }
}

/**
 * This method POSTs a request to the server using XMLHttp.
 */
function xmlHttpPostFormData(url, data)
{
    data = encodeMyUrl(data);
    var response = new String();
    var objXmlHttp = getXMLHttpObject();
    try
    {
        objXmlHttp.open("POST", url, false);
        objXmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");      
        objXmlHttp.send(data);
        
        // Check if the call is successful.
        if (objXmlHttp.status == 200)
        {
            response = objXmlHttp.responseText;
        }
        else
        {
            // alert("The server returned an error status: \n" + objXmlHttp.status + ": " + objXmlHttp.statusText);
        }
    }
    catch (e)
    {
        var errMsg = "";
        for(var i in e)
        {
            errMsg = i + ": " + e[i] + "\n";
        }
        // alert("Error Message: " + errMsg);
    }
    return response;
}

/**
 * This method Asynchronously POSTs a request to the server using XMLHttp.
 */
function xmlHttpAsyncPostFormData(url, data, handler)
{
    var objXmlHttp = getXMLHttpObject();
    try
    {
		objXmlHttp.open("POST", url, true);
		objXmlHttp.onreadystatechange = function (){handler(objXmlHttp);};    
        objXmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");  
        objXmlHttp.send(data); 
    }
    catch (e)
    {
    }
  
}

var draftToolTipId = "";
var draftTimestamp = 0; 
var baToolTipId = "";   
		
function deleteDraft(systemId, requestId, timestamp, draftid, row, aClass)
{
	try 
	{
		var sysPrefix = document.getElementById("sysPrefix").value;
		var url = YAHOO.transbit.tbits.contextPath + "draft/" + sysPrefix;
		
		var data =  "systemId=" + systemId +
					"&requestId=" + requestId +
					"&DtimeStamp=" + timestamp +
					"&draftId=" + draftid +
					"&action=delete";	
								
		xmlHttpPostFormData(url,data);
		
		if (draftToolTipId != null && draftToolTipId != "") domTT_deactivate(draftToolTipId);
	
		if (draftTimestamp != null && document.getElementById("DTimestamp") != null)
		{
			if (draftTimestamp == timestamp)
			{
				draftTimestamp = 0;
				document.getElementById("DTimestamp").value = draftTimestamp;
			}	
		}
		getDrafts();
		updateParent();
	}
	catch(e)
	{
		// do nothing;
	}	
}

function getDrafts()
{	
	try 
	{
	    var sysPrefix = document.getElementById("sysPrefix").value;
	    var location = new String(window.location);
	    var index = location.indexOf("search");
	    var separator = "true";
	    var emptyInfo = "true";
	    if (index > 0)
	    {
	        separator = "false";
	        emptyInfo = "false";
	    }
	    var url = YAHOO.transbit.tbits.contextPath + "draft/" + sysPrefix + 
	              "?action=list" + 
	              "&separator=" + separator + 
	              "&emptyInfo=" + emptyInfo + 
	              "&now=" + getCurrentTime();
	    //executeAsyncGetRequest(url, getDraftsHandler);
	    var output = executeHttpRequest("GET",url,false);
	    var span = document.getElementById("userDraftsInfo");
		if (span != null)
		{
		    span.innerHTML = output;
		}
  	}
	catch(e)
	{
		// alert(e["description"]);
	}			
}	
	
function getDraftsHandler(xmlHttpObject)
{
    if (xmlHttpObject)
    {
        var state = xmlHttpObject.readyState;
	    if (state == 4)
        {
            var span = document.getElementById("userDraftsInfo");
			if (span != null)
			{
			    span.innerHTML = xmlHttpObject.responseText;
			}
        }
    }
}	
	
	
function loadDraft(sysPrefix, systemId, requestId, timestamp, draftid)
{
	try 
	{
	    //var sysPrefix = document.getElementById("sysPrefix").value;
	    //var content = executeHttpRequest("GET", url, "false");
	
	    /*if (content == '')
	    {
	        // alert("Error loading Draft");
	        if (draftToolTipId != null && draftToolTipId != "") domTT_deactivate(draftToolTipId);
	        getDrafts();
	        return;
	    }
	
	    if (content.indexOf("No such") != -1)
	    {
	        alert(content);
	        if (draftToolTipId != null && draftToolTipId != "") domTT_deactivate(draftToolTipId);
	        getDrafts();
	        return;
	    }
    	
   	    var DELIM = "$_D_E_L_I_M_$";
	    var subject = content.split(DELIM)[0];
	    var description = content.split(DELIM)[1];
	    var summary = content.split(DELIM)[2];
	    var category = content.split(DELIM)[3];
	    var status = content.split(DELIM)[4];
	    var severity = content.split(DELIM)[5];
	    var requestType = content.split(DELIM)[6];
	    var subscribers = content.split(DELIM)[7];
	    var cc = content.split(DELIM)[8];
	    var assignees = content.split(DELIM)[9];
	    var parent = content.split(DELIM)[10];
	    var relatedRequests = content.split(DELIM)[11];
	    var confidential = content.split(DELIM)[12];
	    var prefix = content.split(DELIM)[13];
	    
    		
        var location = window.location.href.toLowerCase();
	    var sysPrefix = document.getElementById("sysPrefix").value;*/
	    
	    var redirection = YAHOO.transbit.tbits.contextPath + "add-request/" + sysPrefix + "?drafts=true&DTimestamp=" + timestamp + 
						  "&draftload=true" + "&sys_id=" + systemId + "&request_id=" + requestId + 
	                      "&timeStamp=" + timestamp + "&now=" + getCurrentTime() + "&draftid=" + draftid;
	    var innerForm = "addRequestForm";
    			
	    if (requestId > 0)
	    {
		    redirection = YAHOO.transbit.tbits.contextPath + "add-action/" + sysPrefix + "/" + requestId + "?drafts=true&DTimestamp=" + timestamp + 
						  "&draftload=true" + "&sys_id=" + systemId + "&request_id=" + requestId + 
	                      "&timeStamp=" + timestamp + "&now=" + getCurrentTime() + "&draftid=" + draftid;
		    innerForm = "updateDiv.addRequestForm";
		}
    				
	    var sName =  "_blank";
    	
	    /*if (location.indexOf("/search") > 0 || location.indexOf("/q/") > 0)
		    sName =  "_blank";*/
		var newWindow = window.open(redirection, sName);
	    if (draftToolTipId != null && draftToolTipId != "") domTT_deactivate(draftToolTipId);	
	    getDrafts();
	    return;
	}
	catch(e)
	{
	// do nothing
	}
}
    
function saveDraftHandler(xmlHttpObject)
{
  if (xmlHttpObject)
    {
        var state = xmlHttpObject.readyState;
		if (state == 4)
        {
			// Check if the call is successful.
			if (xmlHttpObject.status == 200)
			{
				var response = xmlHttpObject.responseText;
				getDrafts();
				updateParent();
			}	
        }
    }	
}	 
  
    
function showDrafts(event)
{	
    getDrafts();
	try
	{
	    var content = document.getElementById("drafts").innerHTML;
	     if (content == null)
		    return;
	    // Get the domLib_autoId value and thereby generate the id of the tooltip div.
	    draftToolTipId = domTT_activate(this, event,'caption','Drafts','content', content, 'type','velcro','statusText','Drafts',
    									'closeAction','remove');  
	}
	catch(e)
	{
		// do nothing;
	}										            
}    

function updateParent()
{
	if (window.opener != null)
	{
		try
		{
			window.opener.getDrafts();
		}
		catch (e)
		{
			// Ignore this exception.
		}
	}
}
	
function createObject(name, value)
{
	var obj = document.createElement("input");
	obj.name = name;
	obj.id = name;
	obj.value = value;
	obj.type = "hidden";
	return obj;
}

function toggleCheckBox(chbox)
{
    var ctrl = document.getElementById(chbox);
    if (ctrl != null)
    {
        if (ctrl.checked == true) ctrl.checked = false;
        else ctrl.checked = true;
    }
}

/**
    * The following are the list of arguments expected by this function:
    *
    * @param url       Page to be opened in the dialog. 
    * @param key       Key of the attribute.
    * @param value     value for the above attribute.
    *
    * There can be one or more [key, value] pairs. Key can be
    *  - width
    *  - height
    *  - status
    *  - scroll
    *  - size
    *  - menu
    *  - location
    */
function openModalDialog(url)
{
    var isIE = (document.all) ? true : false;
    var argMap = new Object();
    var child;
    var dialogArg = arguments[1];
	for (var i = 2; i < arguments.length; i += 2)
	{
		if (typeof(arguments[i + 1]) != 'undefined')
		{
			argMap[arguments[i]] = arguments[i + 1];
		}
	}
	
	var features = "";
	if (isIE == true)
	{
	    // Check if width is present.
	    if (argMap["width"])
	    {
	        features = features + " dialogWidth: " + argMap["width"] + "px;";
	    }
	    // Check if height is present.
	    if (argMap["height"])
	    {
	        features = features + " dialogHeight: " + argMap["height"] + "px;";
	    }
	    // Check if status is present.
	    if (argMap["status"])
	    {
	        features = features + " status: " + argMap["status"] + ";";
	    }
	    // Check if scroll is present.
	    if (argMap["scroll"])
	    {
	        features = features + " scroll: " + argMap["scroll"] + ";";
	    }
	    // Check if resize is present.
	    if (argMap["resize"])
	    {
	        features = features + " resizable: " + argMap["resize"] + ";";
	    }
	   
		return window.showModalDialog(url, dialogArg, features);
	    
	}
	else
	{
	    // Check if left is present.
	    if (argMap["left"])
	    {
	        features = features + " left=" + argMap["left"] + "px,";
	    }
	    // Check if top is present.
	    if (argMap["top"])
	    {
	        features = features + " top=" + argMap["top"] + "px,";
	    }
	    // Check if width is present.
	    if (argMap["width"])
	    {
	        features = features + " width=" + argMap["width"] + "px,";
	    }
	    // Check if height is present.
	    if (argMap["height"])
	    {
	        features = features + " height=" + argMap["height"] + "px,";
	    }
	    // Check if status is present.
	    if (argMap["status"])
	    {
	        features = features + " status=" + argMap["status"] + ",";
	    }
	    // Check if scroll is present.
	    if (argMap["scroll"])
	    {
	        features = features + " scrollbars=" + argMap["scroll"] + ",";
	    }
	    // Check if resize is present.
	    if (argMap["resize"])
	    {
	        features = features + " resizable=" + argMap["resize"] + ",";
	    }
	    // Check if menu is present.
	    if (argMap["menu"])
	    {
	        features = features + " menubar=" + argMap["menu"] + ",";
	    }
	    // Check if location is present.
	    if (argMap["location"])
	    {
	        features = features + " location=" + argMap["location"] + ",";
	    }
	    
	    features = features + " modal=yes, dialog=yes, dependent=yes, left=-1, top=-1";
		//alert("features: " + features);
		aChild = window.open(url, "name", features);
	    aChild.opener = window;
       
        }
}





function openUserPicker(userField)
{
    var systemId = document.getElementById("systemId").value;
    var sysPrefix = document.getElementById("sysPrefix").value;

    var redirection = getRelativePath(window.location, "user-picker") + "/" + sysPrefix + "?systemId=" + systemId + "&sysPrefix=" + sysPrefix + "&userTypeId=7" + "&existingUser=" + userField.value + "&date=" + new Date().getTime();	
    if(isIE == true)
    {
        var features = "dialogTop: px; dialogLeft: px;dialogWidth:640px;dialogHeight:500px;resizable:0;status:0;help:0;scroll:0";
        var object = new Object();
        object.userList = userField.value;
        object.disabled = userField.disabled;
        object.returnValue = 2;
        object.userFilter = "7";
        while (object.returnValue == 2)
        {
            redirection = getRelativePath(window.location, "user-picker") + "/" + sysPrefix + "?systemId=" + systemId + "&sysPrefix=" + sysPrefix + "&userTypeId=" + object.userFilter + "&existingUser=" + object.userList + "&date=" + new Date().getTime();
            window.showModalDialog(redirection, object, features);
        }

        if (object.returnValue == false) return;

        userField.value = object.userList;
    }

    else
    {
        var features = "width=640px, height=500px, resizable=0, status=0, help=0, scroll=0";
        window.userList = userField.value;
        window.disabled = userField.disabled;
        window.userField = userField;
        window.returnValue = 2;
        window.userFilter = "7" ;
        redirection = getRelativePath(window.location, "user-picker") + "/" + sysPrefix + "?systemId=" + systemId + "&sysPrefix=" + sysPrefix + "&userTypeId=" + window.userFilter + "&existingUser=" + window.userList + "&date=" + new Date().getTime();
        var newWindow = window.open(redirection, "name" , features);
        newWindow.opener = window;
        window.userField = userField;

        if (window.returnValue == false) return;
        userField.value = window.userList;
    }
}

 /*
 * method used to generate the ba shortcut menu.
 */
 
function showBAList(site, action, heading, e)
{
	//closeToolTip();
	//notInToolTip = undefined;
	var event = (!document.all) ? e : window.event;
	var content = "";
	//if (isIE == false) return content;
	var caption;
	if(heading == 'hyd')
		caption = "HYD Business Areas"; 
	else if(heading == 'nyc')
	    caption = "NYC Business Areas";	
		
	/*if (document.getElementById("BAToolTipDiv").innerHTML != '' )
	{
		content = document.getElementById("BAToolTipDiv").innerHTML;
	}
	else
	{	*/
		if(isIE == true)
		{
		     var url = YAHOO.transbit.tbits.contextPath + "search-util?" +
				       "baList=true" +
				       "&actionPage=" + action +
				       "&now=" + getCurrentTime();
	           content = executeHttpRequest("GET", url, false);
	           document.getElementById("BAToolTipDiv").innerHTML = content;
	           baToolTipId = domTT_activate(this, event,'caption',caption,'content', content, 'type','velcro','statusText','Business Areas',
    								        'closeAction','remove');  
        }
        else
        {
             var sysPrefix = document.getElementById("sysPrefix").value;
             var obj1 = document.createElement("form");
			 obj1.name="dymForm1";
			 obj1.id="dymForm1";
			 obj1.target="hiddenBAIFrame";
			 obj1.action = site + YAHOO.transbit.tbits.contextPath + "search-util/";	
		     obj1.method="GET";
			 obj1.appendChild(createObject("baList", "true"));
			 obj1.appendChild(createObject("actionPage",action)); 
    		 document.body.appendChild(obj1);
    		 dymForm1.submit();
    		 document.body.removeChild(obj1); 
        }
	//} 
	return content;
}
 
 function closeToolTip()
 {
	notInToolTip = true;
	this.tt_Hide();
 }

function closeTarget(e,parent,name)
{		
	notInToolTip = false;
	
	if (!e) var e = window.event;
	var tg = (window.event) ? e.srcElement : e.target;

	var parent1;
	var parent2;

	parent1 = tg.parentNode;		
	if(parent1 == null)
	{
		closeToolTip();
		return;
	}		 
	while(parent1.parentNode != null && parent1.nodeName != parent)
		parent1 = parent1.parentNode;
		
	var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
	if(reltg == null)
	{
		closeToolTip();
		return true;;
	}
	
	parent2 = reltg.parentNode;
	if(parent2 == null)
	{
		closeToolTip();
		return true;;
	}
			
	while(parent2.parentNode != null && parent2.nodeName != parent)
		parent2 = parent2.parentNode;
		
	if (isIE == true)
	{
		if (parent1 != parent2) 
			{	
				closeToolTip();
				return true;
			}	
	}
	else
	{
		var name2 = parent2.attributes.getNamedItem('NAME') ? parent2.attributes.getNamedItem('NAME').nodeValue : "";
		if (name2 != name)
			{
				closeToolTip();
				return true;
			}	
	}
	return true;
}

var notInToolTip;
var toolTipTimerId;

function setMouseOutTimer()
{
	toolTipTimerId = window.setInterval(function(){mouseOutToolTip()}, '1000');
	notInToolTip = true;
}

function mouseOutToolTip()
{
	if (notInToolTip == true)
	{
	closeToolTip();
	window.clearInterval(toolTipTimerId);
	}
	else
	{
	window.clearInterval(toolTipTimerId);
	}
}


/**
 * Gets the value of the specified cookie.
 *
 * name  Name of the desired cookie.
 *
 * Returns a string containing value of specified cookie,
 *   or null if cookie does not exist.
 */
function getCookie(name)
{
    var dc = document.cookie;
    var prefix = name + "=";
    var begin = dc.indexOf("; " + prefix);
    if (begin == -1)
    {
        begin = dc.indexOf(prefix);
        if (begin != 0) return null;
    }
    else
    {
        begin += 2;
    }
    var end = document.cookie.indexOf(";", begin);
    if (end == -1)
    {
        end = dc.length;
    }
    return unescape(dc.substring(begin + prefix.length, end));
}



/**
 * Sets a Cookie with the given name and value.
 *
 * name       Name of the cookie
 * value      Value of the cookie
 * expires    Milliseconds after which it will expire.
 */
function setCookie(name, value, expires)
{
    // By default, the expiry date is set to an year.
    var expireDate = new Date ();
    if (expires)
    {
        try
        {
            Number(expires);
            expireDate.setTime(expireDate.getTime() + expires);
        }
        catch (e)
        {
            expireDate.setTime(expireDate.getTime() + 5000);
        }
    }
    else
    {
        expireDate.setTime(expireDate.getTime() + (365 * 24 * 3600 * 1000));
    }

    document.cookie= name + "=" + escape(value) + "; expires=" + expireDate.toGMTString();
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

function getAbsoluteLeft(ctrl)
{
	var eL = 0;
	for(var p=ctrl; p&&p.tagName!='BODY'; p=p.offsetParent)
	{
		eL+=p.offsetLeft;
	}
	return eL;
}

function userSetup()
{ 	
	if (usersSet == false)
	{
	    var nearestPath = "";
	    var url = "";
	    if (document.getElementById("nearestPath"))
	    {
	        nearestPath = document.getElementById("nearestPath").value;
	        // In case of firefox, get the list from the corresponding site
	        // instead.
	        if (!document.all)
	        {
	            url = YAHOO.transbit.tbits.contextPath + "search-util/";
	        }
	        else
	        {
	            url = nearestPath + "search-util/";
	        }
	    }
	    else
	    {
	        url = YAHOO.transbit.tbits.contextPath + "search-util/";
	    }
		var sysPrefix = document.getElementById("sysPrefix").value;
		url = url + sysPrefix + "?userInfo=true";
        var content = "[" + executeHttpRequest("GET", url, false) + "]";
        var userCollection = eval(content);
        registerDefaultCollection(userCollection);
	    usersSet = true;
    }  
}

function checkSpelling(txt)
{
   	var content = new String(txt.value);
   	if (content == '')
   		{
   		return;
   		}
   		
	 if (content.length > 1400) 
	{
		alert(SPELL_CHECK_ABORTED);
		return false;
	} 
	// give the spellChecker object a reference to our textarea
	// pass any number of text objects as arguments to the constructor:
	var speller = new spellChecker( txt );
	// kick it off
	speller.openChecker();
}

function encodeMyUrl(data)
{
	var rExp = /\+/gi;
	var newString = new String ("%2B");
	return data.replace(rExp, newString);
	//return data.replace("+","%2B");
}

/*
 *  Method to compare two arrays.
 */
 
 function compareTwoArrays( array1, array2)
 {
    var tmp = new Array();
    var key;
    
    if( array1.length != array2.length )
    {
       return false;
    }
    
    for (var i = 0 ; i < array1.length ; i++)
    {
        key = trim(array1[i]);
        if(key != "")
        {
			if(tmp[key] != null)
			{
				tmp[key]++;
			}
			else
			{
				tmp[key] = 1;
			}
        }
    }
    
    for (var i = 0 ; i < array2.length ; i++)
    {
        key = trim(array2[i]);
        if (key != "")
        {
			if(tmp[key])
			{
				if(tmp[key] == 0)
				{
					return false;
				}
				else
				{
					tmp[key] --;
				}
			}
			else
			{
				return false;
			}
        }
    }
    
    return true;
 }

/*

 * This function parses comma-separated name=value argument pairs from

 * the query string of the URL. It stores the name=value pairs in 

 * properties of an object and returns that object.

 */

function getArgs(  ) {

    var args = new Object(  );

    var query = location.search.substring(1);     // Get query string

    var pairs = query.split(",");                 // Break at comma

    for(var i = 0; i < pairs.length; i++) {

        var pos = pairs[i].indexOf('=');          // Look for "name=value"

        if (pos == -1) continue;                  // If not found, skip

        var argname = pairs[i].substring(0,possaveDraft);  // Extract the name

        var value = pairs[i].substring(pos+1);    // Extract the value

        args[argname] = unescape(value);          // Store as a property

        // In JavaScript 1.5, use decodeURIComponent(  ) instead of escape(  )

    }

    return args;                                  // Return the object

}

