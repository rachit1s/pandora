 // Enums to different views.
//var HOME_VIEW       = 0;
//var SIMPLE_VIEW     = 1;
var NORMAL_VIEW     = 0;
var ADVANCED_VIEW   = 1;
var ALLAREAS_VIEW   = 2;
var MYREQUESTS_VIEW = 3;
var MYREPORTS_VIEW = 4;
var DASHBOARD_VIEW= 5;
var NEW_REQUEST_TAB = 6;
// Various cases on request_id field.
var REQID_QUERY             = -2;
var REQID_EMPTY             = -1;
var REQID_HANDLED           =  0;
var INVALID_PREFIX          =  1;
var INCORRECT_PREFIX_FORMAT =  2;
var NONEXISTING_REQUEST_ID  =  3;
var INVALID_REQUEST_ID      =  4;
// Variables related to time calculations.
var start = 0;
var end = 0;
// Variables related to timer to be set up for auto refresh.
var timerId = -1;               // Id of the timer that refreshes the search page periodically.
var interval = 1000 * 60 * 5;   // Default interval.

// Message to be displayed when the user clicks on refresh button in My Requests page without selecting any checkboxes.
var FILTER_MANDATORY = "You must check one or more of Logger, Assignee, Subscriber and Primary Assignee.\nThis specifies your relationship to the requests displayed.";

var INCORRECT_DATE_FORMAT = "The date specified should be in mm/dd/yyyy format.";
// Messages to be displayed during Request# field handling.
var reqMessages = new Array();
reqMessages[REQID_HANDLED]              = "";
reqMessages[INVALID_PREFIX]             = "The business area prefix does not exist."
reqMessages[INCORRECT_PREFIX_FORMAT]    = "Business area prefixes cannot be specified when searching for a range of requests. \nSelect the business area and then enter the range.";
reqMessages[NONEXISTING_REQUEST_ID]     = "The request number does not exist."
reqMessages[INVALID_REQUEST_ID]         = "The request number entered is invalid.\nSpecify a value that is greater than zero, is not a fraction and has no spaces.";

// Group actions related messages.
var SELECT_REQUEST_FOR_GROUP_ACTION = "No requests have been selected.";
var GROUP_ACTION_FAILURE_REASON_BELOW = "The selected requests could not be marked as '<action>' because\n<message>";
var GROUP_ACTION_SUCCESSFUL = "The selected requests have been successfully marked as '<action>'.";
var CONFIRM_BULK_UPDATE_ALL = "You are about to update <count> requests. This action CANNOT BE UNDONE.\nDo you want to continue?";

var TT_REMOVE = "Select the 'Show in My Requests page' checkbox in 'Status Filter...' dialog to get this back.";

var READ_UNREAD_HELP = "To enable the selection of only read or unread requests, use Configure Search Results to track read and unread requests.";

// Array to hold the results in various views.
var viewResultsTable = new Array();
//viewResultsTable[SIMPLE_VIEW]       = "";
viewResultsTable[NORMAL_VIEW]       = "";
viewResultsTable[ADVANCED_VIEW]     = "";
viewResultsTable[ALLAREAS_VIEW]     = "";
viewResultsTable[MYREQUESTS_VIEW]   = "";
//viewResultsTable[HOME_VIEW]         = "";

//*********************************************************************
// Exception class used for throwing exceptions.
//*********************************************************************
function TBitsException(description)
{
    this.type = "TBits Application Exception";
    this.description = description;
}
//*********************************************************************
// Data used by advanced search panel.
//*********************************************************************
var dataTypeInfo = 
{
    "1": 
    {
        "dataTypeName":"boolean",
        "allowedClauseList": "true,false"
    },
    "2":
    {
        "dataTypeName":"date",
        "allowedClauseList": "on,bef,aft,bet,today,yesterday,tomorrow,lw,lm,ly,nw,nm,ny,lnh,lnd,lnw,lnm,lny,nnh,nnd,nnw,nnm,nny,iem,inem"
    },
    "3":
    {
        "dataTypeName":"time",
        "allowedClauseList": "on,bef,aft,bet,today,yesterday,tomorrow,lw,lm,ly,nw,nm,ny,lnh,lnd,lnw,lnm,lny,nnh,nnd,nnw,nnm,nny,iem,inem"
    },
    "4":
    {
        "dataTypeName":"datetime",
        "allowedClauseList": "on,bef,aft,bet,today,yesterday,tomorrow,lw,lm,ly,nw,nm,ny,lnh,lnd,lnw,lnm,lny,nnh,nnd,nnw,nnm,nny,iem,inem"
    },
    "5":
    {
        "dataTypeName":"integer",
        "allowedClauseList": "eq,ne,gt,lt,ge,le,bet,in,ni"
    },
    "6":
    {
        "dataTypeName":"real",
        "allowedClauseList": "eq,ne,gt,lt,ge,le,bet,in,ni"
    },
    "7":
    {
        "dataTypeName":"varchar",
        "allowedClauseList": "ep,alw,ntw,anw"
    },
    "8":
    {
        "dataTypeName":"text",
        "allowedClauseList": "ep,alw,ntw,anw"
    },
    "9":
    {
        "dataTypeName":"type",
        "allowedClauseList": "in,ni"
    },
    "10":
    {
        "dataTypeName":"multi-value",
        "allowedClauseList": "in,ni,iem,inem,inmem,nimem"
    },
    "11":
    {
        "dataTypeName":"attachments",
        "allowedClauseList": "ep,alw,ntw,anw"
    }
};

var clauseInfo = 
{
	"aft"       : "After",
	"alw"       : "All the words",
	"anw"       : "Any of the words",
	"bef"       : "Before",
	"bet"       : "Between",
	"ep"        : "Exact phrase",
	"eq"        : "Equals",
	"false"     : "False",
	"ge"        : "Greater than or equal to",
	"gt"        : "Greater than",
	"in"        : "including",
	"inmem"     : "including members of",
	"iem"       : "is empty",
	"inem"      : "is not empty",
	"le"        : "Less than or equal to",
	"lm"        : "Last month",
	"lnd"       : "Last 'n' days",
	"lnh"       : "Last 'n' hours",
	"lnm"       : "Last 'n' months",
	"lnw"       : "Last 'n' weeks",
	"lny"       : "Last 'n' years",
	"lt"        : "Less than",
	"lw"        : "Last week",
	"ly"        : "Last year",
	"ne"        : "Not equal to",
	"ni"        : "excluding",
	"nimem"     : "excluding members of",
	"nm"        : "Next month",
	"nnd"       : "Next 'n' days",
	"nnh"       : "Next 'n' hours",
	"nnm"       : "Next 'n' months",
	"nnw"       : "Next 'n' weeks",
	"nny"       : "Next 'n' years",
	"ntw"       : "None of the words",
	"nw"        : "Next week",
	"ny"        : "Next year",
	"on"        : "On",
	"today"     : "Today",
	"tomorrow"	: "Tomorrow",
	"true"      : "True",
	"yesterday" : "Yesterday"	
};

var dateClauses = 
{
    "after"     : "aft",
    "before"    : "bef",
    "on"        : "on",
    "between"   : "bet",
    "today"     : "today",
    "yesterday" : "yesterday",
    "tomorrow"	: "tomorrow",
    "last"      : "last",
    "next"      : "next"
}

/*
 * Method called when the user tries to change the view.
 */
function onChangeView(aView)
{
	// Get the current view.
    var currentView = getIntValue("sv");
    
    // Return if the user clicked on the view he is currently viewing.
    if (currentView == aView) return;
    
    // Hold the search results in the current view.
    viewResultsTable[currentView] = getInnerHtml("searchResultsHolder");
    setInnerHtml("timeSplit", "");
    setValue("sv", aView);

    // Notify the server about the latest view seen by this user.
    var sysPrefix = getValue("sysPrefix");
    var url = YAHOO.transbit.tbits.contextPath + "search-renderer/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&sv=" + aView + "&now=" + getCurrentTime();
    executeAsyncGetRequest(url, function() {});
    // Show the configureLine.
    showElement("configureLine");
     /*
     * Set the className of the selected view's tab to sClass and show it.
     * Set the className of the other tabs to uClass and hide them.
     */
    var uClass = "sx l stus";
    var sClass = "sx b cw sts";
    
    switch(aView)
    {
//        case HOME_VIEW:
//           document.getElementById("homeTitle").className = sClass;        showElement("homeBlock");
//            document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//            document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//            document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = uClass;  hideElement("myRequestsBlock");
//            break;
//        case SIMPLE_VIEW:
//            document.getElementById("simpleTitle").className = sClass;      showElement("simpleBlock");
//            document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//            document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = uClass;  hideElement("myRequestsBlock");
////            document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
//            document.getElementById("simpleQuery").focus();
//            break;
        case NORMAL_VIEW:
//            document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//            document.getElementById("normalTitle").className = sClass;      showElement("normalBlock");
//            document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = uClass;  hideElement("myRequestsBlock");
////            document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
            document.getElementById("normalDesc").focus();
            break;
        case ADVANCED_VIEW:
            loadAdvancedView();
//            document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//            document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//            document.getElementById("advancedTitle").className = sClass;    showElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = uClass;  hideElement("myRequestsBlock");
////            document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
            document.getElementById("fieldList").focus();
            break;
        case ALLAREAS_VIEW:
            loadAllAreasView();
        
//            document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//            document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//            document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = sClass;    showElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = uClass;  hideElement("myRequestsBlock");
 //           document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
            document.getElementById("aa_Desc").focus();
            break;
        case MYREQUESTS_VIEW:
//            document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//            document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//            document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//            document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//            document.getElementById("myRequestsTitle").className = sClass;  showElement("myRequestsBlock");
  //          document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
            document.getElementById("refreshMyRequests").focus();
            getResults("", "", "", "", "Loading");
            break;
       
        case DASHBOARD_VIEW:
//          document.getElementById("simpleTitle").className = uClass;      hideElement("simpleBlock");
//          document.getElementById("normalTitle").className = uClass;      hideElement("normalBlock");
//          document.getElementById("advancedTitle").className = uClass;    hideElement("advancedBlock");
//          document.getElementById("allAreasTitle").className = uClass;    hideElement("allAreasBlock");
//          document.getElementById("myRequestsTitle").className = sClass;  showElement("myRequestsBlock");
//          document.getElementById("homeTitle").className = uClass;        hideElement("homeBlock");
			break;
    }
    // Restore the search results in this view saved before, if any.
    if ((aView != MYREQUESTS_VIEW) && (aView != DASHBOARD_VIEW))   
    	setInnerHtml("searchResultsHolder", viewResultsTable[aView]);
}

function loadAllAreasView()
{
    var sysPrefix = getValue("sysPrefix");
    var allAreasLoaded = getValue("allAreasLoaded");
    if (allAreasLoaded != "true")
    {
        var url = YAHOO.transbit.tbits.contextPath + "search-renderer/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&sv=allareas&now=" + getCurrentTime();
        var output = executeHttpRequest("GET", url, true);
        setInnerHtml("allAreasBlock", output);
        setValue("allAreasLoaded", "true");
        loadDateFields("aa_");
    }
}

//*********************************************************************
// Request Id Field related methods.
//*********************************************************************
function checkRequestId()
{
    var sysPrefix = new String(getValue("sysPrefix"));
    var requestId = new String(removeWS(getValue("requestId")));
	var actionId = "";
    
    if (requestId == "") return REQID_EMPTY;

	// Check if the Request# contains a single ID.
	if(requestId.match(/^[0-9]+$/) != null)
	{
		requestId = Number(requestId);

		// For the sake of QA, report the case of request_id being 0 as an error :).
		if (requestId == 0)	return INVALID_REQUEST_ID;
		
		// Validate the request in this BA.
		return validateAndOpenRequest(sysPrefix, requestId, actionId)
	}
	// Check if the request contains prefix#Id 
	else if (requestId.indexOf("#") > 0)
	{
	    var reqRegex = /^[a-zA-Z0-9_\-;\}\{\[\]\,\|]+#[0-9]+$/;
	    var actRegex = /^[a-zA-Z0-9_\-;\}\{\[\]\,\|]+#[0-9]+#[0-9]+$/;
	    if (requestId.match(reqRegex) == null && requestId.match(actRegex) == null) return INVALID_REQUEST_ID;

		var prefix = "", reqId = "", actId = "";
		var arr = requestId.split("#");
		prefix = arr[0];
		reqId  = arr[1];
		if (arr.length > 2) actId = arr[2];
		
		// Validate the request in this BA.
		return validateAndOpenRequest(prefix, reqId, actId)
	}
    
    // Replace all semicolons with commas.
	requestId = requestId.replace(/;/ig, ",");
	
	// Check for the normal case of comma separated ids or with ranges
	var regex = /^([1-9][0-9]{0,10}|([1-9][0-9]{0,10}-[1-9][0-9]{0,10}))([,;]([1-9][0-9]{0,10}|([1-9][0-9]{0,10}-[1-9][0-9]{0,10})))*$/;
	
	if (requestId.match(regex)) 
	    return REQID_QUERY;
	else
	    return NONEXISTING_REQUEST_ID;

	return REQID_EMPTY;
}

/*
 * This method validates if the given request exists. If so, the request will be opened in a new window.
 */
function validateAndOpenRequest(sysPrefix, requestId, actionId)
{
    // Form a smart tag and check if the request is a valid one.
	var smartTag = sysPrefix + "#" + requestId;
	var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?prefixId=" + escape(smartTag) + "&now=" + getCurrentTime();
	var output = executeHttpRequest("GET", url, false);
	output = trim(output);
	
	if (output == "")                           return INVALID_PREFIX;
	if (output.indexOf("no_ba") >= 0)           return INVALID_PREFIX;
	if (output.indexOf("no_requestId") >= 0)    return NONEXISTING_REQUEST_ID;

	var arr = output.split("\n");
	var sysId = arr[0];
	var reqId = arr[1];
	var prefix = arr[2];
	
	url = YAHOO.transbit.tbits.contextPath + "Q/" + prefix + "/" + reqId;
    // Append the action id if present.
	if (actionId != "") url = url + "#" + actionId;

	quickOpen(prefix, reqId, url);
    return REQID_HANDLED;
}

