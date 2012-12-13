var SPELL_CHECK_ABORTED = "tBits cannot spell check text exceeding 1400 characters.\nPlease use the spell check facility in Microsoft Word or Outlook instead.";
var formChanged = false;
var totalAttCount = 0;
var rndCount = 0;
var blankAppend = true;
var autoSave = false;
var submit = false;

//Variables to store field values on body load.
var globalCategory;
var globalStatus;
var globalSeverity;
var globalStatus;
var globalRequestType;
var globalOffice;
var globalLogger;
var globalAssignee;
var globalSubscriber;
var globalCc;
var globalDueDate;
var globalSummary;
var globalParentId;
var globalRelatedRequest;
var globalExtendedField = new Array();
var globalSubject;
var globalPrivate;
var globalDescription;


var timerId = -1;
var interval = 1000 * 60 * 5; // Every 5 Minutes; 

//Html snippets for a single exception
var exceptionBeginning = "<tr><td class=\"s\" align=\"left\">";
var exceptionEnding = "</td></tr>";

//Initialize error output.
var errorOutput = "" ;
var count=1;

//Initialize the state of cc box.
var ccBoxInitial = false;


function CustomSubmitEvent(name)
{
	this.name = name ;
	this.preSubmitEvent = new YAHOO.util.CustomEvent(name);
}
// this is the event that the listeners will have to register for 
// their listners to be called before the form-submit.
var customSubmitEvent = new CustomSubmitEvent('pre-submit-event');

function PreSubmitResult()
{
	this.canContinue = true ;
	this.message = "" ;
}
/*
 * This function called when the page is loaded.
 */
function onLoadBody()
{
  	  	
	// var t1 = (new Date()).getTime();
	var caller = trim(document.getElementById("caller").value);
	
	//Send the appropriate values to post.
	if(caller == 1)
	{
		document.getElementById("addRequestForm").action = getRelativePath(window.location, "add-request") + "/" + 
		document.getElementById("sysPrefix").value;
		
		if(document.getElementById("subject").disabled == false)
			document.getElementById("subject").focus();
		
    }
	else if(caller == 2)
	{
		//document.getElementById("action_subject").style.display = "block";
		var requestId = trim(document.getElementById("requestId").value);
		document.getElementById("addRequestForm").action = getRelativePath(window.location,"add-action") + "/" + 
		document.getElementById("sysPrefix").value+"/"+requestId;
	    storeFieldValues();	
		if(document.getElementById("description").disabled == false)
		{
		    try
		    {
			    document.getElementById("description").focus();
			}
			catch (e)
			{
			}
	    }
	}
	else if(caller == 3)
	{
		var requestId = trim(document.getElementById("requestId").value);
		document.getElementById("addRequestForm").action = getRelativePath(window.location,"add-subrequest") + "/" + 
		document.getElementById("sysPrefix").value+"/"+requestId;
		document.getElementById("openAssociate").value = "true";
	    document.getElementById("associate").style.cssText = "";
	    
	    if(document.getElementById("subject").disabled == false)
			document.getElementById("subject").focus();
	}
	
	//If notify is turned off,notify analysts and notify loggers
	//Should be turned off
	initializeCheckBoxes();
	initializeCcArea();
	onChangeCategory();
	//Function which controls the state of notify and notify-loggers checkboxes.
	onChangeMailSend();
	var today = new Date();	
	document.getElementById("clientOffset").value = today.getTimezoneOffset();
	//When an exception occures on the server side and the page is reloaded, 
	//set the show-exception div display to block.
	if(document.getElementById("show_exception_block").value == "true")
	{
		document.getElementById("exceptionBlock").style.display = "block";
	    if(document.getElementById("action_diff_exception").value == "true")
		{
		 //  openActionDialog();
		   blankAppend = false;
		}
	    window.location = "#top";
	}
	//If the summary box was open, and an exception occured on the server side,
	//it should open up on reloading the page.
	if(document.getElementById("openSummary").value == "true")
	{
		document.getElementById("summaryLabel").style.cssText = "";
		var myEditor=YAHOO.widget.EditorInfo.getEditorById("summary");
		showRTE(myEditor);
		//document.getElementById("summaryField").style.cssText = "";
	    //document.getElementById("summary").style.cssText = "width: 99%; height: 100%;";
	}
	//If the extended-fields were open, and an exception occured on the server side,
	//it should open up on reloading the page.
	if(document.getElementById("openExtended").value == "true")
	{
		var csList = document.getElementById("csList").value;	
		var strList = csList.split('|');
		
	    for(var i=0 ;i< strList.length; i++)
	     {
			if(document.getElementById(strList[i]) != null)
			{
			   var index = strList[i].indexOf("_ex");
			   var index_ex = strList[i].indexOf("_ex2");
			   var index_div = strList[i].indexOf("_exdiv");
			   var rteheaderindex = strList[i].indexOf("_rteheader");
			   var rteindex = strList[i].indexOf("_container");
			   
			   if(rteindex>0||rteheaderindex>0){
				   document.getElementById(strList[i]).style.cssText="";
			   }
			   else{
			   
			   if(index_div > 0)
			   {
			       var fieldVal = strList[i].substring(0,index_div);
			       document.getElementById(strList[i]).style.cssText = "";
			       if(isIE)
			       {
						document.getElementById(fieldVal+"_excol").style.display = "inline";
			       }
			       else
			       {
			            document.getElementById(fieldVal+"_excol").style.display = "table-cell";
				   }
			       document.getElementById(fieldVal+"_ex").style.cssText = "";
			       continue;
			   }
			   //var exValue = strList[i].substring(0,strList[i].length - 3);
			   var exValue = strList[i].substring(0,index);
			   if(index_ex > 0)
			   {
			      document.getElementById(strList[i]).style.cssText = "width:99%; height: 100%;";
			      document.getElementById(exValue).style.cssText = "width:99%; height: 100%;";
			   }
			   else
			   {
			      document.getElementById(strList[i]).style.cssText = "";
			      document.getElementById(exValue).style.cssText = "";
			   }
		    }
		        
         } 
    }
	}	    
	//If the associate request id box was open, and an exception occured on the server side,
	//it should open up on reloading the page.
	if(document.getElementById("openAssociate").value == "true")
	{
	   document.getElementById("associate").style.cssText = "";
	   document.getElementById("parent_request_id").style.cssText = "";
	}
	//If the related request id box was open, and an exception occured on the server side,
	//it should open up on reloading the page.
	if(document.getElementById("openRelated").value == "true")
	{
	   document.getElementById("related").style.cssText = "";
	   document.getElementById("related_requests").style.cssText = "";
	}
		
	if(document.getElementById("DTimestamp").value > 0)
	{
	  draftTimestamp = document.getElementById("DTimestamp").value;
	}
	var updateDivCtrl = document.getElementById("updateDiv");
	if(updateDivCtrl == null || updateDivCtrl.style.display == "BLOCK")
	{
		scheduleRefresher();
	}
    if(document.getElementById("is_draft").value == "true")
	{
		blankAppend = false;
	}
	//var t2 = (new Date()).getTime();
	//alert(t2-t1);
	registerSmartInputListeners();
}

function onBodyUnload()
{
    cancelRefresher();

}

function onBeforeUnload()
{
    if(submit == false)
    {
        var caller = document.getElementById("caller").value;
        if((caller == 1) && (autoSave == true))
        var saveDraft = confirm(AUTO_SAVE_DRAFT);
        if(saveDraft == false)
        {
            var systemId = document.getElementById("systemId").value;
            var requestId = 0;
            var timestamp = document.getElementById("DTimestamp").value ;
            var row = 0;
            deleteDraft(systemId, requestId, timestamp, row);
        }
        else
        {
    
        }
    }
}

function storeFieldValues()
{
 //Category
 if(document.getElementById("category_id") != null) 
    globalCategory = document.getElementById("category_id").value;
 //Severity
  if(document.getElementById("severity_id") != null) 
    globalSeverity = document.getElementById("severity_id").value;
 //Status
  if(document.getElementById("status_id") != null) 
    globalStatus = document.getElementById("status_id").value;
 //Request Type
  if(document.getElementById("request_type_id") != null) 
    globalRequestType = document.getElementById("request_type_id").value;
 //Office
  if(document.getElementById("office_id") != null) 
    globalOffice = document.getElementById("office_id").value;
 //Assignee
  if(document.getElementById("assignee_ids") != null) 
    globalAssignee = document.getElementById("assignee_ids").value;
 //Logger
  if(document.getElementById("logger_ids") != null) 
    globalLogger = document.getElementById("logger_ids").value;
 //DueDate
  if(document.getElementById("due_datetime") != null) 
    globalDueDate = document.getElementById("due_datetime").value;
 //Cc
  if(document.getElementById("cc_ids") != null) 
    globalCc = document.getElementById("cc_ids").value;
 //Subscriber
  if(document.getElementById("subscriber_ids") != null) 
    globalSubscriber = document.getElementById("subscriber_ids").value;
 //Subject
  if(document.getElementById("subject") != null) 
    globalSubject = document.getElementById("subject").value;
 //Summary
  if(document.getElementById("summary") != null) 
    globalSummary = document.getElementById("summary").value;
 //Parent
  if(document.getElementById("parent_request_id") != null) 
    globalParentId = document.getElementById("parent_request_id").value;
 //Link Requests
  if(document.getElementById("related_requests") != null) 
    globalRelatedRequest = document.getElementById("related_requests").value;
 //Extended Fields
  if(document.getElementById("csList").value != "")
    { 
    	var csList = document.getElementById("csList").value;	
    	var strList = csList.split('|');
	    for(var i=0 ;i< strList.length; i++)
	      {
	         var index = strList[i].indexOf("_ex");
			 var exValue = strList[i].substring(0,index);
	         if (document.getElementById(exValue) != null)
	          {
	             globalExtendedField[i] = document.getElementById(exValue).value;
	          }
	      }    
    }
 //Description.
   if(document.getElementById("description") != null) 
    globalDescription = document.getElementById("description").value; 
    
 //Confidential.
  if(document.getElementById("is_private") != null) 
    globalPrivate = document.getElementById("is_private").checked;
}

