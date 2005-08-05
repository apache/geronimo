<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<c:if test="${empty(connectors)}">There are no AJP13 Connectors defined</c:if>
<c:if test="${!empty(connectors)}"><table>
    <tr><th>Connector</th><th>State</th><th>Port</th></tr>
<c:forEach var="info" items="${connectors}">
    <tr><td>${info.objectName}</td><td>${info.stateName}</td><td>${info.port}</td></tr>
</c:forEach>
</table>
</c:if>
