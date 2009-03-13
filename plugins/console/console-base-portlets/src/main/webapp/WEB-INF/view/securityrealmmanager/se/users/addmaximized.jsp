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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<script language="JavaScript">
var <portlet:namespace/>formName = "adduser";
var <portlet:namespace/>requiredFields = new Array("userId","password");
var <portlet:namespace/>passwordFields = new Array("password");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="securityrealmmanager.common.emptyText"/>');
        return false;    
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="securityrealmmanager.common.passwordMismatch"/>');
        return false;
    }
    
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<c:set var="add" value="${userID == null}"/>
<form name="adduser" action="<portlet:actionURL portletMode="view"/>" method="POST">
    <table cellspacing="5">
    <tr>
        <td colspan="2" align="left">
        <c:choose>
        <c:when test="${add}"> 
       		<b><fmt:message key="securityrealmmanager.se.users.addmaximized.addUser" /></b>
      		<c:set var="UserName" value=""/>
      		<c:set var="Action" value="add"/>      		
      		<c:set var="Submit" value="securityrealmmanager.common.add"/>
       </c:when>
       <c:otherwise>
			<b><fmt:message key="securityrealmmanager.se.users.addmaximized.updateUser" /></b>
      		<c:set var="UserName" value="${userID}"/>
      		<c:set var="Action" value="update"/>      		
      		<c:set var="Submit" value="securityrealmmanager.common.update"/>
       </c:otherwise>
       </c:choose>
        </td>
    </tr>
    <tr>
        <td width="200"><fmt:message key="consolebase.common.userID"/></td>
        <td>
        <input type="hidden" name="action" value="${Action}">
        <c:choose>
        <c:when test="${add}"> 
            <input type="text" name="userId" title='<fmt:message key="consolebase.common.userID"/>' value="${UserName}">
        </c:when>
        <c:otherwise>
            <input type="hidden" name="userId" value="${UserName}">
            ${UserName}
        </c:otherwise>
        </c:choose>
            
        </td>
    </tr>   
    <c:choose>
      <c:when test="${add}">
        <tr>
          <td width="200"><label for="<portlet:namespace/>group"><fmt:message key="consolebase.common.group"/></label></td>
          <td>
            <select name="group" id="<portlet:namespace/>group">
              <c:forEach var="groups" items="${groupsInfo}">
                <option value="${groups.key}">${groups.key}</option>
              </c:forEach>
            </select>
          </td>
        </tr>
      </c:when>
      <c:otherwise>
      </c:otherwise>
    </c:choose>
    <tr>
        <td width="200"><label for="<portlet:namespace/>password"><fmt:message key="consolebase.common.password"/></label></td>
        <td><input type="password" name="password" id="<portlet:namespace/>password" value=""></td>
    </tr>   
    <tr>
        <td width="200"><label for="<portlet:namespace/>confirmpassword"><fmt:message key="consolebase.common.confirmPassword"/></label></td>
        <td><input type="password" name="confirm-password" id="<portlet:namespace/>confirmpassword" value=""></td>
    </tr>
    <tr>   
       <td>&nbsp;</td> <td  align="left" class="formElement"><input type="submit" value="<fmt:message key="${Submit}"/>" onclick="return <portlet:namespace/>validateForm()"> <input type="submit" name="cancel"  value="<fmt:message key="consolebase.common.cancel"/>"></td>
     </tr>
    </table>
</form>
