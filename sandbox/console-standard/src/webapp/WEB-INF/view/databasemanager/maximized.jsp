<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
    <c:forEach var="configInfo" items="${configurations}">
        <tr>
            <td width="500">${configInfo.state}</td>
            <td>
<c:if test="${configInfo.state.running}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="changeState" value="stop"/></portlet:actionURL>">Stop</a></c:if>
<c:if test="${configInfo.state.stopped}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="changeState" value="start"/></portlet:actionURL>">Start</a></c:if>
</td>
            <td>${configInfo.configID}</td>
        </tr>
    </c:forEach>
</table>
