<%=previewButtons%>
<script type="text/javascript">
var is_IE = false ;
</script> 
<!--[if IE]>
<script type="text/javascript">
is_IE = true ;
</script>
<![endif]-->
<script type="text/javascript">

// trim function for strings
String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

var myErrors = "" ; // global variable : be careful
var myFirstLoad = true ;

var userMap = <%=user_agency_map_value%> ;

function changeOriginator()
{
	var logger = document.getElementById('<%=logger_field_name%>' ) ;
	var originator = document.getElementById('<%=originator_field_name%>' ) ;	
		
	setAllOptionsDisable(originator, true) ;
	
	var myLogger = "" ;
	var allLoggers = removeRedundantUserListSeparators(logger.value) ;
	var loggerArray = allLoggers.split(';');
	if( loggerArray.length > 0 )
		myLogger = loggerArray[0].trim() ;
	else
	{
		originator.selectedIndex = -1 ;
		if( is_IE == true )
			emulate(originator)
			
		return false ;
	}
	var orgName = null ;  
	for(var i = 0 ; i < userMap.length ; i++ )
	{
		if( userMap[i].userLogin == myLogger )
		{
			orgName = userMap[i].agency ;
		}
	}
	
	var ret = enableAndSelectOption( originator, orgName ) ;
	
	if( ret == false )
	{
		myErrors += 'No agency mapped to user ( ' + myLogger + ' )<br>' ;
		return false ;
	}	
	
	if( is_IE == true )
		emulate(originator)
		
	return true ;
}


function changeRecepient() 
{	
	var assignee = document.getElementById('<%=assignee_field_name%>' ) ;
	var recepient = document.getElementById('<%=recepient_field_name%>' ) ;	
	
	setAllOptionsDisable(recepient, true) ;	

	var myAssignee = "" ;
	var allAssignees = removeRedundantUserListSeparators(assignee.value) ;
	var assArray = allAssignees.split(';');
	if( assArray.length > 0 )
		myAssignee = assArray[0].trim() ;
	else
	{
		recepient.selectedIndex = -1 ;
		if( is_IE == true )
			emulate(recepient)
			
		return false ;
	}
	var recName = null ;  
	for(var i = 0 ; i < userMap.length ; i++ )
	{
		if( userMap[i].userLogin == myAssignee )
		{
			recName = userMap[i].agency ;
		}
	}
	
	var ret = enableAndSelectOption( recepient, recName ) ;
	
	if( ret == false )
	{
		myErrors += 'No agency mapped to user ( ' + myAssignee + ' )<br>' ;
		return false ;
	}	
	
	if( is_IE == true )
		emulate(recepient)
		
	return true ;
}


function enableAndSelectOption(select, optionName )
{
	if( null == select )
	{
		return false ;
	}
	if( null == optionName )
	{
		select.selectedIndex = -1 ;
		return false ;
	}
	
	for( var i = 0 ; i < select.length ; i++ )
	{
		if( select.options[i].value == optionName )
		{
			select.selectedIndex = i ;
			select.options[i].disabled = false ;
			return true ;
		}
	}

	if( is_IE == true )
		emulate(select);
	
	return false; 
}

// set the disabled attribute of 'select's option with value = 'optionName' to 'disable' 
function setSelectOptionDisability(select, optionName, disable)
{
	if( null == select || null == optionName )
		return false ;
	
	for( var i = 0 ; i < select.length ; i++ )
	{
		if( select.options[i].value == optionName )
		{
			select.options[i].disabled = disable ;
			
			if(is_IE == true )
				emulate(select);
			
			return true ;
		}
	}
	
	return false; 
}

function handleOnChangeGenerate()
{
	var generate = document.getElementById('<%=generate_field_name%>');	
	var actionOnly = '<%=generate_action_only_value%>' ;
	var genFileAndNo = '<%=generate_file_and_no_value%>'
	var corrNo = document.getElementById('<%=corr_no_field_name%>');
	
//	console.log("onChange Generate called ") ;
	if( generate.options[generate.selectedIndex].value == actionOnly )
	{
		// clear the corr. no. mark it disabled
		if( null != corrNo )
		{
			corrNo.value = '' ;
			corrNo.disabled = true ;
		}
		return false ;
	}
	else
	{
		if( generate.options[generate.selectedIndex].value == genFileAndNo )
		{
			corrNo.value = '' ;
			corrNo.disabled = true ;
		}
		else
		{
//			corrNo.value = '' ;
			corrNo.disabled = false ;
		}
		
		return true ;
	}
}