function rejectBlankAppends(errorOutput)
{
 var ctrl;
  
 //Category
 ctrl = document.getElementById("category_id");
 if(ctrl != null && ctrl.disabled == false)
 { 
    var finalCategory = ctrl.value;
    if(finalCategory != globalCategory)
        blankAppend = false;
 }
 //Severity
 ctrl = document.getElementById("severity_id");
  if(ctrl != null && ctrl.disabled == false) 
  {
    var finalSeverity = ctrl.value;
    if(globalSeverity != finalSeverity)
       blankAppend = false;
  }
 //Status
 ctrl = document.getElementById("status_id");
  if(ctrl != null && ctrl.disabled == false)
  { 
    var finalStatus = ctrl.value;
    if (globalStatus != finalStatus)
       blankAppend = false;
  }
 //Request Type
 ctrl = document.getElementById("request_type_id") ;
  if(ctrl != null && ctrl.disabled == false) 
  { 
    var finalRequestType = ctrl.value;
    if (globalRequestType != finalRequestType)
       blankAppend = false;
  }
   //Office
   ctrl = document.getElementById("office_id");
  if(ctrl != null && ctrl.disabled == false) 
  { 
    var finalOffice = ctrl.value;
    if (globalOffice != finalOffice)
       blankAppend = false;
  }
   //DueDate
  ctrl = document.getElementById("due_datetime");
  if(ctrl != null && ctrl.disabled == false) 
   { 
    var finalDueDate = ctrl.value;
    if (globalDueDate != finalDueDate)
       blankAppend = false;
  }
  
 //Assignee
  ctrl = document.getElementById("assignee_ids");
  if(ctrl != null && ctrl.disabled == false) 
  { 
    var finalAssignee = ctrl.value;
    finalAssignee = removeRedundantUserListSeparators(finalAssignee);
    globalAssignee = removeRedundantUserListSeparators(globalAssignee);
    if (finalAssignee != globalAssignee)
       blankAppend = false;
  }
  
 //Logger
 ctrl = document.getElementById("logger_ids");
  if(ctrl != null && ctrl.disabled == false) 
   { 
    var finalLogger = ctrl.value;
    finalLogger = removeRedundantUserListSeparators(finalLogger);
    globalLogger = removeRedundantUserListSeparators(globalLogger);
    if (globalLogger != finalLogger)
       blankAppend = false;
  }

 //Cc
  ctrl = document.getElementById("cc_ids");
  if(ctrl != null && ctrl.disabled == false) 
  { 
    var finalCc = ctrl.value;
    finalCc = removeRedundantUserListSeparators(finalCc);
    globalCc = removeRedundantUserListSeparators(globalCc);
    if (globalCc != finalCc)
       blankAppend = false;
  }
  
 //Subscriber
  ctrl = document.getElementById("subscriber_ids");
  if(ctrl != null && ctrl.disabled == false) 
  { 
    var finalSubscriber = document.getElementById("subscriber_ids").value;
    finalSubscriber = removeRedundantUserListSeparators(finalSubscriber);
    globalSubscriber = removeRedundantUserListSeparators(globalSubscriber);
    if (globalSubscriber != finalSubscriber)
       blankAppend = false;
  }
 //Subject
  if(document.getElementById("subject") != null) 
  { 
    var finalSubject = document.getElementById("subject").value;
    if (globalSubject != finalSubject)
       blankAppend = false;
  }
 //Summary
  if(document.getElementById("summary") != null) 
  { 
    var finalSummary = document.getElementById("summary").value;
    if (globalSummary != finalSummary)
       blankAppend = false;
  }
 //Parent
  if(document.getElementById("parent_request_id") != null) 
  { 
    var finalParentId = document.getElementById("parent_request_id").value;
    if (globalParentId != finalParentId)
       blankAppend = false;
  }
 //Link Requests
  if(document.getElementById("related_requests") != null)
  { 
    var finalRelatedRequest = document.getElementById("related_requests").value;
    if (globalRelatedRequest != finalRelatedRequest)
       blankAppend = false;
  }
 //Extended Fields
  if(document.getElementById("csList").value != "")
    { 
    	var csList = document.getElementById("csList").value;	
    	var strList = csList.split('|');
	    for(var i=0 ;i< strList.length; i++)
	      {
	      	 var index = strList[i].indexOf("_ex");
			 var exValue = strList[i].substring(0,index);
	         if (document.getElementById(exValue) != null)
	          {
	             if( globalExtendedField[i] != document.getElementById(exValue).value )
	                  blankAppend = false;
	          }
	      }    
    }
 //Description.
  if(document.getElementById("description") != null) 
  { 
	  if (CKEDITOR.instances['description']){
		  CKEDITOR.instances['description'].updateElement();
	  }    
	  var finalDescription = document.getElementById("description").value;
	  if (globalDescription != finalDescription)
		  blankAppend = false;
  } 
  
 
 //Confidential.
  if(document.getElementById("is_private") != null) 
  { 
    var finalPrivate = document.getElementById("is_private").checked;
    if (globalPrivate != finalPrivate)
       blankAppend = false;
  }
  
  if( (typeof(tbitsUploader) != 'undefined') && (tbitsUploader != null) )
  if(tbitsUploader.isChanged)
	  blankAppend = false;
  
//  if(totalAttCount > 0)
//  {
//       blankAppend = false;
//  }
  
  if(blankAppend == true)
  {
     count++;
	 errorOutput = errorOutput + exceptionBeginning + count + "." + EMPTY_APPEND + exceptionEnding;
  }
  
  return errorOutput;
  
}

function cancelRefresher()
{
    if (timerId != -1)
    {
        window.clearInterval(timerId);
    }
    timerId = -1;
}

function scheduleRefresher()
{
	// Check if the interval is specified in the HTML source.
    var value = document.getElementById("autoSaveRate").value;
    
    if (value > 0)
    {
		interval = 1000 * 60 * value;   
		timerId =  window.setInterval(function() {saveDraft('automatic');autoSave = true;},interval);   
	}
}

function openActionDialog()
{
//	console.log("openActionDialog called.");
    var sysPrefix = document.getElementById("sysPrefix").value;
    var location = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + 
                   "?actionDiff=openActionDiff" + 
                   "&sysPrefix=" + sysPrefix + 
                   "&now=" + getCurrentTime();
    // var location = "/web/tbits-action-diff.htm?now=" + new Date();
    
	var features = "status:no; dialogWidth:650px;resizable:yes";
    var child = new Object();
    child.actionDiff = document.getElementById("showDiff").innerHTML;
    child.diffList = document.getElementById("diffList").value; 
    child.checkedList = "";
	var strCheckedList = "" ;	
	var strList = document.getElementById("diffList").value.split("|");
	var strFieldList = document.getElementById("diffFieldList").value.split("|");
    if(isIE == true)
      {
		  window.showModalDialog(location, child, features);
		  strCheckedList = child.checkedList.split("|");
		  for(i=0 ; i < strList.length ; i++)
	      {
		    var control = new String(strList[i]);
		    var currentControl = document.getElementById(control);
		    var fldValue = strFieldList[i];
		    var currentFieldChecked = strCheckedList[i];
		     if(currentFieldChecked == "true")
		      {
		        if(currentControl != null)
                {
	              if (currentControl.type == "text")
	              {
		             currentControl.value = fldValue;
	              }
	              else if(currentControl.type == "textarea")
	              {
	                 currentControl.innerText = fldValue;
	              }
	              else if(currentControl.type == "select-one")
	              {
		             var len = currentControl.options.length;
		             for(var j = 0; j <= len - 1; j++) 
		              {
			             var typeValue = currentControl.options[j].text;
			             if((typeValue == fldValue))
			               {
				              currentControl.options[j].selected = true;
			               }                     
		              }
	               }
                 }
              }
	     } 
	}
	else
	{
	    window.showDiff = document.getElementById("showDiff").innerHTML;
		var newWindow = window.open(location, "name" , features);
		newWindow.opener = window;
		window.actionDiff = document.getElementById("showDiff").innerHTML;
        window.diffList = document.getElementById("diffList").value;
        window.diffFieldList =  document.getElementById("diffFieldList").value;
	}
	
	 if ((isIE == true) && (child.result == "submit"))
		 return onSubmit() ;
//	 ?    document.getElementById("addRequestForm").submit(); 
	        
	 if ((isIE != true) && (window.result == "submit"))
		 return onSubmit() ;
//	     document.getElementById("addRequestForm").submit(); 
}
	 
/*
 * This function is used to validate some of the fields from the client side after clicking the 
 * 'Submit' button.
 */

