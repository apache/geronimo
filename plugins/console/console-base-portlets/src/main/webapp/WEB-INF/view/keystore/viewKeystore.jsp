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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" />

<p><fmt:message key="keystore.viewKeystore.title"/></p>

<table width="100%">
  <tr>
    <td class="DarkBackground">&nbsp;</td>
    <td class="DarkBackground"><fmt:message key="keystore.common.alias"/></td>
    <td class="DarkBackground" align="center"><fmt:message key="consolebase.common.type"/></td>
    <td class="DarkBackground" align="center"><fmt:message key="keystore.viewKeystore.certificateFingerprint"/></td>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="alias" items="${keystore.certificates}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        <fmt:message key="consolebase.common.view"/>
        </a>    
    </td>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td class="${backgroundClass}"><fmt:message key="keystore.common.trustedCertificate"/></td>
    <td class="${backgroundClass}">${keystore.fingerprints[alias]}</td>
  </tr>
</c:forEach>
<c:forEach var="alias" items="${keystore.keys}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        <fmt:message key="consolebase.common.view"/> 
        </a>    
    </td>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td class="${backgroundClass}"><fmt:message key="keystore.viewKeystore.privateKey"/></td>
    <td class="${backgroundClass}">${keystore.fingerprints[alias]}</td>
  </tr>
</c:forEach>
</table>

<p>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="uploadCertificate-before" /><portlet:param name="id" value="${keystore.name}" /></portlet:actionURL>"><fmt:message key="keystore.viewKeystore.addtrustCertificate"/></a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="configureKey-before" /><portlet:param name="keystore" value="${keystore.name}" /></portlet:actionURL>"><fmt:message key="keystore.viewKeystore.createprivateKey"/></a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="changePassword-before" /><portlet:param name="keystore" value="${keystore.name}" /></portlet:actionURL>"><fmt:message key="keystore.viewKeystore.changePassword"/></a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="list-before" /></portlet:actionURL>"><fmt:message key="keystore.viewKeystore.returnToKeystoreList"/></a>
</p>