/*
 * This method returns the request id query.
 */
function getRequestIdQuery()
{
    var requestId = getValue("requestId");
    requestId = requestId.replace(/;/ig, ",");
    
    var arr = requestId.split(",");
    var len = arr.length;
    var query = "";
    var first = true;
    var desc = getPrimaryDesc("request_id");
    for(var i=0; i < len; i++)
    {
        var str = trim(arr[i]);
        if (str == "") continue;
        if (first == false) query = query + " OR ";
        else first = false;
        query = query + desc + ":" + str;
    }
    return query;
}

/*
 * method used to generate the shortcut menu for onmouseover of request column.
 */
function stt(sysPrefix, id, rowId, track, read)
{
	closeToolTip();
	notInToolTip = undefined;
    var requestId = id;
    var rrLink = "onr(\"" + sysPrefix + "\", " + requestId + ", \"" + rowId + "\");";
    var urLink = "window.open(\"" + YAHOO.transbit.tbits.contextPath + "Q/" + sysPrefix + "/" + requestId + "?u=1\")";
    var srLink = "window.open(\"" + YAHOO.transbit.tbits.contextPath + "add-subrequest/" + sysPrefix + "/" + requestId + "\")";
    var bgMouseOver = "darkgray";
    var html = 
        "<TABLE name='tt' cellpadding='0' cellspacing='0' class='shortcuts' " + 
        "onmouseover='notInToolTip = false;' onmouseout='closeTarget(event,\"TABLE\", \"tt\");' " + 
        "style='width: 100%;'>" + 
        "    <colgroup>" + 
        "        <col width='150px' />" + 
        "    </colgroup>" + 
        "    <THEAD><TR><TD class=\"title\">" + sysPrefix + "#" + requestId + "</TD></TR></THEAD>" + 
        "    <TBODY>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + rrLink + "'>" + //View Request" + 
        "            </TD>" + 
        "        </TR>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + urLink + "'>" + //Update Request" + 
        "            </TD>" + 
        "        </TR>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + srLink + "'>" + //Add Subrequest" + 
        "            </TD>" + 
        "        </TR>" + 
        "    </TBODY>" + 
        "</TABLE>" + 
        "";
    return html;
}

function stt(sysPrefix, id, rowId, track, read, viewReqStr, updateReqStr, addSubReqStr)
{
	closeToolTip();
	notInToolTip = undefined;
    var requestId = id;
    var rrLink = "onr(\"" + sysPrefix + "\", " + requestId + ", \"" + rowId + "\");";
    var urLink = "window.open(\"" + YAHOO.transbit.tbits.contextPath + "Q/" + sysPrefix + "/" + requestId + "?u=1\")";
    var srLink = "window.open(\"" + YAHOO.transbit.tbits.contextPath + "add-subrequest/" + sysPrefix + "/" + requestId + "\")";
    var bgMouseOver = "darkgray";
    var html = 
        "<TABLE name='tt' cellpadding='0' cellspacing='0' class='shortcuts' " + 
        "onmouseover='notInToolTip = false;' onmouseout='closeTarget(event,\"TABLE\", \"tt\");' " + 
        "style='width: 100%;'>" + 
        "    <colgroup>" + 
        "        <col width='150px' />" + 
        "    </colgroup>" + 
        "    <THEAD><TR><TD class=\"title\">" + sysPrefix + "#" + requestId + "</TD></TR></THEAD>" + 
        "    <TBODY>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + rrLink + "'>" + viewReqStr + //View Request" + 
        "            </TD>" + 
        "        </TR>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + urLink + "'>" + updateReqStr + //Update Request" + 
        "            </TD>" + 
        "        </TR>" + 
        "        <TR>" + 
        "            <TD class='l' " + 
        "                onmouseover='this.style.backgroundColor=\"" + bgMouseOver + "\";' " + 
        "                onmouseout='this.style.backgroundColor=\"transparent\";closeTarget(event,\"TABLE\", \"tt\");'" + 
        "                onclick='" + srLink + "'>" + addSubReqStr + //Add Subrequest" + 
        "            </TD>" + 
        "        </TR>" + 
        "    </TBODY>" + 
        "</TABLE>" + 
        "";
    return html;
}

//*********************************************************************
// Methods to open navigation links.
//*********************************************************************
/*
 * Method to open options page.
 */
function openOptions()
{
    var location = YAHOO.transbit.tbits.contextPath + "options/" + getValue("sysPrefix");
    var features = "status=no,location=no,menubar=no,toolbar=no,resizable=yes,scrollbars=yes,width=750px,height=760px";
    var child = window.open(location, "OptionsPage", features);
    return;
}

/*
 * Method to open Admin page.
 */
function openAdmin()
{
    var location = YAHOO.transbit.tbits.contextPath + "web/admin-frame.html#?q=" + getValue("sysPrefix");
    var features = "status=no,location=no,menubar=no,toolbar=no,resizable=yes,scrollbars=yes,left=0,top=0,width="+screen.width+",height="+screen.height;
    window.open(location, "_blank", features);
}

//*********************************************************************
// Event Handlers.
//*********************************************************************
/*
 * Event handler for onload event of body.
 */
function onBodyLoad()
{
    // Enable the read/unread options based on isVEEnabled value.
    checkReadUnread();
    setInnerHtml("expSpan", "Collapse All&nbsp; | ");
    window.focus();	
//    var currentView = getIntValue("sv");
//    switch(currentView)
//    {
//        case SIMPLE_VIEW:
//            document.getElementById("simpleQuery").focus();
//            break;
//        case NORMAL_VIEW:
//            document.getElementById("normalDesc").focus();
//            break;
//        case ADVANCED_VIEW:
//            document.getElementById("fieldList").focus();
//            break;
//        case ALLAREAS_VIEW:
//            document.getElementById("aa_Desc").focus();
//            break;
//        case MYREQUESTS_VIEW:
//            document.getElementById("refreshMyRequests").focus();
//            break;
//    }
    scheduleRefresher();
    
    processDateDefaults("log_");
    processDateDefaults("upd_");
    processDateDefaults("due_");
    loadDateFields("");
    
    var allAreasLoaded = getValue("allAreasLoaded");
    if (allAreasLoaded == "true")
    {
        loadDateFields("aa_");
    }

    var nearestPath = getValue("nearestPath");
    var sysPrefix = getValue("sysPrefix");
    var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?userInfo=true";
    var content = "[" + executeHttpRequest("GET", url, false) + "]";
    var collection = eval(content);
    registerSmartInputListeners();
    registerDefaultCollection(collection);

    // Now load the advanced view also.
    loadAdvancedView();

}

/*
 * Event handler for onunload event of body.
 */
function onBodyUnload()
{
    cancelRefresher();
    getInfoDash();
}

/*
 * Event handler for onchange event of Business Area dropdown.
 */
function onChangeBusinessArea()
{
    storeValuesInCookies();
	window.location = YAHOO.transbit.tbits.contextPath + "search" + "/" + getValue("sys");
}

/*
 * Event handler for onClick event of Business Area menuButton.
 */
function onClickBusinessArea(sPrefix)
{
    storeValuesInCookies();
	window.location = YAHOO.transbit.tbits.contextPath + "search" + "/" + sPrefix;
}


/*
 * Event handler for onkeydown event of form.
 */
function onKeyDownForm(e)
{
    var view = getValue("sv");
    var event = (document.all) ?  window.event : e;
    var keyCode = (document.all) ?  event.keyCode : event.which;
    if (keyCode == 13 && view != ADVANCED_VIEW)
    {
        startSearching("Searching");
    }
}

function onKeyDownBody(e)
{
    var event = (document.all) ?  window.event : e;
    var keyCode = (document.all) ?  event.keyCode : event.which;
    var ctrlKey = (event.ctrlKey || event.ctrlLeft);
    var altKey = (event.altKey || event.altLeft);
    var KEY_1 = 49,
        KEY_2 = 50,
        KEY_3 = 51,
        KEY_4 = 52,
        KEY_5 = 53;
    if (ctrlKey == true)
    {
//        if (keyCode == KEY_1)
//            onChangeView(SIMPLE_VIEW);
//        else 
        if (keyCode == KEY_2)
            onChangeView(NORMAL_VIEW);
        else if (keyCode == KEY_3)
            onChangeView(ADVANCED_VIEW);
        else if (keyCode == KEY_4)
            onChangeView(ALLAREAS_VIEW);
        else if (keyCode == KEY_5)
            onChangeView(MYREQUESTS_VIEW);
    }
}

function onKeyDownAdvQuery(e)
{
	var event = (document.all) ? window.event : e;
	var keyCode = (document.all) ? event.keyCode  : e.which;
	if (keyCode == 13) 
	{
	    startSearching("Searching");
	    return false;
	}
	return true;
}

/*
 * Event handler for onclick event of "More..." span element.
 */
function onClickMore(rowId)
{
    var ctrl = document.getElementById(rowId);
    if (ctrl != null)
    {
        var display = ctrl.style.display;
        display = display.toLowerCase();
        if (display != "none")
            hideTableRow(ctrl);
        else
            showTableRow(ctrl);
    }
}

/*
 * Method to initiate the process of searching.
 */
function startSearching(word)
{
    // Check the state of listAll.
    var listAll = "";
    var ctrl = document.getElementById("listAll");
    if (ctrl != null && ctrl.checked == true) listAll = "true";
    else listAll = "";
    
    if(!word) word = 'Searching';

    getResults('', '', '', listAll, word);
}
//*********************************************************************
// Methods that executes search query.
//*********************************************************************

/*
 * This method obtains the DQL query and sends an asynchronous request to the server.
 */
function getResults(sortQuery, renderType, pageNumber, listAll, word)
{
    // Clear the timesplit span.
    setInnerHtml("timeSplit", "");
    
    // Clear the shortcutName field.
    setValue("shortcutName", "");

    // Note the starting time.
    start = getCurrentTime();

    // Get the current view of the search page.
    var currentView = getIntValue("sv");
    
    // Get the SysPrefix.
    var sysPrefix = getValue("sysPrefix");
    
    // This will hold the URL.
    var url = "";

    // Check if anything is present in the request id field.    
    var reqIdValue = checkRequestId();
    switch(reqIdValue)
    {
        case REQID_HANDLED:
            setValue("requestId", "");
            return;
        case INVALID_PREFIX:
            alert(reqMessages[INVALID_PREFIX]);
            return;
        case INCORRECT_PREFIX_FORMAT:
            alert(reqMessages[INCORRECT_PREFIX_FORMAT]);
            return;
        case NONEXISTING_REQUEST_ID:
            alert(reqMessages[NONEXISTING_REQUEST_ID]);
            return;
        case INVALID_REQUEST_ID:
            alert(reqMessages[INVALID_REQUEST_ID]);
            return;
        case REQID_QUERY:
            url = YAHOO.transbit.tbits.contextPath + "search/" + sysPrefix + "?xmlHttp=true&query=" + getRequestIdQuery();
            break;
        case REQID_EMPTY:
            try
            {
                url = getURL(currentView);
            }
            catch (e)
            {
                if (e instanceof TBitsException)
                {
                    alert(e["description"]);
                    url = "";
                }
                else
                {
                    alert(e["description"]);
                }
            }
            break;
    }
    
    if (url == "")  return;
    
    // This should be an XML Http request.
    url = url + "&xmlHttp=true";
    
    if (sortQuery != "")        url = url + sortQuery;
    if (renderType == "true")   url = url + "&renderType=" + getValue("renderType");
    if (listAll != null && listAll != "" && listAll == "true")  url = url + "&listAll=true";
    
    if (pageNumber != "") url = url + "&pageNumber=" + pageNumber;
    

    // append the current view of search page.
    url = url + "&sv=" + currentView;
    
    // append the current time to the URL to make it unique.
    url = url + "&now=" + getCurrentTime();
    
    toggleIFrame(word, true);
    
    // Send the request to the url.
    try
    {
        executeAsyncGetRequest(url, searchHandler);
    }
    catch (e)
    {
        // TODO: log this event with the server.
        toggleIFrame(word, false);
    }
    return;
}

/*
 * This method prepares the URL for the current search.
 */