function handleOnChangeCorrType()
{
//	console.log("handleOnChangeCorrType called.");
	var corrType = document.getElementById('<%=corrType_field_name%>');
	var ionCorrTypeValue = '<%=ion_value%>' ;
	var allowedAgencies = <%=allowedAgencies%> ; // array of allowed agencies eg. ['NCCP','NCCB']
	var ccs = document.getElementById("<%=cc_field_name%>");
	var subs = document.getElementById("<%=subs_field_name%>");
	var loggers = document.getElementById("<%=logger_field_name%>");
	var assignees = document.getElementById("<%=assignee_field_name%>");

	if( null == corrType || null == ccs || null == subs || null == loggers || null == assignees )
	{
		myErrors += "Cannot access the form properly. Please select fields manually." ;
		return true ;
	}
	
	if( corrType.options[corrType.selectedIndex].value == ionCorrTypeValue )
	{
		var allAssignees = removeRedundantUserListSeparators(assignees.value) ;
		var assArray = allAssignees.split(';');
		var allLoggers = removeRedundantUserListSeparators(loggers.value) ;
		var logArray = allLoggers.split(';');
		var allccs = removeRedundantUserListSeparators(ccs.value) ;
		var ccArray = allccs.split(';');
		var allSubs = removeRedundantUserListSeparators(subs.value) ;
		var subArray = allSubs.split(';');
		
		var illAss = [] ;
		for( var i = 0 ; i < assArray.length ; i++ )
		{
			var v = assArray[i];
			if( !isPresent(v,allowedAgencies,userMap) )
			{
				illAss.push( v );
			}
		}
		
		var illLog = [] ;
		for( var i = 0 ; i < logArray.length ; i++ )
		{
			var v = logArray[i];
			if( !isPresent(v,allowedAgencies,userMap) )
			{
				illLog.push(v) ;
			}
		}
		
		var illCc = [] ;
		for( var i = 0; i < ccArray.length ; i++ )
		{
			var v = ccArray[i];
			if( !isPresent(v,allowedAgencies,userMap) )
			{
				illCc.push(v) ;
			}
		}
		
		var illSub = [] ;
		for( var i = 0 ; i < subArray.length ; i++ )
		{
			var v = subArray[i];
			if( !isPresent(v,allowedAgencies,userMap) )
			{
				illSub.push(v) ;
			}
		}
		
		var errors = "" ;
		var retValue = true ;
		if( illLog != "" )
		{
			errors += "<br /> in Loggers : " + illLog ;
			retValue = false ;
		}
		if( illAss != "" )
		{
			errors += "<br /> in Assignee : " + illAss ;
			retValue = false ;
		}
		if( illCc != "" )
		{
			errors += "<br /> in CCs : " + illCc ;
			retValue = false ;
		}
		if( illSub != "" )
		{
			errors += "<br /> in Subscribers : " + illSub ;
			retValue = false ;
		}
		if( retValue == false )
		{
			myErrors += "Following users are not allowed" + errors + "<br />as they do not belong to " + allowedAgencies.toString()  ;			
		}
		
		return retValue ;
	}		
	
	return true; 
}

function isPresent( x , allowedAgenciesArray, userAgencyJson )
{
	if( null == allowedAgenciesArray || null == x || null == userAgencyJson)
		return false ;
	else
	{
		for( var i = 0 ; i < userAgencyJson.length ; i++ )
		{			
			if( userAgencyJson[i].userLogin == x )
			{
				for( var j = 0 ; j < allowedAgenciesArray.length ; j++ )
				{
					
					if( allowedAgenciesArray[j] == userAgencyJson[i].agency )
						return true ;
				}
				
				return false;
			}
		}
		
		return false ;
	}
}

