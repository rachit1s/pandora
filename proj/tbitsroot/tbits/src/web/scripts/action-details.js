 //
 // This variable 'pageCompletelyLoaded ' will be set to true in onLoadActionDetails() function as last statement.
 // This is used in getSubject() to avoid wtt.js based smartlink tooltips to pre-fetch subject.
 // Pre-fetching subject can make the page with cross-site smartlinks hang, when cross-site connection is bad
 //  
  var pageCompletelyLoaded = false;
        


/*
 * Method to open Admin page.
 */
function openAdmin()
{
    var location = YAHOO.transbit.tbits.contextPath + "web/admin-frame.html#?q=" + document.getElementById("sysPrefix").value;
    var features = "status=no,location=no,menubar=no,toolbar=no,resizable=yes,scrollbars=yes";
    window.open(location, "_blank", features);
}

function onLoadActionDetails()
{
    var sysPrefix = document.getElementById('sysPrefix').value;
	var requestId = document.getElementById('requestId').value;
	var singleIEWindow = document.getElementById("singleIEWindow").value;
	var smartTag = sysPrefix + "_" + requestId;

    var windowName = sysPrefix;
	if (singleIEWindow == "false")
		windowName = smartTag;
    var ctrl = document.getElementById("updateDiv");
    if (ctrl != null)
    {
        if (ctrl.style.display != "none")
	  	    window.name = windowName + "_update";
        else
	        window.name = windowName;
	}
	else
	{
	    window.name = windowName;
	}
	window.focus();
    if (window.opener != null)
    {
        try
        {
            var requestId = document.getElementById("requestId").value;
            window.opener.markAsRead(requestId);
        }
        catch (e)
        {
            // Ignore the warning.
        }
    }
    
    var isLocked = document.getElementById('isLocked').value;
	if (isLocked != "true")
	{
	   onLoadBody();
	} 
	pageCompletelyLoaded = true;
}

function expandAll()
{
	var maxAction = document.getElementById('maxAction').value;
	if (document.getElementById('extendedHeader'))
		{
			document.getElementById('extendedHeader').style.display = '';
		}
	if (document.getElementById('summary_id'))
		{	
			document.getElementById('summary_id').style.display = '';
		}	
	for (var i = 2; i < maxAction; i++)
	{
		var name = 'actionLog' + i;
		document.getElementById(name).style.display = '';
	}
		
	var expandHtml = '<a id="spnCollapse" class="l cb" href="javascript:collapseAll();"' +
			'onmouseover="textMouseOver(event,\'Collapse Header/Action Log\');" onmouseout="textMouseOut();">' +
		'<img src="' + nearestPath + 'web/images/collapse.gif" class="am"/>&nbsp;Collapse All</a>';
								
	document.getElementById('expand').innerHTML = expandHtml;
}

function collapseAll()
{
	var maxAction = document.getElementById('maxAction').value;
	if (document.getElementById('extendedHeader'))
		{
			document.getElementById('extendedHeader').style.display = 'none';
		}
	if (document.getElementById('summary_id'))
		{	
			document.getElementById('summary_id').style.display = 'none';
		}	
	for (var i = 2; i < maxAction; i++)
	{
		var name = 'actionLog' + i;
	document.getElementById(name).style.display = 'none';
	}
	
	var expandHtml = '<a id="spnExpand" class="l cb" href="javascript:expandAll();"' +
			'onmouseover="textMouseOver(event,\'Expand Header/Action Log\');" onmouseout="textMouseOut();">' +
		'<img src="' + nearestPath + 'web/images/expand.gif" class="am"/>&nbsp;Expand All</a>';
			
	document.getElementById('expand').innerHTML = expandHtml;
}

