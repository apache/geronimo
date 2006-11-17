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

<p>This portlet allows you to setup a Certification Authority (CA) and issue certificates in reply to
Certificate Signing Requests (CSRs).  <i>Setup Certification Authority</i> function allows to initialize
the CA by providing CA Identity details, algorithm parameters for CA's key pair and self-signed certificate
and a password to protect the CA's private key.  This password is to be used to unlock the CA to access CA
functions.
Once the CA is initialized, CSRs can be processed using <i>Issue New Certificate</i> function.  Previously
issued certificates can be viewed using <i>View Issued Certificate</i> function.
</p>

<jsp:include page="_header.jsp" />

<c:choose>
  <c:when test="${caNotSetup}">
    <!-- CA needs initialization -->
    <p>CA is not running or the CA may not have been initialized.  Please initialize the CA using the link provided below.
    <table border="0">
      <tr>
        <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="setupCA-before" /></portlet:actionURL>">Setup Certification Authority</a> </td>
      </tr>
    </table>
  </c:when>
  <c:otherwise>
    <!-- CA is ready for use -->
    <p>CA has been initialized.
    <c:choose>
      <c:when test="${caLocked}">
        But, the CA is locked.  Please unlock the CA to access CA functions.
        <table border="0">
        <tr>
          <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="unlockCA-before" /></portlet:actionURL>">Unlock CA</a> </td>
        </tr>
        </table>
      </c:when>
      <c:otherwise>
        CA functions can be accessed using the links provided below.
        <table border="0">
        <tr>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-after" /><portlet:param name="lock" value="lock" /></portlet:actionURL>">Lock CA</a> &nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="caDetails-before" /></portlet:actionURL>">View CA Details</a> &nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-after" /><portlet:param name="publish" value="publish" /></portlet:actionURL>">Publish CA Certificate</a> &nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="listRequestsVerify-before" /></portlet:actionURL>">Requests to be verified</a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="listRequestsIssue-before" /></portlet:actionURL>">Requests to be fulfilled</a>&nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="processCSR-before" /></portlet:actionURL>">Issue New Certificate</a> &nbsp;</td>
          <td>&nbsp;<a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="viewCert-before" /></portlet:actionURL>">View Issued Certificate</a> &nbsp;</td>
        </tr>
        </table>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
