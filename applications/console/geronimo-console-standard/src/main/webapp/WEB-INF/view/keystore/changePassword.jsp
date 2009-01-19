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
<portlet:defineObjects/>

<jsp:include page="_header.jsp" />

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>ChangePasswordForm";
var <portlet:namespace/>requiredFields = new Array("password", "newPassword");
var <portlet:namespace/>passwordFields = new Array("newPassword");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        return false;
    }
    return true;
}
</script>

<c:choose>
    <c:when test="${!empty(alias)}">
        <b>Change password for private key ${alias}</b><br/>
    </c:when>
    <c:otherwise>
        <b>Change password for keystore ${keystore}</b><br/>
    </c:otherwise>
</c:choose>

<form name="<portlet:namespace/>ChangePasswordForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="alias" value="${alias}" />
    <input type="hidden" name="mode" value="${mode}" />
    <table border="0">
        <tr>
            <th align="right">Old password:</th>
            <td>
                <input type="password" name="password" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">New password:</th>
            <td>
                <input type="password" name="newPassword" size="20" maxlength="200" />
            </td>
        </tr>
        <tr>
            <th align="right">Confirm new password:</th>
            <td>
                <input type="password" name="confirm-newPassword" size="20" maxlength="200" />
            </td>
        </tr>
    </table>
    <input type="submit" value="Change Password" onClick="return <portlet:namespace/>validateForm();"/>
</form>

<c:choose>
    <c:when test="${!empty(alias)}">
        <p><a href="<portlet:actionURL portletMode="view">
                        <portlet:param name="mode" value="certificateDetails-before" />
                        <portlet:param name="id" value="${keystore}" />
                        <portlet:param name="alias" value="${alias}" />
                    </portlet:actionURL>">Cancel</a></p>
    </c:when>
    <c:otherwise>
        <p><a href="<portlet:actionURL portletMode="view">
                        <portlet:param name="mode" value="viewKeystore-before" />
                        <portlet:param name="id" value="${keystore}" />
                    </portlet:actionURL>">Cancel</a></p>
    </c:otherwise>
</c:choose>