function showHide(id, imgid, plusImgUrl, minusImgUrl)
{
	if (document.getElementById(id).style.display == '')
	{
		document.getElementById(id).style.display = 'none';
		if(imgid)
		{
			document.getElementById(imgid).src = plusImgUrl;
		}
	}
	else
	{
		document.getElementById(id).style.display = '';
		if(imgid)
		{
			document.getElementById(imgid).src = minusImgUrl;
		}
	}	
}
function sortActions(sortOrder)
{
	var maxAction = document.getElementById('maxAction').value;
	
	if (sortOrder == "asc") 
	{
		var details = new Array();
		for (i = 1; i <=  maxAction; i++) 
		{
			var name = 'action' + i;
			details.push('<div id="' + name + '" style="border: 2px solid white">');
			details.push(document.getElementById(name).innerHTML) ;
			details.push('</div>');
		}
		
		document.getElementById('actionDetails').innerHTML=details.join('');
		
		var historyHtml = '<a class="l b cw" href="javascript:sortActions(\'desc\');"' +
			'onmouseover="textMouseOver(event,\'Sort '+ requestCaption +' History\');" onmouseout="textMouseOut();">' + requestCaption +
		' History&nbsp;&nbsp;<img src="' + nearestPath + 'web/images/up-white.gif" class="am" ' +
		'onmouseover="textMouseOver(event,\'Sort '+ requestCaption +' History\');" onmouseout="textMouseOut();" /></a>';
				
		document.getElementById('history').innerHTML = historyHtml;
	}
	else if (sortOrder == "desc") 
	{
		var details = new Array();
		for (i=maxAction; i >0 ;i--) 
		{
			var name = 'action' + i;
			details.push('<div id="' + name + '" style="border: 2px solid white">');
			details.push(document.getElementById(name).innerHTML) ;
			details.push('</div>');
		}
	
		document.getElementById('actionDetails').innerHTML=details.join('');
		
		var historyHtml = '<a class="l b cw" href="javascript:sortActions(\'asc\');"' +
			'onmouseover="textMouseOver(event,\'Sort '+ requestCaption +' History\');" onmouseout="textMouseOut();">' + requestCaption +' History&nbsp;&nbsp;<img src="' + nearestPath + 'web/images/down-white.gif" class="am" ' + 
		'onmouseover="textMouseOver(event,\'Sort '+ requestCaption +' History\');" onmouseout="textMouseOut();" /></a>';
		
		document.getElementById('history').innerHTML = historyHtml;
	}
}

function getRelativePath(location, target)
{
//	var parentLocation = new String(location);
//	if (parentLocation.charAt(parentLocation.length - 1) == '/')
//	{
//		parentLocation = parentLocation.substring(0, parentLocation.length - 1);
//	}
//	
//	var arrParts = parentLocation.split("/");
//	var action = "";
	action = YAHOO.transbit.tbits.contextPath + target;
	return action;
}

function callGoRequest()
{
	var sysPrefix =	document.getElementById("sysPrefix").value;
	var requestId = document.getElementById('requestBox').value;
	// Check if the requestId field contains a single ID.
	if(requestId.match(/^[0-9]+$/) != null)
	{
		//var parentLocation = new String(window.location);
		//var index = parentLocation.lastIndexOf('/');
		//parentLocation = parentLocation.substring(0, index);
		var redirection = YAHOO.transbit.tbits.contextPath +  "Q/" + sysPrefix +  "/" +  requestId;
		window.location = redirection;
	}	
	// Check if the request contains prefix#Id 
	else if (requestId.indexOf("#") > 0) 
    {
		var redirection = YAHOO.transbit.tbits.contextPath + "Q/" + requestId.replace('#','/');
		window.location = redirection;
    } 
}
	
function openRequest(sysPrefix, requestId, sessionId, pageCounter)
{
	var obj = document.createElement("form");
	var redirection = getRelativePath(window.location, "Q") + "/" + sysPrefix + "/" + requestId;
	
	obj.name="dynFrm";
	obj.id="dynFrm";
	obj.action = redirection;
	obj.method="post";
	
	obj.appendChild(createInputObject("sessionId", sessionId));
	obj.appendChild(createInputObject("pageCounter", pageCounter));
	
	document.body.appendChild(obj);
	dynFrm.submit();
	document.body.removeChild(obj);

}

