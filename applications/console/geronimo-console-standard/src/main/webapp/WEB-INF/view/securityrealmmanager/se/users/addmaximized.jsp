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
<script language="JavaScript">
var <portlet:namespace/>formName = "adduser";
var <portlet:namespace/>requiredFields = new Array("userId","password");
function <portlet:namespace/>validateForm(){
    return (textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields) && <portlet:namespace/>passwordMatch());
}
function <portlet:namespace/>passwordMatch(){
     with(document.adduser){
        if(password.value != confirmpassword.value){
            alert("Password and confirm password do not match!");
            password.focus();
            return false;
        }
    }
}
</script>
<c:set var="add" value="${userID == null}"/>
<form name="adduser" action="<portlet:actionURL portletMode="view"/>" method="POST">
    <table cellspacing="5">
    <tr>
        <td colspan="2" align="left">
        <c:choose>
        <c:when test="${add}"> 
               <b>ADD USER</b>
              <c:set var="UserName" value=""/>
              <c:set var="Action" value="add"/>              
              <c:set var="Submit" value="Add"/>
       </c:when>
       <c:otherwise>
            <b>UPDATE USER</b>
              <c:set var="UserName" value="${userID}"/>
              <c:set var="Action" value="update"/>              
              <c:set var="Submit" value="Update"/>
       </c:otherwise>
       </c:choose>
        </td>
    </tr>
    <tr>
        <td width="200">UserID</td>
        <td>
        <input type="hidden" name="action" value="${Action}">
        <c:choose>
        <c:when test="${add}"> 
            <input type="text" name="userId" value="${UserName}">
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
          <td width="200">Group</td>
          <td>
            <select name="group">
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
        <td width="200">Password</td>
        <td><input type="password" name="password" value=""></td>
    </tr>   
    <tr>
        <td width="200">Confirm Password</td>
        <td><input type="password" name="confirmpassword" value=""></td>
    </tr>
    <tr>   
       <td>&nbsp;</td> <td  align="left" class="formElement"><input type="submit" value="${Submit}" onclick="return <portlet:namespace/>validateForm()"> <input type="submit" name="cancel"  value="Cancel"></td>
     </tr>
    </table>
</form>
