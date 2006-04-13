<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
    ${message}
 	<tr>
		<td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New Group</a> </td><td colspan="2">&nbsp;</td>
	</tr>
        <tr>
            <td width="100">Group Name</td>
            <td width="150">Description</td>
            <td></td>
        </tr>
    <c:forEach var="group" items="${groups}">
        <tr>
            <td width="100"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="group" value="${group.key}"/></portlet:actionURL>">${group.key}</a></td>
            <td width="150">${group.value}</td>
            <td><a href="<portlet:actionURL><portlet:param name="group" value="${group.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete?');">Delete</a></td>
        </tr>
    </c:forEach>
</table>