function getURL(currentView)
{
    var sysPrefix = getValue("sysPrefix");
    
    // This will hold the URL that should be executed to fill the searchResultsHolder.
    var url = YAHOO.transbit.tbits.contextPath + "search/" + sysPrefix;
    
    switch (currentView)
    {
//        case SIMPLE_VIEW:
//            url = url + "?query=" + escape(getValue("simpleQuery"));
//            break;
        case NORMAL_VIEW:
            url = url + "?query=" + escape(formSingleBAQuery()) + "&filter=" + getValue("filter") + "&description=" + getValue("normalDesc");
            break;
        case ADVANCED_VIEW:
            url = url + "?query=" + escape(getValue("advancedQuery"));
            break;
        case ALLAREAS_VIEW:
            {
                var query  = formMultiBAQuery();
                var filter = getValue("aa_filter");
                var desc   = getValue("aa_Desc");
                
                url = url + "?allAreas=true&query=" + escape(query) + "&filter=" + filter + "&description=" + desc;
                var sortPrefix = new String(trim(getValue("aa_sortPrefix")));
                if (sortPrefix != "")
                {
                    var uSortPrefix = sortPrefix.toUpperCase();
                    var sortField = trim(getValue("aa_sortField"));
                    var sortOrder = trim(getValue("aa_sortOrder"));
                    url = url + "&" + uSortPrefix + "_" + ALLAREAS_VIEW + "_sortField=" + sortField;
                    url = url + "&" + uSortPrefix + "_" + ALLAREAS_VIEW + "_sortOrder=" + sortOrder;
                }
            }
            break;
        case MYREQUESTS_VIEW:
            {
                var filter = getMyRequestsFilter();
                if (filter == 0)
                {
                    alert(FILTER_MANDATORY);
                    return "";
                }
                url = url + YAHOO.transbit.tbits.contextPath + "my-requests?filter=" + filter;
                var sortPrefix = new String(trim(getValue("mr_sortPrefix")));
                if (sortPrefix != "")
                {
                    var uSortPrefix = sortPrefix.toUpperCase();
                    var sortField = trim(getValue("mr_sortField"));
                    var sortOrder = trim(getValue("mr_sortOrder"));
                    url = url + "&" + uSortPrefix + "_" + MYREQUESTS_VIEW + "_sortField=" + sortField;
                    url = url + "&" + uSortPrefix + "_" + MYREQUESTS_VIEW + "_sortOrder=" + sortOrder;
                }
            }
            break;
    }
    return url;
}

/*
 * This is a callback method for the asynchronous search request.
 */
function searchHandler(xmlHttpObject)
{
    if (xmlHttpObject)
    {
        var state = xmlHttpObject.readyState;
        if (state == 4)
        {
            // Output is ready to be absorbed.
            var output = xmlHttpObject.responseText;
            
            // Output contains the search results. So display them
            displaySearchResults(output);
            setInnerHtml("expSpan", "Collapse All&nbsp; | ");

            toggleIFrame("", false);
            
            checkReadUnread();

            // Note the ending time.
            var end = getCurrentTime();
            
            // Set the total time to the difference of end and start.
            setValue("totalTime", (end - start));
            
            // Calculate the time spent at various stages.
            calcTimes();
            
            // Initialize the tooltips again.
            try{tt_Init();}catch (e){}
        }
    }
    else
    {
        //TODO: Log this error with the server.
        toggleIFrame("", false);
    }
}

function checkReadUnread()
{
    var isVEEnabled = getValue("isVEEnabled");
    var ctrl = document.getElementById("readFilter");
    if (ctrl)
    {
        if (isVEEnabled == "true")
        {
            if (ctrl.options.length == 3)
            {
                // Nothing needs to be done here.
            }
            else if (ctrl.options.length == 1)
            {
                ctrl.options.length = 3;
                ctrl.options[1].value = "true";
                ctrl.options[1].text = "Read";

                ctrl.options[2].value = "false";
                ctrl.options[2].text = "Unread";
                
            }
            setInnerHtml("readUnreadHelp", "");
            document.getElementById("readUnreadLabel").className = "";
        }
        if (isVEEnabled == "false")
        {
            if (ctrl.options.length == 1)
            {
                // Nothing needs to be done here.
            }
            else if (ctrl.options.length == 3)
            {
                ctrl.options.length = 1;
            }
            setInnerHtml("readUnreadHelp", READ_UNREAD_HELP);
            document.getElementById("readUnreadLabel").className = "cb l";
        }
    }
}

/*
 * This method toggles the iframe shown while search is in progress.
 */
function toggleIFrame(word, show)
{
    var ctrl = document.getElementById("frmTable");
    if (ctrl)
    {
        if (show == true)
        {
            hideElement("searchResultsHolder");
            var url = YAHOO.transbit.tbits.contextPath + "search-util/" + getValue("sysPrefix") + "?loading=true&word=" + word
            window.frames["bottomIFrame"].location = url;
            ctrl.style.display  = "block";
        }
        else
        {
            ctrl.style.display  = "none";
            showElement("searchResultsHolder");
            window.frames["bottomIFrame"].location = "about:blank";
        }
    }
    else
    {
        //TODO: log this event with the server.
    }
}

/*
 * This method sends a request to TBits server to return the results in excel format.
 */
function exportToExcel()
{
    // Get the current view of the search page.
    var currentView = getIntValue("sv");
    
    // This will hold the URL.
    var url = getURL(currentView);
    
    // Append the following to the URL:
    //   - shortcut name
    //   - format as excel
    url = url + "&shortcutName=" + getValue("shortcutName") + "&format=excel" + "&sv=" + currentView + "&now=" + getCurrentTime();

    // Finally open the results in a new window.
    window.open(url);    
}

/*
 * This method sets the innerHTML of search results holder to the output.
 */
function displaySearchResults(output)
{
    var holder = document.getElementById("searchResultsHolder");
    if (holder != null) 
    {
        var dStart = getCurrentTime();
        holder.innerHTML = output;
        holder.style.display = "block";
        var dEnd = getCurrentTime();
        
        setValue("parseTime", (dEnd - dStart));
    }
    else
    { 
        // TODO: Log this event with the server.
        alert("Please let the page be loaded completely.");
    }
}

/*
 * This method initiates a request to sort results.
 */
function sortResults(sortPrefix, sortField, sortOrder)
{
    // Check the state of listAll.
    var listAll = getListAll();

    if (sortOrder == 0) sortOrder = 1;
    else sortOrder = 0;
    
    var currentView = getValue("sv");
    if (currentView == NORMAL_VIEW || currentView == ADVANCED_VIEW)
    {
        var currentSortField = getValue(currentView + "_sortField");
        var currentSortOrder = getValue(currentView + "_sortOrder");

        var sortQuery = "&sortField=" + sortField;
        if (sortField == currentSortField)
        {
            sortQuery = sortQuery + "&sortOrder=" + sortOrder;
        }
        setValue(currentView + "_sortField", sortField);
        setValue(currentView + "_sortOrder", sortOrder);
        getResults(sortQuery, "", "", listAll, "Sorting");
    }
    else if (currentView == ALLAREAS_VIEW)
    {
        setValue("aa_sortPrefix", sortPrefix);
        setValue("aa_sortField", sortField);
        setValue("aa_sortOrder", sortOrder);
        getResults("", "", "", listAll, "Sorting");
    }
    else if (currentView == MYREQUESTS_VIEW)
    {
        var filter = getMyRequestsFilter();
        if (filter == 0)
        {
            alert(FILTER_MANDATORY);
            return "";
        }
        var url = "";
        url = url + YAHOO.transbit.tbits.contextPath + "my-requests" + 
                    "?filter=" + filter + 
                    "&areas=" + sortPrefix + 
                    "&sortField=" + sortField + 
                    "&sortOrder=" + sortOrder + 
                    "&now=" + getCurrentTime();
        try
        {
            var divId = sortPrefix + "_MyRequests";
            var ctrl = document.getElementById(divId);
            var height = ctrl.offsetHeight - 8;
            if (height < 75) height = 75;
            var ih = 
                "<TABLE width='100%'>" + 
                "    <TR>" + 
                "        <TD>" +
                "            <IFRAME src='"+ YAHOO.transbit.tbits.contextPath +"search-util?loading=true&word=Sorting' width='100%' height='" + height + "px' frameBorder='no' scrolling='no'>" + 
                "        </TD>" + 
                "    </TR>" + 
                "</TABLE>'";
            document.getElementById(divId).style.width = "500px";
            setInnerHtml(divId, ih);
            var paramHash = new Object();
            paramHash["sysPrefix"] = sortPrefix;
            executeAsyncGetRequest(url, mrSortHandler, paramHash);
        }
        catch (e)
        {
        }
    }
    return;
}

/*
 * This is a callback method for the asynchronous sort request in My Requests view.
 */
function mrSortHandler(xmlHttpObject, paramHash)
{
    if (xmlHttpObject)
    {
        var state = xmlHttpObject.readyState;
        if (state == 4)
        {
            // Output is ready to be absorbed.
            var output = xmlHttpObject.responseText;
            
            // Output contains the search results. So display them
            if (paramHash)
            {
                var sysPrefix = paramHash["sysPrefix"];
                if (sysPrefix)
                {
                    var divId = sysPrefix + "_MyRequests";
                    var str = new String(output);
                    var index = str.indexOf(/<table/i);
                    if (index > 0)
                    {
                        str = str.substring(index, str.length);
                        index = str.lastIndexOf(/<\/table>/i);
                        if (index > 0)
                        {
                            str = str.substring(0, index);
                        }
                    }
                    
                    var ctrl = document.getElementById(divId);
                    if (ctrl)
                    {
                        setInnerHtml(divId, str);
                    }
                    else
                    {
                        //TODO: Log this event at the server.
                    }
                }

                // Initialize the tooltips again.
                try{tt_Init();}catch (e){}
            }
        }
    }
    else
    {
        //TODO: Log this error with the server.
        toggleIFrame("", false);
    }
}


/*
 * This method initiates a request to page search results.
 */
function onChangePageNumber()
{
    var pageNumber = getValue("pageNumber");
    var currentView = getValue("sv");
    var sortField = getValue(currentView + "_sortField");
    var sortOrder = getValue(currentView + "_sortOrder");
    
    if (sortField == null || sortField == "")   sortField = "request";
    if (sortOrder == null || sortOrder == "")   sortOrder = "desc";
    
    var sortQuery = " sortfield:" + sortField + " sortorder:" + sortOrder;

    getResults(sortQuery, "", pageNumber, getListAll(), "Paging");
    return;
}

function getListAll()
{
    var ctrl = document.getElementById("listAll");
    if (ctrl != null && ctrl.checked == true) return "true";
    return "";
}

function onClickListAll()
{
    var listAll = getListAll();
    var word = "Listing All";
    if (listAll == true || listAll == 'true') word = "Listing All";
    else word = "Paging";
    getResults('', '', '' , listAll, word);
}

//*********************************************************************
// Methods to open configuration dialogs.
//*********************************************************************
/*
 * This method opens the dialog to configure search results display.
 */
function onClickConfigure()
{
    var url = YAHOO.transbit.tbits.contextPath + "configure-search/" + getValue("sysPrefix") + "?date=" + getCurrentTime();
    var obj = new Object();
    obj.returnValue = false;
    
    var status = "no";
    var width = "520";
    var height = "480";
    var resize = "true";
    openModalDialog(url, obj, 
                    "status", status,
                    "width", width,
                    "height", height,
                    "resize", resize,
		    "scroll", true);
    
    if (isIE == true && obj.returnValue == true)
        onSaveConfigureHandler();
}

function onSaveConfigureHandler()
{
    var searchResults = document.getElementById(getValue("sysPrefix") + "_searchResults");
    if (searchResults != null)
        startSearching("Refreshing");
}

/*
 * This method opens the save search dialog.
 */
function onClickSaveSearch()
{
    var sysPrefix = getValue("sysPrefix");
    var view = getValue("sv");
    var desc = getValue("normalDesc");
    var filter = getValue("filter");
    var query = getQuery();

    var url = 
    	YAHOO.transbit.tbits.contextPath + "shortcut-handler/" + sysPrefix     + 
                "?request="          + "display"     +
                "&sysPrefix="        + sysPrefix     + 
                "&view="             + view          + 
                "&description="      + escape(desc)  + 
                "&filter="           + filter        + 
                "&query="            + escape(query) + 
                "&listAll="          + getListAll()  + 
                "&now="              + getCurrentTime();
    var width = "550";
    var height = "200";
    var resize = "yes";
    var status = "no";
    var obj = new Object();
    obj.returnValue = false;
    openModalDialog(url, obj, 
                    "status", status, 
                    "width", width, 
                    "height", height, 
                    "resize", resize);
}

//*********************************************************************
// Methods related to My Requests view.
//*********************************************************************

/*
 * This method opens the filter status dialog in my-requests view.
 */
function onFilterStatus()
{
    var sysPrefix = getValue("sysPrefix");
    var filter = getMyRequestsFilter();
    var url = YAHOO.transbit.tbits.contextPath + "configure-my-requests/" + sysPrefix + 
              "?sysPrefix=" + sysPrefix + 
              "&filter=" + filter + 
              "&now=" + getCurrentTime();
    var width = "550";
    var height = "360"
    var resize = "no";
    var status = "no";
    var object = new Object();
    object.saved = false;
    openModalDialog(url, object,
                    "status", status, 
                    "width", width, 
                    "height", height, 
                    "resize", resize);
    
    if (isIE == true)
    {
        if (object != null)
        {
            if (object.saved == "true")
            {
                startSearching("Refreshing");
            }
        }
    }
}

/*
 * This method finds the filter value in my-requests view.
 */
function getMyRequestsFilter()
{
    var filter = 0;
    if (isChecked("chboxLogger") == true) filter = filter + getIntValue("chboxLogger");
    if (isChecked("chboxAssignee") == true) filter = filter + getIntValue("chboxAssignee");
    if (isChecked("chboxSubscriber") == true) filter = filter + getIntValue("chboxSubscriber");
    if (isChecked("chboxPrimaryAssignee") == true) filter = filter + getIntValue("chboxPrimaryAssignee");
    return filter;
}

//*********************************************************************
// Methods to generate query in different views in search page.
//*********************************************************************
function getQuery()
{
    var query = "";
    var view = getIntValue("sv");
    if      (view == -1) query = getValue("simpleQuery");
    else if (view == NORMAL_VIEW) query = formSingleBAQuery();
    else if (view == ADVANCED_VIEW) query = getValue("advancedQuery");
    return query;
}