function onFormSubmit()
{
//	console.log("onFormSubmit called.");
    submit = true;
    //Rich retrieving the description content from rich text editor.
    
    //YAHOO.widget.EditorInfo.getEditorById("description").saveHTML();
    //saveAllRTEs();    
    var caller = trim(document.getElementById("caller").value);
	//Initialize errorOutput;
	errorOutput = "";
	count = 0;
    if (totalAttCount > 0)
    {
        document.getElementById("addRequestForm").enctype = "multipart/form-data";
        //document.getElementById("addRequestForm").encoding = "multipart/form-data";
    }
    else
    {
        document.getElementById("addRequestForm").encType = "application/x-www-form-urlencoded";
    }
    
    
    
	// There should be atleast one logger.
	if (document.getElementById("logger_ids") != null)
	{
	    var strLogger = document.getElementById("logger_ids").value;
	    if ((document.getElementById("logger_ids").disabled==false) && (trim(strLogger) == ""))
	    {
		   count++;
		   errorOutput = errorOutput + exceptionBeginning + count + "." + LOGGER_MANDATORY + exceptionEnding;
		  if (document.getElementById("logger_ids").disabled == false)
			  document.getElementById("logger_ids").focus();
	    }
	 }
	
	//The subject cannot be empty
	// if(document.getElementById("subject") != null)
	// 	{
	// 	    var strSubject = document.getElementById("subject").value;
	// 	   if ((document.getElementById("subject").disabled==false) && (trim(strSubject) == ""))
	// 	   {
	// 		  count++;
	// 		  errorOutput = errorOutput + exceptionBeginning + count + "." + SUBJECT_MANDATORY + exceptionEnding;
	// 		  if (document.getElementById("subject").disabled == false)
	// 			  document.getElementById("subject").focus();
	// 	  }
	// 	}
	
	//Validate due-date based on regular expression.
	/*if(document.getElementById("due_datetime") != null)
	{
	   var strDueDateTime = document.getElementById("due_datetime").value;	
	   var dueDateOption = document.getElementById("due_date_option").value;
	   if((document.getElementById("due_datetime").disabled==false) && (trim(strDueDateTime) != ""))
	    {
	      errorOutput = validateDueDate(errorOutput,trim(strDueDateTime));
	    }
	   if((document.getElementById("due_datetime").disabled==false) && (trim(strDueDateTime) == "") &&
	  	      (dueDateOption == "false"))
	    {
	      		count++;
	      		errorOutput = errorOutput + exceptionBeginning + count + "." + EMPTY_DUE_DATE + exceptionEnding;
	    }
    }*/
    
	//Check if the attachment process is running
	
		//Update the attachments
	
	if(isUploading())
	{
		errorOutput += "The attachments are being uploaded. Please try again after sometime.";
	}
	else
	{
		setTheAttachmentField();
	}
    //Validate all the users( assignees, loggers,subscribers and cc list)
    var usersList = "";
    var ctl;
    // We should use json instead of appeding values in this format by separators
    ctl = document.getElementById("logger_ids");
    if (ctl != null)
    {
         var list = removeRedundantUserListSeparators(ctl.value);
         var userArray = list.split(";");
         for (var i = 0 ; i < userArray.length ; i++)
         {
           usersList = usersList + userArray[i] + ":dbUser;"
         }
    }
    
    ctl = document.getElementById("assignee_ids");
    if (ctl != null)
    {
         var list = removeRedundantUserListSeparators(ctl.value);
         var userArray = list.split(";");
         for (var i = 0 ; i < userArray.length ; i++)
         {
           usersList = usersList + userArray[i] + ":dbUser;"
         }
    }

    ctrl = document.getElementById("subscriber_ids");
    if (ctrl != null)
    {
         var list = removeRedundantUserListSeparators(ctrl.value);
         var userArray = list.split(";");
         for (var i = 0 ; i < userArray.length ; i++)
         {
           usersList = usersList + userArray[i] + ":extUser;"
         }
    }
    
    ctrl = document.getElementById("cc_ids"); 
    if (ctrl != null)
    {
         var list = removeRedundantUserListSeparators(ctrl.value);
         var userArray = list.split(";");
         for (var i = 0 ; i < userArray.length ; i++)
         {
           usersList = usersList + userArray[i] + ":extUser;"
         }
    }
    
    errorOutput = validateUsersList(errorOutput, usersList);
    
    if(caller == 2)
       errorOutput = rejectBlankAppends(errorOutput);
   	      	
	//Validate parent-request id text box.
	if(document.getElementById("parent_request_id") != null)
	{
	   var strParentReqId = document.getElementById("parent_request_id").value;
	   if((document.getElementById("parent_request_id").style.display != "none") && 
		  (document.getElementById("parent_request_id").disabled==false) && 
		  (trim(strParentReqId) != ""))
	   {
	      errorOutput = validateParentRequestId(errorOutput,strParentReqId);
	  }
	}
	
	//Validate related requests text box.
	if(document.getElementById("related_requests") != null)
	{
	   var strRelReqId = document.getElementById("related_requests").value;
	   if((document.getElementById("related_requests").style.display != "none") && 
		  (document.getElementById("related_requests").disabled==false) && 
		  (trim(strRelReqId) != ""))
	   {
	      errorOutput = validateRelatedRequestId(errorOutput,strRelReqId);
	  }
	}
	
	//Validate each of the extended fields based on their type.
	if(document.getElementById("execFunctions") != null)
	{
	   var evalFunctions = document.getElementById("execFunctions").value;
	   var strList = evalFunctions.split('|');
	   for(i=0 ; i <strList.length ; i++)
	   {
	      var argList = strList[i].split(',');
//	      if(argList[0] == "date")
//	       {
//	         if(document.getElementById(argList[1]) != null)
//	         {
//	           var date = document.getElementById(argList[1]).value;
//	           if(date != "")
//	             {
//	               errorOutput = validateDueDate(errorOutput,date);
//	             }
//	         }
//	       }
//	       else
	       if((argList[0] == "real")||(argList[0] == "int"))
	       {
	         if(document.getElementById(argList[1]) != null)
	         {
	           var val = document.getElementById(argList[1]).value;
	           var regex = argList[2];
	           if(val != "")
	             {
	               errorOutput = validateField(errorOutput,val,regex, argList[0], argList[3]);
	             }
	         }
	      }
	       
	   }

	}
	
	// firing the presubmit event.
	var preSubmitActionResult = new PreSubmitResult() ;
	customSubmitEvent.preSubmitEvent.fire(preSubmitActionResult);
	
	if( preSubmitActionResult.canContinue == false )
	{
		errorOutput += preSubmitActionResult.message ;		
	}
	
	//If error output is not null, then show all the exceptions in the show-exception
	// div.
	if(errorOutput!= "" || preSubmitActionResult.canContinue == false )
	{
		if( errorOutput != "" )
			showAutomaticRestrictions(errorOutput);
		
		submit = false ;
		return false;
	}
		
	var sysPrefix = document.getElementById("sysPrefix").value;
	var requestId = document.getElementById("requestId").value;
	var action = "";
	
	// Set the action depending on the caller.
	if(caller == 1)
		 action = getRelativePath(window.location, "add-request") + "/" + sysPrefix;	
	else if(caller == 2)	
		action = getRelativePath(window.location, "add-action") + "/" + sysPrefix + "/" + requestId;
	else if(caller == 3)
		action = getRelativePath(window.location, "add-subrequest") + "/" + sysPrefix + "/" + requestId;

	// Disable submit buttons after clicking submit.
	if (document.getElementById("btnSubmit1")) document.getElementById("btnSubmit1").disabled = true;
	if (document.getElementById("btnSubmit2")) document.getElementById("btnSubmit2").disabled = true;
	
	document.getElementById("addRequestForm").action = action;	
	
	return true ;
}

function onSubmit()
{
//	console.log("onSubmit called.");
	if( onFormSubmit() )
	{
		document.getElementById('addRequestForm').submit() ;
	}
	else
	{
		return false;
	}
}

/*
 * This function is used to reset the page.
 * 
 */
function onReset()
{
	var caller = trim(document.getElementById("caller").value);
	if (formChanged == true)
	{
		if(confirm(NAVIGATION_ALERT) == false) 
		{
			document.getElementById("btnSubmit1").disabled = false;
			document.getElementById("btnSubmit2").disabled = false;
			return;
		}
	}
	var systemId = document.getElementById("systemId").value;
	var sysPrefix = document.getElementById("sysPrefix").value;
	var requestId = document.getElementById("requestId").value;
	var redirection = "";
	if(caller == 1)         
	        redirection = getRelativePath(window.location, "add-request") + "/" + sysPrefix;
	else 
	if(caller == 2)    
	        redirection = getRelativePath(window.location, "add-action") + "/" + sysPrefix + "/" + requestId;
	else 
	if(caller == 3)    
	        redirection = getRelativePath(window.location, "add-subrequest") + "/" + sysPrefix + "/" + requestId;
	
	window.location = redirection;
}

function validateUsersList(errorOutput, aUsersList)
{
   var usersList = new String(aUsersList);
   var sysPrefix = document.getElementById("sysPrefix").value;
   var dueDate = null;
   var dueDateCtrl = document.getElementById("due_datetime");
   if(dueDateCtrl != null && dueDateCtrl.disabled == false)
   {
      dueDate = dueDateCtrl.value;
   }
   var url = getRelativePath(window.location, "search-util/")+sysPrefix + 
			 "?validateUsersList=true&usersList=" + usersList + "&now=" + (new Date()).getTime();
   //if(dueDate != null && dueDate!= "")
     // url = url + "&dueDate=" + dueDate;
   var output =  executeHttpRequest("GET",url,false);
   var outputArray = output.split("$_D_E_L_I_M_$");
   var arraySize = outputArray.length;
   if(outputArray[0].indexOf(" invalid") >= 0)
   {
       count++ ;
       errorOutput = errorOutput + exceptionBeginning + count + "." + 
			              outputArray[0] + exceptionEnding;
   }  
    
   if(arraySize > 1 && outputArray[1].indexOf(" invalid") >= 0)
   {
		count++;
		errorOutput = errorOutput + exceptionBeginning + count + "." + 
			              outputArray[1] + exceptionEnding;
   }
    return errorOutput;
}

