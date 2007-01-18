<%@ page language="java" contentType="text/html; charset=ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head><title>test</title></head>
<% 
java.util.ArrayList locations = new java.util.ArrayList();
locations.add("One");
locations.add("Two");
pageContext.setAttribute("locations", locations);
%>
<body>
<c:forEach var="location" items="#{locations}">
<c:out value="${location}"/>
</c:forEach>
</body>
</html>
