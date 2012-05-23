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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" /><br>

<table>
<th class="DarkBackground"><fmt:message key="keystore.common.keystore"/></th>
<th class="DarkBackground"><fmt:message key="keystore.common.alias"/></th>
<th class="DarkBackground"><fmt:message key="consolebase.common.type"/></th>
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
<fmt:message key="keystore.certificateDetails.generateCSR"/></a></td>
<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="importCAReply-before" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
<fmt:message key="keystore.certificateDetails.importCAReply"/></a></td>
<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="changePassword-before" />
<portlet:param name="keystore" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
<fmt:message key="keystore.certificateDetails.changeKeyPwd"/></a></td>
</c:if>

<c:if test="${(keyLocked)}">
<td><u><fmt:message key="keystore.certificateDetails.generateCSR"/></u></td>
<td><u><fmt:message key="keystore.certificateDetails.importCAReply"/></u></td>
<td><u><fmt:message key="keystore.certificateDetails.changeKeyPwd"/></u></td>
</c:if>

<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="deleteEntry-before" />
<portlet:param name="id" value="${id}" />
<portlet:param name="alias" value="${alias}" /></portlet:actionURL>" onClick="return confirm('<fmt:message key="keystore.certificateDetails.reallyDelete"><fmt:param value="${alias}"/></fmt:message>');">
<fmt:message key="keystore.certificateDetails.deleteEntry"/></a></td>

<td>
<a href="<portlet:actionURL portletMode="view">
<portlet:param name="mode" value="certificateDetails-after" />
<portlet:param name="id" value="${id}" /></portlet:actionURL>">
<fmt:message key="keystore.certificateDetails.backToKeystore"/></a></td>
</tr>
</table>
<br/>

<c:set var="backgroundClass" value='LightBackground'/>
<c:forEach items="${certs}" var="cert">
<table>
<th class="DarkBackground" colspan="2" align="left"><fmt:message key="keystore.certificateDetails.certificateInfo"/></th>
<tr>
<td class="LightBackground"><fmt:message key="keystore.common.version"/>:</td>
<td class="LightBackground"><c:out value="${cert.version}"/></td>
</tr>
<tr>
<td class="MediumBackground"><fmt:message key="keystore.common.subject"/>:</td>
<td class="MediumBackground"><c:out value="${cert.subjectDN.name}"/></td>
</tr>
<tr>
<td class="LightBackground"><fmt:message key="keystore.common.issuer"/>:</td>
<td class="LightBackground"><c:out value="${cert.issuerDN.name}"/></td>
</tr>
<tr>
<td class="MediumBackground"><fmt:message key="keystore.common.serialNumber"/>:</td>
<td class="MediumBackground"><c:out value="${cert.serialNumber}"/></td>
</tr>
<tr>
<td class="LightBackground"><fmt:message key="keystore.certificateDetails.validFrom"/>:</td>
<td class="LightBackground"><c:out value="${cert.notBefore}"/></td>
</tr>
<tr>
<td class="MediumBackground"><fmt:message key="keystore.certificateDetails.validTo"/>:</td>
<td class="MediumBackground"><c:out value="${cert.notAfter}"/></td>
</tr>
<tr>
<td class="LightBackground"><fmt:message key="keystore.certificateDetails.signatureAlg"/>:</td>
<td class="LightBackground"><c:out value="${cert.sigAlgName}"/></td>
</tr>
<tr>
<td class="MediumBackground"><fmt:message key="keystore.certificateDetails.publicKeyAlg"/>:</td>
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
<td class="${backgroundClass}"><fmt:message key="keystore.certificateDetails.criticalExt"/>: </td>
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
<td class="${backgroundClass}"><fmt:message key="keystore.certificateDetails.nonCriticalExt"/>: </td>
<td class="${backgroundClass}"><c:out value="${extoid}"/></td>
</tr>
</c:forEach>
</table>
<br/>
</c:forEach>
