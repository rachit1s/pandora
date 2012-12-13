<script type='text/javascript'>
var is_IE = false ;
</script> 
<!--[if IE]>
<script type='text/javascript'>
is_IE = true ;
</script>
<![endif]-->
<script type='text/javascript'>
//trim function for strings
String.prototype.trim = function () {
    return this.replace(/^\s*/, '').replace(/\s*$/, '');
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
				selObj.options[i].style.color = '#DCDCDC' ;
			}
			else
			{
				selObj.options[i].style.color = '#000000' ;
			}
		}
	}
}

// the last enabled value will be selected
function setSelectValues( selectObject, enabledValues )
{
	if( null == selectObject || null == enabledValues )
		return ;

	initialSelectedIndex = selectObject.selectedIndex ;
	var v ;	
	setAllOptionsDisable(selectObject,true);
	lastEnabledIndex = -1 ;
	for( v in enabledValues )
	{
		for( var i = 0; i< selectObject.length ;i++ )
		{
			if( selectObject.options[i].value == enabledValues[v] )
			{
				selectObject.options[i].disabled = false ;
				lastEnabledIndex = i; 
				break;
			}
		}
	}
	
	if( selectObject.options[initialSelectedIndex].disabled == false )
	{
		selectObject.selectedIndex = initialSelectedIndex ;
	}
	else
	{
		selectObject.selectedIndex = lastEnabledIndex ;
	}
	
	if( is_IE == true )
		emulate( selectObject ) ;
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
//	alert('inside restore');
	if( e.selectedIndex >= 0 )
	{
		if (true == e.options[e.selectedIndex].disabled) {
			e.selectedIndex = window.select_current[e.id];
		}
	}
}

function appendMessage(msg)
{		
		var existingMsg = document.getElementById('exceptions').innerHTML ;
		if( existingMsg == null )
			existingMsg = '' ;
		var newMsg = existingMsg + "<table id='table6' cellpadding='0' cellspacing='0' width='100%'>" 
		+ "<span style='{font-weight: bold;color:blue}'>Messages:</span><br><span style='color:blue'>" + msg + "</span>" + "</table>";
		document.getElementById('exceptions').innerHTML = newMsg ;
		document.getElementById('exceptionBlock').style.display = 'block';
		window.location = '#top';
}

function changeUser( field, map )
{
	var flowStatus = document.getElementById('<%=QLT_FLOW_STATUS_FIELD_NAME%>') ;
	var fsvalue = flowStatus.options[flowStatus.selectedIndex].value ;
	
	var fv = field.value ;
	var av = removeRedundantUserListSeparators(fv) ;
	var ua = av.split(';');	
	var nfv = fv ;
	for( var i = 0 ; i < map.length ; i++ )
	{
		if( map[i].type == fsvalue )
		{
			users = map.users ;
			
			for( var j = 0 ; j < users.length ; j++ )
			{
				contains = false ;
				for( var k = 0 ; k < ua.length ; k++ )
				{
					if( users[j] == ua[ k ] )
					{
						contains = true ;
						break;
					}
				}
				
				if( contains == false )
				{
					nfv += "," + users[j] ; 
				}
			}
		}
	}
}

function reEvaluate(event,id)
{	
	changeUser(document.getElementById('<%=assignee_field_name%>'), <%=ass_map%> );
	changeUser(document.getElementById('<%=subscriber_field_name%>'), <%=sub_map%> );
}

function prefillCallBack()
{

	var flowStatus = document.getElementById('<%=QLT_FLOW_STATUS_FIELD_NAME%>') ;	
	var decision = document.getElementById('<%=QLT_DECISION_FIELD_NAME%>') ;	
	var pdfi = document.getElementById('<%=QLT_PDFI_FIELD_NAME%>') ;
	var genInsp = document.getElementById('<%=QLT_GEN_INSP_NO_FIELD_NAME%>') ;
	var genMdcc = document.getElementById('<%=QLT_GEN_MDCC_NO_FIELD_NAME%>' ) ;

	if( null != flowStatus )
	{		
		YAHOO.util.Event.addListener(flowStatus, "change", reEvaluate, flowStatus.id );
		reEvaluate();
	}
	
	var enabledFS = <%=enabledFS_value%> ;
	var enabledDec = <%=enabledDec_value%> ;
	var enabledPDFI = <%=enabledPDFI_value%> ;

	var message = '<%=message_value%>' ;
	
	setSelectValues(flowStatus,enabledFS);
	setSelectValues(decision,enabledDec);
	setSelectValues(pdfi,enabledPDFI);

	genInsp.checked = <%=genInsp_checked%> ;
	genMdcc.checked = <%=genMdcc_checked%> ;

	if( true == is_IE )
	{
		//alert( 'is_IE == true')
		var selArray = new Array();
		selArray[0] = flowStatus ;
		selArray[1] = decision ;
		selArray[2] = pdfi ;
				
		if (selArray.length > 0) 
		{
			window.select_current = new Array();

			for (var i=0, select; select = selArray[i]; i++) 
			{
				//alert( ' for IE : select = ' + select.id ) ;	
				select.onfocus = function(){ window.select_current[this.id] = this.selectedIndex; }
				select.onchange = function(){ restore(this); }
				emulate(select);
			}
		}
	}
	if( message != '' )
	{
		appendMessage(message);
	}
}

//register the window load event
YAHOO.util.Event.addListener(window, 'load', prefillCallBack);

</script>