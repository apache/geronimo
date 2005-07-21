<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">

    <br />${messageInstalled} ${messageStatus}
    <c:forEach var="configInfo" items="${configurations}">
        <tr>
            <td width="100">${configInfo.state}</td>
            <td width="100">
<c:if test="${configInfo.state.running}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="stop"/></portlet:actionURL>">Stop</a></c:if>
<c:if test="${configInfo.state.stopped}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="start"/></portlet:actionURL>">Start</a></c:if>
<c:if test="${configInfo.state.failed}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="stop"/></portlet:actionURL>">Stop</a></c:if>
            </td>
            <td width="100">
              <a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="uninstall"/></portlet:actionURL>">Uninstall</a>
            </td>
            <td>${configInfo.configID}</td>
        </tr>
    </c:forEach>
</table>
