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
<script language="JavaScript">
var formName = "adduser";
var requiredFields = new Array("UserName","Password");
function <portlet:namespace/>validateForm(){
    return textElementsNotEmpty(formName,requiredFields) && <portlet:namespace/>passwordMatch(); 
}
function <portlet:namespace/>passwordMatch(){
     with(document.adduser){
        if(Password.value != ConfirmPassword.value){
            alert("Password and confirm password do not match!");
            Password.focus();
            return false;
        }
    }
}

</script>
<c:set var="add" value="${user == null}"/>
<form name="adduser" action="<portlet:actionURL portletMode="view"/>" method="POST">
    <table cellspacing="5">
    <tr>
      <td colspan="2" align="left" class="formHeader">
       <c:choose>
       <c:when test="${add}"> 
               <b><fmt:message key="securityrealmmanager.derby.users.addmaximized.addUser" /></b>
              <c:set var="UserName" value=""/>
              <c:set var="Password" value=""/>
              <c:set var="Password2" value=""/>
              <c:set var="FirstName" value=""/>
              <c:set var="MiddleInit" value=""/>
              <c:set var="LastName" value=""/>
              <c:set var="Department" value=""/>
              <c:set var="Email" value=""/>
              <c:set var="Submit" value="Add"/>
       </c:when>
       <c:otherwise>
            <b><fmt:message key="securityrealmmanager.derby.users.addmaximized.updateUser" /></b>
              <c:set var="UserName" value="${user['UserName']}"/>
              <c:set var="Password" value="xxxxxxxx"/>
              <c:set var="Password2" value="yyyyyyyy"/>              
              <c:set var="FirstName" value="${user['FirstName']}"/>
              <c:set var="MiddleInit" value="${user['MiddleInit']}"/>
              <c:set var="LastName" value="${user['LastName']}"/>
              <c:set var="Department" value="${user['Department']}"/>
              <c:set var="Email" value="${user['Email']}"/>
              <c:set var="Submit" value="Update"/>
       </c:otherwise>
       </c:choose>
        </td>
    </tr>
    <tr>
        <td width="200" class="formLabel"><fmt:message key="consolebase.common.userName"/></td>
        <td class="formElement">
       <c:choose>
       <c:when test="${add}"> 
        <input type="hidden" name="action" value="add">
        <input type="text" name="UserName" title='<fmt:message key="consolebase.common.userName"/>' value="" maxlength="30">
       </c:when>
       <c:otherwise>
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="UserName" value="${UserName}">
        ${UserName}
       </c:otherwise>
       </c:choose>       
        </td>
    </tr>   
    <tr>
        <td width="200" class="formLabel"> <label for="<portlet:namespace/>Password"><fmt:message key="consolebase.common.password"/></label></td>
        <td class="formElement"><input type="password" name="Password" id="<portlet:namespace/>Password" value="${Password}" maxlength="30"></td>
    </tr>   
    <tr>
        <td width="200"><label for="<portlet:namespace/>ConfirmPassword"><fmt:message key="consolebase.common.confirmPassword"/>Confirm Password</label></td>
        <td><input type="password" name="ConfirmPassword" id="<portlet:namespace/>ConfirmPassword" value="${Password2}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel"><label for="<portlet:namespace/>FirstName"><fmt:message key="consolebase.common.givenName"/></label></td>
        <td class="formElement"><input type="text" name="FirstName" id="<portlet:namespace/>FirstName" value="${FirstName}" maxlength="30"></td>
    </tr>

    <input type="hidden" name="MiddleInit" value="" >
    <tr>
        <td width="200" class="formLabel"><label for="<portlet:namespace/>LastName"><fmt:message key="consolebase.common.familyName"/></label></td>
        <td class="formElement"><input type="text" name="LastName" id="<portlet:namespace/>LastName" value="${LastName}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel"><label for="<portlet:namespace/>Department"><fmt:message key="consolebase.common.department"/></label></td>
        <td class="formElement"><input type="text" name="Department" id="<portlet:namespace/>Department" value="${Department}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel"><label for="<portlet:namespace/>Email"><fmt:message key="consolebase.common.email"/></label></td>
        <td class="formElement"><input type="text" name="Email" id="<portlet:namespace/>Email" value="${Email}" maxlength="30"></td>
    </tr>

    <tr>   
         <td>&nbsp;</td><td  align="left"><input type="submit" value="${Submit}" class="formElement" onclick="return <portlet:namespace/>validateForm()"> <input type="submit" name="cancel" value='<fmt:message key="consolebase.common.cancel"/>' ></td>
    </tr>
    </table>
</form>
