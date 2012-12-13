<%@tag description="put the tag description here" pageEncoding="UTF-8"%>
<%-- Taglib directives can be specified here: --%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
--%>
<%--@taglib uri='../displaytag.tld' prefix='display' %>
<%-- --%>
<%@taglib uri='/WEB-INF/displaytag.tld' prefix='display' %>
<%-- --%>
<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="userID" rtexprvalue="true" description="UserID for which results are to be retrived" required="true"%>
<%@attribute name="query" rtexprvalue="true" description="query for which results are to be retrived" required="false"%>
<%@attribute name="BAPrifix"  description="business Area it may be give as all to search all business are" rtexprvalue="true" required="true"%>
<%@attribute name="numberOfResults"  rtexprvalue="true" description="number of results to be displayed " required="true"%>
<%@attribute  name="headerFileds"    rtexprvalue="true" description="Name of fields to be Displayed write in formate fieldDisplayName1 : fieldDisplayName2" required="false"%>
<%@attribute name="exportOptions"  rtexprvalue="true" description="" required="false"%>
<%@attribute name="pagesize"  rtexprvalue="true" description="number of results per page" required="false"%>
<%@attribute name="displayClass"  rtexprvalue="true" required="false"%>
<%@attribute name="generateLink"  rtexprvalue="true" description="vaue = true | false   based on do you want to generate link on given result" required="false"%>
<table border="1">
<%//perform Search based on give parameters           
            String error = null;
            java.util.Hashtable<String, transbit.tbits.search.Result> requests =null;
            transbit.tbits.domain.User myUser = transbit.tbits.domain.User.lookupAllByUserId(Integer.parseInt(userID));
            transbit.tbits.domain.BusinessArea myBA = transbit.tbits.domain.BusinessArea.lookupBySystemPrefix(BAPrifix);
            transbit.tbits.config.WebConfig userConfig = transbit.tbits.config.WebConfig.getWebConfig(myUser.getWebConfig());
            transbit.tbits.config.BAConfig myUserBaConfig = userConfig.getBAConfig(BAPrifix);
            java.util.ArrayList<String> myDisplayHeader = null;
            String DQL = "";
            int tableSize = -1;
            boolean doExport = false;
            boolean doLink = false;
            int resultCount = 10;
            if(jspContext.getAttribute("query") != null){
                DQL = query;
            }
            if(jspContext.getAttribute("pagesize") != null){
                tableSize = Integer.parseInt(pagesize);
            }
            if(jspContext.getAttribute("generateLink") != null){
                if(((String)generateLink).trim().equals("true")){
                    doLink = true;
                }
            }
            if(jspContext.getAttribute("exportOptions") != null){
                if(((String)exportOptions).trim().equals("true")){
                    doExport = true;
                }
            }
            if(jspContext.getAttribute("headerFileds") == null){
                myDisplayHeader  = myUserBaConfig.getDisplayHeader();
            }else{
                myDisplayHeader = new java.util.ArrayList();
                java.util.StringTokenizer st = new java.util.StringTokenizer(headerFileds, ":");
                while (st.hasMoreTokens()) {
                    myDisplayHeader.add(st.nextToken().trim());
                }
            }
            try{
                transbit.tbits.search.Searcher searcher = new transbit.tbits.search.Searcher( myBA.getSystemId(),myUser.getUserId(),DQL);
                if(jspContext.getAttribute("numberOfResults")!= null){
                    resultCount = Integer.parseInt(((String)numberOfResults));
                    searcher.setMaximumPageSize(Integer.parseInt(numberOfResults));
                }
                searcher.search();
                requests = searcher.getRequestsTable();
                //request.setAttribute("table", requests);
            }catch(Exception e){
                error =  e.getLocalizedMessage();
            }
            java.util.ArrayList<transbit.tbits.search.Result> results=new java.util.ArrayList();
            if(requests != null){ java.util.Enumeration  requestEnum = requests.keys();
                        while(requestEnum.hasMoreElements() ) {
                String key = (String) requestEnum.nextElement();
                results.add(requests.get(key));
                }
            }else{
                error = "No Results!";
            }
