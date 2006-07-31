<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
keystore: ${id}<br/>
alias: ${alias}<br/>

<form method="post"
action="<portlet:actionURL>
<portlet:param name="mode" value="importCAReply-after" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
<table>
<th>PKCS7 Certificate Reply</th>
<tr>
<td>
<textarea rows="20" cols="80" name="pkcs7cert">
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
</form>