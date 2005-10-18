<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

alias: <c:out value="${alias}"/><br/>

<table>
<th>PKCS10 Certification Request</th>
<tr>
<td>
<form action=>
<textarea rows="15" cols="80" readonly>
<c:out value="${requestScope['org.apache.geronimo.console.cert.csr']}"/>
</textarea>
</td>
</tr>
<tr>
<td><a href="javascript:history.back();">back</a></td>
</tr>
</table>
