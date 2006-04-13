<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

alias: <c:out value="${alias}"/><br/>

<form method="post"
action="<portlet:actionURL><portlet:param name="action" value="save-pkcs7-cert"/></portlet:actionURL>">
<table>
<th>PKCS7 Certificaticate Reply</th>
<tr>
<td>
<textarea rows="25" cols="80" name="pkcs7cert">
...paste pkcs7 encoded certificate reply here...
</textarea>
</td>
</tr>
</table>
<table>
<tr>
<td><input type="submit" name="submit" value="Save"/></td>
<td><input type="submit" name="submit" value="Cancel"/></td>
</tr>
</table>
<input type="hidden" name="alias" value="${alias}">
</form>