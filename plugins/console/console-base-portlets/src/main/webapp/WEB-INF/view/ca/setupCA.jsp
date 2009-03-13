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
var <portlet:namespace/>formName = "<portlet:namespace/>CADetailsForm";
var <portlet:namespace/>requiredFields = new Array("caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "password", "sNo", "validFrom", "validTo");
var <portlet:namespace/>integerFields = new Array("sNo");
var <portlet:namespace/>dateFields = new Array("validFrom", "validTo")
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.emptyText"/>');
        return false;
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.passwordMismatch"/>');
        return false;
    }
    for(i in <portlet:namespace/>integerFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>integerFields[i])) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="ca.common.integer"/>');
            return false;
        }
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

<jsp:include page="_header.jsp"/>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<fmt:message key="ca.setupCA.title"/>

<p>
<fmt:message key="ca.setupCA.summary"/>
</p>

<form name="<portlet:namespace/>CADetailsForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="setupCA-after"/>
    <table border="0">
        <tr>
            <th colspan="2" align="left"><fmt:message key="ca.common.certAuthorityIdentity"/></th>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caCN"><fmt:message key="ca.common.commonName"/> (CN)</label>:</th>
            <td>
                <input type="text" name="caCN" id="<portlet:namespace/>caCN" size="20" maxlength="200" value="${caCN}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caOU"><fmt:message key="ca.common.Division_BusinessUnit"/> (OU)</label>:</th>
            <td>
                <input type="text" name="caOU" id="<portlet:namespace/>caOU" size="20" maxlength="200" value="${caOU}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caO"><fmt:message key="ca.common.company_Organization"/> (O)</label>:</th>
            <td>
                <input type="text" name="caO" id="<portlet:namespace/>caO" size="20" maxlength="200" value="${caO}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caL"><fmt:message key="ca.common.city_Locality"/> (L)</label>:</th>
            <td>
                <input type="text" name="caL" id="<portlet:namespace/>caL" size="20" maxlength="200" value="${caL}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caST"><fmt:message key="ca.common.state_Province"/> (ST)</label>:</th>
            <td>
                <input type="text" name="caST" id="<portlet:namespace/>caST" size="20" maxlength="200" value="${caST}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>caC"><fmt:message key="ca.common.countryCode"/> (2 char) (C)</label>:</th>
            <td>
                <input type="text" name="caC" id="<portlet:namespace/>caC" size="3" maxlength="2" value="${caC}"/>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th colspan="2" align="left"><fmt:message key="ca.common.keyDetails"/></th>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>alias"><fmt:message key="consolebase.common.alias"/></label>:</th>
            <td>
                <input type="text" name="alias" id="<portlet:namespace/>alias" size="20" maxlength="100" value="${alias}"/>
            </td>
        </tr>

        <tr>
            <th align="right"><label for="<portlet:namespace/>keyAlgorithm"><fmt:message key="ca.common.keyAlgorithm"/></label>:</th>
            <td>
                <select name="keyAlgorithm" id="<portlet:namespace/>keyAlgorithm">
                    <option <c:if test="${keyAlgorithm eq 'RSA' || keyAlgorithm eq '' || empty(keyAlgorithm)}">selected</c:if> >RSA</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>keySize"><fmt:message key="ca.common.keySize"/></label>:</th>
            <td>
                <select name="keySize" id="<portlet:namespace/>keySize">
                    <option <c:if test="${keySize eq '512'}">selected</c:if> >512</option>
                    <option <c:if test="${keySize eq '1024' || keySize eq '' || empty(keySize)}">selected</c:if> >1024</option>
                    <option <c:if test="${keySize eq '2048'}">selected</c:if> >2048</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>password"><fmt:message key="consolebase.common.password"/></label>:</th>
            <td>
                <input type="password" name="password" id="<portlet:namespace/>password" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>confirmPassword"><fmt:message key="consolebase.common.confirmPassword"/></label>:</th>
            <td>
                <input type="password" name="confirm-password" id="<portlet:namespace/>confirmPassword" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr><td>&nbsp</td></tr>
        <tr>
            <th colspan="2" align="left"><fmt:message key="ca.common.certificateDetails"/></th>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>sNo"><fmt:message key="ca.common.certSerialNumber"/></label>:</th>
            <td>
                <input type="text" name="sNo" id="<portlet:namespace/>sNo" size="20" maxlength="200" value="${sNo}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>validFrom"><fmt:message key="ca.common.validFromDate"/>(mm/dd/yyyy)</label>:</th>
            <td>
                <input type="text" name="validFrom" id="<portlet:namespace/>validFrom" size="20" maxlength="200" value="${validFrom}"/>
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>validTo"><fmt:message key="ca.common.validToDate"/>(mm/dd/yyyy)</label>:</th>
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
    <input type="submit" value='<fmt:message key="ca.common.reviewCADetails"/>' onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value='<fmt:message key="consolebase.common.reset"/>'>
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
