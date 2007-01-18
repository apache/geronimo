<%@ page language="java" contentType="text/html; charset=ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head><title>test</title></head>
<body>
<c:set var="var1" value="value1" scope="page"/>
<c:set var="var2" value="value2" scope="request"/>
<c:set var="var3" value="value3" scope="session"/>
<c:set var="var4" value="value4" scope="application"/>
<c:out value='${pageScope.var1}' />
<c:out value='${requestScope.var2}' />
<c:out value='${sessionScope.var3}' />
<c:out value='${applicationScope.var4}' />
</body>
</html>
