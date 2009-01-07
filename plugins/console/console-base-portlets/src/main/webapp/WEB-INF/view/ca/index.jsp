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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" />

<p>
<fmt:message key="ca.index.title"/>
</p>

<c:choose>
  <c:when test="${caNotSetup}">
    <!-- CA needs initialization -->
    <p>
    <fmt:message key="ca.index.CANotInitialized"/>
    
    <table border="0">
      <tr>
        <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="setupCA-before" /></portlet:actionURL>">
        <fmt:message key="ca.common.setupCertAuthority"/>
        </a> </td>
      </tr>
    </table>
  </c:when>
  <c:otherwise>
    <!-- CA is ready for use -->
    <p><fmt:message key="ca.index.CAInitialized"/>
    <c:choose>
      <c:when test="${caLocked}">
        &nbsp;<fmt:message key="ca.index.CALocked"/>
        <table border="0">
        <tr>
          <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="unlockCA-before" /></portlet:actionURL>">
          <fmt:message key="ca.common.unlockCA"/>
          </a> </td>
        </tr>
        </table>
      </c:when>
      <c:otherwise>
      &nbsp;<fmt:message key="ca.index.CAFunctionsAccessed"/>        
        <table border="0">
        <tr>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-after" /><portlet:param name="lock" value="lock" /></portlet:actionURL>"><fmt:message key="ca.common.lockCA"/></a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="caDetails-before" /></portlet:actionURL>"><fmt:message key="ca.common.viewCADetails"/></a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-after" /><portlet:param name="publish" value="publish" /></portlet:actionURL>"><fmt:message key="ca.index.publishCACert"/></a>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="listRequestsVerify-before" /></portlet:actionURL>"><fmt:message key="ca.index.requestsToBeVerified"/></a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="listRequestsIssue-before" /></portlet:actionURL>"><fmt:message key="ca.index.requestsToBeFulfilled"/></a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="processCSR-before" /></portlet:actionURL>"><fmt:message key="ca.common.issueNewCert"/></a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="viewCert-before" /></portlet:actionURL>"><fmt:message key="ca.common.viewIssuedCert"/></a>&nbsp;</td>
        </tr>
        </table>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
