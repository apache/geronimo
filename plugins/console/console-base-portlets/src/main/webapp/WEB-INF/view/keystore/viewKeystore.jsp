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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This screen lists the contents of a keystore.</p>

<table width="100%">
  <tr>
    <td class="DarkBackground">&nbsp;</td>
    <td class="DarkBackground">Alias</td>
    <td class="DarkBackground" align="center">Type</td>
    <td class="DarkBackground" align="center">Certificate Fingerprint</td>
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
        view
        </a>    
    </td>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td class="${backgroundClass}">Trusted Certificate</td>
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
        view
        </a>    
    </td>
    <td class="${backgroundClass}"><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td class="${backgroundClass}">Private Key</td>
    <td class="${backgroundClass}">${keystore.fingerprints[alias]}</td>
  </tr>
</c:forEach>
</table>

<p>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="uploadCertificate-before" /><portlet:param name="id" value="${keystore.name}" /></portlet:actionURL>">Add Trust Certificate</a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="configureKey-before" /><portlet:param name="keystore" value="${keystore.name}" /></portlet:actionURL>">Create Private Key</a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="list-before" /></portlet:actionURL>">Return to keystore list</a>
</p>