%>
<%--f(error != null){%>
Error:
<%=error%>
<%}--%>
<jsp:scriptlet> request.setAttribute( "test", results); </jsp:scriptlet>
<%if(tableSize > 0){%>
<display:table export='<%=doExport%>'  name="test" id="currentRow" length='<%=resultCount%>'  class="results" pagesize='<%=tableSize%>' >
 <%for(String headerField : myDisplayHeader){%>
    <%if(doExport){%>
    <display:setProperty name="export.rtf.filename" value="example.rtf"/>
    <display:setProperty name="export.pdf" value="true" />
    <%}%>
    <%if(doLink){
        transbit.tbits.domain.Field myField = transbit.tbits.domain.Field.lookupBySystemIdAndFieldName(myBA.getSystemId(),headerField);
        
   %>
    <display:column title='<%=(myField.getDisplayName()+"          ")%>' href='<%= ("/Q/" +  myBA.getSystemPrefix() + "/" +((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get("request_id"))%>'  sortable="true" headerClass="sortable" >
       <%
        String fieldValue = "";
        Object fieldValueObj = ((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get(headerField);
            if(fieldValueObj != null){ 
                fieldValue = transbit.tbits.Helper.ReportHelper.ConvertToString(myField,fieldValueObj);
            }
        
    %>
    <%=fieldValue%> 
    </display:column>
    <%}else{
    transbit.tbits.domain.Field myField = transbit.tbits.domain.Field.lookupBySystemIdAndFieldName(myBA.getSystemId(),headerField);
    %>
    <display:column title='<%=(myField.getDisplayName()+"          ")%>'  autolink="true" sortable="true" headerClass="sortable" >
    <%
        String fieldValue = "";
        Object fieldValueObj = ((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get(headerField);
            if(fieldValueObj != null){ 
                fieldValue = transbit.tbits.Helper.ReportHelper.ConvertToString(myField,fieldValueObj);
            }
        
    %>
    <%=fieldValue%> 
    </display:column>
    <%}%>  
    <%}%>      
</display:table>
<%}else{%>
<display:table export='<%=doExport%>'  name="test" id="currentRow" class="results" length='<%=resultCount%>'  >
    <%for(String headerField : myDisplayHeader){%>
    <%if(doExport){%>
    <display:setProperty name="export.rtf.filename" value="dashboard_items.rtf"/>
    <display:setProperty name="export.pdf" value="true" />
    <%}%>
    <%if(doLink){
        transbit.tbits.domain.Field myField = transbit.tbits.domain.Field.lookupBySystemIdAndFieldName(myBA.getSystemId(),headerField);
   %>
    <display:column title='<%=(myField.getDisplayName()+"          ")%>' href='<%=("/Q/" + myBA.getSystemPrefix() + "/" +((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get("request_id"))%>'  sortable="true" headerClass="sortable" >
       <%
        String fieldValue = "";
        Object fieldValueObj = ((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get(headerField);
            if(fieldValueObj != null){ 
                fieldValue = transbit.tbits.Helper.ReportHelper.ConvertToString(myField,fieldValueObj);
            }
        
    %>
    <%=fieldValue%> 
    </display:column>
    <%}else{
    transbit.tbits.domain.Field myField = transbit.tbits.domain.Field.lookupBySystemIdAndFieldName(myBA.getSystemId(),headerField);
    %>
    <display:column title='<%=(myField.getDisplayName()+"          ")%>'  autolink="true" sortable="true" headerClass="sortable" >
    <%
        String fieldValue = "";
        Object fieldValueObj = ((transbit.tbits.search.Result)getJspContext().getAttribute("currentRow")).get(headerField);
            if(fieldValueObj != null){ 
                fieldValue = transbit.tbits.Helper.ReportHelper.ConvertToString(myField,fieldValueObj);
            }
        
    %>
    <%=fieldValue%> 
    </display:column>
    <%}%>  
    <%}%>    
</display:table>
<%}%>