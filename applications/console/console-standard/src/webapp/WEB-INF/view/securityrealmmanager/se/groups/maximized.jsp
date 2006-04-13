<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="100%">
    ${message}
	<tr>
		<td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New Group</a> </td>
		<td >&nbsp;</td>
	</tr>
        <tr>
            <td width="100">Group Name</td>
            <td>&nbsp;</td>
        </tr>
    <c:forEach var="group" items="${groupsInfo}">
        <form action="<portlet:actionURL></portlet:actionURL>">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="group" value="${group.key}">
        <tr>
            <td width="100"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="group" value="${group.key}"/></portlet:actionURL>">${group.key}</a></td>
            <td><a href="<portlet:actionURL><portlet:param name="group" value="${group.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete group ${group.key}?');">Delete</a></td>
        </tr>
        </form>
    </c:forEach>

</table>
