<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<form enctype="multipart/form-data" method="POST"
    action="<portlet:actionURL><portlet:param name="action" value="upload-certificate-file"/></portlet:actionURL>">
<table>
  <tr><th align="right">Certificate File: </th><td><input type="file" name="certfile" size="60"/></td></tr>
  <tr><td colspan="2" align="center"><input type="submit" name="submit" value="View Certificate" /></td></tr>
</table>
</form>

<c:set var="certs" value="${requestScope['org.apache.geronimo.console.certs']}"/>

<form method="POST"
action="<portlet:actionURL><portlet:param name="action" value="import-trusted-certificate"/></portlet:actionURL>">
<table>
<tr>
<td>Alias:</td><td><input type="text" name="alias"/></td>
</tr>
</table>

<br/>

<c:forEach items="${certs}" var="cert">
<table>
<th>Certificate Info</th>
<th><c:out value="${requestScope['org.apache.geronimo.console.cert.file.enc']}"/></th>
<tr>
<td>Version:</td>
<td><c:out value="${cert.version}"/></td>
</tr>
<tr>
<td>Subject:</td>
<td><c:out value="${cert.subjectDN.name}"/></td>
</tr>
<tr>
<td>Issuer:</td>
<td><c:out value="${cert.issuerDN.name}"/></td>
</tr>
<tr>
<td>Serial Number:</td>
<td><c:out value="${cert.serialNumber}"/></td>
</tr>
<tr>
<td>Valid From:</td>
<td><c:out value="${cert.notBefore}"/></td>
</tr>
<tr>
<td>Valid To:</td>
<td><c:out value="${cert.notAfter}"/></td>
</tr>
<tr>
<td>Signature Alg:</td>
<td><c:out value="${cert.sigAlgName}"/></td>
</tr>
<tr>
<td>Public Key Alg:</td>
<td><c:out value="${cert.publicKey.algorithm}"/></td>
</tr>
<tr>
<c:forEach items="${cert.criticalExtensionOIDs}" var="extoid">
<tr>
<td>critical ext: </td>
<td><c:out value="${extoid}"/></td>
</tr>
</c:forEach>
<c:forEach items="${cert.nonCriticalExtensionOIDs}" var="extoid">
<tr>
<td>non-critical ext: </td>
<td><c:out value="${extoid}"/></td>
</tr>
</c:forEach>
</table>
</c:forEach>

<br/>

<table>
<tr>
<c:choose>
<c:when test="${empty certs}">
<td align="center"><input type="submit" name="submit" value="Import" disabled/></td>
</c:when>
<c:otherwise>
<td align="center"><input type="submit" name="submit" value="Import"/></td>
</c:otherwise>
</c:choose>
<td align="center"><input type="submit" name="submit" value="Cancel"/></td>
</tr>
</table>
<input type="hidden" name="org.apache.geronimo.console.cert.file.enc"
    value="<c:out value="${requestScope['org.apache.geronimo.console.cert.file.enc']}"/>"/>
</form>
