package kskQlt;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.ExtUI.ISubRequestFooterSlotFiller;
import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import static kskQlt.QltConstants.*;
import static kskQlt.QltHelper.*;

public class QltPrefill implements IUpdateRequestFooterSlotFiller,
		IAddRequestFooterSlotFiller, ISubRequestFooterSlotFiller {

	
	
	private static final String PREFILL_ERROR = "Error occurred in prefilling. Please select values manually.";
	public static final String script = "<script type='text/javascript'>\n"+
	"var is_IE = false ;\n"+
	"</script> \n"+
	"<!--[if IE]>\n"+
	"<script type='text/javascript'>\n"+
	"is_IE = true ;\n"+
	"</script>\n"+
	"<![endif]-->\n"+
	"<script type='text/javascript'>\n"+
	"//trim function for strings\n"+
	"String.prototype.trim = function () {\n"+
	"    return this.replace(/^\\s*/, '').replace(/\\s*$/, '');\n"+
	"}\n"+
	"\n"+
	"function emulate( selObj )\n"+
	"{\n"+
	"	// this function changes the color and background of select options\n"+
	"	// required for browser is IE\n"+
	"	if( selObj != null )\n"+
	"	{\n"+
	"		for( var i = 0 ; i < selObj.length ; i++ )\n"+
	"		{\n"+
	"			if( selObj.options[i].disabled == true )\n"+
	"			{\n"+
	"				selObj.options[i].style.color = '#DCDCDC' ;\n"+
	"			}\n"+
	"			else\n"+
	"			{\n"+
	"				selObj.options[i].style.color = '#000000' ;\n"+
	"			}\n"+
	"		}\n"+
	"	}\n"+
	"}\n"+
	"\n"+
	"// the last enabled value will be selected\n"+
	"function setSelectValues( selectObject, enabledValues )\n"+
	"{\n"+
	"	if( null == selectObject || null == enabledValues )\n"+
	"		return ;\n"+
	"\n"+
	"	initialSelectedIndex = selectObject.selectedIndex ;\n"+
	"	var v ;	\n"+
	"	setAllOptionsDisable(selectObject,true);\n"+
	"	lastEnabledIndex = -1 ;\n"+
	"	for( v in enabledValues )\n"+
	"	{\n"+
	"		for( var i = 0; i< selectObject.length ;i++ )\n"+
	"		{\n"+
	"			if( selectObject.options[i].value == enabledValues[v] )\n"+
	"			{\n"+
	"				selectObject.options[i].disabled = false ;\n"+
	"				lastEnabledIndex = i; \n"+
	"				break;\n"+
	"			}\n"+
	"		}\n"+
	"	}\n"+
	"	\n"+
	"	if( selectObject.options[initialSelectedIndex].disabled == false )\n"+
	"	{\n"+
	"		selectObject.selectedIndex = initialSelectedIndex ;\n"+
	"	}\n"+
	"	else\n"+
	"	{\n"+
	"		selectObject.selectedIndex = lastEnabledIndex ;\n"+
	"	}\n"+
	"	\n"+
	"	if( is_IE == true )\n"+
	"		emulate( selectObject ) ;\n"+
	"}\n"+
	"// selectObject : any select DOM\n"+
	"//disableValue = true/false \n"+
	"function setAllOptionsDisable( selectObject, disableValue ) \n"+
	"{\n"+
	"	if( null != selectObject )\n"+
	"	{\n"+
	"		for( var i = 0 ; i < selectObject.length ; i++ )\n"+
	"		{\n"+
	"			selectObject.options[i].disabled = disableValue ;\n"+
	"		}\n"+
	"	}\n"+
	"	\n"+
	"	if( is_IE == true )\n"+
	"		emulate( selectObject ) ;\n"+
	"}\n"+
	"\n"+
	"function restore(e) {\n"+
	"//	alert('inside restore');\n"+
	"	if( e.selectedIndex >= 0 )\n"+
	"	{\n"+
	"		if (true == e.options[e.selectedIndex].disabled) {\n"+
	"			e.selectedIndex = window.select_current[e.id];\n"+
	"		}\n"+
	"	}\n"+
	"}\n"+
	"\n"+
	"function appendMessage(msg)\n"+
	"{		\n"+
	"		var existingMsg = document.getElementById('exceptions').innerHTML ;\n"+
	"		if( existingMsg == null )\n"+
	"			existingMsg = '' ;\n"+
	"		var newMsg = existingMsg + \"<table id='table6' cellpadding='0' cellspacing='0' width='100%'>\" \n"+
	"		+ \"<span style='{font-weight: bold;color:blue}'>Messages:</span><br><span style='color:blue'>\" + msg + \"</span>\" + \"</table>\";\n"+
	"		document.getElementById('exceptions').innerHTML = newMsg ;\n"+
	"		document.getElementById('exceptionBlock').style.display = 'block';\n"+
	"		window.location = '#top';\n"+
	"}\n"+
	"\n"+
	"function changeUser( field, map )\n"+
	"{\n"+
	"	var flowStatus = document.getElementById('<%=QLT_FLOW_STATUS_FIELD_NAME%>') ;\n"+
	"	var fsvalue = flowStatus.options[flowStatus.selectedIndex].value ;\n"+
	"	\n"+
	"	var fv = field.value ;\n"+
	"	var av = removeRedundantUserListSeparators(fv) ;\n"+
	"	var ua = av.split(';');	\n"+
	"	var nfv = fv ;\n"+
	"	for( var i = 0 ; i < map.length ; i++ )\n"+
	"	{\n"+
	"		if( map[i].type == fsvalue )\n"+
	"		{\n"+
	"			users = map[i].users ;\n"+
	"			\n"+
	"			for( var j = 0 ; j < users.length ; j++ )\n"+
	"			{\n"+
	"				contains = false ;\n"+
	"				for( var k = 0 ; k < ua.length ; k++ )\n"+
	"				{\n"+
	"					if( users[j] == ua[ k ] )\n"+
	"					{\n"+
	"						contains = true ;\n"+
	"						break;\n"+
	"					}\n"+
	"				}\n"+
	"				\n"+
	"				if( contains == false )\n"+
	"				{\n"+
	"					nfv += \",\" + users[j] ; \n"+
	"				}\n"+
	"			}\n"+
	"		}\n"+
	"	}\n"+
	"   field.value = nfv\n" +
	"}\n"+
	"\n"+
	"function reEvaluate(event,id)\n"+
	"{	\n"+
	"	changeUser(document.getElementById('<%=assignee_field_name%>'), <%=ass_map%> );\n"+
	"	changeUser(document.getElementById('<%=subscriber_field_name%>'), <%=sub_map%> );\n"+
	"}\n"+
	"\n"+
	"function prefillCallBack()\n"+
	"{\n"+
	"\n"+
	"	var flowStatus = document.getElementById('<%=QLT_FLOW_STATUS_FIELD_NAME%>') ;	\n"+
	"	var decision = document.getElementById('<%=QLT_DECISION_FIELD_NAME%>') ;	\n"+
	"	var pdfi = document.getElementById('<%=QLT_PDFI_FIELD_NAME%>') ;\n"+
	"	var genInsp = document.getElementById('<%=QLT_GEN_INSP_NO_FIELD_NAME%>') ;\n"+
	"	var genMdcc = document.getElementById('<%=QLT_GEN_MDCC_NO_FIELD_NAME%>' ) ;\n"+
	"\n"+
	"	if( null != flowStatus )\n"+
	"	{		\n"+
	"		YAHOO.util.Event.addListener(flowStatus, \"change\", reEvaluate, flowStatus.id );\n"+
	"		reEvaluate();\n"+
	"	}\n"+
	"	\n"+
	"	var enabledFS = <%=enabledFS_value%> ;\n"+
	"	var enabledDec = <%=enabledDec_value%> ;\n"+
	"	var enabledPDFI = <%=enabledPDFI_value%> ;\n"+
	"\n"+
	"	var message = '<%=message_value%>' ;\n"+
	"	\n"+
	"	setSelectValues(flowStatus,enabledFS);\n"+
	"	setSelectValues(decision,enabledDec);\n"+
	"	setSelectValues(pdfi,enabledPDFI);\n"+
	"\n"+
	"	genInsp.checked = <%=genInsp_checked%> ;\n"+
	"	genMdcc.checked = <%=genMdcc_checked%> ;\n"+
	"\n"+
	"	if( true == is_IE )\n"+
	"	{\n"+
	"		//alert( 'is_IE == true')\n"+
	"		var selArray = new Array();\n"+
	"		selArray[0] = flowStatus ;\n"+
	"		selArray[1] = decision ;\n"+
	"		selArray[2] = pdfi ;\n"+
	"				\n"+
	"		if (selArray.length > 0) \n"+
	"		{\n"+
	"			window.select_current = new Array();\n"+
	"\n"+
	"			for (var i=0, select; select = selArray[i]; i++) \n"+
	"			{\n"+
	"				//alert( ' for IE : select = ' + select.id ) ;	\n"+
	"				select.onfocus = function(){ window.select_current[this.id] = this.selectedIndex; }\n"+
	"				select.onchange = function(){ restore(this); }\n"+
	"				emulate(select);\n"+
	"			}\n"+
	"		}\n"+
	"	}\n"+
	"	if( message != '' )\n"+
	"	{\n"+
	"		appendMessage(message);\n"+
	"	}\n"+
	"}\n"+
	"\n"+
	"//register the window load event\n"+
	"YAHOO.util.Event.addListener(window, 'load', prefillCallBack);\n"+
	"\n"+
	"</script>\n";
		
	private String processAddRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user)
	{
		Hashtable<String,String> params = new Hashtable<String,String>();
		initializeParams(params) ;
		
		ArrayList<String> dec = new ArrayList<String>() ;
		dec.add(QLT_DEC_NONE);
		
		ArrayList<String> pdfi = new ArrayList<String>() ;
		pdfi.add(QLT_PDFI_NA);
		
		ArrayList<String> fs = new ArrayList<String>() ;
		fs.add(QLT_FS_PEND_ISS_IC);
		
		Gson gson = new Gson() ; 
    	params.put("enabledDec_value", gson.toJson(dec) );
    	params.put("enabledFS_value", gson.toJson(fs));
    	params.put("enabledPDFI_value", gson.toJson(pdfi));
    	params.put("genInsp_checked", "true");
    	params.put("genMdcc_checked", "false");
    	String msg = "" ;
    	try {
			msg = "Check that " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_INSP_NO_FIELD_NAME).getDisplayName() + " field is selected and submit." ;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		params.put("message_value", msg);
		return getJs(params) ;
	}
	
	private String getJs(Hashtable<String, String> params) 
	{
		String js = script ;
		for( Enumeration<String> keys = params.keys() ; keys.hasMoreElements() ; )
		{
			String key = keys.nextElement() ;
			String value = params.get(key);
			js = js.replaceAll("<%=" + key + "%>" , value);
		}
		return js;
	}

	public static void initializeParams(Hashtable<String, String> params) 
	{
		/*	
QLT_FLOW_STATUS_FIELD_NAME
QLT_DECISION_FIELD_NAME
QLT_PDFI_FIELD_NAME
QLT_GEN_INSP_NO_FIELD_NAME
QLT_GEN_MDCC_NO_FIELD_NAME
enabledFS_value
enabledDec_value
enabledPDFI_value
message_value
genInsp_checked
genMdcc_checked
		 */
	
		params.put("QLT_FLOW_STATUS_FIELD_NAME",QLT_FLOW_STATUS_FIELD_NAME);
		params.put("QLT_DECISION_FIELD_NAME", QLT_DECISION_FIELD_NAME);
		params.put("QLT_PDFI_FIELD_NAME", QLT_PDFI_FIELD_NAME);
		params.put("QLT_GEN_INSP_NO_FIELD_NAME", QLT_GEN_INSP_NO_FIELD_NAME );
		params.put("QLT_GEN_MDCC_NO_FIELD_NAME", QLT_GEN_MDCC_NO_FIELD_NAME);
		
		params.put("ass_map", getAssMap() ) ;
		params.put("sub_map", getSubMap() ) ;
		params.put("assignee_field_name", Field.ASSIGNEE);
		params.put("subscriber_field_name", Field.SUBSCRIBER );
		
		ArrayList<String> empty = new ArrayList<String>() ;
		QltHelper.setPrefillTags(params,empty,empty,empty,false,false,"");
	}

	static class TypeU
	{
		public String type ;
		public ArrayList<String> users ;
//		public TypeU( String t, ArrayList<String> u )
//		{
//			type = t ;
//			users = u ;
//		}
		public String toString()
		{
			return " type = " + type + " users = " + users ;
		}
	}
	private static String getSubMap() 
	{
		ArrayList<TypeU> map = new ArrayList<TypeU>() ;
		
		TypeU tu = new TypeU() ;
		tu.type = QLT_FS_PEND_ISS_IC ;
		ArrayList<String> u = new ArrayList<String>() ;
		u.add("SEPCOQuality");
		tu.users = u ;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type=QLT_FS_INSP_CALL_ISS;
		u = new ArrayList<String>() ;
		u.add("KMPCLQuality");
		u.add("SEPCOQuality");		
		tu.users = u;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_PEND_ISS_MDCC ;
		u = new ArrayList<String>() ;
		u.add("SEPCOQuality");
		u.add("LRA");
		tu.users = u ;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_MDCC_ISS ;
		u = new ArrayList<String>() ;
		u.add("SEPCO");
		u.add("KMPCLQuality");
		tu.users = u ;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_PEND_ISS_RE_INSP_CALL ;
		u = new ArrayList<String>() ;
		u.add("LRA");
		u.add("KMPCLQuality");
		tu.users = u ;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_REINSP_CALL_ISS ;
		u = new ArrayList<String>() ;
		u.add("KMPCLQuality");
		u.add("SEPCOQuality");
		tu.users = u ;
		map.add(tu);
		
		Gson gson = new Gson() ;
		String json = gson.toJson(map);
		System.out.println("subs map = " + map );
		System.out.println("subs json = " + json);
		return json ;
	}

	private static String getAssMap() 
	{
		ArrayList<TypeU> map = new ArrayList<TypeU>() ;
		
		TypeU tu = new TypeU() ;		
		ArrayList<String> u = new ArrayList<String>() ;
		
		tu.type=QLT_FS_INSP_CALL_ISS;
		u = new ArrayList<String>() ;
		u.add("LRA");
//		u.add("SEPCOQuality");		
		tu.users = u;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_PEND_ISS_MDCC ;
		u = new ArrayList<String>() ;
		u.add("KMPCLQuality");
//		u.add("LRA");
		tu.users = u ;
		map.add(tu);

		tu = new TypeU() ;
		tu.type = QLT_FS_PEND_ISS_RE_INSP_CALL ;
		u = new ArrayList<String>() ;
//		u.add("LRA");
		u.add("SEPCOQuality");
		tu.users = u ;
		map.add(tu);
		
		tu = new TypeU() ;
		tu.type = QLT_FS_REINSP_CALL_ISS ;
		u = new ArrayList<String>() ;
		u.add("LRA");
//		u.add("SEPCOQuality");
		tu.users = u ;
		map.add(tu);
		
		Gson gson = new Gson() ;
		return gson.toJson(map);
	}

	private String processUpdateRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user)
	{
		try
		{
			QltObject qo = new QltObject(oldRequest);
			int currState = getState(qo);

			Hashtable<String,String> params = new Hashtable<String,String>();
			initializeParams(params);
						
			switch(currState)
			{
				case 1 : 
					{
						prefillState1(qo,params,ba);
						break; 
					}

					case 2 : 
					{
						prefillState2(qo,params,ba);
						break; 
					}

					case 3 : 
					{
						prefillState3(qo,params,ba);
						break; 
					}

					case 4 : 
					{
						prefillState4(qo,params,ba);
						break; 
					}

					case 5 : 
					{
						prefillState5(qo,params,ba);
						break; 
					}

					case 6 : 
					{
						prefillState6(qo,params,ba);
						break; 
					}

					case 7 : 
					{
						prefillState7(qo,params,ba);
						break; 
					}

					case 8 : 
					{
						prefillState8(qo,params,ba);
						break; 
					}
					
					case 100 :
					{
						prefillState100(qo,params,ba);
						break ;
					}
					
					default :
						throw new TBitsException("Request is trapped in an illegal state.");
				}


			if( null == params || params.isEmpty() )
				return showError(PREFILL_ERROR) ;
			
			return getJs(params);
		}
		catch(Exception e)
		{
			return showError(PREFILL_ERROR);
		}
	}
	
	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user) 
	{
		if( null == ba || null == ba.getSystemPrefix() || ! ba.getSystemPrefix().equalsIgnoreCase(QLT_SYSPREFIX))
			return "" ;
		
		return processUpdateRequest(httpRequest, httpResponse, ba, oldRequest, user);
	}

	public double getUpdateRequestFooterSlotFillerOrder() 
	{
		return 0;
	}

	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) 
	{
		if( null == ba || null == ba.getSystemPrefix() || ! ba.getSystemPrefix().equalsIgnoreCase(QLT_SYSPREFIX))
			return "" ;
		
		return processAddRequest(httpRequest, httpResponse, ba, user);
	}

	public double getAddRequestFooterSlotFillerOrder()
	{
		return 0;
	}

	public String getSubRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request parentRequest, User user) 
	{	
		if( null == ba || null == ba.getSystemPrefix() || ! ba.getSystemPrefix().equalsIgnoreCase(QLT_SYSPREFIX))
			return "" ;

		return processAddRequest(httpRequest, httpResponse, ba, user);
	}

	public double getSubRequestFooterOrder() 
	{
		return 0;
	}
}