function openAssigneePicker()
{
  showAssigneeList(document.getElementById("assignee_ids"),document.getElementById("category_id"));
}

/*
* Function to pull up the assignee picker
*/
function showAssigneeList(assigneeFldName, categoryField)
{
	var IE = (document.all) ? true : false;
	var features = "dialogTop: px; dialogLeft: px;dialogWidth:640px;dialogHeight:500px;resizable:0;status:0;help:0";
	var systemId = document.getElementById("systemId").value;
	var sysPrefix = document.getElementById("sysPrefix").value;
	var categoryId = document.getElementById("category_id").value;
	var url = getRelativePath(window.location, "assignee-frm") + "/" + sysPrefix + "?systemId=" + systemId  + "&sysPrefix=" + sysPrefix  + "&categoryId=" + categoryId + "&existingAssignee=" + assigneeFldName.value +"&userTypeId=7&date=" + new Date();
	var output =  executeHttpRequest("GET",url,false);
	output = trim(output);
	if (output.indexOf("No Assignee Found") > 0)
	{
		alert(NO_ANALYSTS_FOR_CATEGORY);
		return;
	}

	if(IE == true)
	{
	    var object = new Object();
	    object.assignee = assigneeFldName.value;
	    object.disabled = assigneeFldName.disabled;
	    object.userFilter = "7";
	    object.returnValue = 2;

	    //0- cancel;
	    //1-OK;
	    //2-Reload with new filter.

	     while(object.returnValue == 2)
	     {	
		    redirection = getRelativePath(window.location, "assignee-frm") + "/" + sysPrefix + "?systemId=" + systemId  + "&sysPrefix=" + sysPrefix  + "&categoryId=" + categoryId + "&existingAssignee=" + object.assignee + "&userTypeId=" + object.userFilter + "&date=" + new Date();
		   	window.showModalDialog(redirection, object, features);
	     }

	     if (object.returnValue == 0) return;
	     	
	     if (assigneeFldName.disabled == true) return;
	         assigneeFldName.value = object.assignee;
	}
	else
	{
		  window.assignee = assigneeFldName.value;
	      window.disabled = assigneeFldName.disabled;
	      window.userFilter = "7";
	      window.returnValue = "2";
          redirection = getRelativePath(window.location, "assignee-frm") + "/" + sysPrefix + "?systemId=" + systemId  + "&sysPrefix=" + sysPrefix  + "&categoryId=" + categoryId + "&existingAssignee=" + window.assignee + "&userTypeId=" + window.userFilter + "&date=" + new Date();
		  var newWindow = window.open(redirection, "name" , features);
		  newWindow.opener = window;
		  window.assigneeFldName  = assigneeFldName;
	}
	
}

/*
 * This function is used to control the mailAnalyst and mailLogger checkboxes when eMail box 
 * is checked/unchecked.
 */
function initializeCheckBoxes()
{
	if (document.getElementById("mailSend").disabled == true)
	{
		document.getElementById("chkLoggers").disabled = true;
	}
	
	if (document.getElementById("chkLoggers").disabled == true)
	{ 
		document.getElementById("loggerStatus").value = "false";
	}
	else 
	{
	    document.getElementById("loggerStatus").value = "true";
	}
}

function onChangeLoggers()
{
	document.getElementById("mailLoggers").value = document.getElementById("chkLoggers").checked;
}

function onChangeAnalysts()
{
	document.getElementById("mailAnalysts").value = document.getElementById("chkAnalysts").checked;
}

/*
 * This function is used to control the state of the cc box 
 * 
 */
function initializeCcArea()
{
  if(document.getElementById("cc_ids").disabled == false)
    {
		 ccBoxInitial = true;
    }
  if (document.getElementById("mailSend").checked == false)
    {
         document.getElementById("cc_ids").disabled = true;
    }
}

/*
 * This function sets the Confidential class to bold on checking it.
 * 
 */
function onChangeConfidential()
{
  var is_private = document.getElementById("is_private");
  if((is_private != null) && (document.getElementById("is_private").checked == true))
   {
       document.getElementById("confidential").className = "sx b cr";
   }
  else if((is_private != null) && (document.getElementById("is_private").checked == false))
   {
       document.getElementById("confidential").className = "sx cr";
   }
}

/*
 * This function automatically changes the status to "Active" or "Assigned" on typing an
 * name in assignee id box.
 */
function onTypeAssigneeIds()
{
    var ctrlAssignee = document.getElementById("assignee_ids");
	if((ctrlAssignee != null) && (ctrlAssignee.disabled == false) &&
	   (trim(ctrlAssignee.value) != ""))
	{
	      var ctrlStatus = document.getElementById("status_id");
	      if(ctrlStatus.disabled == false)
	      {	      
	           var currentStatus = ctrlStatus.value;
	           if(currentStatus.toLowerCase() == "open" || currentStatus.toLowerCase() == "new")
	           {
	                 var len = ctrlStatus.options.length;
	                 for(var i = 0; i <= len - 1; i++) 
                       {
                           var typeValue = ctrlStatus.options[i].value;
                           if((typeValue.toLowerCase() == "assigned") || (typeValue.toLowerCase() == "active"))
                           {
                              ctrlStatus.options[i].selected = true;
                           }                     
                       }
                }
	      }
	}
}

/*
 * This function enables the due date on checking the check-box.	
 */
function disableDueDate()
{
	if (document.getElementById("dueDateHref").innerHTML == 'Clear')
	{
	   document.getElementById("due_datetime").value = "";
	   document.getElementById("dueDateHref").innerHTML = 'Set default';
	}
	else
    {	
	   document.getElementById("due_datetime").value = document.getElementById("strDueDate").value;
	   document.getElementById("dueDateHref").innerHTML = 'Clear';
	}
}

function onChangeNullDueDate()
{
    if(document.getElementById("chkAllowNullDueDate").checked == true)
    {
	      document.getElementById("due_datetime").disabled = false;
	}
    
  if(document.getElementById("chkAllowNullDueDate").checked == false)
    {
	      document.getElementById("due_datetime").disabled = true;
    }
}

/*
 * This function displays the error output in the exception-div.	
 */
function showAutomaticRestrictions(errorOutput)
{				
	document.getElementById("exceptions").innerHTML = '<table id=\"table6\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">' 
	+ errorOutput + '</table>';
	document.getElementById("exceptionBlock").style.display = "block";
	window.location = "#top";
}


function onChangeDiff()
{
   var diffList = document.getElementById("diffList").value;
   var strList = diffList.split('|');
   if(document.getElementById("replaceAll").checked == true)
   {
     for(i=0 ; i < strList.length ; i++)
	   {
	     var field_ck = strList[i] + "_ck";
	     alert(field_ck);
	     var currentElement = document.getElementById(field_ck);
	      if(currentElement != null)
	            currentElement.checked = true
	   }
   }
}


function onUpdateAllFields()
{
	var diffList = document.getElementById("diffList").value;
	var newFieldValues = document.getElementById("diffFieldList").value;
	var strList = diffList.split("|");
	var strFieldList = newFieldValues.split("|");

	for(i=0 ; i <strList.length ; i++)
	{
		var control = new String(strList[i]);
		var currentControl = document.getElementById(control);
		var fldValue = strFieldList[i];
		if(currentControl != null)
        {
	        if (currentControl.type == "text")
	        {
		        currentControl.value = fldValue;
	        }
	        else if(currentControl.type == "textarea")
	        {
	            currentControl.innerText = fldValue;
	        }
	        else if(currentControl.type == "select-one")
	        {
		        var len = currentControl.options.length;
		        for(var j = 0; j <= len - 1; j++) 
		        {
			        var typeValue = currentControl.options[j].text;
			        if((typeValue == fldValue))
			        {
				        currentControl.options[j].selected = true;
			        }                     
		        }
	        }
        }
	}
	
	return;
}


function onUpdateFieldValue(fieldName, fieldValue)
{
  var fldName = new String(fieldName);
  var fldValue = new String(fieldValue);
  var currentControl = document.getElementById(fldName);
  if(currentControl != null)
  {
	if (currentControl.type == "text")
	{
		currentControl.value = fldValue;
	}
	else if(currentControl.type == "textarea")
	{
	    currentControl.innerText = fldValue;
	}
	else if(currentControl.type == "select-one")
	{
		var len = currentControl.options.length;
		for(var i = 0; i <= len - 1; i++) 
		{
			var typeValue = currentControl.options[i].text;
			if((typeValue == fldValue))
			{
				currentControl.options[i].selected = true;
			}                     
		}
	}
  }
  return;
}


/*
 * This function is used to validate a field from the client side and populate the error o
 *  output based on the regular expression.
 */
function validateField(errorOut,value, reg, type, field)
{
	var value = new String(value);
	var dataType = type;
	var errorOutput = errorOut;
	var fieldName = field;
	var regex = new String(reg);
	if (regex == null || regex == "" || value == "") 
	{
		return errorOutput;
	}
	//alert("Regex: " + regex + "\n"  +  "Value: " + value + " = " + value.match(regex));
	if(value.match(regex))
	{
		return errorOutput;
	}
	else 
	{
		var typ = "";
		count++;
		if (dataType.toLowerCase() == "int")
			typ = "integer";
		else 
		if (dataType.toLowerCase() == "real")
			typ = "real number";
			
			errorOutput = errorOutput + exceptionBeginning + count + "." + 
			              INVALID_EXT_FIELD_VALUE.replace("<DATATYPE>", typ).replace("<FIELDNAME>", fieldName)+ 
			              exceptionEnding;
        return errorOutput;	
	}
}