function createInputObject(name, value)
{
	var obj = document.createElement("input");
	obj.name = name;
	obj.id = name;
	obj.value = value;
	obj.type = "hidden";
	return obj;
}

function nop()
{
	return;
}

function onClickOptions()
{
    var sysPrefix = document.getElementById("sysPrefix").value;
    var location = YAHOO.transbit.tbits.contextPath + "options/" + sysPrefix;
    
    var features = "status=no," + 
                    "location=no," + 
                    "menubar=no," + 
                    "toolbar=no," + 
                    "resizable=yes," + 
                    "scrollbars=yes," + 
                    "width=750px," + 
                    "height=700px, top:-1,left:100,";
    var child = window.open(location, "OptionsPage", features);
    child.focus();
    return;
}
            
function editContent(url)
{
	var object = new Object();
	object.returnValue = null;

	var newUrl = url + "&now=" + (new Date()).getTime();
	var features="dialogWidth: 620px; dialogHeight: 760px; resizable: yes; status: no"
	var width = "680";
	var height= "760";
	var status= "no";
	var resize= "yes";
	openModalDialog(newUrl, object,
	                "width", width,
	                "height", height,
	                "status", status,
	                "resize", resize);
	if (isIE == true)
	{
        if (object.returnValue != null && object.returnValue == 1)
        {
	        window.location = window.location.href; 
	    }
	}
}

/*
 * Method to transfer the request.
 * Written by Giris.
 */		
function transferRequest()
{
    var sysPrefix = document.getElementById("sysPrefix").value;
    var requestId = document.getElementById("requestId").value;

    var url = YAHOO.transbit.tbits.contextPath + "transfer-request/" + sysPrefix + 
                "?sysPrefix=" + sysPrefix + 
                "&requestId=" + requestId + 
                "&now=" + getCurrentTime();
                
	var object = new Object();
	object.returnValue = false;
	
	var width = "920";
	//var height= "728";
	var status= "no";
	var resize= "yes";
	var scroll = "yes";
	openModalDialog(url, object,
	                "width", width,
	                "status", status,
	                "resize", resize,
	                "scroll", scroll
	                );
	    
	if (isIE == true)
	{
        if (object.returnValue != null && object.returnValue == 1)
        {
	        window.location = window.location.href; 
	   }
	}
}

// Variable to store tool-tip Id 
var textToolTipID = "";

function textMouseOver(event,txt)
{
	try
	{
		textToolTipID =	domTT_activate(this, event, 'content', txt, 'statusText', txt, 'closeAction', 'remove', 'delay', 0);
	}
	catch(e)
	{
	}
}

function textMouseOut()
{
	try
	{
		if (textToolTipID != null && textToolTipID != "") domTT_deactivate(textToolTipID);	
	}
	catch(e)
	{
	}
}