function reEvaluate( event, myID )
{
//	window.console.log("reEvaluate called");
	myErrors = "" ;	
	var generate = document.getElementById('<%=generate_field_name%>');	
	var previewPDFButton = document.getElementById('<%=previewPDFButton_field_name%>') ;
	var submitButton = document.getElementById('<%=submitButton_field_name%>') ;
	var genAtt = document.getElementById('<%=general_attributes_field_name%>');
	var wbsAtt = document.getElementById('<%=wbs_attributes_field_name%>');
	var corrType = document.getElementById('<%=corrType_field_name%>');
		
	var retOnChangeGenerate = handleOnChangeGenerate();
	var retChangeOriginator = changeOriginator() ;
	var retChangeRecepient = changeRecepient() ;
	var retOnChangeCorrType = handleOnChangeCorrType() ;
//	console.log("return values : " + retOnChangeGenerate + " : " + retChangeOriginator + " : " + retChangeRecepient + " : " + retOnChangeCorrType );
	if( retChangeOriginator == false || retChangeRecepient == false || retOnChangeGenerate == false || retOnChangeCorrType == false)
	{		
		previewPDFButton.disabled = true ;		
	}
	else
	{
		previewPDFButton.disabled = false ;		
	}
	
	if( retChangeOriginator == false || retChangeRecepient == false || retOnChangeCorrType == false )
	{		
		submitButton.disabled = true ;		
	}
	else
	{
		submitButton.disabled = false ;		
	}
	
	
	if( myFirstLoad == false )
	{	
		if( myErrors == "" )
		{
			document.getElementById("exceptionBlock").style.display='none' ;
		}
		else
		{
			showAutomaticRestrictions(myErrors) ;
		}
	}
	myFirstLoad = false ;
}

function emulate( selObj )
{
	// this function changes the color and background of select options
	// required for browser is IE
	if( selObj != null )
	{
		for( var i = 0 ; i < selObj.length ; i++ )
		{
			if( selObj.options[i].disabled == true )
			{
				//selObj.options[i].style.background = '#FFFFFF' ;
				selObj.options[i].style.color = '#DCDCDC' ;
			}
			else
			{
				//selObj.options[i].style.background = '#FFFFFF' ;
				selObj.options[i].style.color = '#000000' ;
			}
		}
	}
}

// selectObject : any select DOM
//disableValue = true/false 
function setAllOptionsDisable( selectObject, disableValue ) 
{
	if( null != selectObject )
	{
		for( var i = 0 ; i < selectObject.length ; i++ )
		{
			selectObject.options[i].disabled = disableValue ;
		}
	}
	
	if( is_IE == true )
		emulate( selectObject ) ;
}

function restore(e) {
//	alert("inside restore");
	if( e.selectedIndex >= 0 )
	{
		if (true == e.options[e.selectedIndex].disabled) {
			e.selectedIndex = window.select_current[e.id];
		}
	}
}

