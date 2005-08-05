<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<table width="100%" cellspacing="5">
    ${message}
	<tr>
		<td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New User</a> </td>
		<td >&nbsp;</td>
	</tr>
        <tr>
            <td>Username</td>
            <td>&nbsp;</td>
            
        </tr>
    <c:forEach var="user" items="${users}">
        <tr>
            <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="userId" value="${user.key}"/></portlet:actionURL>">${user.key}</a></td>
            
            <td><a href="<portlet:actionURL><portlet:param name="userId" value="${user.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete?');">Delete</a></td>
        </tr>
    </c:forEach>

</table>