/*
 * This function is used to validate the parent request id box.
 */
function validateParentRequestId(errorOut,strParentReqId)
{	
	var errorOutput = errorOut;		
	
	if (!(trim(strParentReqId).match(/^[0-9]*$/)))
	{
		count++;
		errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_PARENT_REQUEST_ID_FORMAT + exceptionEnding;
		if (document.getElementById("parent_request_id").disabled == false) 
			document.getElementById("parent_request_id").focus();
		return errorOutput;
	}
	return errorOutput;	
}

/*
 * This function is used to validate the related request id box.
 */
function validateRelatedRequestId(errorOut,strRelReqId)
{	
	var errorOutput = trim(errorOut);	
	var hrefsPresent = false;
	strRelReqId = strRelReqId.replace(/\s+/g, ";");
	strRelReqId = strRelReqId.replace(/,+/g, ";");
	strRelReqId = strRelReqId.replace(/;+/g, ";");
	
	var arr = strRelReqId.split(";");
	var errMesg = "";
	var comma = "";
	var sysPrefix = document.getElementById('sysPrefix').value;
	
	for (i = 0; i < arr.length; i++)
		{
			var requestId = arr[i];
			requestId = (trim(requestId)).toLowerCase();
			
			if (requestId.indexOf("http:") >= 0)
			 {
			 	  var regex = /^http:\/\/([^\s\n\r:\/]*)(:([0-9]+))?\/?\/q\/([a-zA-Z0-9_]+)\/([0-9]+)(#[0-9]+)*$/;
			 	  var matches = requestId.match(regex);
			 	 
				 	  if (matches == null)
						{
							errMesg = errMesg + comma + requestId ;
							comma = ", ";
							continue;
						}
					else
					{	
						var reqId = matches[4] + "#" + matches[5];
						var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?prefixId=" + escape(reqId) + "&now=" + getCurrentTime();
						var output = executeHttpRequest("GET", url, false);
						output = trim(output);
						if (output.indexOf("no_ba") >= 0)
						{
							errMesg = errMesg + comma + requestId ;
							comma = ", ";
							continue;
						}
						if (matches.length > 5)
						reqId = reqId + matches[6];
						arr[i] = reqId;
						hrefsPresent = true;
					}
				 	
			 }	
			// Check if the request contains prefix#Id 
				else if (requestId.indexOf("#") >= 0) 
				{
					var regex = /^[a-zA-Z0-9_]+#[0-9]+(#[0-9]+)*$/;
					if (requestId.match(regex) == null)
						{
							errMesg = errMesg + comma + requestId ;
							comma = ", ";
							continue;
						}	
					var url = YAHOO.transbit.tbits.contextPath + "search-util/" + sysPrefix + "?prefixId=" + escape(requestId) + "&now=" + getCurrentTime();
					var output = executeHttpRequest("GET", url, false);
					output = trim(output);
					if (output.indexOf("no_ba") >= 0)
					{
						errMesg = errMesg + comma + requestId ;
						comma = ", ";
						continue;
					}
				}
			else
				{
					if(requestId.match(/^[0-9]+$/) == null)
						{
							errMesg = errMesg + comma + requestId ;
							comma = ", ";
							continue;
						}	
				}	
		}	
	if (errMesg != "")	
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + ". " + INVALID_RELATED_REQUEST + errMesg + "." + exceptionEnding;
			if (document.getElementById("related_requests").disabled == false) 
			document.getElementById("related_requests").focus();
			return errorOutput;
		}
	else if (hrefsPresent == true)
	{
		var str = "";
		for (i = 0; i < arr.length; i++)
		{
		  if (i != 0)
		  str = str + ",";
		  
		  str = str + arr[i];
		  document.getElementById("related_requests").value = str;
		}
	}	
		
	return errorOutput;	
}


/* 
 *  This function is used to validate duedate input text box 
 */

function validateDueDatePrev(errorOut,strDueDateTime)
{
	var dueDate = strDueDateTime;
	var errorOutput = errorOut;
	
	//The expected pattern for the due-date-time (eg : 06/23/2005 22:30 
	if (!dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4} [0-9]{1,2}:[0-9]{1,2}$/) && 
	    !dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4}$/))
	{
		count++;
		errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_DUE_DATE_FORMAT + exceptionEnding;
		return errorOutput;
	}
	
	if(dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4} [0-9]{1,2}:[0-9]{1,2}$/))
	{
		var splitString = dueDate.split(" ");
		var datePart = splitString[0];
		var timePart = splitString[1];
		var dueHour = splitString[1].split(":")[0];
		var dueMin = splitString[1].split(":")[1];
		var mPart = Number(datePart.split('/')[0]);
		var dPart = Number(datePart.split('/')[1]);
		var yPart = Number(datePart.split('/')[2]);
		
		//Date should be between 0 and 31.	
		if (dPart < 1 || dPart > 31)
		{
			count ++;
			errorOutput = errorOutput+exceptionBeginning+ count + "." + INCORRECT_DATE+exceptionEnding;
		}
		
		//Month should be between 0 and 12.
		if (mPart < 1 || mPart > 12)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_MONTH+exceptionEnding;
		}
		
		//Year should be > 0.
		if (yPart < 0 )
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_YEAR+exceptionEnding;
		}
		
		//If month belongs to {2,4,6,9,11) then date cannot be > 30.
		if ( (mPart == 2 || mPart == 4 || mPart == 6  || mPart == 9 || mPart == 11) && dPart > 30)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}
		
		//If month is february then we do the appropriate validation depending upon whether the year is 
		//leap year or not.
		if ( mPart == 2 )
		{
			count++;
			if ( dPart > 29 )
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
				
			if (!(yPart % 400 == 0 || (yPart % 4 == 0 && yPart % 100 != 0)) && dPart > 28)
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}	

		//Minutes should be between 0 and 59
		if(!dueMin.match(/^[0-9]{1,2}$/) || Number(dueMin) < 0 || Number(dueMin) >= 60)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_MINUTE_VALUE + exceptionEnding;
		}

		//Hours should be between 0 and 23
		if(!dueHour.match(/^[0-9]{1,2}$/) || Number(dueHour) < 0 || Number(dueHour) >= 24)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_HOUR_VALUE + exceptionEnding;
		}
	}
	if(dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4}$/))
	{
		var splitString = dueDate.split(" ");
		var datePart = splitString[0];
		var mPart = Number(datePart.split('/')[0]);
		var dPart = Number(datePart.split('/')[1]);
		var yPart = Number(datePart.split('/')[2]);
		
		//Date should be between 0 and 31.	
		if (dPart < 1 || dPart > 31)
		{
			count ++;
			errorOutput = errorOutput+exceptionBeginning+ count + "." + INCORRECT_DATE+exceptionEnding;
		}
		
		//Month should be between 0 and 12.
		if (mPart < 1 || mPart > 12)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_MONTH+exceptionEnding;
		}
		
		//Year should be > 0.
		if (yPart < 0 )
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_YEAR+exceptionEnding;
		}
		
		//If month belongs to {2,4,6,9,11) then date cannot be > 30.
		if ( (mPart == 2 || mPart == 4 || mPart == 6  || mPart == 9 || mPart == 11) && dPart > 30)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}
		
		//If month is february then we do the appropriate validation depending upon whether the year is 
		//leap year or not.
		if ( mPart == 2 )
		{
			count++;
			if ( dPart > 29 )
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
				
			if (!(yPart % 400 == 0 || (yPart % 4 == 0 && yPart % 100 != 0)) && dPart > 28)
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}	
	}

	return errorOutput
}

/*
"dd-MMM-yyyy HH:mm:ss"
"dd-MMM-yyyy"

"MM/dd/yyyy HH:mm:ss"
"MM/dd/yyyy"
"yyyy-MM-dd"
"MMM dd, yyyy"
*/

