

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
<span class="sx  l stus">Cendura in News</span>
<%try{%>
<tbits:searchResult headerFileds="subject"  userID='<%=String.valueOf(user.getUserId())%>' BAPrifix='news'  exportOptions="false" generateLink="true" numberOfResults="10" />
<%}catch(Exception e){%>
    No News Found!
<%}%>