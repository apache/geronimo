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
var <portlet:namespace/>formName = "<portlet:namespace/>certReqDetailsForm";
var <portlet:namespace/>requiredFields = new Array("validFrom", "validTo");
var <portlet:namespace/>dateFields = new Array("validFrom", "validTo");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.emptyText"/>');
        return false;
    }
    for(i in <portlet:namespace/>dateFields) {
        if(!checkDateMMDDYYYY(<portlet:namespace/>formName, <portlet:namespace/>dateFields[i])) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.mmddyyyy"/>');
            return false;
        }
    }
    // Check if from date is before to date
    var fromDate = new Date(document.forms[<portlet:namespace/>formName].validFrom.value);
    var toDate = new Date(document.forms[<portlet:namespace/>formName].validTo.value);
    if(fromDate >= toDate) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.wrongDate"/>');
        return false;
    }

    return true;
}
</script>

<jsp:include page="_header.jsp" />

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<fmt:message key="ca.certReqDetails.title"/>
<p>
<fmt:message key="ca.certReqDetails.summary"/>
</p>

<form name="<portlet:namespace/>certReqDetailsForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="certReqDetails-after"/>
    <input type="hidden" name="pkcs10certreq" value="${pkcs10certreq}"/>
    <input type="hidden" name="requestId" value="${requestId}"/>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left"><fmt:message key="ca.common.certRequestorDetails"/>
            </th>
        </tr>
        <tr>
            <th class="LightBackground" align="right"><fmt:message key="ca.common.subject"/>:</th>
            <td class="LightBackground">
                <input type="hidden" name="subject" value="${subject}" /> ${subject}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right"><fmt:message key="ca.common.publicKey"/>:</th>
            <td class="MediumBackground">
                <input type="hidden" name="publickey" value="${publickey}" /><pre>${publickey}</pre>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
    </table>
    <table border="0">
        <tr>
            <th colspan="2" align="left"><fmt:message key="ca.common.detailsOfCert"/></th>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>sNo"><fmt:message key="ca.common.certSerialNo"/></label>:</th>
            <td>
                <input type="text" name="sNo" id="<portlet:namespace/>sNo" size="20" maxlength="200" value="${sNo}" READONLY/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>validFrom"><fmt:message key="ca.common.validFromDate"/> (mm/dd/yyyy)</label>:</th>
            <td>
                <input type="text" name="validFrom" id="<portlet:namespace/>validFrom" size="20" maxlength="200" value="${validFrom}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>validTo"><fmt:message key="ca.common.validToDate"/> (mm/dd/yyyy)</label>:</th>
            <td>
                <input type="text" name="validTo" id="<portlet:namespace/>validTo" size="20" maxlength="200" value="${validTo}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>algorithm"><fmt:message key="ca.common.signatureAlgorithm"/></label>:</th>
            <td>
                <select name="algorithm" id="<portlet:namespace/>algorithm">
                    <option <c:if test="${algorithm eq 'MD2withRSA'}">selected</c:if> >MD2withRSA</option>
                    <option <c:if test="${algorithm eq 'MD5withRSA' || algorithm eq '' || empty(algorithm)}">selected</c:if> >MD5withRSA</option>
                    <option <c:if test="${algorithm eq 'SHA1withRSA'}">selected</c:if> >SHA1withRSA</option>
                </select>
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="ca.common.reviewClientCertDetails"/>' onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value="Reset">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