function validateDueDate(errorOut,strDueDateTime)
{
	var dueDate = strDueDateTime;
	var errorOutput = errorOut;
	
	//The expected pattern for the due-date-time (eg : 06/23/2005 22:30:05 
	if (!dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$/) && 
	    !dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4}$/) &&
		!dueDate.match(/^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$/) &&
		!dueDate.match(/^[A-Z][a-z]{2} [0-9]{1,2}, [0-9]{4}$/) && 
		!dueDate.match(/^[0-9]{1,2}-[A-Z][a-z]{1,2}-[0-9]{4}$/) &&
		!dueDate.match(/^[0-9]{1,2}-[A-Z][a-z]{1,2}-[0-9]{4} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$/))
	{   
		count++;
		errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_DUE_DATE_FORMAT + exceptionEnding;
		return errorOutput;
	}

	if (dueDate.match(/^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$/) ||
		dueDate.match(/^[A-Z][a-z]{2} [0-9]{1,2}, [0-9]{4}$/) ||
		dueDate.match(/^[0-9]{1,2}-[A-Z][a-z]{1,2}-[0-9]{4}$/) ||
		dueDate.match(/^[0-9]{1,2}-[A-Z][a-z]{1,2}-[0-9]{4} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$/))
	{
		return errorOutput;
	}
	
	if(dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}$/))
	{
		var splitString = dueDate.split(" ");
		var datePart = splitString[0];
		var timePart = splitString[1];
		var dueHour = splitString[1].split(":")[0];
		var dueMin = splitString[1].split(":")[1];
		var dueSec = splitString[1].split(":")[2];
		var mPart = Number(datePart.split('/')[0]);
		var dPart = Number(datePart.split('/')[1]);
		var yPart = Number(datePart.split('/')[2]);
		
		//Date should be between 0 and 31.	
		if (dPart < 1 || dPart > 31)
		{
			count ++;
			errorOutput = errorOutput+exceptionBeginning+ count + "." + INCORRECT_DATE+exceptionEnding;
		}
		
		//Month should be between 0 and 12.
		if (mPart < 1 || mPart > 12)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_MONTH+exceptionEnding;
		}
		
		//Year should be > 0.
		if (yPart < 0 )
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_YEAR+exceptionEnding;
		}
		
		//If month belongs to {2,4,6,9,11) then date cannot be > 30.
		if ( (mPart == 2 || mPart == 4 || mPart == 6  || mPart == 9 || mPart == 11) && dPart > 30)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}
		
		//If month is february then we do the appropriate validation depending upon whether the year is 
		//leap year or not.
		if ( mPart == 2 )
		{
			count++;
			if ( dPart > 29 )
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
				
			if (!(yPart % 400 == 0 || (yPart % 4 == 0 && yPart % 100 != 0)) && dPart > 28)
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}
		
		//Minutes should be between 0 and 59
		if(!dueSec.match(/^[0-9]{1,2}$/) || Number(dueSec) < 0 || Number(dueSec) >= 60)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_SECOND_VALUE + exceptionEnding;
		}

		//Minutes should be between 0 and 59
		if(!dueMin.match(/^[0-9]{1,2}$/) || Number(dueMin) < 0 || Number(dueMin) >= 60)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_MINUTE_VALUE + exceptionEnding;
		}

		//Hours should be between 0 and 23
		if(!dueHour.match(/^[0-9]{1,2}$/) || Number(dueHour) < 0 || Number(dueHour) >= 24)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INVALID_HOUR_VALUE + exceptionEnding;
		}
	}
	if(dueDate.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4}$/))
	{
		var splitString = dueDate.split(" ");
		var datePart = splitString[0];
		var mPart = Number(datePart.split('/')[0]);
		var dPart = Number(datePart.split('/')[1]);
		var yPart = Number(datePart.split('/')[2]);
		
		//Date should be between 0 and 31.	
		if (dPart < 1 || dPart > 31)
		{
			count ++;
			errorOutput = errorOutput+exceptionBeginning+ count + "." + INCORRECT_DATE+exceptionEnding;
		}
		
		//Month should be between 0 and 12.
		if (mPart < 1 || mPart > 12)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_MONTH+exceptionEnding;
		}
		
		//Year should be > 0.
		if (yPart < 0 )
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_YEAR+exceptionEnding;
		}
		
		//If month belongs to {2,4,6,9,11) then date cannot be > 30.
		if ( (mPart == 2 || mPart == 4 || mPart == 6  || mPart == 9 || mPart == 11) && dPart > 30)
		{
			count++;
			errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}
		
		//If month is february then we do the appropriate validation depending upon whether the year is 
		//leap year or not.
		if ( mPart == 2 )
		{
			count++;
			if ( dPart > 29 )
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
				
			if (!(yPart % 400 == 0 || (yPart % 4 == 0 && yPart % 100 != 0)) && dPart > 28)
				errorOutput = errorOutput + exceptionBeginning + count + "." + INCORRECT_DATE_FOR_MONTH+exceptionEnding;
		}	
	}

	return errorOutput
}

function getMonthFromString(monthString){	
	switch (monthString.toLowerCase())
	{
		case 'jan': return 1;
		case 'feb': return 2;
		case 'mar': return 3;
		case 'apr': return 4;
		case 'may': return 5;
		case 'jun': return 6;
		case 'jul': return 7;
		case 'aug': return 8;
		case 'sep': return 9;
		case 'oct': return 10;
		case 'nov': return 11;
		case 'dec': return 12;
		default : return 0;
	}
}

/*
 * This function is used to redirect the page to the appropriate page after a business 
 * Area is chosen in Add-Request form.	
 */
function onPropertyChangeSysId()
{
	//console.log("onPropertyChangedSysId called.")
	//Persist the subject and description;
	var subject = document.getElementById("subject").value;
	if (CKEDITOR.instances.description)
		CKEDITOR.instances.description.updateElement();
	var description = document.getElementById("description").value;
	if (CKEDITOR.instances.summary)
		CKEDITOR.instances.summary.updateElement();
	var summary = document.getElementById("summary");
	var selIndex = document.getElementById("sys_id").options.selectedIndex;
	var baName = new String(document.getElementById("sys_id").options[selIndex].text);
	var index = baName.indexOf("[");
	var sysPrefix = baName.substring(index + 1, baName.lastIndexOf("]"));
	var redirection = getRelativePath(window.location, "add-request") + "/" + sysPrefix;
	//Persist the subject and the description only if they are not null and non-empty.
	//else reload the page.
	if(((subject != null) && (subject != "")) || ((description != null) && (description != "")) || 
	   ((summary != null) && (summary.value != "")))
	{
		document.getElementById("redirection").value = "true";
		document.getElementById("addRequestForm").action = redirection;
		document.getElementById("addRequestForm").submit();
	}
	else
	{
	   window.location = redirection;
	}
	
}

/*
 * This function is used to redirect the page to the appropriate page after a business 
 * Area is chosen in Add-Request form for the specified system-prefix.	
 */
function onPropertyChangeSysId(sPrefix)
{
	//Persist the subject and description;
	var subject = document.getElementById("subject").value;
	if (CKEDITOR.instances.description)
		CKEDITOR.instances.description.updateElement();
	var description = document.getElementById("description").value;
	
	if (CKEDITOR.instances.summary)
		CKEDITOR.instances.summary.updateElement();
	var summary = document.getElementById("summary");
	//var selIndex = document.getElementById("sys_id").options.selectedIndex;
	//var baName = new String(document.getElementById("sys_id").options[selIndex].text);
	//var index = baName.indexOf("[");
	//var sysPrefix = baName.substring(index + 1, baName.lastIndexOf("]"));
	var redirection = getRelativePath(window.location, "add-request") + "/" + sPrefix;
	//Persist the subject and the description only if they are not null and non-empty.
	//else reload the page.
	if(((subject != null) && (subject != "")) || ((description != null) && (description != "")) || 
	   ((summary != null) && (summary.value != "")))
	{
		document.getElementById("redirection").value = "true";
		document.getElementById("addRequestForm").action = redirection;
		document.getElementById("addRequestForm").submit();
	}
	else
	{
	   window.location = redirection;
	}
	
}



/*
 * This function removes the leading and trailing white spaces.
 */
function trim(str)
{
	str = str.replace(/^\s+/, "");
	str = str.replace(/\s+$/, "");
	return str;
}

/*
 * This function removes the leading and trailing commas and semi colons.
 */
function removeRedundantUserListSeparators(str)
{
	str = str.replace(/^[;,]+/, "");
	str = str.replace(/[;,]+$/, "");
	str = str.replace(/,+/g, ";");
	str = str.replace(/;+/g, ";");
	return str;
}


function onFocusDescription()
{
   document.getElementById("description").focus();
}

/*
 * This function is used to automatically check/uncheck notify-
 * Loggers and notifyAnalysts depending upon whether notify
 * is checked or not.It is dictated by the hidden properties
 * analystStatus and LoggerStatus.
 */
function onChangeMailSend()
{
	if (document.getElementById("mailSend").checked == true)
	{
		document.getElementById("sendMail").value = true;

		if (document.getElementById("loggerStatus").value == "true")
		{
			document.getElementById("chkLoggers").disabled = false;
		}
		if (document.getElementById("mailLoggers").value == "false")
		{
			document.getElementById("chkLoggers").checked = false;
		}
		else 
		{
			document.getElementById("chkLoggers").checked = true;
		}
		
		if(ccBoxInitial == true)
		{
	        document.getElementById("cc_ids").disabled = false;
	    }
	}
	else 
	{
		document.getElementById("sendMail").value = false;
		document.getElementById("chkLoggers").disabled = true;
		document.getElementById("chkLoggers").checked = false;
		document.getElementById("cc_ids").value = "";
		document.getElementById("cc_ids").disabled = true;	
	}
}

/*
 * This function opens up the summary text box on clicking the summary link.
 */	
//function controlSummary()
//{
//    if(document.getElementById("summaryLabel").style.display == "none")
//    {
//        document.getElementById("openSummary").value = "true"
//	    //document.getElementById("summaryLabel").style.display = "block";
//	    //document.getElementById("summaryField").style.display = "block";
//	    document.getElementById("summaryLabel").style.cssText = "";
//	    document.getElementById("summary_container").style.cssText = "";
//	    document.getElementById("summary_container").style.cssText = "width: 99%; height: 100%;";
//	    return;
//    }
//    else
//    {
//	     document.getElementById("openSummary").value = "false";
//	    //document.getElementById("summaryLabel").style.display = "none";
//	    //document.getElementById("summaryField").style.display = "none";
//	    document.getElementById("summaryLabel").style.cssText = "DISPLAY: none";
//	    document.getElementById("summary_container").style.cssText = "DISPLAY: none";
//	    document.getElementById("summary_container").style.cssText = "width: 99%; height: 100%;display:none";
//	    return;
//    }  
//    
//}

/*
 * This function opens up the parent-request text box on clicking associate-request link
 */	
function associateRequest()
{
    if(document.getElementById("associate").style.display == "none")
    {
         document.getElementById("openAssociate").value = "true"
	    //document.getElementById("associate").style.display = "block";
	    document.getElementById("associate").style.cssText = "";
	    document.getElementById("parent_request_id").style.cssText = "";
	}
    else
    {
	    //document.getElementById("associate").style.display = "none";
	    document.getElementById("associate").style.cssText = "DISPLAY: none";
	    document.getElementById("parent_request_id").style.cssText = "DISPLAY: none";
	}
}

