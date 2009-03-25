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

<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>KeystoreForm";
var <portlet:namespace/>requiredFields = new Array("filename", "password");
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    var illegalChars= /[\.]{2}|[()<>,;:\\/"'\|]/ ;
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.emptyText"/>');
        return false;
    } else if (document.forms[<portlet:namespace/>formName].filename.value.match(illegalChars)) {
        alert("Keystore name contains illegal characters");
        return false;
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="keystore.common.passwordMismatch"/>');
        return false;
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="createKeystore-after" />
    <table border="0">
        <tr>
            <th align="right"><label for="<portlet:namespace/>filename"><fmt:message key="keystore.createKeystore.keystoreFileName"/></label>:</th>
            <td>
                <input type="text" name="filename" id="<portlet:namespace/>filename" size="20" maxlength="100" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>password"><fmt:message key="keystore.createKeystore.passwordForKeystore"/></label>:</th>
            <td>
                <input type="password" name="password" id="<portlet:namespace/>password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>confirm-password"><fmt:message key="consolebase.common.confirmPassword"/></label>:</th>
            <td>
                <input type="password" name="confirm-password" id="<portlet:namespace/>confirm-password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right"><label for="<portlet:namespace/>type"><fmt:message key="keystore.createKeystore.type"/></label>:</th>
            <td>
                <select name="type" id="<portlet:namespace/>type">
                    <c:forEach var="keystoreType" items="${keystoreTypes}">
                        <option <c:if test="${defaultType eq keystoreType}">selected</c:if>>${keystoreType}</option>
                    </c:forEach>
                </select>
            </td>
        </tr>
    </table>
    <input type="submit" value='<fmt:message key="keystore.createKeystore.createKeystore"/>' onClick="return <portlet:namespace/>validateForm();"/>
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