function formSingleBAQuery()
{
    var buffer = new String();
    var query = "";

    var userDesc = "user";
    if (document.getElementById("cbLast").checked == false)
        userDesc = "appender";
        
    var read = getValue("readFilter");
    if (read != "" && (read == "true" || read == "false"))
    {
        query = trim(query) + " read:" + read;
    }

    query = trim(query) + " " + getSubQuery(getPrimaryDesc("category_id"), getTypeList("category_id"));
    query = trim(query) + " " + getSubQuery(getPrimaryDesc("status_id"), getTypeList("status_id"));
    query = trim(query) + " " + getSubQuery(getPrimaryDesc("severity_id"), getTypeList("severity_id"));
    query = trim(query) + " " + getSubQuery(getPrimaryDesc("request_type_id"), getTypeList("request_type_id"));
    
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("logger_ids"), "logger_ids");
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("assignee_ids"), "assignee_ids");
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("subscriber_ids"), "subscriber_ids");
    query = trim(query) + " " + getUserQuery(userDesc, "user_id");
    
    // Get the date sub-queries only if the date panel is expanded
    var ctrl = document.getElementById("trDates");
    if (ctrl && ctrl.style.display != "none")
    {
        var clause = "";
        clause = document.getElementById("log_clauseList").value;
        query = trim(query) + " " + getDateQuery(getPrimaryDesc("logged_datetime"), "log_", clause);

        clause = document.getElementById("upd_clauseList").value;
        query = trim(query) + " " + getDateQuery(getPrimaryDesc("lastupdated_datetime"), "upd_", clause);

        clause = document.getElementById("due_clauseList").value;
        query = trim(query) + " " + getDateQuery(getPrimaryDesc("due_datetime"), "due_", clause);

        query = trim(query) + " " + getSubQuery(getPrimaryDesc("office_id"), getTypeList("office_id"));
    }
    
    var text   = getValue("normalDesc");
    var filter = getValue("filter");
    if (text != "" && filter != "" && filter != "none")
        query = trim(query) + " " + getSubQuery(filter, getTextQuery(text));
    
    // Trim the query and return;
    query = trim(query);
    // try{document.getElementById("querySection").innerHTML = query;}catch (e) {}
    return query;
}

function formMultiBAQuery()
{
    var query = "";
    
    var userDesc = "user";
    if (document.getElementById("aa_cbLast").checked == false) 
        userDesc = "appender";

    query = trim(query) + " " + getSubQuery("areas", getTypeList("aa_business_areas"));
    query = trim(query) + " " + getSubQuery(getPrimaryDesc("status_id"), getTypeList("aa_status_id"));
    query = trim(query) + " " + getSubQuery(getPrimaryDesc("severity_id"), getTypeList("aa_severity_id"));
    
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("logger_ids"), "aa_logger_ids");
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("assignee_ids"), "aa_assignee_ids");
    query = trim(query) + " " + getUserQuery(getPrimaryDesc("subscriber_ids"), "aa_subscriber_ids");
    query = trim(query) + " " + getUserQuery(userDesc, "aa_user_id");
    
    var clause = "";
    clause = document.getElementById("aa_log_clauseList").value;
    query = trim(query) + " " + getDateQuery(getPrimaryDesc("logged_datetime"), "aa_log_", clause);

    clause = document.getElementById("aa_upd_clauseList").value;
    query = trim(query) + " " + getDateQuery(getPrimaryDesc("lastupdated_datetime"), "aa_upd_", clause);
    
	clause = document.getElementById("aa_due_clauseList").value;
	query = trim(query) + " " + getDateQuery(getPrimaryDesc("due_datetime"), "aa_due_", clause);

    var text   = getValue("aa_Desc");
    var filter = getValue("aa_filter");
    if (text != "" && filter != "" && filter != "none")
        query = trim(query) + " " + getSubQuery(filter, getTextQuery(text));

    query = trim(query);
    return query;
}

function getTypeList(select)
{
    var argList = new Array();
    var ctrl = document.getElementById(select);
    if (ctrl != null)
    {
        var allSelected = true;
        var noneSelected = true;
        var listLen = 0;
        var optLen = ctrl.options.length;
        for(var i = 1; i < optLen; i++)
        {
            var selected = ctrl.options[i].selected;
            if (selected == false)
            {
                allSelected = false;
            }
            else
            {
                noneSelected = false;
                var value = trim(ctrl.options[i].value);
                if (value.match(/[ \-_]/) != null)
                {
                    value = "\"" + value + "\"";
                }
                argList[listLen] = value;
                listLen ++;
            }
        }
        // Check if all the elements are selected. 
        if (allSelected == true || noneSelected == true)
        {
            // Then we need not pass the entire list that is selected.
            argList = new Array();
        
            // Get the value of all option which is the first one in the list.
            var allValue = new String(ctrl.options[0].value);
            if (allValue != "")
            {
                // Then split this value on , and then assign this to argList.
                argList = allValue.split(",");
            }
        }
    }
    var value = argList.join(",");
    return value;
}

function getUserQuery(descriptor, ctrlName)
{
    var query = "";
    var nQuery = trim(getSubQuery(descriptor, getUserList(ctrlName)));
    var hQuery = trim(getSubQuery("-has", checkQuotes(ctrlName, descriptor)));
    if (nQuery != "")
    {
        if (hQuery != "")
            query = "(" + hQuery + " OR " + nQuery + ")";
        else
            query = nQuery;
    }
    else if (hQuery != "")
    {
        query = hQuery;
    }
    return query;
}

