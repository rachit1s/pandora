<%=previewButtons%>
<script type="text/javascript">
var is_IE = false ;
</script> 
<!--[if IE]>
<script type="text/javascript">
is_IE = true ;
//alert( "Yes this is IE") ;
</script>
<![endif]-->
<script type="text/javascript">

// trim function for strings
String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

function validateCorrInit()
{	
	var userCorrInitMap = <%=userCorrInitMap_value%> ; // json array
	var userCorrAgencyMap = <%=userCorrAgencyMap_value%> ;
	var logger = document.getElementById('<%=logger_field_name%>' ) ;
	var corrInit = document.getElementById('<%=correspondence_initiator_field_name%>') ;
	var corrGenAgency = document.getElementById('<%=corr_generation_agency_field_name%>') ;

	if( null == logger || null == corrInit )
		return false ;

	setAllOptionsDisable( corrGenAgency, true ) ;
	
	var myLoggers = removeRedundantUserListSeparators(logger.value)
	var loggerArray = myLoggers.split(";") ;
	

	var noOfLoggers = loggerArray.length ;
	if( noOfLoggers > 1 )
	{
		myErrors += "Only single logger is allowed.<br />" ;
		return false ;
	}
	
	var loggerValue = loggerArray[0].trim() ;
	var corrInitIndex = -1 ;
	var corrAgencyIndex = -1 ;
	
	for( var i = 0 ; i < userCorrInitMap.length ; i++ )
	{
		if( userCorrInitMap[i].userLogin == loggerValue )
		{
			corrInitIndex = userCorrInitMap[i].index ;
			break ;
		}
	}

	for( var i = 0 ; i < userCorrAgencyMap.length ; i++ )
	{
		if( userCorrAgencyMap[i].userLogin == loggerValue )
		{
			corrAgencyIndex = userCorrAgencyMap[i].index ;
			break ;
		}
	}
	
	corrGenAgency.selectedIndex = corrAgencyIndex ;
	if( corrInitIndex >= 0 && corrAgencyIndex >= 0 )
	{
		corrGenAgency.options[corrAgencyIndex].disabled = false ;
		if( true == is_IE )
		{		
			emulate( corrGenAgency ) ;
		}
		return true ;
	}
	else
	{	
		corrAgencyIndex.selectedIndex = -1 ; 
		myErrors += "Illegal logger (" + loggerValue + ").<br />" ;
		return false ;
	}	
}

function changeAssSub()
{
	//alert( "changeAssSub called.") ;
	var userMap = <%=userMap_value%> ; // json array for all WPCL and for loggerSEPCO

	var type = document.getElementById('<%=type_field_name%>') ;
	var logger = document.getElementById('<%=logger_field_name%>' ) ;
	var assignee = document.getElementById('<%=assignee_field_name%>') ;
	var subscriber = document.getElementById('<%=subscriber_field_name%>') ;
	var previewPDFButton = document.getElementById('<%=previewPDFButton_field_name%>') ;
	var submitButton = document.getElementById('<%=submitButton_field_name%>') ;	
	
	if( null == type || null == logger || null == assignee || null == subscriber || type.selectedIndex < 0 )
	{
		if( assignee != null )
			assignee.value = "" ;
		if( subscriber != null )
			subscriber.value = "" ;
		return false ;
	}

	var option = type.options[type.selectedIndex] ;
	var ovalue = option.value ;
	
	var myLoggers = removeRedundantUserListSeparators(logger.value);
	var loggerArray = myLoggers.split(";") ;	
	var loggerValue = loggerArray[0].trim() ;
	var mapIndex = null ;
	
	var allowedCorrTypes = "" ;
	
	if( null != userMap )
	{		
		for( var i = 0 ; i < userMap.length ; i++ )
		{
			if( userMap[i].userLogin == loggerValue )
			{
				allowedCorrTypes += userMap[i].corrDisplayName + ",";
				
				if( userMap[i].corr == ovalue )
					mapIndex = i ;				
			}
		}
	}

	assignee.readOnly = true ;
	if( null != mapIndex )
	{
			assignee.value = userMap[mapIndex].to.join(",") ;			
			subscriber.value = userMap[mapIndex].cc.join(",") ;
	//		previewPDFButton.disabled = false ;
	//		submitButton.disabled = false ;
	//		document.getElementById("exceptionBlock").style.display='none' ;
			return true ;
	}
	else
	{
			myErrors += loggerValue + "'s valid Corr.Types are : " + allowedCorrTypes + "<br />";
		//	showAutomaticRestrictions( myErrors ) ;
			assignee.value = "" ;			
			subscriber.value = "" ;
		//	previewPDFButton.disabled = true ;
		//	submitButton.disabled = true ;
			return false ;
	}
}