function prefillCallBack() 
{	
	var showMessages = <%=showMessages_value%> ;
	var message = '<%=messages_value%>' ;
	
	var originator = document.getElementById('<%=originator_field_name%>' );	
	var recepient = document.getElementById('<%=recepient_field_name%>');
	var logger = document.getElementById('<%=logger_field_name%>' );	
	var assignee = document.getElementById('<%=assignee_field_name%>');
	var generate = document.getElementById('<%=generate_field_name%>');
	var genAtt = document.getElementById('<%=general_attributes_field_name%>');
	var wbsAtt = document.getElementById('<%=wbs_attributes_field_name%>');
	var corrType = document.getElementById('<%=corrType_field_name%>');
	var ionCorrTypeValue = '<%=ion_value%>' ;
	var isUpdate = <%=isUpdate%> ;
	var wasLastION = <%=wasLastION%> ; // true ( if it is update and last value of corrType was ion.) else false.
	var ccs = document.getElementById("<%=cc_field_name%>");
	var subs = document.getElementById("<%=subs_field_name%>");
	
	setAllOptionsDisable(recepient, true ) ;
	setAllOptionsDisable(originator, true ) ;
	
	if( null != corrType )
	{
		if( isUpdate == true )
		{
			if( wasLastION == true )
			{
				// disable other than ion
				setAllOptionsDisable(corrType, true);
				enableAndSelectOption(corrType, ionCorrTypeValue );
			}
			else
			{			
				//disable ION
				setSelectOptionDisability(corrType, ionCorrTypeValue, true);
			}
		}
	}
	if( null != logger )
	{
		YAHOO.util.Event.addListener(logger, "blur", reEvaluate, logger.id );
	}
	
	if( null != ccs )
	{
		YAHOO.util.Event.addListener(ccs, "blur", reEvaluate, ccs.id );
	}
	
	if( null != subs )
	{
		YAHOO.util.Event.addListener(subs, "blur", reEvaluate, subs.id );
	}
	
	if( null != assignee )
	{		
		YAHOO.util.Event.addListener(assignee, "blur", reEvaluate, assignee.id );
	}
	
	if( null != generate )
	{		
		YAHOO.util.Event.addListener(generate, "change", reEvaluate, generate.id );
	}
	
	if( null != corrType )
	{		
//		console.log("registering corrType change.");
		YAHOO.util.Event.addListener(corrType, "change", reEvaluate, corrType.id );
	}
//	else
//	{
//		console.log("corrType was null and hence not registered with reEvaluate");
//	}
	
	if( true == is_IE )
	{
		//alert( "is_IE == true")
		var selArray = new Array();
		selArray[0] = originator ;
		selArray[1] = recepient ;
		selArray[2] = corrType ;
				
		if (selArray.length > 0) 
		{
			window.select_current = new Array();

			for (var i=0, select; select = selArray[i]; i++) 
			{
				//alert( " for IE : select = " + select.id ) ;	
				select.onfocus = function(){ window.select_current[this.id] = this.selectedIndex; }
				select.onchange = function(){ restore(this); }
				emulate(select);
			}
		}
	}
	
	reEvaluate() ;
		
	if( showMessages == true ) 
		showAutomaticRestrictions( message ) ;
}

// register the window load event
YAHOO.util.Event.addListener(window, "load", prefillCallBack);

function onPreview( format ) 
{
	// call=CorrespondencePreview
	// create the form object
	//YAHOO.widget.EditorInfo.getEditorById("description").saveHTML();
	saveAllRTEs();	
	//Initialize errorOutput;
	errorOutput = "";
	count = 0;
    if (totalAttCount > 0)
    {
        document.getElementById("addRequestForm").enctype = "multipart/form-data";
        document.getElementById("addRequestForm").encoding = "multipart/form-data";
    }
    else
    {
        document.getElementById("addRequestForm").encType = "";
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
    ctl = document.getElementById("logger_ids");
    if (ctl != null)
    {
         var list = removeRedundantUserListSeparators(ctl.value);
         var userArray = list.split(";");
         if( userArray.length > 1 )
        	 errorOutput += " More than one logger not allowed." ;
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
//         if( userArray.length > 1 )
//        	 errorOutput += " More than one assignee not allowed." ;
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
  
	//If error output is not null, then show all the exceptions in the show-exception
	// div.
	if(errorOutput!= "")
	{
	    showAutomaticRestrictions(errorOutput);
		return false;
	}

	var formObject = document.getElementById("addRequestForm" ) ;
	YAHOO.util.Connect.setForm(formObject);
	// add one more parameter "requestId" in case of update-request
	var url = YAHOO.transbit.tbits.contextPath + 'proxy/corr-preview?format='+format ;
	  var callback = 
		{ 
		  success: function(o) 
		  {	
	            var url1 = YAHOO.transbit.tbits.contextPath + 'proxy/corr-preview?filename=' + o.responseText ;
	            window.open( url1 ) ;	       
	      }, 
		  failure: function(o){    	 		
		    	alert( "Sorry. Your request cannot be completed, because of following reason.\n " + o.statusText ) ;	  
	  
	      }
		}
	  
	    var cObj = YAHOO.util.Connect.asyncRequest('POST', url, callback);
	    alert( "Please Wait while we generate preview for you.\nPlease Click OK.") ;
}
</script>
