<%@ page import="transbit.tbits.domain.User"%>
<%@ page import="transbit.tbits.webapps.WebUtil"%>
<% 
            User user = null;
            try{
                 user = WebUtil.validateUser(request);
           }catch(Exception e){
               return;
           }
%>
<%@ taglib prefix="tbits" tagdir="/WEB-INF/tags" %>
<span class="sx  l stus" align="center">Latest Updates on your requests.</span>
<%try{%>
<tbits:searchResult headerFileds="request_id:subject:lastupdated_datetime"  userID='<%=String.valueOf(user.getUserId())%>' BAPrifix='<%=request.getParameter("ba").trim()%>'  query="( assign:giris OR logger:giris  OR subscriber:giris  OR user:giris ) AND updateddate:last:20days" exportOptions="false" generateLink="true" numberOfResults="10" />
<%}catch(Exception e){%>
    No updates Found!
<%}%>