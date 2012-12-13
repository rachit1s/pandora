
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
<%try{%>
<span class="l b s cbk">New Releases</span>
<tbits:searchResult headerFileds="subject"  userID='<%=String.valueOf(user.getUserId())%>' BAPrifix='KMS'  exportOptions="false" generateLink="true" numberOfResults="10" query="type:(\"New Releases\")"/>
<%}catch(Exception e){%>
    No releases Found!
<%}%>