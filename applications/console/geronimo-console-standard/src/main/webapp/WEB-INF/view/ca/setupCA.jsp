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
var <portlet:namespace/>formName = "<portlet:namespace/>CADetailsForm";
var <portlet:namespace/>requiredFields = new Array("caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "password", "validFrom", "validTo");
var <portlet:namespace/>numericFields = new Array("sNo");
var <portlet:namespace/>dateFields = new Array("validFrom", "validTo")

function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i]))
            return false;
    }
    for(i in <portlet:namespace/>dateFields) {
        if(!checkDateMMDDYYYY(<portlet:namespace/>formName, <portlet:namespace/>dateFields[i]))
            return false;
    }
    // Check if from date is before to date
    var fromDate = new Date(document.forms[<portlet:namespace/>formName].validFrom.value);
    var toDate = new Date(document.forms[<portlet:namespace/>formName].validTo.value);
    if(fromDate >= toDate) {
        alert('validFrom date must be before validTo date');
        return false;
    }
    // Check if password and confirmPassword match
    if(document.forms[<portlet:namespace/>formName].password.value != document.forms[<portlet:namespace/>formName].confirmPassword.value) {
        alert('password and confirm password do not match.');
        document.forms[<portlet:namespace/>formName].password.focus();
        return false;
    }
    return true;
}
</script>

<b>Setup Certification Authority</b> - Step 1: Enter CA details

<p>On this screen you can enter the Certification Authority (CA) details, algorithm parameters
for CA's keypair, algorithm for CA's self signed certificate and a password to protect
the CA's private key. The next screen will let you review this information before
generating the CA's keypair and self-signed certificate.</p>

<jsp:include page="_header.jsp"/>

<form name="<portlet:namespace/>CADetailsForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="setupCA-after"/>
    <table border="0">
        <tr>
            <th colspan="2" align="left">Certification Authority's Identity</th>
        </tr>
        <tr>
            <th align="right">Common Name (CN):</th>
            <td>
                <input type="text" name="caCN" size="20" maxlength="200" value="${caCN}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Division/Business Unit (OU):</th>
            <td>
                <input type="text" name="caOU" size="20" maxlength="200" value="${caOU}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Company/Organization (O):</th>
            <td>
                <input type="text" name="caO" size="20" maxlength="200" value="${caO}"/>
            </td>
        </tr>
        <tr>
            <th align="right">City/Locality (L):</th>
            <td>
                <input type="text" name="caL" size="20" maxlength="200" value="${caL}"/>
            </td>
        </tr>
        <tr>
            <th align="right">State/Province (ST):</th>
            <td>
                <input type="text" name="caST" size="20" maxlength="200" value="${caST}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Country Code (2 char) (C):</th>
            <td>
                <input type="text" name="caC" size="3" maxlength="2" value="${caC}"/>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <th colspan="2" align="left">Key Details</th>
        </tr>
        <tr>
            <th align="right">Alias:</th>
            <td>
                <input type="text" name="alias" size="20" maxlength="100" value="${alias}"/>
            </td>
        </tr>

        <tr>
            <th align="right">Key Algorithm:</th>
            <td>
                <select name="keyAlgorithm">
                    <option <c:if test="${keyAlgorithm eq 'RSA' || keyAlgorithm eq '' || empty(keyAlgorithm)}">selected</c:if> >RSA</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right">Key Size:</th>
            <td>
                <select name="keySize">
                    <option <c:if test="${keySize eq '512'}">selected</c:if> >512</option>
                    <option <c:if test="${keySize eq '1024' || keySize eq '' || empty(keySize)}">selected</c:if> >1024</option>
                    <option <c:if test="${keySize eq '2048'}">selected</c:if> >2048</option>
                </select>
            </td>
        </tr>
        <tr>
            <th align="right">Password:</th>
            <td>
                <input type="password" name="password" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr>
            <th align="right">Confirm Password:</th>
            <td>
                <input type="password" name="confirmPassword" size="20" maxlength="200"/>
            </td>
        </tr>
        <tr><td>&nbsp</td></tr>
        <tr>
            <th colspan="2" align="left">Certificate Details</th>
        </tr>
        <tr>
            <th align="right">Certificate Serial Number:</th>
            <td>
                <input type="text" name="sNo" size="20" maxlength="200" value="${sNo}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Valid From Date(mm/dd/yyyy):</th>
            <td>
                <input type="text" name="validFrom" size="20" maxlength="200" value="${validFrom}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Valid To Date(mm/dd/yyyy):</th>
            <td>
                <input type="text" name="validTo" size="20" maxlength="200" value="${validTo}"/>
            </td>
        </tr>
        <tr>
            <th align="right">Signature Algorithm:</th>
            <td>
                <select name="algorithm">
                    <option <c:if test="${algorithm eq 'MD2withRSA'}">selected</c:if> >MD2withRSA</option>
                    <option <c:if test="${algorithm eq 'MD5withRSA' || algorithm eq '' || empty(algorithm)}">selected</c:if> >MD5withRSA</option>
                    <option <c:if test="${algorithm eq 'SHA1withRSA'}">selected</c:if> >SHA1withRSA</option>
                </select>
            </td>
        </tr>
    </table>
    <input type="submit" value="Review CA Details" onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value="Reset">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>">Cancel</a></p>
