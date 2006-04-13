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
<table width="100%" cellspacing="5">
    ${message}
	<tr>
		<td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New User</a> </td>
		<td></td>
	</tr>
        <tr>
            <td>Username</td>
            <td>&nbsp;</td>
        </tr>
     <c:set var="count" value="1"/>
    <c:forEach var="user" items="${userInfo}">
        <tr>
            <td width="100">
            <a href = "<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="userId" value="${user.key}"/></portlet:actionURL>">
            ${user.key}
            </a>
            </td>
            <td><a href="<portlet:actionURL><portlet:param name="userId" value="${user.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete user ${user.key}?');">Delete</a></td>
        </tr>
    </c:forEach>
    
</table>
