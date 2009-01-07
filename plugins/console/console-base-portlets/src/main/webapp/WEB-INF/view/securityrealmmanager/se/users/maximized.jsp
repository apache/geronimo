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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script language="JavaScript">
function <portlet:namespace/>validateForm(formname){
    with(eval("document."+formname)){
        if(isEmptyString(password.value)){
            alert("Please enter a password");
            password.focus();
            return false;
        }
        if(isEmptyString(confirmpassword.value)){
            alert("Please re-enter password");
            confirmpassword.focus();
            return false;
        }
        if(password.value != confirmpassword.value){
            alert("Password and confirm password do not match!");
            password.focus();
            return false;
        }
    }
    return true;
}
function isEmptyString(value){
    return value.length < 1;
}
</script>

<CommonMsg:commonMsg/>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>"><fmt:message key="securityrealmmanager.se.users.maximized.createNewUser"/></a></p>

<table width="50%" class="TableLine" summary="Console Realm Users">
    <tr class="DarkBackground">
        <th scope="col"><fmt:message key="consolebase.common.userName"/></th>
        <th scope="col"><fmt:message key="consolebase.common.actions"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="user" items="${userInfo}">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}"> ${user.key} </td>
            <td class="${backgroundClass}">
            <a href = "<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="userId" value="${user.key}"/></portlet:actionURL>"><fmt:message key="consolebase.common.edit"/></a>
            &nbsp;
            <a href="<portlet:actionURL><portlet:param name="userId" value="${user.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete user ${user.key}?');"><fmt:message key="consolebase.common.delete"/></a>
            </td>
        </tr>
    </c:forEach>
</table>
