<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib uri="/WEB-INF/tbits" prefix="tbits"%>
<%@taglib uri="/WEB-INF/tags/SearchResult" prefix="tbits"%>



<%@taglib uri='../taglib/displaytag.tld' prefix='display' %>
<%@ taglib prefix="tbits" tagdir="../taglib/tags"%>



<%@ taglib prefix="tbits" tagdir="/WEB-INF/tags" %>
<%@taglib uri='/WEB-INF/displaytag.tld' prefix='display' %>




--%>
<%--@ taglib prefix="tbits" tagdir="../taglib/tags"%>
<%-- --%>
<%@ taglib prefix="tbits" tagdir="/WEB-INF/tags" %>
<%-- --%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
    <link rel="stylesheet" type="text/css" media="screen" href="/web/css/tbits.css" />
    <tbits:searchResult headerFileds="subject"  user="giris" BAPrifix="kms"  exportOptions="false" generateLink="true" numberOfResults="10" />
    </body>
</html>
