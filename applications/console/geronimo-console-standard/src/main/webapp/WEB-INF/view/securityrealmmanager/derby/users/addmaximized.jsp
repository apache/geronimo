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
               <b>ADD USER</b>
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
            <b>UPDATE USER</b>
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
        <td width="200" class="formLabel">User Name</td>
        <td class="formElement">
       <c:choose>
       <c:when test="${add}"> 
        <input type="hidden" name="action" value="add">
        <input type="text" name="UserName" value="" maxlength="30">
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
        <td width="200" class="formLabel">Password</td>
        <td class="formElement"><input type="password" name="Password" value="${Password}" maxlength="30"></td>
    </tr>   
    <tr>
        <td width="200">Confirm Password</td>
        <td><input type="password" name="ConfirmPassword" value="${Password2}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel">Given Name</td>
        <td class="formElement"><input type="text" name="FirstName" value="${FirstName}" maxlength="30"></td>
    </tr>

    <input type="hidden" name="MiddleInit" value="" >
    <tr>
        <td width="200" class="formLabel">Family Name</td>
        <td class="formElement"><input type="text" name="LastName" value="${LastName}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel">Department</td>
        <td class="formElement"><input type="text" name="Department" value="${Department}" maxlength="30"></td>
    </tr>
    <tr>
        <td width="200" class="formLabel">Email</td>
        <td class="formElement"><input type="text" name="Email" value="${Email}" maxlength="30"></td>
    </tr>

    <tr>   
         <td>&nbsp;</td><td  align="left"><input type="submit" value="${Submit}" class="formElement" onclick="return <portlet:namespace/>validateForm()"> <input type="submit" name="cancel" value="Cancel" ></td>
    </tr>
    </table>
</form>