function getUserList(input)
{
    var argList = new Array();
    var ctrl = document.getElementById(input);
    if (ctrl != null)
    {
        var temp = trim(ctrl.value);
        if (temp != "")
        {
            // Remove the trailing comma.
            if (temp.lastIndexOf(',') == temp.length - 1)
                temp = temp.substring(0, temp.length - 1);
            // Replace quotes.
            temp = temp.replace(/"/ig, '');
            if (temp != "")
                argList = temp.split(/[,;]/);
        }
    }
    var len = argList.length;
    for (var i =0 ; i < len; i++)
    {
        var value = argList[i];
        if (hasSpecialChar(value) == true)
        argList[i] = '"' + value + '"';
    }
    var value = argList.join(",");
    return value;
}

function checkQuotes(input, value)
{
    var ctrl = document.getElementById(input);
    if (ctrl != null)
    {
        var temp = new String(trim(ctrl.value));
        if (temp.indexOf('""') >= 0)
            return value;
    }
    return "";
}

function getDateQuery(primaryDesc, prefix, clause)
{
    var filterValue = "";
    var dtCtrl, dtCtrlFrom, dtCtrlTo;
    var dtCtrlValue = "", dtCtrlFromValue = "", dtCtrlToValue = "";
    var dtCtrlName = prefix + "dtCtrl";
    var dtCtrlFromName = prefix + "dtCtrlFrom";
    var dtCtrlToName = prefix + "dtCtrlTo";
    
    try { dtCtrl = document.getElementById(dtCtrlName);         dtCtrlValue     = trim(dtCtrl.value);     }   catch (e) {}
    try { dtCtrlFrom = document.getElementById(dtCtrlFromName); dtCtrlFromValue = trim(dtCtrlFrom.value); }   catch (e) {}
    try { dtCtrlTo = document.getElementById(dtCtrlToName);     dtCtrlToValue   = trim(dtCtrlTo.value);   }   catch (e) {}

    // validate the content based on the clause.
    if (clause == "on"  || clause == "bef" || clause == "aft")
    {
        if (isValidDate(dtCtrlValue) == false) return "";
    }
    else if (clause == "lnh" || clause == "lnd" || clause == "lnw" || clause == "lnm" || clause == "lny" || 
             clause == "nnh" || clause == "nnd" || clause == "nnw" || clause == "nnm" || clause == "nny")
    {
        if (dtCtrlValue == "") return "";
        if (isValidInteger(dtCtrlValue) == false) return "";
    }
    
	if (clause == "on")         filterValue = dtCtrlValue;
	else if (clause == "bef")   filterValue = "before: " + dtCtrlValue;
	else if (clause == "aft")   filterValue = "after: " + dtCtrlValue;
	else if (clause == "bet")
	{
		if(dtCtrlFromValue == "" && dtCtrlToValue == "") return "";
		if(dtCtrlFromValue != "" && dtCtrlToValue != "")
		{
		    if (isValidDate(dtCtrlFromValue) == false || isValidDate(dtCtrlToValue) == false) return "";
		    filterValue = "between:[" + dtCtrlFromValue + ", " + dtCtrlToValue + "]";
		}
		else if (dtCtrlFromValue != "")
		{
		    if (isValidDate(dtCtrlFromValue) == false) return "";
		    filterValue = "after:" + dtCtrlFromValue;
		}
		else if (dtCtrlToValue != "")
		{
		    if (isValidDate(dtCtrlToValue) == false) return "";
		    filterValue = "before:" + dtCtrlToValue;
		}
	}
	else if (clause == "today")     filterValue = "today";
	else if (clause == "yesterday") filterValue = "yesterday";
	else if (clause == "tomorrow")  filterValue = "tomorrow";
	else if (clause == "lw")        filterValue = "last:1week";
	else if (clause == "lm")        filterValue = "last:1month";
	else if (clause == "ly")        filterValue = "last:1year";
	else if (clause == "nw")        filterValue = "next:1week";
	else if (clause == "nm")        filterValue = "next:1month";
	else if (clause == "ny")        filterValue = "next:1year";
	else if (clause == "lnh")       filterValue = "last:" + dtCtrlValue + "hours";
	else if (clause == "lnd")       filterValue = "last:" + dtCtrlValue + "days";
	else if (clause == "lnw")       filterValue = "last:" + dtCtrlValue + "weeks";
	else if (clause == "lnm")       filterValue = "last:" + dtCtrlValue + "months";
	else if (clause == "lny")       filterValue = "last:" + dtCtrlValue + "years";
	else if (clause == "nnh")       filterValue = "next:" + dtCtrlValue + "hours";
	else if (clause == "nnd")       filterValue = "next:" + dtCtrlValue + "days";
	else if (clause == "nnw")       filterValue = "next:" + dtCtrlValue + "weeks";
	else if (clause == "nnm")       filterValue = "next:" + dtCtrlValue + "months";
	else if (clause == "nny")       filterValue = "next:" + dtCtrlValue + "years";

    filterValue = trim(filterValue);
    if (filterValue == "") return "";
    
    return primaryDesc + ":" + filterValue;
}

function getIntQuery(desc, clause)
{
    var query = "";
    var dtCtrl, dtCtrlFrom, dtCtrlTo;
    var dtCtrlValue = "", dtCtrlFromValue = "", dtCtrlToValue = "";
    var dtCtrlName = "dtCtrl";
    var dtCtrlFromName = "dtCtrlFrom";
    var dtCtrlToName = "dtCtrlTo";
    
    try { dtCtrl = document.getElementById(dtCtrlName);         dtCtrlValue     = trim(dtCtrl.value);     }   catch (e) {}
    try { dtCtrlFrom = document.getElementById(dtCtrlFromName); dtCtrlFromValue = trim(dtCtrlFrom.value); }   catch (e) {}
    try { dtCtrlTo = document.getElementById(dtCtrlToName);     dtCtrlToValue   = trim(dtCtrlTo.value);   }   catch (e) {}
    
    if (clause == "eq" || clause == "ne" || clause == "gt" || clause == "ge" || clause == "lt" || clause == "le")
    {
        if (isValidInteger(dtCtrlValue) == false) return "";
    }
    else if (clause == "in" || clause == "ni")
    {
        if (isValidIntegerList(dtCtrlValue) == false) return "";
		if (dtCtrlValue.lastIndexOf(",") == dtCtrlValue.length - 1 || dtCtrlValue.lastIndexOf(";") == dtCtrlValue.length - 1)
			dtCtrlValue = dtCtrlValue.substring(0, dtCtrlValue.length - 1);
		if (dtCtrlValue.indexOf(",") > 0 || dtCtrlValue.indexOf(";") > 0)
			dtCtrlValue = "(" + dtCtrlValue + ")";
    }
    else if (clause == "bet")
    {
        if (isValidInteger(dtCtrlFromValue) == false || isValidInteger(dtCtrlToValue) == false) return "";
    }

	var filterValue = "";
	if (clause == "eq")         query = desc + ":"   + dtCtrlValue;
	else if (clause == "gt")    query = desc + ":>"  + dtCtrlValue;
	else if (clause == "ge")    query = desc + ":>=" + dtCtrlValue;
	else if (clause == "lt")    query = desc + ":<"  + dtCtrlValue;
	else if (clause == "le")    query = desc + ":<=" + dtCtrlValue;
	else if (clause == "bet")   query = desc + ":"   + dtCtrlFromValue + "-" + dtCtrlToValue;
	else if (clause == "in")    query = desc + ":"   + dtCtrlValue;
	else if (clause == "ne")    query = " -" + desc + ":"   + dtCtrlValue;
	else if (clause == "ni")    query = " -" + desc + ":"   + dtCtrlValue;
	return query;
}

function getTextQuery(argument)
{
    // Trim the leading and trailing spaces.
    argument = trim(argument);
    
    // Replace two or more whitespaces with a single one.
    argument = replaceMWSCharacters(argument);

    // Get an array of characters
    var wArray = new Array();
    var buffer = "";
    for(var i = 0 ; i < argument.length; i++)
    {
        var token = argument.charAt(i);
        if (token == '"')
        {
            buffer = buffer + token;
            i = i + 1;
            token = argument.charAt(i);
            while (token != '"' && i < argument.length)
            {
                token = argument.charAt(i);
                buffer = buffer + token;
                i = i + 1;
            }
            if (token == '"')
            {
                wArray.push(buffer);
                buffer = "";
            }
            else
            {
                buffer = buffer + '"';
                wArray.push(buffer);
                buffer = "";
            }
            continue;
        }
        else if(token == " ")
        {
            if (hasSpecialChar(buffer) == true)
            {
                buffer = '"' + buffer + '"';
            }
            wArray.push(buffer);
            buffer = "";
        }
        buffer = buffer + token;
    }
    if (buffer != "")
    {
        if (hasSpecialChar(buffer) == true)
        {
            buffer = '"' + buffer + '"';
        }
        wArray.push(buffer);
        buffer = "";
    }
    
    argument = wArray.join(" AND ");
    return argument;
}

function hasSpecialChar(buffer)
{
    if (buffer.indexOf(":") >= 0 || // Colon
        buffer.indexOf("-") >= 0 || // Hyphen
        buffer.indexOf("(") >= 0 || // Opening parenthesis
        buffer.indexOf(")") >= 0 || // Closing parenthesis
        buffer.indexOf("[") >= 0 || // Opening Square bracket
        buffer.indexOf("]") >= 0 || // Closing Square bracket
        buffer.indexOf(",") >= 0 || // Comma
        buffer.indexOf(";") >= 0)   // Semicolon
    {
        return true;
    }
    return false;
}

function getSubQuery(fieldName, argList)
{
    var query = "";
    if (argList != "" && argList != "\"\"")
        query = fieldName + ":(" + argList + ")";
    return query;
}

function getPrimaryDesc(fieldName)
{
    var desc = primaryDescTable[fieldName];
    if (desc == null || desc == "")
        desc = fieldName;
    return desc;
}

//*********************************************************************
// Methods that operate on results displayed.
//*********************************************************************
function ec(sysPrefix, requestId, expand)
{
    if (isIE == true)
    {
        ecIE(sysPrefix, requestId, expand)
        return;
    }

    var colIcon = document.getElementById("col_" + sysPrefix + "_" + requestId);
    var expIcon = document.getElementById("exp_" + sysPrefix + "_" + requestId);
    var table = document.getElementById(sysPrefix + "_searchResults");
    if (table == null) return;
    var currentRowId = "row_" + sysPrefix + "_" + requestId;
    var currentRow = document.getElementById(currentRowId);

    if (expand == "true")
    {
        try
        {
            while (currentRow.nextSibling)
            {
                currentRow = currentRow.nextSibling;
                var currentRowClass = currentRow.className;
                if (currentRowClass.indexOf("normal") >= 0 || 
                    currentRowClass.indexOf("root") >= 0 )
                    break;
                showTableRow(currentRow);
            }
        }
        catch (e)
        { 
        }
        colIcon.style.display = "";
        expIcon.style.display = "none";
    }
    else
    {
        try
        {
            while (currentRow.nextSibling)
            {
                currentRow = currentRow.nextSibling;
                var currentRowClass = currentRow.className;
                if (currentRowClass.indexOf("normal") >= 0 ||
                    currentRowClass.indexOf("root") >= 0)
                    break;
                hideTableRow(currentRow);
            }
        }
        catch (e)
        { 
        }
        colIcon.style.display = "none";
        expIcon.style.display = "";
    }
}

function ecIE(sysPrefix, requestId, expand)
{
    var b, m, e;
    b = getCurrentTime();
    var colIcon = document.getElementById("col_" + sysPrefix + "_" + requestId);
    var expIcon = document.getElementById("exp_" + sysPrefix + "_" + requestId);
    var table = document.getElementById(sysPrefix + "_searchResults");
    if (table == null) return;
    
    var rows = table.rows;
    var rowLen = rows.length;
    var currentRowId = "row_" + sysPrefix + "_" + requestId;
    var currentRowPos = 0;
    /*
     * Go the current row first.
     */
    var i = 0;
    for (i = 0; i < rowLen;i++)
    {
        var id = rows[i].id;
        if (id == currentRowId) break;
    }
    currentRowPos = i;
    m = getCurrentTime();

    if (expand == "true")
    {
        try
        {
            for (i = currentRowPos + 1 ; i < rowLen ; i++)
            {
                var className = rows[i].className;
                if (className.indexOf("normal") >= 0 ||
                    className.indexOf("root") >= 0)
                {
                    break;
                }
                showTableRow(rows[i]);
            }
        }
        catch (e)
        { 
        }
        colIcon.style.display = "";
        expIcon.style.display = "none";
    }
    else
    {
        try
        {
            for (i = currentRowPos + 1 ; i < rowLen ; i++)
            {
                var className = rows[i].className;
                if (className.indexOf("normal") >= 0 ||
                    className.indexOf("root") >= 0)
                {
                    break;
                }
                hideTableRow(rows[i]);
            }
        }
        catch (e)
        { 
        }
        colIcon.style.display = "none";
        expIcon.style.display = "";
    }
    e = getCurrentTime();
    window.statusText  = (m - b) + " " + (e - m);
}

function ech()
{
    var sysPrefix = getValue("sysPrefix");
    var expState = getValue("expState");
    var name = "";
    if (expState == "true")
    {
        setValue("expState", "false");
        setInnerHtml("expSpan", "Expand All&nbsp; | ");
        name = "colIcon";
    }
    else 
    {
        setValue("expState", "true");
        setInnerHtml("expSpan", "Collapse All&nbsp; | ");
        name = "expIcon";
    }
    var arrList = document.getElementsByName(name);
    if (arrList == null || arrList.length == 0) return;
    if (document.all)
    {
        var len = arrList.length;
        for(var i=0; i < len; i++)
            arrList[i].click();
    }
    else
    {
        for(var i=0; i < arrList.length; i++)
            simulateClick(arrList[i]);
    }

}

function showTableRow(row)
{
    var className = row.className;
    row.style.display = "";
    row.className = className.replace("hide", "show");
}

function hideTableRow(row)
{
    row.style.display = "none";
    var className = row.className;
    if (className.indexOf("show") >= 0)
    {
        row.className = className.replace("show", "hide");
    }
    else
    {
        row.className = className + " hide";
    }
}

function markAsRead(requestId) // Marking a request as read.
{
    var table = document.getElementById(getValue("sysPrefix") + "_searchResults");
    if (table == null) return;

    var rows = table.rows;
    //
    // This particular request which should be marked as read can exist more than once 
    // in the search results table, i.e. as a normal request and as a part of hierarchy
    // if this is a parent. And, the ids of all the rows in which the request is rendered
    // end with "_<RequestId>".
    //
    var endStr = new String("_" + requestId);
    // Iterate through the rows.
    for(var i = 1; i < rows.length;i++)
    {
        // Get the id of the current row.
        var trId = new String(rows[i].id);
        
        // Skip this row if the id is null or empty.
        if (trId == null || trId == "") continue;
        
        // Check if the id of the row contains our pattern.
        var index = trId.indexOf(endStr);
        
        // If the pattern is not found, skip this row.
        if (index == -1) continue;
        
        // Check if the pattern occurs at the end.
        var length = index + endStr.length;
        if (length == trId.length)
        {
            //
            // ToDo: Change the className instead of individual properties.
            //
            rows[i].style.fontWeight = "normal";
            // rows[i].style.backgroundColor = "transparent";
            
            // form the image id.
            var imgId = trId.replace("row_", "new_");

            var img = document.getElementById(imgId);
            if (img != null) img.style.display = "none";
        }
    }
}

//*********************************************************************
// Methods related to opening requests.
//*********************************************************************
function onr(sysPrefix, requestId, trId)
{
    markAsRead(requestId);
    var view = getValue("sv");
    var sessionId = "";
    var pageCounter = 0;
    if (view == ALLAREAS_VIEW || view == MYREQUESTS_VIEW)
    {
        sessionId = getValue(sysPrefix + "_sessionId");
    }
    else
    {
        sessionId = getValue("sessionId");
        pageCounter = getValue("pageCounter");
    }
	var singleIEWindow = getValue("singleIEWindow");
	var searchText = getValue("searchText");
	var sName =  sysPrefix + "_" + requestId;
	if (singleIEWindow == "true") 
	    sName = sysPrefix;
	var redirection = YAHOO.transbit.tbits.contextPath + "Q/" + sysPrefix + "/" + requestId;
	var obj = document.createElement("form");
	obj.name="dynFrm";
	obj.id="dynFrm";
	obj.action = redirection;
	obj.target= sName;
	obj.method="post";
	obj.appendChild(createObject("requestId", requestId));
	obj.appendChild(createObject("sessionId", sessionId));
	obj.appendChild(createObject("pageCounter", pageCounter));
	obj.appendChild(createObject("searchText", searchText));
	document.body.appendChild(obj);
	document.getElementById("dynFrm").submit();
	document.body.removeChild(obj);
    return false;
}

function quickOpen(sysPrefix, requestId, url)
{
	var singleIEWindow = getValue("singleIEWindow");
	var sName =  sysPrefix + "_" + requestId;
	if (singleIEWindow == "true")
	    sName = sysPrefix;
    window.open(url, sName);    
    return false;
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

//*********************************************************************
// Methods that operate on shortcuts.
//*********************************************************************
var shortcutToolTipId; // Id of the shortcut sticky tooltip, used to delete and destory it.

function getShortcutList()
{
    var sysPrefix = getValue("sysPrefix");
    var url = YAHOO.transbit.tbits.contextPath + "shortcut-handler/" + sysPrefix + "?request=list&sysPrefix=" + sysPrefix + "&now=" + getCurrentTime();
    var content = executeHttpRequest("GET", url, "false");
    setInnerHtml("searchShortcuts", content);
    return content;
}

function showShortcutList(e)
{
    getShortcutList();
    var shortcutHolder = document.getElementById("searchShortcuts");
    if (shortcutHolder == null)
    {
        // TODO: log this event with the server.
        return;
    }
    var event = (document.all) ? window.event : e;
    var caption = '&nbsp;';
    var content = shortcutHolder.innerHTML;
    myPanel = new YAHOO.widget.Panel("saved_searches", 
    	{   
	    	width:"300px",
    		constraintoviewport:true,  
	    	underlay:"shadow",  
	    	close:true,  
	    	visible:true
		} ); 
    myPanel.setHeader("The Panel Control"); 
	myPanel.setBody(content);
	myPanel.render();
}

function openShortcut(shortcut, shared, isBA)
{
    var sysPrefix = getValue("sysPrefix");
    var userLogin = getValue("userLogin");
    // Append the user login to a shared shortcut.
    if (isBA != "true")
    {
        if (shared == "true")
        {
            shortcut = userLogin + ":" + shortcut;
        }
    }
    var redirection = YAHOO.transbit.tbits.contextPath + "search/" + sysPrefix + "/" + shortcut;
    window.location = redirection;
}

function setAsDefault(ctrl)
{
    if (ctrl == null)
    {
        return;
    }
    
    var shortcut = ctrl.value;
    var flag = true;
    var className = "";
    var request = "";
    
    /*
     * Get the class name of the selected item.
     */
    try
    {
        className = ctrl.options[ctrl.selectedIndex].className;
    }
    catch (e)
    {
    }

    /*    
     * If the className is not empty, then user has selected None.
     * So unset the current default value.
     */
    if (className != "")
    {
        request = "UNSET_DEFAULT";
        flag = false;
    }
    else
    {
        request = "SET_DEFAULT";
        flag = true;
    }
    
    var sysPrefix = getValue("sysPrefix");
    var url = YAHOO.transbit.tbits.contextPath + "shortcut-handler/"  + sysPrefix + 
              "?sysPrefix="         + sysPrefix + 
              "&shortcut="          + shortcut  + 
              "&request="           + request   +
              "&now="               + getCurrentTime();
    var output = executeHttpRequest("GET", url, false);
    if (output.indexOf("true") < 0)
    {
        if (flag == true)
            alert("The shortcut could not be set as default due to a database error.");
        else
            alert("The shortcut could not be removed as default due to a database error.");
    }
}

function markAsPrivate(shortcut, chbox, flag)
{
    var request = "";
    if (chbox.checked == true)
    {
        request = "MARK_SHARED";
        flag = false;
    }
    else
    {
        request = "MARK_PRIVATE";
        flag = true;
    }
    var sysPrefix = getValue("sysPrefix");
    var url = YAHOO.transbit.tbits.contextPath + "shortcut-handler/"  + sysPrefix + 
              "?sysPrefix="         + sysPrefix + 
              "&shortcut="          + shortcut  + 
              "&request="           + request   +
              "&now="               + getCurrentTime();
    var output = executeHttpRequest("GET", url, false);
    if (output.indexOf("true") < 0)
    {
        if (flag == "true")
            alert("The shortcut could not be marked as private due to a database error.");
        else
            alert("The shortcut could not be marked as public due to a database error.");
    }
}

function removeShortcut(shortcut, trId, ev, isDefault, isBA)
{
    var result = confirm("Are you sure you want to delete the shortcut \"" + shortcut + "\"?");
    if (result == false) return;
    var sysPrefix = getValue("sysPrefix");
    var request = "DELETE";
    var url = 
    	YAHOO.transbit.tbits.contextPath + "shortcut-handler/" + sysPrefix    + 
        "?sysPrefix="        + sysPrefix    + 
        "&request="          + request   +
        "&shortcut="         + shortcut     + 
        "&now="              + getCurrentTime();
    var output = executeHttpRequest("GET", url, false);
    if (output.indexOf("false") >= 0)
    {
        alert("The shortcut you are trying to delete does not exist.");
        return;
    }
    if (output.indexOf("success") > 0)
    {
        var arr;
        var i;
        var len;

        arr = document.getElementsByName(trId);
        len = arr.length;
        i = 0;
        for(var i=0; i < len; i++)
            arr[i].style.display = "NONE";
        
        arr = document.getElementsByName("defShortcutList");
        len = arr.length;
        i = 0;
        for(var i=0; i < len; i++)
            removeOption(arr[i], shortcut, isDefault, isBA);

        if (isBA == "false")
        {
            arr = document.getElementsByName("userShortcutCount");
            len = arr.length;
            i = 0;
            var count = 0;
            for(var i = 0; i < len; i++)
            {
                var value = arr[i].value;
                value = value - 1;
                arr[i].value = value;
                count = value;
            }
            
            if (count == 0)
            {
                arr = document.getElementsByName("tblUserShortcuts");
                len = arr.length;
                i = 0;
                for(var i = 0; i < len; i++)
                {
                    arr[i].style.display = "none";
                }
                
                arr = document.getElementsByName("spnNoShortcuts");
                len = arr.length;
                i = 0;
                for(var i = 0; i < len; i++)
                {
                    arr[i].style.display = "";
                }
            }
        }
        else
        {
            arr = document.getElementsByName("baShortcutCount");
            len = arr.length;
            i = 0;
            var count = 0;
            for(var i = 0; i < len; i++)
            {
                var value = arr[i].value;
                value = value - 1;
                arr[i].value = value;
                count = value;
            }
            
            if (count == 0)
            {
                arr = document.getElementsByName("tblBAShortcuts");
                len = arr.length;
                i = 0;
                for(var i = 0; i < len; i++)
                {
                    arr[i].style.display = "none";
                }

                arr = document.getElementsByName("trBASearchLabel");
                len = arr.length;
                i = 0;
                for(var i = 0; i < len; i++)
                {
                    arr[i].style.display = "none";
                }
            }
        }
    }
    return;
}

function removeOption(select, option, isDefault, isBA)
{
    if (select == null) return;
    
    /*
     * rValue is the value of the shortcut to be removed in uppercase.
     */
    var rValue = new String(option).toUpperCase();
    var baOptGroup = false;
    var children = select.childNodes;
    var len = children.length;
    for (var i=0; i < len; i++)
    {
        var tagName = children[i].tagName;
        if (tagName)
        {
            if (tagName == "OPTGROUP")
            {
                var optionsList = children[i].childNodes;
                var optLen = optionsList.length;
                for (var j=0; j < optLen; j++)
                {
                    var optTagName = optionsList[j];
                    if (optTagName)
                    {
                        var cValue = new String(optionsList[j].value);
                        if (cValue.toUpperCase() == rValue)
                        {
                            /*
                             * We found a shortcut with the required value.
                             * Check if we should be deleting a BA shortcut and
                             * the value found is in a BA opt group.
                             */
                            if (isBA == "true")
                            {
                                if (baOptGroup == true)
                                {
                                    children[i].removeChild(optionsList[j]);
                                }
                            }
                            /*
                             * We are supposed to delete a user-shortcut.
                             * Check if the one we found is not in baOptGroup.
                             */
                            else
                            {
                                if (baOptGroup == false)
                                {
                                    children[i].removeChild(optionsList[j]);
                                }
                            }
                        }
                    }
                }
                /*
                 * Since we are at the end of an OPTGROUP, the one that
                 * come next will be a BA option group.
                 */
                baOptGroup = true;
                
                /*
                 * Check if all the shortcuts in this option group are deleted.
                 */
                optionsList = children[i].childNodes;
                optLen = optionsList.length;
                var optCount = 0;
                for (var k=0; k < optLen; k++)
                {
                    if(optionsList[k].tagName)
                    {
                        optCount = optCount + 1;
                    }
                }
                if (optCount == 0)
                {
                    select.removeChild(children[i]);
                }
            }
        }
    }
    if (isDefault == true)
    {
        select.options[0].selected = true;
    }
}

//*********************************************************************
// Group Action Component related variables and methods.
//*********************************************************************
function cfga(chkBox)
{
	var requestList = getValue("requestList");
	if (chkBox.checked == true)
	{
		if (requestList != "") requestList = requestList + ",";
		var present = isPresent(requestList, chkBox.value);
		if (present == false)
		{
		    requestList = requestList + chkBox.value;
		    toggleGroupCheckBox(chkBox.value, true);
		}
	}
	else
	{
		var value = chkBox.value;
		var index = requestList.indexOf(value);
		if ( index == 0)
		{
			if (requestList.length == value.length)
				requestList = "";
			else
				requestList = requestList.replace(value + ",", "");
		}
		else 
		{
			requestList = requestList.replace("," + value, "");
		}
        toggleGroupCheckBox(chkBox.value, false);
        document.getElementById("chboxCheckAll").checked = false;
	}
	setValue("requestList", requestList);
}

function toggleGroupCheckBox(requestId, value)
{
    var arr = document.getElementsByName("grp_chbox");

    var endStr = new String("_" + requestId);
    var count = arr.length;

    for(var i = 0 ; i < count; i++)
    {
        var chbox = arr[i];
        var chboxId = chbox.id;
        if (chboxId == null) continue;
        
        var index = chboxId.indexOf(endStr);
        if (index < 0) continue;
        
        // Check if the pattern occurs at the end.
        var length = index + endStr.length;
        if (length == chboxId.length)
            chbox.checked = value;
    }
}

function actOnGroup()
{
    var groupAction = document.getElementById('grpActionOption');
    var actionDesc = document.getElementById('groupDesc');
	var sysPrefix = getValue("sysPrefix");
	var requestList = getValue("requestList");
	var actionOption = groupAction.value;
	var groupDesc = actionDesc.value;
	var grpIndex = groupAction.options.selectedIndex;
	var actionName = groupAction.options[grpIndex].text;
	
	if (groupDesc == "[Description]") groupDesc = "";
	
	if (requestList == "")
	{
		alert(SELECT_REQUEST_FOR_GROUP_ACTION);
		return;
	}
	
    // Check if bulk update can be performed.	
	if (continueBulkUpdate() != true)
	    return;
	
	var url =	YAHOO.transbit.tbits.contextPath + "group-action/" + sysPrefix + 
	            "?sysPrefix=" + sysPrefix + 
	            "&groupActionType=simple" + 
			    "&requestList=" + requestList + 
			    "&groupAction=" + actionOption + 
			    "&description=" + escape(groupDesc) + 
			    "&now=" + getCurrentTime();
	var output = executeHttpRequest("POST", url, false);
	output = trim(output);
	if (output.indexOf("true") >=0) 
	{
		setValue("requestList", "");
		alert(GROUP_ACTION_SUCCESSFUL.replace("<action>", actionName));
		startSearching("Refreshing");
		return;
	}
	else
	{
		if (output.indexOf("Rejected") < 0)
		{
			alert(GROUP_ACTION_FAILURE_REASON_BELOW.replace("<action>", actionName).replace("<message>", output));
			return;
		}
		else
		{
    		setValue("requestList", "");
    		alert(GROUP_ACTION_SUCCESSFUL.replace("<action>", actionName));
    	    startSearching("Refreshing");
		}
	}
}


function continueBulkUpdate()
{
    var status = false;
	var arr = document.getElementsByName("grp_chbox");
	if (arr)
	{
	    var count = arr.length;
	    var selCount = 0;
	    for (var i=0; i < count; i++)
	    {
	        if (arr[i].checked == true) selCount = selCount + 1;
	        else break;
	    }
	    if (selCount == count)
	    {
	        var result = confirm(CONFIRM_BULK_UPDATE_ALL.replace("<count>", selCount));
	        if (result == false)
	        {
	            status = false;
	        }
	        else
	        {
	            // TODO: We shall quickly log this event with the server. 
	            // Then, we can sue the guy if this turns out to be serious :-)
	            status = true;
	        }
	    }
	    else
	    {
	        status = true;
	    }
	}
	else
	{
	    status = true;
	}
	return status;
}

function openAdvancedGrpAction()
{
    // Check if bulk update can be performed.	
	if (continueBulkUpdate() != true)
	    return;
    var sysPrefix = getValue("sysPrefix");
    var requestList = getValue("requestList");
    if (trim(requestList) == "")
    {
        alert(SELECT_REQUEST_FOR_GROUP_ACTION);
        return;
    }
    
    var url = YAHOO.transbit.tbits.contextPath + "group-action/" + sysPrefix + 
                "?sysPrefix=" + sysPrefix + 
                "&requestList=" + requestList + 
                "&now=" + getCurrentTime();
    var features = "status:no; dialogWidth:680px; dialogHeight: 700px;resizable: yes";
    var obj = new Object();
    obj.returnValue = false;
    
    
    var width = "680";
    var height = "700"
    var resize = "yes";
    var status = "no";
    openModalDialog(url, obj, 
                    "status", status, 
                    "width", width, 
                    "height", height, 
                    "resize", resize);
    
    if (isIE == true)
    {
        if (obj.returnValue == true)
        {
		    setValue("requestList", "");
            startSearching("Refreshing");
        }
    }
    return;
}

function checkAll()
{
    var arr = document.getElementsByName("grp_chbox");
    var ctrl = document.getElementById("chboxCheckAll");
    if (ctrl)
    {
	    var requestList = getValue("requestList");
        var status = ctrl.checked;
        for (var i=0; i< arr.length; i++)
        {
            arr[i].checked = status;
		    
		    var present = isPresent(requestList, arr[i].value);
		    if (present == false)
		    {
		        if (requestList != "") requestList = requestList + ",";
		        requestList = requestList + arr[i].value;
		    }
        }
    }
    setValue("requestList", requestList);
}

//*********************************************************************
// Methods related to refreshing the search page.
//*********************************************************************

function scheduleRefresher()
{
    // Check if the interval is specified in the HTML source.
    var value = getIntValue("refreshRate");
    if (value < 0) // no need to schedule.
    {
        return;
    }
    interval = value;
    timerId =  window.setInterval
                (
                    function ()
                    {
                        // Check if the user has searched for something in this page.
                        var currentView = getValue("sv");
                        if (currentView == ALLAREAS_VIEW)
                        {
                            // We do not want to refresh results in All Areas View.
                            return;
                        }
                        
                        var sysPrefix = getValue("sysPrefix");
                        var ctrl = document.getElementById(sysPrefix + "_searchResults");
                        if (ctrl == null)
                        {
                            // Since the user has not searched anything, there is not point in refreshing. So, return.
                            return;
                        }
                        
                        // Start Searching.
                        startSearching("Refreshing");
                    }
                    , 
                    interval
                );
}

function cancelRefresher()
{
    if (timerId != -1)
    {
        window.clearInterval(timerId);
    }
    timerId = -1;
}

//*********************************************************************
// Utility Methods to set or get a value.
//*********************************************************************

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
 * This method replaces two or more whitespace characters with a single space.
 */
function replaceMWSCharacters(str)
{
	str = str.replace(/\s{2,}/g, " ");
	return str;
}

/**
 * This method removes the whitespaces in the given string.
 */
function removeWS(str)
{
	str = str.replace(/\s+/g, "");
	return str;
}


function getValue(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var value = "";
    if (ctrl != null) value = trim(ctrl.value);
    return value;
}

function setValue(ctrlId, value)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null) ctrl.value = value;
}

