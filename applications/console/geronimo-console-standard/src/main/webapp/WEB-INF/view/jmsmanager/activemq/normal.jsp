<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
    <tr class="DarkBackground">
        <th class="LightBackground">&nbsp;</th>
        <th align="left">Name</th>
        <th align="left">State</th>
        <th align="left">Test Result</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="info" items="${cFactories}"><tr>
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <td class="${backgroundClass}"><a href='<portlet:renderURL>
            <portlet:param name="name" value="${info.objectName}"/>
            <portlet:param name="mode" value="detail"/>
            </portlet:renderURL>'>detail</a>
        </td>
        <td class="${backgroundClass}">${info.name}</td>
        <td class="${backgroundClass}"><c:choose>
            <c:when test='${info.state == 0}'>Starting</c:when>
            <c:when test='${info.state == 1}'>Running</c:when>
            <c:when test='${info.state == 2}'>Stopping</c:when>
            <c:when test='${info.state == 3}'>Stopped</c:when>
            <c:when test='${info.state == 4}'>Failed</c:when>
        </c:choose></td>
        <td class="${backgroundClass}">
        		<c:if test="${!info.working}">
        			<a href='<portlet:renderURL>
        					<portlet:param name="name" value="${info.objectName}"/>
        					<portlet:param name="mode" value="list"/>
        					<portlet:param name="check" value="true"/></portlet:renderURL>'>
        					test connection
        			</a>
        		</c:if>
        		<c:if test="${info.working}">
        			${info.message}
        		</c:if>
        </td>
    </tr></c:forEach>
</table>
<br>
<a href="<portlet:actionURL portletMode="view">
    <portlet:param name="mode" value="addACF" />
    </portlet:actionURL>">Add New JMS Connection Factory</a>