/*
 * This function opens up the Related Request text box on clicking the related-requests link
 */	
function addRelatedRequests()
{
    if(document.getElementById("related").style.display == "none")
    {
         document.getElementById("openRelated").value = "true"
	    //document.getElementById("associate").style.display = "block";
	    document.getElementById("related").style.cssText = "";
	    document.getElementById("related_requests").style.cssText = "";
	}
    else
    {
	    //document.getElementById("associate").style.display = "none";
	    document.getElementById("related").style.cssText = "DISPLAY: none";
	    document.getElementById("related_requests").style.cssText = "DISPLAY: none";
	}
}

/*
 * This function opens up the extended fields on clicking extended-fields link.
 */	
function addExtendedFields(cslist)
{
	
	var strList = cslist.split('|');
	var openExtended = 0;
	for(var i=0 ;i< strList.length; i++)
	{
	  if (document.getElementById(strList[i]) != null)
	  {
		   if(document.getElementById(strList[i]).style.display == "none")
		    {
			   var rteheaderindex=strList[i].indexOf("_rteheader");
			 
			   if(rteheaderindex>0)
			   {
				   var fieldId = strList[i].split("_",1);
				   var myEditor=YAHOO.widget.EditorInfo.getEditorById(fieldId[0]);
				   showRTE(myEditor);
				   document.getElementById(strList[i]).style.cssText = "";
			   }
			   else{
			   var index = strList[i].indexOf("_ex");
			   var index_ex = strList[i].indexOf("_ex2");
			   var index_div = strList[i].indexOf("_exdiv");
			   if(index_div > 0)
			   {
			       var fieldVal = strList[i].substring(0,index_div);
			       document.getElementById(strList[i]).style.cssText = "";
			       if(isIE)
			       {
						document.getElementById(fieldVal+"_excol").style.display = "inline";
			       }
			       else
			       {
			            document.getElementById(fieldVal+"_excol").style.display = "table-cell";
				   }
			       document.getElementById(fieldVal+"_ex").style.cssText = "";
			       continue;
			   }
			   //var exValue = strList[i].substring(0,strList[i].length - 3);
			   var exValue = strList[i].substring(0,index);
			   if(index_ex > 0)
			   {
			      document.getElementById(strList[i]).style.cssText = "width:99%; height: 100%;";
			      document.getElementById(exValue).style.cssText = "width:99%; height: 100%;";
			   }
			   else
			   {
			      document.getElementById(strList[i]).style.cssText = "";
			      document.getElementById(exValue).style.cssText = "";
			   }
			   openExtended = openExtended + 1;
	        
		    }
		    }
		    else
		    {
		       var index_div = strList[i].indexOf("_exdiv");
			   if(index_div > 0)
			   {
			       var fieldVal = strList[i].substring(0,index_div);
			       document.getElementById(strList[i]).style.cssText = "DISPLAY: none";
				   document.getElementById(fieldVal+"_excol").style.display = "none";
			       document.getElementById(fieldVal+"_ex").style.cssText = "DISPLAY: none";
			       continue;
			   }
			  
			   var rteheaderindex=strList[i].indexOf("_rteheader");
			   if(rteheaderindex>0)
			   {
				   var fieldId = strList[i].split("_",1);
				   var myEditor=YAHOO.widget.EditorInfo.getEditorById(fieldId[0]);
				   hideRTE(myEditor);
				   document.getElementById(strList[i]).style.cssText = "DISPLAY: none";
			   }
			   else
			   {   
			   var index = strList[i].indexOf("_ex");
			   var exValue = strList[i].substring(0,index);
			   document.getElementById(strList[i]).style.cssText = "DISPLAY: none";
			   document.getElementById(exValue).style.cssText = "DISPLAY:none";
			    }
			  }
	  }
    } 
    if(openExtended > 0)
    {
        document.getElementById("openExtended").value = "true";
    }     
}

function frmChanged()
{
	formChanged = true;
}

function onClickHelp()
{
    window.open(nearestPath + "web/help.htm");
}

