<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<script language="JavaScript">
function <portlet:namespace/>validateForm(formname){
    with(eval("document."+formname)){
        if(isEmptyString(password.value)){
            alert("Please enter a password");
            password.focus();
            return false;
        }
        if(isEmptyString(confirmpassword.value)){
            alert("Please re-enter password");
            confirmpassword.focus();
            return false;
        }
        if(password.value != confirmpassword.value){
            alert("Password and confirm password do not match!");
            password.focus();
            return false;
        }
    }
    return true;
}
function isEmptyString(value){
    return value.length < 1;
}
</script>
<table width="50%" cellspacing="5">
    ${message}
	<tr>
		<td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New User</a> </td>
		<td></td>
	</tr>
    <tr class="DarkBackground">
        <th>Username</th>
        <th>Actions</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="user" items="${userInfo}">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}"> ${user.key} </td>
            <td class="${backgroundClass}">
            <a href = "<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="userId" value="${user.key}"/></portlet:actionURL>">Details</a>
            &nbsp;
            <a href="<portlet:actionURL><portlet:param name="userId" value="${user.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete user ${user.key}?');">Delete</a>
            </td>
        </tr>
    </c:forEach>
    
</table>
