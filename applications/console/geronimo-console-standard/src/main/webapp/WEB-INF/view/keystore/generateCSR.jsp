<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
keystore: ${id}<br/>
alias: ${alias}<br/>

<table>
<th>PKCS10 Certification Request</th>
<tr>
<td>
<form action=>
<textarea rows="15" cols="80" readonly>
${csr}
</textarea>
</td>
</tr>
<tr>
<td><a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="generateCSR-after" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
Back</a></td>
</tr>
</table>
