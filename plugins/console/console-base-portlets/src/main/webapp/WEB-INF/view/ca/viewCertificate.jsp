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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>sNoForm";
var <portlet:namespace/>requiredFields = new Array("sNo");
var <portlet:namespace/>integerFields = new Array("sNo");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.emptyText"/>');
        return false;
    }
    for(i in <portlet:namespace/>integerFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>integerFields[i])) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.integer"/>');
            return false;
        }
    }

    return true;
}
</script>

<jsp:include page="_header.jsp" />

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<b><fmt:message key="ca.common.title"/></b>

<c:choose>
  <c:when test="${empty(sNo)}">
    <!-- No serial number was received to show a certificate -->
    <p>
    <fmt:message key="ca.viewCertificate.enterSerialNumber"/>
    </p>
    <form name="<portlet:namespace/>sNoForm" action="<portlet:actionURL/>" method="POST">
      <input type="hidden" name="mode" value="viewCert-before" />
      <table border="0">
        <tr>
            <th align="right"><label for="<portlet:namespace/>sNo"><fmt:message key="ca.common.certSerialNo"/></label>:</th>
            <td>
                <input type="text" name="sNo" id="<portlet:namespace/>sNo" size="20" maxlength="200" />
            </td>
        </tr>
      </table>
      <input type="submit"value='<fmt:message key="ca.common.title"/>' onClick="return <portlet:namespace/>validateForm();"/>
      <input type="reset" name="reset" value='<fmt:message key="consolebase.common.reset"/>'>
    </form>
  </c:when>
  <c:otherwise>
    <p> 
     <fmt:message key="ca.viewCertificate.screenShows"/></p>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.certificateDetails"/></th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="consolebase.common.version"/>:</th>
            <td class="LightBackground">${cert.version}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.subject"/>:</th>
            <td class="MediumBackground">${cert.subjectDN.name}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.issuer"/>:</th>
            <td class="LightBackground">${cert.issuerDN.name}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.serialNumber"/>:</th>
            <td class="MediumBackground">${cert.serialNumber}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.validFrom"/>:</th>
            <td class="LightBackground">${cert.notBefore}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.validTo"/>:</th>
            <td class="MediumBackground">${cert.notAfter}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.signatureAlg"/>:</th>
            <td class="LightBackground">${cert.sigAlgName}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.publicKeyAlg"/>:</th>
            <td class="MediumBackground">${cert.publicKey.algorithm}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.keySize"/>:</th>
            <td class="LightBackground">${keySize}</td>
        </tr>
  <c:set var="backgroundClass" value='LightBackground'/> <!-- This should be set from the row above. -->
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
            <th class="${backgroundClass}" align="right"><fmt:message key="ca.common.criticalExt"/>: </th>
            <td class="${backgroundClass}">${extoid}</td>
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
            <th class="${backgroundClass}" align="right"><fmt:message key="ca.common.nonCriticalExt"/>: </th>
            <td class="${backgroundClass}">${extoid}</td>
        </tr>
  </c:forEach>
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
            <th class="${backgroundClass}" align="right"><fmt:message key="ca.common.fingerPrints"/>:</th>
            <td class="${backgroundClass}">
  <c:forEach items="${fingerPrints}" var="fp">
                ${fp.key} = &nbsp; ${fp.value} <br/>
  </c:forEach>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th colspan="2" align="left"><label for="<portlet:namespace/>certText"><fmt:message key="ca.common.base64EncodedCertText"/></label></th>
        </tr>
        <tr>
            <td colspan="2">
                <form><textarea rows="15" cols="80" id="<portlet:namespace/>certText" READONLY>${certText}</textarea></form>
            </td>
        </tr>
    </table>
</c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${linkToListRequests}">
    <p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="listRequestsIssue-before" />
            </portlet:actionURL>"><fmt:message key="ca.viewCertificate.backToRequest"/></a></p>
  </c:when>
  <c:otherwise>
    <p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="ca.common.backToCAHome"/></a></p>
  </c:otherwise>
</c:choose>
