<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:set var="info" value="${requestScope['org.apache.geronimo.console.keystore.entry.info']}"/>
<table>
<th>alias</th>
<th>type</th>
<th>modified</th>
<tr>
<td><c:out value="${info.alias}"/></td>
<td><c:out value="${info.type}"/></td>
<td><c:out value="${info.created}"/></td>
</tr>
</table>
<br/>

<c:if test="${info.privateKey}">
<table cellspacing="5">
<tr>
<td><a href="<portlet:renderURL>
<portlet:param name='action' value='generate-csr'/>
<portlet:param name='alias' value='${info.alias}'/>
</portlet:renderURL>">
generate CSR</a></td>
<td><a href="<portlet:renderURL>
<portlet:param name='action' value='import-ca-reply'/>
<portlet:param name='alias' value='${info.alias}'/>
</portlet:renderURL>">
import CA reply</a></td>
<td><a href="<portlet:renderURL>
</portlet:renderURL>">
keystore list</a></td>
</tr>
</table>
<br/>
</c:if>

<c:set var="certs" value="${requestScope['org.apache.geronimo.console.certs']}"/>

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
<br/>
</c:forEach>