function getInnerHtml(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var innerHtml = "";
    if (ctrl != null) innerHtml = ctrl.innerHTML;
    return innerHtml;
}

function setInnerHtml(ctrlId, value)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null) ctrl.innerHTML = value;
}

function getIntValue(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    var value = -1;
    if (ctrl != null)
    {
        try { value = Number(trim(ctrl.value)); } catch (e) { value = -1; }
    }
    return value;
}

function isChecked(ctrlId)
{
    var ctrl = document.getElementById(ctrlId);
    if (ctrl != null && ctrl.checked == true) return true;
    return false;
}

function hideElement(element)
{
    var ctrl = document.getElementById(element);
    if (ctrl != null)
    {
        try { ctrl.style.display = "none"; } catch (e) {}
    }
}

function showElement(element)
{
    var ctrl = document.getElementById(element);
    if (ctrl != null)
    {
        try { ctrl.style.display = "block"; } catch (e) {}
    }
}

function showElementInline(element)
{
    var ctrl = document.getElementById(element);
    if (ctrl != null)
    {
        try { ctrl.style.display = "inline"; } catch (e) {}
    }
}

function isPresent(list, value)
{
    var arr = list.split(",");
    for(var i = 0; i< arr.length; i++)
    {
        var item = trim(arr[i]);
        if (item == null || item == "") continue;
        if (item == value) return true;
    }
    return false;
}
//*********************************************************************
// Method to calculate the time spent at various stages.
//*********************************************************************
function calcTimes()
{
    var dbTime = getIntValue("databaseTime");
    var inTime = getIntValue("interfaceTime");
    var parseTime = getIntValue("parseTime");
    var totalTime = getIntValue("totalTime");

    if (dbTime < 0 || inTime < 0 || parseTime < 0 || totalTime < 0) return;
    
    var renderTime = (inTime + parseTime);
    renderTime = ((renderTime >= 0) ? renderTime : 0);
    
    var ntTime = (totalTime - inTime - dbTime - parseTime);
    ntTime = ((ntTime >= 0) ? ntTime : 0);
    
    var buffer = "<b>Time spent:</b> ";
    buffer = buffer + "Database: " + dbTime + " ms | ";
    buffer = buffer + "Rendering: " + renderTime + " ms | ";
    buffer = buffer + "Network Transfer: " + ntTime + " ms | ";
    buffer = buffer + "Total: " + totalTime + " ms";
    
    setInnerHtml("timeSplit", buffer);
}

