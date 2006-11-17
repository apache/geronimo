<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<table>
<th>keystore</th>
<th>alias</th>
<th>type</th>
<tr>
<td>${id}</td>
<td>${alias}</td>
<td>${type}</td>
</tr>
</table>
<br/>
<table cellspacing="5">
<tr>
<c:if test="${!(keyLocked)}">
<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="generateCSR-before" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
Generate CSR</a></td>
<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="importCAReply-before" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
Import CA reply</a></td>
</c:if>

<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="deleteEntry-before" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${alias}?');">
Delete Entry</a></td>

<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="certificateDetails-after" />
<portlet:param name="id" value="${id}" /></portlet:actionURL>">
Back to keystore</a></td>
</tr>
</table>
<br/>

<c:forEach items="${certs}" var="cert">
<table>
<th>Certificate Info</th>
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