function onClickSaveDraft()
{
    try 
    {
    	
	    var description = "";
	    if (document.getElementById("description") != null){
	    	if (CKEDITOR.instances.description)
	    		CKEDITOR.instances.description.updateElement();
		    description = document.getElementById("description").value;
		}
    	
	    var subject = "";
	    if (document.getElementById("subject") != null)
		    subject = document.getElementById("subject").value;
    	
	    var summary = "";
	    if (document.getElementById("summary") != null){
	    	if (CKEDITOR.instances.summary)
	    		CKEDITOR.instances.summary.updateElement();
		    summary = document.getElementById("summary").value; 
	    }

	    
	    if (draftTimestamp > 0)
	    {
	 	    message = "Draft updated at " + getDate();
	 	}
    	
	    if (description == '' && subject == '' && summary == '')
	    {
		    
		    return;
	    }

	    saveDraft('manual');
	   
	    return;
    }
    catch (e)
    {
		/* Also Post this error to the server */
	    alert("Error saving draft. Draft not saved.\nError Name: " + e.name + "\nError Message: " + e.message);
    } 
    return;
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
			   
 var draftMode = "";			   
function saveDraft(mode)
{
	if(isUploading())
	{
		if( mode == 'manual')
			alert("The attachments are being uploaded. Please try again after sometime.");
		
		return ;
	}
	else
	{
		setTheAttachmentField();
	}
	saveAllRTEs();
    var formObject = document.getElementById("addRequestForm" ) ;
	YAHOO.util.Connect.setForm(formObject);
	var sysPrefix = document.getElementById("sysPrefix").value;
	var url = YAHOO.transbit.tbits.contextPath + "draft/" + sysPrefix;
	var callback = 
		{ 
		  success: function(o) 
		  {	
	            var draftId = document.getElementById('draftId');
	            draftId.value=o.responseText;
	            var message = "Draft saved at " + getDate();
	    	    var spnMsg = document.getElementById("spnMessage");
	    	    if (spnMsg == null)
	    	    {
	    	        alert(message);
	    	    }
	    	    else
	    	    {
	    	        spnMsg.innerHTML = message;
	    	    }
	            
	    	   getDrafts();
	    	   updateParent();        
	      }, 
		  failure: function(o){    	 		
		    	alert( "Sorry. Your request cannot be completed, because of following reason.\n " + o.statusText ) ;	  
	  
	      }
		}
	  
	    var cObj = YAHOO.util.Connect.asyncRequest('POST', url, callback);
}
	
	

function showAttachmentPanel()
{
    var ctrl = document.getElementById("addAttachments");
    if (ctrl.style.display != "none")
    {
        ctrl.style.display = "none";
    }
    else
    {
		ctrl.style.display = "";
	    addAttachment();
    }
}
            		
function replaceAttachmentWithSpan()
{
    // Get the corresponding file control.
    var fcId = "fc_" + (totalAttCount - 1);
    var fctrl = document.getElementById(fcId);
        if (fctrl == null) return true;
    var fileName = new String(fctrl.value);
                
    // If the control is not used yet, ignore a request to display another control.
    if (fileName == "") return false;

    // Replace the backward slashes with forward slashes.
    fileName = fileName.replace(/\\/ig, "/");
            
	fctrl.style.display = "none";	
	
	var fNameList = fileName.split("/");	
	if (isExistsSameFile(fileName))
	{		
		var fName = fNameList [fNameList.length - 1];
		alert("File with name \"" + fName + "\" already attached.");		
	}

	else 
	{	
		var spId = "sp_" + (totalAttCount - 1);
		var span = document.getElementById(spId);
		var aView = document.createElement("A");
		aView.href = "javascript:viewAttachment('" + fileName + "');";
		var aViewText = document.createTextNode(fctrl.value);
		aView.appendChild(aViewText);
		span.appendChild(aView);
		
		span.style.styleFloat = "left";
		span.setAttribute("style","display:;float:left;"); 
				
		var sprmId = "sprm_" + (totalAttCount - 1);
		var span1 = document.getElementById(sprmId);
		var trId = "tr_" +   (totalAttCount - 1);
		var sprmCtrl = document.getElementById(sprmId);
		var aRem = document.createElement("A");
		aRem.href = "javascript:removeAttachment(document.getElementById('" + trId + "'));";
		var textNode = document.createTextNode("Remove");
		aRem.appendChild(textNode);
		span1.appendChild(aRem);
		
		span1.style.styleFloat = "right";
		span1.setAttribute("style","display:;float:right;");
	}
	fNameList = null;
	return true;
}
        
function addAttachment()
{
	document.getElementById("addAttachments").setAttribute("style","display:;");
	
	var table = document.getElementById("tableBody");
	if (totalAttCount > 0)
	{
		// Replace the previous one with the File Name.
		if (replaceAttachmentWithSpan() == false) return;
	}
            
	var trId = "tr_" + totalAttCount;
	var fcId = "fc_" + totalAttCount;
	var spId = "sp_" + totalAttCount;
	var sprmId = "sprm_" + totalAttCount;
            
	var tr = document.createElement("TR");
	tr.id	= trId;
	tr.name	= trId;
            
	var td = document.createElement("TD");
	
	var fc = document.createElement("INPUT");
	fc.id	= fcId;
	fc.name = fcId;
	fc.className = "sx";
	fc.type	= "file";
	fc.size = "60";
	fc.onchange = function() {addAttachment();};
	        
	var sp = document.createElement("SPAN");
	sp.id	= spId;
	sp.className = "sx";
        
    var sp1 = document.createElement("SPAN");
    sp1.id	= sprmId;
    sp1.className = "sx";

	td.appendChild(fc);
    td.appendChild(sp); 
	td.appendChild(sp1); 
	tr.appendChild(td);
	table.appendChild(tr);
	totalAttCount = totalAttCount + 1;
}
            		
function viewAttachment(fileName)
{
	var child = window.open(fileName, rndCount);
	child.focus();
	rndCount = rndCount + 1;
}

function removeAttachment(tr)
{
    var table = document.getElementById("tableBody");
    table.removeChild(tr);

    var rows = table.rows;
    if (rows.length == 0)
    {
	    showAttachmentPanel();
    }
}
            
function onKeyDownField(e)
{
//	console.log("onKeyDownField called.");
	// On <Enter>
    var event = (!document.all) ? e : window.event;
    var keyCode = (!document.all) ? e.which : event.keyCode;
	if (keyCode == 13 && (event.ctrlKey == true || event.ctrlLeft == true))
	{
		return true; // this event will bubble up to the form.
	}
	return false;
}
function onKeyPressForm(e)
{
//	console.log("onKeyPressForm called.");
	// alert('key down called');
	// On <Enter>
    var event = (!document.all) ? e : window.event;
	var target = (!document.all) ? event.target.type : event.srcElement.type;
    var keyCode = (!document.all) ? e.which : event.keyCode;
	// alert(target);
	if ((keyCode == 13) && (target != 'textarea') && (target != 'submit') && (target != 'button') && (target != 'a'))
	{
		event.cancelBubble = true;
		event.returnValue = false;

		if (event.stopPropagation) {
			event.stopPropagation();
			event.preventDefault();
		}
		// alert('got the enter and is not text area')
		if(event.ctrlKey == true || event.ctrlLeft == true)
		{				
			return onSubmit();
		}
		
		return false;
	}
	return true;
}
	    
function calendarSetup()
{
	var timeDisabled = document.getElementById("due_time_disabled").value;
	if(timeDisabled == "true")
	{
		Calendar.setup({
		inputField  : "due_datetime",
		button      : "_due_date_img_cal",
		ifFormat	: "%m/%d/%Y"
		//showsTime	: "false",
		//timeFormat	: "24"
		});
	}
	else
	{
		Calendar.setup({
		inputField  : "due_datetime",
		button      : "_due_date_img_cal",
		ifFormat	: "%m/%d/%Y %H:%M",
		showsTime	: "false",
		timeFormat	: "24"
		});
	}
}

function calendarSetup(webDateFormat)
{
	var timeDisabled = document.getElementById("due_time_disabled").value;
	var dateFormat = getCalDateFormat(webDateFormat, timeDisabled);
	if(timeDisabled == "true")
	{
		Calendar.setup({
		inputField  : "due_datetime",
		button      : "_due_date_img_cal",
		ifFormat	: dateFormat
		});
	}
	else
	{
		Calendar.setup({
		inputField  : "due_datetime",
		button      : "_due_date_img_cal",
		ifFormat	: dateFormat,
		showsTime	: "false",
		timeFormat	: "24"
		});
	}
}

/*
 * This function toggles the time option in the calendar 
 */
 
 function onClickDisableTime()
 {
	 var dateFormat = document.getElementById("webDateFormat").value;
     var timeCtrl = document.getElementById("due_time_disabled");
	 var webDateFormat = "";
     if(timeCtrl != null)
       {
          var timeOption = timeCtrl.value;		 
          if(timeOption == "false")
            {              	
				 //document.getElementById("disableTime").innerText = "Enable Time";
				 document.getElementById("due_time_disabled").value = "true";
				 timeOption = timeCtrl.value;
				 webDateFormat = getCalDateFormat(dateFormat, timeOption);
				 Calendar.setup({
					inputField  : "due_datetime",
					button      : "_due_date_img_cal",
					ifFormat	: webDateFormat
					//showsTime	: "false",
					//timeFormat	: "24"
				});
				var due_date = document.getElementById("due_datetime");
				if(due_date != null)
				{
				   var due_date_value = due_date.value;
				   var splitString = due_date_value.split(" ");
		           var datePart = splitString[0];
				   due_date.value = datePart;
				}
            }
			else if (timeOption == "true")
            {
				//document.getElementById("disableTime").innerText = "Disable Time";
				document.getElementById("due_time_disabled").value = "false";
				timeOption = timeCtrl.value;
				webDateFormat = getCalDateFormat(dateFormat, timeOption);
              	Calendar.setup({
				inputField  : "due_datetime",
				button      : "_due_date_img_cal",
				ifFormat	: webDateFormat,
				showsTime	: "false",
				timeFormat	: "24"
				});
				var due_date = document.getElementById("due_datetime");
				if((due_date != null) && (trim(due_date.value) != ''))
				{
				   due_date.value = due_date.value + " 23:59:00";
				}				
            }
       }
 
 }

function getCalDateFormat(webFormat, timeDisabled){
	var calDateFormat = "";

	if (webFormat == "dd-MMM-yyyy")
		calDateFormat = "%d-%b-%Y";
	else if((webFormat == "MM/dd/yyyy HH:mm:ss") || 
				(webFormat == "MM/dd/yyyy HH:mm:ss.SSS") || 
					(webFormat == "MM/dd/yyyy HH:mm:ss zzz")){
		if (timeDisabled == "true")
			calDateFormat = "%m/%d/%Y";
		else
			calDateFormat = "%m/%d/%Y %H:%M:%S";
	}
	else if(webFormat == "yyyy-MM-dd"){
		calDateFormat = "%Y-%m-%d";
	}
	else if(webFormat == "MMM dd, yyyy"){ 
		calDateFormat = "%b %d, %Y";
	}
	else if(webFormat == "dd-MMM-yyyy HH:mm:ss"){ 
		if (timeDisabled == "true")
			calDateFormat = "%d-%b-%Y";
		else
			calDateFormat = "%d-%b-%Y %H:%M:%S";
	}
	else if(webFormat == "MM/dd/yyyy")
		calDateFormat = "%m/%d/%Y";
	else {
		//yyyy -> %Y
		// yy  -> %y
		// MMM -> %b
		// MM  -> %m
		// dd  -> %d
		// HH  -> %H
		// mm  -> %M
		// SSS/zzz -> nothing
		// ss -> %S
		var formatMap = {};
		formatMap["yyyy"] = "%Y";
		formatMap["yy"] = "%y";
		formatMap["MMM"] = "%b";
		formatMap["MM"] = "%m";
		formatMap["dd"] = "%d";
		formatMap["HH"] = "%H";
		formatMap["mm"] = "%M";
		formatMap["ss"] = "%S";
		formatMap["SSS"] = "";
		formatMap["zzz"] = "";
		var calDateFormat = webFormat;
		for(var key in formatMap)
		{
			calDateFormat = calDateFormat.replace(key, formatMap[key]);
		}
	}
	return calDateFormat;
}

 function onClickDisableTimePrev()
 {
     var timeCtrl = document.getElementById("due_time_disabled");
     if(timeCtrl != null)
       {
          var timeOption = timeCtrl.value;
          if(timeOption == "false")
            {
              	Calendar.setup({
				inputField  : "due_datetime",
				button      : "_due_date_img_cal",
				ifFormat	: "%m/%d/%Y"
				//showsTime	: "false",
				//timeFormat	: "24"
				});
				var due_date = document.getElementById("due_datetime");
				if(due_date != null)
				{
				   var due_date_value = due_date.value;
				   var splitString = due_date_value.split(" ");
		           var datePart = splitString[0];
				   due_date.value = datePart; 
				}
				//document.getElementById("disableTime").innerText = "Enable Time";
				document.getElementById("due_time_disabled").value = "true";
            }
           if(timeOption == "true")
            {
              	Calendar.setup({
				inputField  : "due_datetime",
				button      : "_due_date_img_cal",
				ifFormat	: "%m/%d/%Y %H:%M:%S",
				showsTime	: "false",
				timeFormat	: "24"
				});
				var due_date = document.getElementById("due_datetime");
				if((due_date != null) && (trim(due_date.value) != ''))
				{
				   due_date.value = due_date.value + " 23:59:00";
				}
				//document.getElementById("disableTime").innerText = "Disable Time";
				document.getElementById("due_time_disabled").value = "false";
            }
       }
 
 }
	
function onChangeCategory()
{
    var sysPrefix = document.getElementById("sysPrefix").value;	
    var systemId = document.getElementById("systemId").value;
    var categoryId = document.getElementById("category_id").value;
    var url = getRelativePath(window.location, "search-util") + "/" + sysPrefix + "?assigneeInfo=true&systemId=" + systemId + 
	        "&categoryId=" + categoryId + "&now=" + (new Date()).getTime();
    var assigneeContent =  executeHttpRequest("GET",url,false) ;
    var assigneeCollection = eval(assigneeContent);
    registerCollection("assignee_ids",assigneeCollection);          
} 


function isExistsSameFile(fileName){		
	var oRows = document.getElementById('attTable').getElementsByTagName('tr');
	var iRowCount = oRows.length;
	var tempCtrl = null;
	if (iRowCount == 1)
	{
		return false;
	}
	if (iRowCount > 1)
	{
		var count = totalAttCount - 1;
		for (i = count ; i > 0; i--)
		{
			tempCtrl = document.getElementById ("fc_" + (i - 1)); 
			if (tempCtrl == null)
			{ 
				continue;
			}
			else
			{			
				var fname = fileName.split ("/");
				var tempArr = tempCtrl.value.replace(/\\/ig, "/").split("/");
				if (fname [fname.length - 1] == tempArr[tempArr.length - 1])
				{	
					removeAttachment (document.getElementById ("tr_" + (totalAttCount - 1)));
					return true;					
				}
			}
		}	
	}
	return false;
}


