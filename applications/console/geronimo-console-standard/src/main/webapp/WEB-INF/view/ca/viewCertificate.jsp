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

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>sNoForm";
var <portlet:namespace/>requiredFields = new Array("sNo");

function <portlet:namespace/>validateForm(){
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i]))
            return false;
    }
    return true;
}
</script>

<b>View Certificate</b>

<jsp:include page="_header.jsp" />

<c:choose>
  <c:when test="${empty(sNo)}">
    <!-- No serial number was received to show a certificate -->
    <p>Enter the serial number of the certificate to be viewed and click on <i>View Certificate</i> button.</p>
    <form name="<portlet:namespace/>sNoForm" action="<portlet:actionURL/>">
      <input type="hidden" name="mode" value="viewCert-before" />
      <table border="0">
        <tr>
            <th align="right">Certificate Serial Number.:</th>
            <td>
                <input type="text" name="sNo" size="20" maxlength="200" />
            </td>
        </tr>
      </table>
      <input type="submit" value="View Certificate" onClick="return <portlet:namespace/>validateForm();"/>
      <input type="reset" name="reset" value="Reset">
    </form>
  </c:when>
  <c:otherwise>
    <p> This screen shows the details of a certificate issued by the CA.  The base64 encoded certificate text
    should be sent to the requestor as a reply to their Certificate Signing Request (CSR).</p>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Certificate Details</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Version:</th>
            <td class="LightBackground">${cert.version}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Subject:</th>
            <td class="MediumBackground">${cert.subjectDN.name}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Issuer:</th>
            <td class="LightBackground">${cert.issuerDN.name}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Serial Number:</th>
            <td class="MediumBackground">${cert.serialNumber}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Valid From:</th>
            <td class="LightBackground">${cert.notBefore}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Valid To:</th>
            <td class="MediumBackground">${cert.notAfter}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Signature Alg:</th>
            <td class="LightBackground">${cert.sigAlgName}</td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Public Key Alg:</th>
            <td class="MediumBackground">${cert.publicKey.algorithm}</td>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Key Size:</th>
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
            <th class="${backgroundClass}" align="right">critical ext: </th>
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
            <th class="${backgroundClass}" align="right">non-critical ext: </th>
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
            <th class="${backgroundClass}" align="right">Finger prints:</th>
            <td class="${backgroundClass}">
  <c:forEach items="${fingerPrints}" var="fp">
                ${fp.key} = &nbsp; ${fp.value} <br/>
  </c:forEach>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th colspan="2" align="left">Base64 encoded Certificate Text</th>
        </tr>
        <tr>
            <td colspan="2">
                <form><textarea rows="15" cols="80" READONLY>${certText}</textarea></form>
            </td>
        </tr>
    </table>
</c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${linkToListRequests}">
    <p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="listRequestsIssue-before" />
            </portlet:actionURL>">Back to Requests to be fulfilled</a></p>
  </c:when>
  <c:otherwise>
    <p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Back to CA home</a></p>
  </c:otherwise>
</c:choose>