//*********************************************************************
// Method to Generate onclick event for a given target
//*********************************************************************
function simulateClick(target)
{
    var oEvent;
    oEvent = document.createEvent( "MouseEvents" );
    oEvent.initMouseEvent(
    "click",    // the type of mouse event
    false,      // do you want the event to
                // bubble up through the tree?  (sure)
    true,       // can the default action for this
                // event, on this element, be cancelled? (yep)
    window,     // the 'AbstractView' for this event,
                // which I took to mean the thing sourcing
                // the mouse input.  Either way, this is
                // the only value I passed that would work
    1,          // details -- for 'click' type events, this
                // contains the number of clicks. (single click here)
    1,          // screenXArg - I just stuck 1 in cos I
                // really didn't care
    1,          // screenYArg - ditto
    1,          // clientXArg - ditto
    1,          // clientYArg - ditto
    false,      // is ctrl key depressed?
    false,      // is alt key depressed?
    false,      // is shift key depressed?
    false,      // is meta key depressed?
    0,          // which button is involved?
                // I believe that 0 = left, 1 = right,
                // 2 = middle
    target      // the originator of the event
                // if you wanted to simulate a child
                // element firing the event you'd put
                // its handle here, and call this method
                // on the parent catcher.  In this case,
                // they are one and the same.
    );
    target.dispatchEvent( oEvent );
}

//*********************************************************************
// Functions used by advanced search panel.
//*********************************************************************
function isValidInteger(value)
{
	var regex = /^([-]?[0-9])+$/;
	if (value.match(regex)) return true;
	alert("Specify a valid integer value.");
	return false;
}
function isValidIntegerList(value)
{
	var regex = /^([-]?[0-9]+)(,([-]?[0-9]+))*$/;
	if (value.match(regex)) return true;
	return false;
}

function isValidNumeric(value)
{
	var regex = /^[\+\-]?[0-9]*\.?[0-9]*[Ee]?[\+\-]?[0-9]*$/;
	if (value.match(regex)) return true;
	alert("Specify a valid numeric value.");
	return false;
}
function isValidNumericList(value)
{
	var regex = /^([\+\-]?[0-9]*\.?[0-9]*[Ee]?[\+\-]?[0-9]*)(,([\+\-]?[0-9]*\.?[0-9]*[Ee]?[\+\-]?[0-9]*))*$/;
	if (value.match(regex)) return true;
	return false;
}

function isValidDate(dateValue)
{
	if (dateValue == "") return false;
    var regex = /^((0?[1-9])|(1[012]))\/((0?[1-9])|([12][0-9])|(3[01]))\/([0-9]{2}|[0-9]{4})$/;
    if (dateValue.match(regex) == null)
    {
        // throw exception;
        throw new TBitsException(INCORRECT_DATE_FORMAT);
    }
    return true;
}

function setupCalendar(dateField, dateImg)
{
	Calendar.setup({
			inputField  : dateField,
			button      : dateImg,
			ifFormat	: "%m/%d/%Y"});
}

function toMMDDYYYY(date)
{
    var parts = date.split("-");
    var year = parts[0];
    var month = parts[1];
    var day = parts[2];
    
    var date = month + "/" + day + "/" + year;
    
    return date;
}

function processDateDefaults(prefix)
{
    var defValue = getValue(prefix + "defValue");
    var defClause = getValue(prefix + "defClause");
    
    if (defClause == "") return;
    var temp = dateClauses[defClause];
    if (temp) setValue(prefix + "defClause", temp);
    
    if (temp == "aft" || temp == "bef")
    {
        try
        {
            var arr = defValue.split(" ");
            var date = toMMDDYYYY(arr[0]);
            setValue(prefix + "defValue", date);
        }
        catch (e)
        {
            // TODO: log this event with the server.
        }
    }
    else if (temp == "on")
    {
        try
        {
            var arr = defValue.split(" ");
            setValue(prefix + "defValue", arr[0]);
        }
        catch (e)
        {
            // TODO: log this event with the server.
        }
    }
    else if (temp == "bet")
    {
        try
        {
            var arr = defValue.split(",");
            var from = arr[0].split(" ")[0];
            var to = arr[1].split(" ")[0];
            
            setValue(prefix + "defFromValue", toMMDDYYYY(from));
            setValue(prefix + "defToValue", toMMDDYYYY(to));
        }
        catch (e)
        {
            // TODO: log this event with the server.
        }
    }
    else if (temp == "today" || temp == "yesterday" || temp == "tomorrow")
    {
    }
    else if (temp == "last" || temp == "next")
    {
        var regex = new RegExp("([0-9]+)([a-z]+)");
        var arr = regex.exec(defValue);
        if (arr.length == 3)
        {
            var p = (temp == "last") ? "l" : "n";
            var m = "";
            var s = "";

            var value = Number(arr[1]);
            var desc = arr[2];
            
            if (desc.indexOf("h") == 0)           s = "h";
            else if (desc.indexOf("d") == 0)      s = "d";
            else if (desc.indexOf("w") == 0)      s = "w";
            else if (desc.indexOf("mon") == 0)    s = "m";
            else if (desc.indexOf("y") == 0)      s = "y";
            
            if (value != 1 || desc.indexOf("h") == 0) m = "n";
            
            setValue(prefix + "defClause", p + m + s);
            setValue(prefix + "defValue", value);
        }
    }
}

function loadDateFields(prefix)
{
    var clause, ctrl, dataType;
    try
    {
        var defClause = "";

        // Load the logged_datetime related settings.
        defClause = "bet";
        var temp = getValue(prefix + "log_defClause");
        if (temp != "" && clauseInfo[temp]) defClause = temp;
        
        clause = changeClauseList("logged_datetime", prefix + "log_clauseList", defClause)
	    ctrl = document.getElementById(prefix + "log_dataSection");
	    dataType = 4;
	    formHTML(dataType, clause, ctrl, prefix + "log_");

        // Load the lastupdated_datetime related settings.    
        defClause = "bet";
        var temp = getValue(prefix + "upd_defClause");
        if (temp != "" && clauseInfo[temp]) defClause = temp;
        
        clause = changeClauseList("lastupdated_datetime", prefix + "upd_clauseList", defClause)
	    ctrl = document.getElementById(prefix + "upd_dataSection");
	    dataType = 4;
	    formHTML(dataType, clause, ctrl, prefix + "upd_")

        // Load the due_datetime related settings.    
        defClause = "bet";
        var temp = getValue(prefix + "due_defClause");
        if (temp != "" && clauseInfo[temp]) defClause = temp;
        
        clause = changeClauseList("due_datetime", prefix + "due_clauseList", defClause)
	    ctrl = document.getElementById(prefix + "due_dataSection");
	    dataType = 4;
	    formHTML(dataType, clause, ctrl, prefix + "due_")
	}
	catch (e){}
}

function loadAdvancedView()
{
	var fieldList = document.getElementById("fieldList");
	var i = 0;
	for(var field in fieldInfo)
	{
		if (field == "clone") continue;
		fieldList.length = i + 1;
		fieldList.options[i].value = field;
		fieldList.options[i].text = fieldInfo[field]["displayName"];
		i = i + 1;
	}
	sortDropDown(fieldList);
	
	// Select the first one.
	fieldList.options[0].selected = true;
	
	var fieldName = fieldList.options[0].value;
	var dataType = Number(fieldInfo[fieldName]["dataType"]);
	var clause = changeClauseList(fieldName, "clauseList", "");
	changeDescList(fieldName, "spnDescList");
	showDataSection(dataType, clause);
}

function onChangeFieldList(e)
{
	var event = (document.all) ? window.event : e;
	var fieldList = document.getElementById("fieldList");
	var fieldName = fieldList.value;
	var dataType = Number(fieldInfo[fieldName]["dataType"]);
	var clause = changeClauseList(fieldName, "clauseList", "");

	changeDescList(fieldName, "spnDescList");
	showDataSection(dataType, clause);
}

function onChangeClauseList(event)
{
	var event = (document.all) ? window.event : e;
	var fieldList  = document.getElementById("fieldList");
	var clauseList = document.getElementById("clauseList");

	var fieldName = fieldList.value;
	var dataType = Number(fieldInfo[fieldName]["dataType"]);
	var clause = clauseList.value;

	showDataSection(dataType, clause);
}

function onChangeNormalClauseList(event, fieldName, clauseListCtrl, dataSection, prefix)
{
	var event = (document.all) ? window.event : e;
	var clauseList = document.getElementById(clauseListCtrl);

	var dataType = Number(fieldInfo[fieldName]["dataType"]);
	var clause = clauseList.value;

	var ctrl = document.getElementById(dataSection);
	formHTML(dataType, clause, ctrl, prefix);
}

function changeDescList(fieldName, spnDescList)
{
    var primaryDesc = getPrimaryDesc(fieldName);
	var allowedDescList = fieldInfo[fieldName]["descList"];
	var list = allowedDescList.split(",");
	var dispList = new Array();
	var primaryDescSpan = "";
	for(var i = 0; i < list.length; i++)
	{
		var desc = list[i];
		if (desc == primaryDesc)
		{
		    primaryDescSpan = "<u>" + desc + "</u>";
		}
		else
		{
		    dispList[i] = desc;
		}
	}
	/*
	 * Always display the primary descriptor at the beginning.
	 * This logic is not good but since the list is always small, 
	 * assuming this to be okay.
	 */
	dispList.reverse();
	dispList.push(primaryDescSpan);
	dispList.reverse();
	setInnerHtml(spnDescList, dispList.join(", "));
}

function changeClauseList(fieldName, clauseListCtrl, selectClause)
{
	var clauseList = document.getElementById(clauseListCtrl);
	var allowedClauseList = dataTypeInfo[fieldInfo[fieldName]["dataType"]]["allowedClauseList"];
	var list = allowedClauseList.split(",");
	for(var i = 0; i < list.length; i++)
	{
		var clause = list[i];
		var clauseDisplayName = clauseInfo[clause];
		clauseList.length = i + 1;
		clauseList.options[i].value = clause;
		clauseList.options[i].text = clauseDisplayName;
		if (selectClause == clause || selectClause == clauseDisplayName)
		    clauseList.options[i].selected = true;
	}
	return clauseList.value;
}

function addToQuery(str)
{
	var queryCtrl = document.getElementById("advancedQuery");
	var query = queryCtrl.value;
	query = query + " " + str;
	queryCtrl.value = trim(query);
}

function showDataSection(dataType, clause)
{
	var ctrl = document.getElementById("dataSection");
	formHTML(dataType, clause, ctrl, "");
}

function formHTML(dataType, clause, ctrl, prefix)
{
	var nearestPath = getValue("nearestPath");
	var calGif = YAHOO.transbit.tbits.contextPath + "web/images/cal.gif";
	ctrl.innerHTML = "&nbsp;";
	
	var ih = "";
	
	switch (dataType)
	{
	case 1:
		break;
	case 2:
	case 3:
	case 4:
		if (clause == "on" || clause == "aft" || clause == "bef")
		{
		    var defValue = getValue(prefix + "defValue");
		    var defClause = getValue(prefix + "defClause");
		    if (defClause != clause) defValue = "";
			ih = 
			    "<input class='sx' id='" + prefix + "dtCtrl' name='" + prefix + "dtCtrl' type='text' style='width: 100px; margin-top: -1px;' value='" + defValue + "' />" + 
			    "<img id='" + prefix + "dtImg' name='" + prefix + "dtImg' src='" + calGif + "' style='cursor: hand; vertical-align: top;' />";
		    ctrl.innerHTML = ih;
		    setupCalendar(prefix + 'dtCtrl', prefix + 'dtImg');
			
		}
		else if (clause == "bet")
		{
		    var defFromValue = getValue(prefix + "defFromValue");
		    var defToValue = getValue(prefix + "defToValue");
		    var defClause = getValue(prefix + "defClause");
		    if (defClause != clause) 
		    {
		        defFromValue = "";
		        defToValue = "";
		    }
			ih = 
			    "<input class='sx' id='" + prefix + "dtCtrlFrom' name='" + prefix + "dtCtrlFrom' type='text' style='width: 100px; margin-top: -1px;'  value='" + defFromValue + "'/>" + 
			    "<img id='" + prefix + "dtImgFrom' name='" + prefix + "dtImgFrom' src='" + calGif + "' style='cursor: hand; vertical-align: top;' />" + 
			 	"<font class='sx'>&nbsp;&nbsp;and&nbsp;&nbsp;</font>" + 
			 	"<input class='sx' id='" + prefix + "dtCtrlTo' name='" + prefix + "dtCtrlTo' type='text' style='width: 100px; margin-top: -1px;'  value='" + defToValue + "'/>" + 
			 	"<img id='" + prefix + "dtImgTo' name='" + prefix + "dtImgTo' src='" + calGif + "' style='cursor: hand; vertical-align: top;' />" + 
				 "";
		    ctrl.innerHTML = ih;
		    setupCalendar(prefix + 'dtCtrlFrom', prefix + 'dtImgFrom');
		    setupCalendar(prefix + 'dtCtrlTo', prefix + 'dtImgTo');
		}
		else if (clause == "lnh" || clause == "lnd" || clause == "lnw" || clause == "lnm" || clause == "lny" ||
				 clause == "nnh" || clause == "nnd" || clause == "nnw" || clause == "nnm" || clause == "nny")
		{
		    var defValue = getValue(prefix + "defValue");
		    var defClause = getValue(prefix + "defClause");
		    if (defClause != clause) defValue = "";
			ih =
			    "<span class='b sx' style='vertical-align: middle;'>n:</span> " + 
			    "<input class='sx' id='" + prefix + "dtCtrl' name='" + prefix + "dtCtrl' type='text' style='width: 100px; margin-top: -1px;' value='" + defValue + "'/>"
		    ctrl.innerHTML = ih;
		}
		else if (clause == "iem" || clause == "inem")
		{
		    ctrl.innerHTML = "";
		}
		break;
	case 5:
	case 6:
		if (clause == "bet")
		{
			ih = 
			    "<input class='sx' id='dtCtrlFrom' name='dtCtrlFrom' type='text' style='width: 150px; margin-top: -1px;' />" + 
				"<span class='sx'>&nbsp;and&nbsp;</span></td>" + 
				"		<input class='sx' id='dtCtrlTo' name='dtCtrlTo' type='text' style='width: 150px; margin-top: -1px;' />" + 
				"";
		    ctrl.innerHTML = ih;
		}
		else
		{
			ih = "<input class='sx' id='dtCtrl' name='dtCtrl' type='text' style='width: 150px; margin-top: -1px;' />";
		    ctrl.innerHTML = ih;
		}
		break;
	case 7:	
	case 8:
	case 11:
		{
			ih = "<input id='txtCtrl' name='txtCtrl' type='text' class='sx' style='width: 98%; margin-top: -1px;' />";
		    ctrl.innerHTML = ih;
		}
		break;
	case 9:
		{
			var fieldName = document.getElementById("fieldList").value;
			var typeList = typeInfo[fieldName];
			if (typeList)
			{
				var length = typeList.length + 1;
				length = (length > 4) ? 4: length;
				ih = "<SELECT class='sx' id='typeCtrl' name='typeCtrl' multiple size='" + length + "'>";
				ih = ih + "\n<OPTION value=''>All</OPTION>";
				for(var i=0; i < typeList.length; i++)
				{
				    var name = typeList[i]["name"];
				    var displayName =  typeList[i]["displayname"];
				    var selected = typeList[i]["selected"];
				    ih = ih + "\n<OPTION value='" + name + "'";
				    if (selected == "true")
				    {
				        ih = ih + " SELECTED ";
				    }
				    ih = ih + ">" + displayName + "</OPTION>";
				}
		        ctrl.innerHTML = ih;
			}
			else 
			{
				// Ideally, an exception should be logged with the system which would send mails to the dev team.
			}
		}
		break;
	case 10:
		{
		    if (clause == "iem" || clause == "inem")
		    {
		        ctrl.innerHTML = "";
		    }
		    else
		    {
			    ih = "<input id='mvCtrl' name='mvCtrl' type='text' class='sx wickEnabled' style='width: 98%; margin-top: -1px;' />";
		        ctrl.innerHTML = ih;
		        try{registerInputListener("mvCtrl");}catch (e) {}
		    }
		}
		break;
	}
}