function onKeyUpBody(e)
{
    var event = (document.all) ? window.event : e;
    var keyCode = (document.all) ? event.keyCode : event.which;
    var ctrlKey = (event.ctrlKey || event.ctrlLeft);
    var altKey = (event.altKey || event.altLeft);
    var 
        KEY_A = 65,
        KEY_H = 72,
        KEY_M = 77,
        KEY_O = 79,
        KEY_R = 82,
        KEY_S = 83,
        KEY_U = 85,
        KEY_X = 88,
        KEY_Y = 89;
        KEY_RIGHT_ARROW = 39,
        KEY_LEFT_ARROW = 37;
    if (ctrlKey == true && altKey == true)
    {
        if (keyCode == KEY_A)
        {
            var ctrl = document.getElementById("spnAddRequest");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_H)
        {
            var ctrl = document.getElementById("spnHelp");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_M)
        {
            var ctrl = document.getElementById("spnMyRequests");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_O)
        {
            var ctrl = document.getElementById("spnOptions");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_R)
        {
            var ctrl = document.getElementById("requestBox");
            if (ctrl)
            {
                ctrl.focus();
            }
            
        }
        else if (keyCode == KEY_S)
        {
            var ctrl = document.getElementById("spnSearch");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_U)
        {
            var ctrl = document.getElementById("spnUpdateRequest");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_X)
        {
            var ctrl = document.getElementById("spnExpand");
            if (ctrl)
            {
                ctrl.click();
            }
            else
            {
                ctrl = document.getElementById("spnCollapse");
                if (ctrl)
                {
                    ctrl.click();
                }
            }
        }
        else if (keyCode == KEY_Y)
        {
            toggleHighlighting();
        }
        else if (keyCode == KEY_LEFT_ARROW)
        {
            var ctrl = document.getElementById("prevLink");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        else if (keyCode == KEY_RIGHT_ARROW)
        {
            var ctrl = document.getElementById("nextLink");
            if (ctrl)
            {
                ctrl.click();
            }
            
        }
        /*
        else
        {
            alert(keyCode);
        }
        */
    }
}

function onFocusReqBox()
{
    var ctrl = document.getElementById("requestBox");
    if (ctrl)
    {
        ctrl.value = "";
        ctrl.style.color = 'black';
    }
}

function  getSubject(server, sysPrefix, requestId)
{

//
// This is to avoid pre-fetching of subject by wtt.js 
//
 if (pageCompletelyLoaded == false)
	return "";
	
   var url;
   
   if (server == "/")
   {
		url = getRelativePath(window.location, "search-util/") + sysPrefix + 
              "?subject=true&requestId=" + requestId + "&sysPrefix=" + 
              sysPrefix;
   }
   else 
   {
         url = server + "search-util/" + sysPrefix  +
               "?subject=true&requestId=" + requestId + "&sysPrefix=" + 
              sysPrefix;
   }

	var output =  executeHttpRequest("GET",url,false);
 
 return output;
} 



function toggleHighlighting()
{
    var ctrl = document.getElementById("highlight");
    var highlight = "";
    if (ctrl) highlight = ctrl.value;
    
    var arr = document.getElementsByTagName("span");
    if (arr)
    {
        var len = arr.length;
        if (highlight == 'true')
        {
            ctrl.value = "false";
            for (var i=0; i < len; i++)
            {
                var name = arr[i].name;
                if (name && trim(name) == "matchedText")
                {
                    arr[i].className = "";
                }
            }
        }
        else
        {
            ctrl.value = "true";
            for (var i=0; i < len; i++)
            {
                var name = arr[i].name;
                if (name && trim(name) == "matchedText")
                {
                    arr[i].className = "cy";
                }
            }
        }
    }
}

function alertObj(obj)
{
    var desc = "";
    for (i in obj)
    {
        desc = desc + "\n" + i + "=>" + obj[i].name;
    }
    alert(desc);
}
/**
 * For displaying the attachments in datatable.
 */

 function displayAttInTable() {
	var attsContainer = document.getElementById('filesContainer');
	var sysPrefix = getSysPrefix();
	var requestId = getRequestId();
	
	for ( var name in requestUpdateFiles) {
		var val = requestUpdateFiles[name];
		if (typeof (val) == 'function')
			continue;

		var fieldEnvelope = document.createElement('div');
		var labelEnvelope = document.createElement('span');
		var img = document.createElement('img');
		img.src = YAHOO.transbit.tbits.contextPath + 'web/images/plus.gif';
		img.id = name + "NodeImg";
		var displayName = ' ' + requestUpdateFiles[name].fieldDisplayName + ' (' + val.files.length + ') ';
		var caption = document.createTextNode(displayName);
		var attachmentContainer = document.createElement('div');
		attachmentContainer.style.display = "none";
		Dom.addClass(attachmentContainer, 'filestable');

		var tableHolder = document.createElement('div');

		tableHolder.id = name + "TableHolder";
		var downloadAllButton = document.createElement('button');
		downloadAllButton.innerHTML = "Download Selected";
		Dom.addClass(downloadAllButton, 'downloadbutton');

		var copySelButton = document.createElement('button');
		copySelButton.innerHTML = "Copy Selected";
		Dom.addClass(copySelButton, 'downloadbutton');
		copySelButton.id = name + "copySelButton";

		var mergeButton = document.createElement('button');
		mergeButton.innerHTML = "Merge Selected PDF Files";
		Dom.addClass(mergeButton, 'downloadbutton');
		mergeButton.id = name + "mergeButton";
		
		var zipButton = document.createElement('button');
		zipButton.innerHTML = "Download all as zip";
		Dom.addClass(zipButton, 'downloadbutton');
		zipButton.id = name + "zipButton";
		
		var frame = document.createElement('iframe');
		frame.id = name + "downloadFrame";
		frame.style.display = "none";
		
		//assemble
		attsContainer.appendChild(fieldEnvelope);
		fieldEnvelope.appendChild(labelEnvelope);
		labelEnvelope.appendChild(img);
		labelEnvelope.appendChild(caption);
		fieldEnvelope.appendChild(attachmentContainer);
		attachmentContainer.appendChild(tableHolder);
		attachmentContainer.appendChild(copySelButton);
		attachmentContainer.appendChild(mergeButton);
		attachmentContainer.appendChild(zipButton);
		attachmentContainer.appendChild(frame);
		
		Event.on(labelEnvelope, 'click', showHideNode, {
			'img' : img,
			'container' : attachmentContainer
		});
		displayAttInTableForDiv(tableHolder.id, val.files, sysPrefix, requestId, val.fieldId, name);
	}
}
 
function showHideNode(e, o) {
	if (o.container.style.display == "") {
		o.container.style.display = "none";
		o.img.src = YAHOO.transbit.tbits.contextPath + 'web/images/plus.gif';
	} else {
		o.container.style.display = "";
		o.img.src = YAHOO.transbit.tbits.contextPath + 'web/images/minus.gif';
	}
}
function selectAllRows(e, o) {
	//{checkbox: cbId, table: singleSelectDataTable}

	var selectAllCB = document.getElementById(o.checkbox);
	var val = selectAllCB.checked;
	var rs = o.table.getRecordSet();
	for ( var i = 0; i < rs.getLength(); i++) {
		var rec = rs.getRecord(i);
		rs.updateRecordValue(rec, "isSelected", val);
	}
	o.table.render();
}
function getRequestId()
{
	
   return document.getElementById("requestId").value;
}
function getSysPrefix()
{
	return document.getElementById("sysPrefix").value;
}
function displayAttInTableForDiv(divId, myfiles, sysPrefix, requestId, fieldId, name) {
	var cbId = divId + 'SelectAllCB';
	//create the skeleton
	var sizeFormatter = function(elCell, oRecord, oColumn, oData) {
		if (oData > (1024*1024))
			elCell.innerHTML = (oData/(1024*1024)).toFixed(0) + " MB";
		else if (oData < (1024*1024))			
			elCell.innerHTML = (oData/1024).toFixed(0) + " KB";
		if (oData < 1024)
			elCell.innerHTML = oData + " B";				
	};

	var myColumnDefs = [ {
		key : "isSelected",
		label : '<input type="checkbox" id="' + cbId + '"/>',
		sortable : false,
		formatter : "checkbox"
	}, {
		key : "name",
		label : "File Name",
		sortable : true
	}, {
		key : "size",
		label : "Size",
		sortable : true,
		formatter: sizeFormatter
	}, {
		key : "progress",
		label : "",
		sortable : false
	} ];

	var myDataSource = new YAHOO.util.DataSource( []);
	myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	myDataSource.responseSchema = {
		fields : [ "isSelected", "id", "name", "created", "modified", "type",
				"size", "progress", "status", "requestFileId", "repoFileId" ]
	};

	var singleSelectDataTable = new YAHOO.widget.DataTable(divId, myColumnDefs,
			myDataSource, {
				caption : null,
				selectionMode : "multiple",
				MSG_EMPTY : "No file is attached."
			});
	if (myfiles != null) {
		var isAnyFile = false;
		//Load request fields files
		for ( var i = 0; i < myfiles.length; i++) {
			var file = myfiles[i];
			var entry = {};
			entry.requestFileId = file.requestFileId;
			entry.newId = "-1-" + file.requestFileId;
			entry["isSelected"] = false;
			var url = YAHOO.transbit.tbits.contextPath + 'read-attachment/' + sysPrefix + '?request_id=' 
				+ requestId + '&request_file_id=' + file.requestFileId + '&field_id=' + fieldId + '&saveAs=true';
			entry["progress"] = '<a href="'+ url + '"> Download </a> ';// | <a href=""> View In Browser </a> | <a href="">Download as zip</a>';

			if ((file.name == null) || (file.name.length == 0))
				continue;

			entry.name = file.name;
			if (file.size == null)
			{
				file.size = 0;
			}
			
			entry.size = file.size;
			entry.requestFileId = file.requestFileId;
			entry.repoFileId = file.repoFileId;

			var record = singleSelectDataTable.getRecordSet().addRecord(entry);
			//this.filesList[entry.newId] = record;
			isAnyFile = true;
		}
		var attachmentContainer = document.getElementById("attachmentContainer");
		if (isAnyFile) {
			YAHOO.util.Dom.setStyle(attachmentContainer, "display", "none");
			singleSelectDataTable.render();
		} else {
			YAHOO.util.Dom.setStyle(attachmentContainer, "display", "none");
		}
	}
	Event.on(cbId, 'click', selectAllRows, {
		checkbox : cbId,
		table : singleSelectDataTable
	});
	
	var recordSetLen = singleSelectDataTable.getRecordSet().getLength();
	var count = 0;
	var url;
	var requestFileIdStr = "";
	var entry = {};

	var checkBox = null;
	if(document.getElementById(name + "TableHolder").childNodes.item(1).childNodes.item(3).childNodes.length > 0)
		checkBox = document.getElementById(name + "TableHolder").childNodes.item(1).childNodes.item(3).childNodes.item(0);
	
	if(checkBox != null)
	{
		var checkBoxId = checkBox.id
		var n = checkBoxId.substr(7,1) * 1;
		var selectedFiles = 0;
		checkBoxId = checkBoxId.substr(0,7);
		
		var oButton1 = new YAHOO.widget.Button( 
			name + "mergeButton",
	        {
	        	type: "push"
	        } 
	    ); 
		oButton1.on("click", onMergeMouseOver,{
			table : singleSelectDataTable,
			len : recordSetLen,
			baPrefix : sysPrefix,
			reqId : requestId,
			fId : fieldId,
			nme : name
		}); 
		
		var oButton3 = new YAHOO.widget.Button( 
				name + "zipButton",
		        {
		        	type: "push"
		        } 
		    ); 
			oButton3.on("click", onZipMouseOver,{
				table : singleSelectDataTable,
				len : recordSetLen,
				baPrefix : sysPrefix,
				reqId : requestId,
				fId : fieldId,
				nme : name
			}); 
		
		var oButton2 = new YAHOO.widget.Button( 
			name + "copySelButton",
	        {
	        	type: "push"
	        } 
	    ); 
		oButton2.on("click", onCopyClick,{
			table : singleSelectDataTable,
			len : recordSetLen,
			baPrefix : sysPrefix,
			reqId : requestId,
			fId : fieldId,
			fieldName:name
		});
	}
}	

function onMergeMouseOver(event, o){
	var requestFileIdStr = "";
	var checkBoxId = document.getElementById(o.nme + "TableHolder").childNodes.item(1).childNodes.item(3).childNodes.item(0).id;
	var n = checkBoxId.substr(7) * 1;
	checkBoxId = checkBoxId.substr(0,7);
	var count = 0;
	var selectedFiles = 0;
	var name = o.nme;
	while(count < o.len){
		entry = o.table.getRecordSet().getRecord(count).getData();
		if(document.getElementById(checkBoxId + (n+count)).cells.item(0).childNodes.item(0).childNodes.item(0).checked == true){
			requestFileIdStr = requestFileIdStr + "-" + entry["requestFileId"];
			selectedFiles++;
		}
		count++;
	}
	if(selectedFiles > 1){
		var url = YAHOO.transbit.tbits.contextPath + 'read-attachment/' + o.baPrefix + '?request_id=' 
		+ o.reqId + '&request_file_id_str=' + requestFileIdStr + '&field_id=' + o.fId + '&saveAs=true';
		window.open(url);
	}else{
		alert("Select atleast 2 PDF Files to merge");
		var url = "";
	}
}

function onZipMouseOver(event, o){
	var requestFileIdStr = "";
	var checkBoxId = document.getElementById(o.nme + "TableHolder").childNodes.item(1).childNodes.item(3).childNodes.item(0).id;
	var n = checkBoxId.substr(7) * 1;
	checkBoxId = checkBoxId.substr(0,7);
	var count = 0;
	var selectedFiles = 0;
	var name = o.nme;
	while(count < o.len){
		entry = o.table.getRecordSet().getRecord(count).getData();
		if(document.getElementById(checkBoxId + (n+count)).cells.item(0).childNodes.item(0).childNodes.item(0).checked == true){
			requestFileIdStr = requestFileIdStr + "-" + entry["requestFileId"];
			selectedFiles++;
		}
		count++;
	}
	if(selectedFiles > 0){
		var url = YAHOO.transbit.tbits.contextPath + 'read-attachment/' + o.baPrefix + '?request_id=' 
		+ o.reqId + '&request_file_id_str=' + requestFileIdStr + '&field_id=' + o.fId + '&saveAs=true&format=zip';
		window.open(url);
	}else{
		alert("Select atleast 1 File to download");
		var url = "";
	}
}

function onCopyClick(event, o){
	var requestFileIdStr = "";
	var sizeStr = "";
	var checkBoxId = document.getElementById(o.fieldName + "TableHolder").childNodes.item(1).childNodes.item(3).childNodes.item(0).id;
	var n = checkBoxId.substr(7) * 1;
	checkBoxId = checkBoxId.substr(0,7);
	var count = 0;
	var selectedFiles = 0;
	while(count < o.len){
		entry = o.table.getRecordSet().getRecord(count).getData();
		if(document.getElementById(checkBoxId + (n+count)).cells.item(0).childNodes.item(0).childNodes.item(0).checked == true){
			requestFileIdStr = requestFileIdStr + "-" + entry["requestFileId"];
			sizeStr = sizeStr + "-" + entry["size"];
			selectedFiles++;
		}
		count++;
	}
	
	var responseSuccess = function(o){ 
		alert("Selected files have been copied to the clipboard");
	}
	
	var responseFailure = function(o){ 
		alert("Could not copy files to the clipboard");
	}
	
	var callback = 
	{ 
	  success:responseSuccess, 
	  failure:responseFailure
	}; 
	
	var url = YAHOO.transbit.tbits.contextPath + 'copy-paste/' + o.baPrefix + '?request_id=' 
	+ o.reqId + '&request_file_id_str=' + requestFileIdStr + '&size_str=' + sizeStr + '&field_id=' + o.fId;
	if(selectedFiles > 0)
		var transaction = YAHOO.util.Connect.asyncRequest('GET', url, callback, null); 
	else
		alert("You have not selected any files to copy");
}
