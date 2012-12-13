<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri='/WEB-INF/cewolf.tld' prefix='cewolf' %>
<%@ page import="transbit.tbits.webapps.BaReport"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.*"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="transbit.tbits.Helper.Messages"%>
<%@ page import="transbit.tbits.common.DatabaseException"%>
<%@ page import="transbit.tbits.common.Utilities"%>
<%@ page import="transbit.tbits.config.BAConfig"%>
<%@ page import="transbit.tbits.config.SysConfig"%>
<%@ page import="transbit.tbits.config.WebConfig"%>
<%@ page import="transbit.tbits.domain.BusinessArea"%>
<%@ page import="transbit.tbits.domain.Field"%>
<%@ page import="transbit.tbits.domain.User"%>
<%@ page import="transbit.tbits.exception.TBitsException"%>
<%@ page import="transbit.tbits.search.Searcher"%>
<%@ page import="transbit.tbits.webapps.WebUtil"%>
<%@ page import="transbit.tbits.search.Result"%>
<%@ page import="transbit.tbits.search.Searcher"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="transbit.tbits.search.Result"%>
<%@ page import="transbit.tbits.Helper.ReportHelper"%>
<%@ page import="transbit.tbits.Helper.TBitsHelper"%>
<%@ page import="transbit.tbits.domain.DataType"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TBits Reports</title>
    </head>
    <body class="bw">
        <link rel="stylesheet" type="text/css" media="screen" href="/web/css/tbits.css" />
    
        <%--
        following nomenclature is to be remembered for whole section of report generation 
        2)category --------> This indicates specific type of data representation in reporting 
        which includes all but pie and pie3d 
        1)firstField -----> this Idicates first selection field for category type and only selection field for pie chart
        2)secondField -----> this indicates second selection for category Type 
        3)multiple Image--------> this indicates selection of field for generating multiple image based on defferent values of this field
        --%>
        <%--First of all Validate user and retrive all accesible fields for the user in specified business area 
        THEN CREATE COMBO BOX OPTIONS to put in comboxex to provide defferent kinds of selection --%>
        <% 
            User user = null;
            WebConfig userConfig=null;
            Hashtable<String, Object> reqParams=null;
            BusinessArea ba = null;
            String    fieldComboBoxOptions = "";
            StringBuffer    firstVariantComboBoxOptions= new StringBuffer();
            StringBuffer    secondVariantComboBoxOptions = new StringBuffer();
            StringBuffer    multipleImageFieldComboBoxOptions =  new StringBuffer();
            
            try {
                user = WebUtil.validateUser(request);
                userConfig = WebConfig.getWebConfig(user.getWebConfig());
                reqParams = WebUtil.getRequestParams(request, userConfig, WebUtil.SEARCH);
                ba        = BusinessArea.lookupBySystemPrefix(request.getParameter("ba"));
                if (ba == null) {
                    throw new TBitsException(Messages.getMessage("INVALID_BUSINESS_AREA"));
                }
                int       systemId  = ba.getSystemId();
                String    sysPrefix = ba.getSystemPrefix();
                SysConfig sc        = ba.getSysConfigObject();
                // Get all the fields which the user can view in this business_area
                ArrayList<Field> fieldList = Field.getFieldsBySystemIdAndUserId(systemId, user.getUserId());
                int       listSize   = fieldList.size();
                String    fieldDisplayName  = null;
                String    fieldName ;
                for (int i = 0; i < listSize; i++){
                    Field field = (Field) fieldList.get(i);
                    if(field.getDataTypeId() != DataType.TEXT)
                                        {
                    fieldDisplayName = field.getDisplayName();
                    fieldName = field.getName();

                    if(request.getParameter("firstVariant") != null && request.getParameter("firstVariant").equals(fieldName)){
                        firstVariantComboBoxOptions.append("<option VALUE=\""+fieldName+"\" selected=\"selected\">"+fieldDisplayName+"\r\n");

                    }else{
                        firstVariantComboBoxOptions.append("<option VALUE=\""+fieldName+"\">"+fieldDisplayName+"\r\n");

                    }
                    if(request.getParameter("secondVariant") != null && request.getParameter("secondVariant").equals(fieldName)){
                        secondVariantComboBoxOptions.append("<option VALUE=\""+fieldName+"\"selected=\"selected\">"+fieldDisplayName+"\r\n");
                    }else{
                        secondVariantComboBoxOptions.append("<option VALUE=\""+fieldName+"\">"+fieldDisplayName+"\r\n");

                    }
                    if(request.getParameter("multipleImageField") != null && request.getParameter("multipleImageField").equals(fieldName)){
                        multipleImageFieldComboBoxOptions.append("<option VALUE=\""+fieldName+"\"selected=\"selected\">"+fieldDisplayName+"\r\n");
                    }else{
                        multipleImageFieldComboBoxOptions.append("<option VALUE=\""+fieldName+"\">"+fieldDisplayName+"\r\n");
                    }
                }
                    }
            } catch (TBitsException ex) {
                ex.printStackTrace();
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            } catch (ServletException ex) {
                ex.printStackTrace();
            }
            String query = request.getParameter("query");
            if(query == null)
            {
                query = "";
                
            }
            query = TBitsHelper.escapeHTML(query);
        %>
        <table class="result" align="center" border="1">
            <thead>
                <tr >
                  <th></th>
                </tr>
                <tr class='header hp bbn' >
                    <th height="41" class="sxs cw u b l">Tbits Reports  </th>
                </tr>
            </thead>
            <tbody>
                <tr >
                    <td>       

        <form class="hb bhs"  METHOD=POST ACTION="<%="/tbits-reports?ba="+request.getParameter("ba")%>">
            <table class="test" >
              <tbody>
                <tr>
                  <td align="right" class="s b"><b>Query</b></td>
                  
                  <td colspan="4" class="sx"><input  name="query" value="<%= query %>" size="100" type="text">
                     
                  </td>
                  <td width="139"><select class="sx" id="select14" name="chartType">
                      <%if(request.getParameter("chartType")!= null){%>
                      <option value="pie3d" <%=request.getParameter("chartType").equals("pie3d")?"selected=\"selected\"":""%>>pie3d
                      <option value="verticalbar" <%=request.getParameter("chartType").equals("verticalbar")?"selected=\"selected\"":""%>>verticalbar
                      <option value="verticalbar3d" <%=request.getParameter("chartType").equals("verticalbar3d")?"selected=\"selected\"":""%>>verticalbar3d
                      <option value="stackedverticalbar" <%=request.getParameter("chartType").equals("stackedverticalbar")?"selected=\"selected\"":""%>>stackedverticalbar
                      <option value="stackedverticalbar3d" <%=request.getParameter("chartType").equals("stackedverticalbar3d")?"selected=\"selected\"":""%>>stackedverticalbar3d
                      <option value="horizontalbar" <%=request.getParameter("chartType").equals("horizontalbar")?"selected=\"selected\"":""%>>horizontalbar
                      <option value="horizontalbar3d" <%=request.getParameter("chartType").equals("horizontalbar3d")?"selected=\"selected\"":""%>>horizontalbar3d
                      <option value="stackedhorizontalbar" <%=request.getParameter("chartType").equals("stackedhorizontalbar")?"selected=\"selected\"":""%>>stackedhorizontalbar
                         
                      <%--<option VALUE="timeseries">timeseries--%>
                      <%-- <option VALUE="xy">xy--%>                    
                      <option value="line" <%=request.getParameter("chartType").equals("line")?"selected=\"selected\"":""%>>line
                      <option value="area" <%=request.getParameter("chartType").equals("area")?"selected=\"selected\"":""%>>area
                      <option value="pie" <%=(request.getParameter("chartType").equals("pie")?"selected=\"selected\"":"")%>>piechart             
                      <%}else{%>
                      <option value="pie3d ">Pie3d
                       <option value="verticalbar">Verticalbar
                      <option value="verticalbar3d">Verticalbar3d
                      <option value="stackedverticalbar">Stackedverticalbar
                      <option value="stackedverticalbar3d">Stackedverticalbar3d
                     <option value="horizontalbar">Horizontalbar
                      <option value="horizontalbar3d">Horizontalbar3d
              
                      <option value="stackedhorizontalbar">Stackedhorizontalbar
                     
                       <option value="line">Line
                       <option value="area">Area
                       <option value="pie" >Piechart
                      <%}%>
                                </select>
                  </td>
                </tr>
                <tr>
                  <td width="71" align="right" class="s b"> <b>X-Axis[?]</b></td>
                  <td width="174"><select class="sx" id="select17" name="firstVariant" >
                      <option value="none">Not
                      Specified
                      <%=(firstVariantComboBoxOptions.toString())%>
                                </select>
                  </td>
                  <td width="104" align="right" class="s b"><b>Y-Axis[?]</b></td>
                  <td width="174" ><select class="sx" id="select18" name="secondVariant">
                      <option value="none">Not Specified
                      <%=(secondVariantComboBoxOptions.toString())%>
                                </select>
                  </td>
                  <td width="138" align="right" class="s b"><b>Multiple Images</b></td>
                  <td><select class="sx" id="select21" name="multipleImageField">
                      <option value="none">Not Specified
                      <%=(multipleImageFieldComboBoxOptions.toString())%>
                                </select>
                  </td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                  <td align="right" class="sx"><input class="cw bn sxs lsb" name="Request2" value="Prepare Report" type="submit">
                  </td>
                  <td>&nbsp;</td>
                </tr>
              </tbody>
            </table>
        </form>   
             </td>
                </tr>
       <%--If method is Post then get parameters and create Images --%>         
        <% if(request.getMethod().toUpperCase().equals("POST")){%>
        <%
            try{
            
            Searcher searcher = new Searcher( ba.getSystemId(),user.getUserId(),request.getParameter("query"));
            String multipleImageField = request.getParameter("multipleImageField");
            if(multipleImageField == null){
                multipleImageField = "";
            }
            String chartType = request.getParameter("chartType");
            if(chartType == null){
                chartType = "";
            }
            // searcher.setTotalResultCount(1000);
            searcher.setMaximumPageSize(Integer.MAX_VALUE);
            searcher.search();
            ArrayList<Result> results;
            Hashtable<String, Result> requests = searcher.getRequestsTable();%>
            <tr><td>
        <% if(!multipleImageField.equals("none") && !(chartType.equals("pie") || chartType.equals("pie3d"))){%>
        <%@include file="./multipleImageCategory.jspf" %>
        <% }else if(!multipleImageField.equals("none") ){%>
        <%@include file="./multipleImagePie.jspf"%>
        <%}else if(multipleImageField.equals("none") && (chartType.equals("pie") || chartType.equals("pie3d"))){    %>
        <%@include file="./singleImagePie.jspf"%>
        <%}else if(multipleImageField.equals("none") && !chartType.equals("pie")){%>
        <%@include file="./SingleImageCategory.jspf"%>
        <%}%>
        </td>
        </tr>
        <%}catch (Exception pageException ){%>         
         <%="<b>Invalied item selections for requested report please select correct items from the list</b>"%>        
        <%}}%>   
            </tbody>
        </table>
    </body>
</html>