function onClickAddQuery()
{
	var queryCtrl  = document.getElementById("advancedQuery");
	var fieldCtrl  = document.getElementById("fieldList");
	var clauseCtrl = document.getElementById("clauseList");
	
	var query       = queryCtrl.value;
	var fieldName   = fieldCtrl.value;
	var desc        = getPrimaryDesc(fieldName);
	var clause      = clauseCtrl.value;
	
	var filterValue = "";
	var dataType = Number(fieldInfo[fieldName]["dataType"]);
	switch (dataType)
	{
	case 1:	// Boolean Query.
	        queryCtrl.value = trim(query + " " + desc + ":" + clause); 
	        break;
	case 2:	// Date Query.
	case 3:	
	case 4:	
			if (clause == "iem")   queryCtrl.value = trim(query) + " -has:" + desc;
			else if (clause == "inem")  queryCtrl.value = trim(query) + "  has:" + desc;
	        else queryCtrl.value = trim(query + " " + getDateQuery(desc, "", clause)); 
	        break;
	case 5:	// Numeric Query
	case 6:	
	        queryCtrl.value = trim(query + " " + getIntQuery(desc, clause)); 
	        break;
	case 7:	case 8: case 11:// String/Text Query
		{
			var ctrl = document.getElementById("txtCtrl");	
			var value = trim(ctrl.value);
			if (value == "")
			{
				alert("Specify the filter value.");
				return;
			}
			var arrValues = value.split(" ");
			var filterValue = "";
			if (clause == "ep")		    query = query + " " + desc + ":\"" + value + "\"";
			else if (clause == "alw")   query = query + " " + desc + ":(" + arrValues.join(" AND ") + ")";
			else if (clause == "anw")   query = query + " " + desc + ":(" + arrValues.join(" OR ") + ")";
			else if (clause == "ntw")   query = query + " -" + desc + ":(" + arrValues.join(" OR ") + ")";
			else                        alert(clause);
			queryCtrl.value = trim(query);
		}
		break;
	case 9: // Type Query.
		{
			var ctrl = document.getElementById("typeCtrl");
			var temp = trim(getSubQuery(desc, getTypeList("typeCtrl")));
			if (temp && temp != "") 
			{
				if (clause == "in")
				    query = trim(query) + " " + temp;
				else
				    query = trim(query) + " -" + temp;
			}
			queryCtrl.value = trim(query);
		}
		break;
	case 10: // Multi Value Query.
		{
			if (clause == "iem")   query = query + " -has:" + desc;
			else if (clause == "inem")  query = query + "  has:" + desc;
			else
			{
			    var ctrl = document.getElementById("mvCtrl");
			    filterValue = new String(ctrl.value);
			    filterValue = trim(filterValue);
			    if (filterValue == "")
			    {
				    alert("Specify the filter value.");
				    return;
			    }
			    filterValue = getUserList("mvCtrl");
			    if (filterValue.indexOf(",") > 0 || filterValue.indexOf(";") > 0)
			        filterValue = "(" + filterValue + ")";
    			
			    ctrl.value = "";
			    if (clause == "in")         query = query + " "  + desc + ":" + filterValue;
			    else if (clause == "ni")    query = query + " -" + desc + ":" + filterValue;
			    else if (clause == "inmem") query = query + " "  + desc + ":membersof:expand:" + filterValue;
			    else if (clause == "nimem") query = query + " -" + desc + ":membersof:expand:" + filterValue;
			}
			queryCtrl.value = trim(query);
		}
		break;
	}
}

function collapseMyRequestsBA(sysPrefix)
{
    var ctrlId = sysPrefix + "_searchResults";
    var spnId = sysPrefix + "_collapseSpan";
    var table = document.getElementById(ctrlId);
    var spn = document.getElementById(spnId);
    if (table != null)
    {
        var rows = table.rows;
        var len = rows.length;
        var text = trim(spn.innerHTML);
        var flag = (text == "Collapse") ? "true" : "false";
        var display = (text == "Collapse") ? "none" : "";
        var innerHTML = (text == "Collapse") ? "Expand" : "Collapse";
        for (var i = 1; i < len; i++)
        {
            var rowName = rows[i].className;
            if (rowName.indexOf("hiddenRow") < 0)
            {
                rows[i].style.display = display;
            }
        }
        if (spn != null)
        {
            spn.innerHTML = innerHTML;
        }
        var url = YAHOO.transbit.tbits.contextPath + "my-requests?collapse=" + flag + "&areas=" + sysPrefix + "&now=" + getCurrentTime();
        var id = sysPrefix + "_MyRequests";
        executeAsyncGetRequest(url, function() {});
    }
    else
    {
        alert(table);
    }
}

function removeMyRequestsBA(sysPrefix)
{
    var url = YAHOO.transbit.tbits.contextPath + "my-requests?show=false&areas=" + sysPrefix + "&now=" + getCurrentTime();
    var id = sysPrefix + "_MyRequests";
    executeAsyncGetRequest(url, function() {});
    setInnerHtml(id, "");
}

function generateReport()
{
    var location = YAHOO.transbit.tbits.contextPath + "tbits-reports?query=" + getQuery() + "&ba=" + getValue(sysPrefix);
    window.open(location);
    return;
}

function copySearchURLToCB()
{
    var query = getQuery();
    var currentView = getIntValue("sv");
    var url = YAHOO.transbit.tbits.contextPath + "search/" + getValue("sysPrefix") + "?q=" + query + "&sv=" + currentView;
    //var message = "Copied '" + getQuery() + "' at " + getDate();
    var message = "Copied at " + getDate();
    var table = 
        "<table width='350px' class='results' onmouseover='notInToolTip = null;' cellpadding='0' cellspacing='0'" + 
        "    name='tabTsDQL' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">" +
        "   <colgroup>" + 
        "   <col width='50px'>" + 
        "   <col width='300px'>" + 
        "   </colgroup>" + 
        "   <thead>" +  
        "    <tr onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">" + 
        "        <td style='text-align:center; color:black' colspan='2' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">URL copied to clipboard</td>" + 
        "    </tr>" + 
        "   </thead>" +  
        "   <tbody>" +  
        "    <tr>" + 
        "        <td class='ncol b' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">URL</td>" + 
        "        <td class='ncol' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">" + url + "</td>" + 
        "    </tr>" + 
        "    <tr>" + 
        "        <td class='ncol b' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">DQL</td>" + 
        "        <td class='ncol' onmouseout=\"closeTarget(event,'TABLE', 'tabTsDQL');\">" + 
        (query == "" ? "&nbsp;" : query) + "</td>" + 
        "    </tr>" + 
        "   </tbody>" +  
        "</table>" + 
        "";
    if (currentView == NORMAL_VIEW)
    {
        setInnerHtml("nTsCopyURL", message);
        showElementInline("nCopyURL");
        setInnerHtml("normal_tsDQL", table);
    }
    else
    {
        setInnerHtml("aTsCopyURL", message);
        showElementInline("aCopyURL");
        setInnerHtml("adv_tsDQL", table);
    }

    window.clipboardData.setData("text", url);
}

function showCompleteTree(sysPrefix, requestId)
{
    var query = "tree:" + requestId;
    var url = YAHOO.transbit.tbits.contextPath + "search/" + sysPrefix + "?query=" + query + "&sv=" + ADVANCED_VIEW;
    window.open(url);
}

/*
 * Returns absolute top of given control.
 */
function gat(ctrl)
{
	var eT = 0;
	for(var p=ctrl; p&&p.tagName!='BODY'; p=p.offsetParent)
	{
		eT += p.offsetTop;
	}
	return eT;
}

/*
 * Returns absolute left of given control.
 */
function gal(ctrl)
{
	var eL = 0;
	for(var p=ctrl; p&&p.tagName!='BODY'; p=p.offsetParent)
	{
		eL += p.offsetLeft;
	}
	return eL;
}

function storeValuesInCookies()
{
    var cookieName = "";
    var cookieValue = "";
    var expireTime = 1000;

    var currentView = getIntValue("sv");
    
    switch (currentView)
    {
//        case SIMPLE_VIEW:
//            cookieName = 'simpleQuery';
//            cookieValue = getValue(cookieName);
//            setCookie(cookieName, cookieValue, expireTime);
//            break;
        case NORMAL_VIEW:
            /* Types */
            cookieName = 'category_id';
            cookieValue = getTypeList(cookieName);
            if (cookieValue == "") cookieValue = "all";
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'status_id';
            cookieValue = getTypeList(cookieName);
            if (cookieValue == "") cookieValue = "all";
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'severity_id';
            cookieValue = getTypeList(cookieName);
            if (cookieValue == "") cookieValue = "all";
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'request_type_id';
            cookieValue = getTypeList(cookieName);
            if (cookieValue == "") cookieValue = "all";
            setCookie(cookieName, cookieValue, expireTime);

            /* Users */
            cookieName = 'logger_ids';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'assignee_ids';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'subscriber_ids';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);

            cookieName = 'user_id';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);
            
            cookieName = 'cbLast_checked';
            if (document.getElementById("cbLast").checked == true)
            {
                cookieValue = "checked";
            }
            else
            {
                cookieValue = "unchecked";
            }
            setCookie(cookieName, cookieValue, expireTime);
            
            /* Text */
            cookieName = 'normalDesc';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);
            
            cookieName = 'filter';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);
            break;

        case ADVANCED_VIEW:
            cookieName = 'advancedQuery';
            cookieValue = getValue(cookieName);
            setCookie(cookieName, cookieValue, expireTime);
            break;
    }
}

/*
 * Get the details of this BA.
 */
function getBADetails()
{
	closeToolTip();
	notInToolTip = undefined;
    var baHelp = getInnerHtml("baHelp");
    if (baHelp == "")
    {
        var sysPrefix = getValue("sysPrefix");
        var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?sysPrefix=" + sysPrefix + "&baHelp=true&now=" + getCurrentTime();
        var content = executeHttpRequest('GET', url, true);
        setInnerHtml("baHelp", content);
        baHelp = content;
    }
    return baHelp;
}

/*
 * Export to excel my requests for given BA.
 */
function emr(sysPrefix)
{
    var filter = getMyRequestsFilter();
    if (filter == 0)
    {
        alert(FILTER_MANDATORY);
        return "";
    }
    var url = YAHOO.transbit.tbits.contextPath + "my-requests/" + escape(sysPrefix) + 
                "?sysPrefix=" + escape(sysPrefix) + 
                "&export=excel" + 
                "&filter=" + filter
                "&now=" + getCurrentTime();
    window.open(url);
}

function getDate()
{
    var date = new Date();
    var hour = date.getHours();
    var ampm = "AM";
    if (hour == 12)
    {
        ampm = "PM";
    }
    if (hour > 12)
    {
        hour = hour %12;
        ampm = "PM";
    }
    if (hour < 12)
    {
        hour = (hour < 10) ? "0" + hour : hour;
        if(hour == "00") hour = 12;
    }
    
    var min = date.getMinutes();
    min = (min < 10) ? "0" + min : min;
       
    return hour + ":" + min + " " + ampm;
}


/*
 * This method sorts a drop down list based on its text in alphabetical order.
 */
function sortDropDown(member)
{
	var len = member.length;
	for (var a=0;a<len-1;a++)
	{
		for (var b=a+1;b<len;b++)
		{
			if(member.options[a].text > member.options[b].text)
			{
				var tempText = member.options[a].text;
				var tempVal =  member.options[a].value;
				member.options[a].text =member.options[b].text;
				member.options[a].value =member.options[b].value;
				member.options[b].text = tempText;
				member.options[b].value = tempVal;
			}
		}
	}	
}

/*
 * Returns the id's of selected items in the search results in the form of an array
 */
function getSelected(){
	var requestList = getValue("requestList");
	return requestList;
}

var afterBulkUpdate = new Array();
