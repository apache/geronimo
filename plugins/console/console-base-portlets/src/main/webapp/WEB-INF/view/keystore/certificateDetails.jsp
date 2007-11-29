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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" />

<table>
<th class="DarkBackground">keystore</th>
<th class="DarkBackground">alias</th>
<th class="DarkBackground">type</th>
<tr>
<td class="LightBackground">${id}</td>
<td class="LightBackground">${alias}</td>
<td class="LightBackground">${type}</td>
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
<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="changePassword-before" />
<portlet:param name="keystore" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
Change key password</a></td>
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

<c:set var="backgroundClass" value='LightBackground'/>
<c:forEach items="${certs}" var="cert">
<table>
<th class="DarkBackground" colspan="2" align="left">Certificate Info</th>
<tr>
<td class="LightBackground">Version:</td>
<td class="LightBackground"><c:out value="${cert.version}"/></td>
</tr>
<tr>
<td class="MediumBackground">Subject:</td>
<td class="MediumBackground"><c:out value="${cert.subjectDN.name}"/></td>
</tr>
<tr>
<td class="LightBackground">Issuer:</td>
<td class="LightBackground"><c:out value="${cert.issuerDN.name}"/></td>
</tr>
<tr>
<td class="MediumBackground">Serial Number:</td>
<td class="MediumBackground"><c:out value="${cert.serialNumber}"/></td>
</tr>
<tr>
<td class="LightBackground">Valid From:</td>
<td class="LightBackground"><c:out value="${cert.notBefore}"/></td>
</tr>
<tr>
<td class="MediumBackground">Valid To:</td>
<td class="MediumBackground"><c:out value="${cert.notAfter}"/></td>
</tr>
<tr>
<td class="LightBackground">Signature Alg:</td>
<td class="LightBackground"><c:out value="${cert.sigAlgName}"/></td>
</tr>
<tr>
<td class="MediumBackground">Public Key Alg:</td>
<td class="MediumBackground"><c:out value="${cert.publicKey.algorithm}"/></td>
</tr>
<c:set var="backgroundClass" value='MediumBackground'/> <!-- This should be set from the row above. -->
<c:forEach items="${cert.criticalExtensionOIDs}" var="extoid">
<c:choose>
    <c:when test="${backgroundClass == 'MediumBackground'}" >
        <c:set var="backgroundClass" value='LightBackground'/>
    </c:when>
    <c:otherwise>
        <c:set var="backgroundClass" value='MediumBackground'/>
    </c:otherwise>
</c:choose>
<tr>
<td class="${backgroundClass}">critical ext: </td>
<td class="${backgroundClass}"><c:out value="${extoid}"/></td>
</tr>
</c:forEach>
<c:forEach items="${cert.nonCriticalExtensionOIDs}" var="extoid">
<c:choose>
    <c:when test="${backgroundClass == 'MediumBackground'}" >
        <c:set var="backgroundClass" value='LightBackground'/>
    </c:when>
    <c:otherwise>
        <c:set var="backgroundClass" value='MediumBackground'/>
    </c:otherwise>
</c:choose>
<tr>
<td class="${backgroundClass}">non-critical ext: </td>
<td class="${backgroundClass}"><c:out value="${extoid}"/></td>
</tr>
</c:forEach>
</table>
<br/>
</c:forEach>