function changeTypeDisability()
{
	var userMap = <%=userMap_value%> ; // json array for all WPCL and for loggerSEPCO
	var type = document.getElementById('<%=type_field_name%>') ;
	var logger = document.getElementById('<%=logger_field_name%>' ) ;

	if( null == logger || null == type )
		return false ;
		
	var myLoggers = removeRedundantUserListSeparators(logger.value) ; 
	var loggerArray = myLoggers.split(";") ;	
	var loggerValue = loggerArray[0].trim() ;

	changeAllColors( type, true ) ;
	
	for( var i = 0 ; i < userMap.length ; i++ )
	{
		if( userMap[i].userLogin == loggerValue )
		{
			for( var j = 0 ; j < type.length ; j++ )
			{
				if( type.options[j].value == userMap[i].corr )
				{
					changeColor(type.options[j], false ) ;
				}
			}			
		}	
	}

	var retChangeAssSub = changeAssSub() ;
	if( retChangeAssSub == false )
	{		
		return false ;
	}
	else
	{
		return true ;
	}
}

var myErrors = "" ; // global variable : be carefull
var myFirstLoad = true ;
function reEvaluate( event, myID )
{
	myErrors = "" ;
	
	var type = document.getElementById('<%=type_field_name%>') ; 	
	var genCorr = document.getElementById('<%=generate_correspondence_field_name%>') ;	
	var corrGenAgency = document.getElementById('<%=corr_generation_agency_field_name%>') ;
	var logger = document.getElementById('<%=logger_field_name%>' ) ;
	var assignee = document.getElementById('<%=assignee_field_name%>') ;
	var subscriber = document.getElementById('<%=subscriber_field_name%>') ;
	var corrInit = document.getElementById('<%=correspondence_initiator_field_name%>') ;
	var corrProt = document.getElementById('<%=corr_prot_field_name%>') ;
	var previewPDFButton = document.getElementById('<%=previewPDFButton_field_name%>') ;
	var submitButton = document.getElementById('<%=submitButton_field_name%>') ;
	
	var corrProtWPCLSEPCOIndex = <%=corrProtWPCLSEPCOIndex_value%> ;
	var corrProtOthersIndex = <%=corrProtOthersIndex_value%> ;
		
	YAHOO.util.Event.removeListener(type,'change',reEvaluate ) ;
	
	assignee.readOnly = false ;
	var retValidateCorrInit = validateCorrInit() ;
	var retChangeTypeDisability = true ;

	if( corrProt.selectedIndex == corrProtWPCLSEPCOIndex )
	{
		// change the ass, sub and types acc to log
		retChangeTypeDisability = changeTypeDisability() ;
		YAHOO.util.Event.addListener(type,'change',reEvaluate ) ;
		assignee.readOnly = true ;
	}	
	//check caller here first ... this is must ... 
	else if( myID == corrProt.id )
	{
		assignee.value = "" ;
		subscriber.value = "" ;
	}
		
	if( genCorr.checked == false || retValidateCorrInit == false || retChangeTypeDisability == false )
	{
		previewPDFButton.disabled = true ;
	}
	else
	{
		previewPDFButton.disabled = false ;
	}
	
	if( retValidateCorrInit == false || retChangeTypeDisability == false )
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

function changeAllColors( selObj, blur ) 
{
	if( null != selObj )
	{
		for( var i = 0 ; i < selObj.length ; i++ )
		{
			changeColor(selObj.options[i], blur) ;
		}
	}
}

function changeColor( option, blur )
{
	// this function changes the color and background of select options
	if( option != null )
	{
		if( blur == true )
		{
			option.style.color = '#DCDCDC' ;
		}
		else
		{
			option.style.color = '#000000' ;
		}		
	}
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
	var genCorr = document.getElementById('<%=generate_correspondence_field_name%>') ;	
	var corrGenAgency = document.getElementById('<%=corr_generation_agency_field_name%>') ;	
	var corrInit = document.getElementById('<%=correspondence_initiator_field_name%>') ;
	var type = document.getElementById('<%=type_field_name%>') ;
	var logger = document.getElementById('<%=logger_field_name%>' ) ;
	var corrProt = document.getElementById('<%=corr_prot_field_name%>') ;
	
	if( null != corrGenAgency )
	{
         setAllOptionsDisable( corrGenAgency, true ) ;
		 var corrAgencySelectedIndex = <%=corGenAgencyIndex_value%> ;
		 corrGenAgency.options[corrAgencySelectedIndex].disabled = false ;		 
		 corrGenAgency.selectedIndex = corrAgencySelectedIndex ;
	}
	
	if( null != genCorr )
	{
		genCorr.checked = true ;
		YAHOO.util.Event.addListener(genCorr, "click", reEvaluate, genCorr.id );
	}
	
	if( null != corrInit )
	{
		setAllOptionsDisable( corrInit , true ) ;
		var selIndex = <%=corrInitSelectedIndex_value%> ;
		corrInit.options[selIndex].disabled = false ;
		corrInit.selectedIndex = selIndex ;		
	}
	
	if( null != logger )
	{
		logger.value = '<%=logger_value%>' ;
		YAHOO.util.Event.addListener(logger, "blur", reEvaluate, logger.id );
	}
	
	if( null != corrProt )
	{
		YAHOO.util.Event.addListener(corrProt, "change", reEvaluate , corrProt.id );
	}
	
	if( true == is_IE )
	{
		//alert( "is_IE == true")
		var selArray = new Array();
//		selArray[0] = type ;
		selArray[1] = corrGenAgency ;
		selArray[2] = corrInit ;
				
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
	var showMessages = <%=showMessages_value%> ;
		
	if( showMessages == true ) 
		showAutomaticRestrictions( '<%=messages_value%>' ) ;
	
	// to show the extended fields 
	addExtendedFields('sms_id_ex|SendSMS_ex|display_group_header_64_ex|display_group_footer_64_ex|GenerateCorrespondance_ex|CorrespondanceNumber_ex|InternalWPCLCirculationList_ex|InternalWPCLCirculationList_exdiv|CorrGenerationAgency_ex|CorrGenerationAgency_exdiv');
}

// register the window load event
YAHOO.util.Event.addListener(window, "load", prefillCallBack);


function onPreview( format ) 
{	// function that handle the Preview button click event
	 // call=CorrespondencePreview
	// create the form object
	saveAllRTEs();    
    var caller = trim(document.getElementById("caller").value);
	// Initialize errorOutput;
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
	
// Check if the attachment process is running	
// Update the attachments
	
	if(isUploading())
	{
		errorOutput += "The attachments are being uploaded. Please try again after sometime.";
	}
	else
	{
		setTheAttachmentField();
	}
    // Validate all the users( assignees, loggers,subscribers and cc list)
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
         if( userArray.length > 1 )
        	 errorOutput += " More than one assignee not allowed." ;
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
 	
	// If error output is not null, then show all the exceptions in the
	// show-exception
	// div.
	if(errorOutput!= "")
	{
	    showAutomaticRestrictions(errorOutput);
		return false;
	}

	var formObject = document.getElementById("addRequestForm" ) ;
	YAHOO.util.Connect.setForm(formObject);
	// add one more parameter "requestId" in case of update-request
	var requestId = document.getElementById("requestId").value;
	var url = YAHOO.transbit.tbits.contextPath + 'proxy/corr-preview?previewAction=update-request&requestId=' + requestId +'&format='+format ;
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