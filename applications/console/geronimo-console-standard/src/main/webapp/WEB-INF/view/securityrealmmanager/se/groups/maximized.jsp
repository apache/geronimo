<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table width="50%" cellspacing="5">
    ${message}
	<tr>
        <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New Group</a></td>
        <td></td>
	</tr>
    <tr class="DarkBackground">
        <th>Group Name</th>
        <th>Actions</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="group" items="${groupsInfo}">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}"> ${group.key} </td>
            <td class="${backgroundClass}">
            <a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="group" value="${group.key}"/></portlet:actionURL>">Details</a>
            &nbsp;
            <a href="<portlet:actionURL><portlet:param name="group" value="${group.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete group ${group.key}?');">Delete</a>
            </td>
        </tr>
        </form>
    </c:forEach>

</table>
