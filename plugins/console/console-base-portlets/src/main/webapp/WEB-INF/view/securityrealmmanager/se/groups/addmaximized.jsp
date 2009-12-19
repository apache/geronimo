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
<%@ page import="org.apache.geronimo.console.securitymanager.util.SERealmGroupHelper" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>addgroup";
var <portlet:namespace/>requiredFields = new Array("group");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="securityrealmmanager.common.emptyText"/>');
        return false;    
    }
    
    return true;
}

function <portlet:namespace/>move(formName, from, to){
    var objFrom = eval("document." + formName + "." + from);
    var objTo = eval("document." + formName + "." + to);
    for(var pos = objFrom.length-1; pos >=0; pos--){ 
        if(objFrom.options[pos].selected){
            objTo.options.length += 1;
            objTo.options[objTo.options.length-1].value = objFrom.options[pos].value;
            objTo.options[objTo.options.length-1].text = objFrom.options[pos].text;
            <portlet:namespace/>removeAt(objFrom,pos);
        }
    }    
}

function <portlet:namespace/>removeAt(obj,pos){
    for(var i = pos; i < obj.options.length-1; i++){
        obj.options[i].value = obj.options[i+1].value;
        obj.options[i].text = obj.options[i+1].text;
    }
    obj.options.length -= 1;    
}

function <portlet:namespace/>selectAll(formName, objName){
    var obj = eval("document." + formName + "." + objName);
    for(var i = 0; i < obj.options.length; i++){
        obj.options[i].selected = true;
    }
    return true;
}

</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<c:set var="add" value="${group == null}"/>
<form name="<portlet:namespace/>addgroup" action="<portlet:actionURL portletMode="view"/>" method="POST">
    <table cellspacing="5">
    <tr>
      <td colspan="2" align="left" class="formHeader">
       <c:choose>
	   <c:when test="${add}"> 
       		<b><fmt:message key="securityrealmmanager.se.groups.addmaximized.addGroup" /></b>
      		<c:set var="GroupName" value=""/>
      	    <c:set var="Submit" value="securityrealmmanager.common.add"/>
       </c:when>
       <c:otherwise>
			<b><fmt:message key="securityrealmmanager.se.groups.addmaximized.updateGroup" /></b>
      		<c:set var="GroupName" value="${group}"/>
      		<c:set var="Submit" value="securityrealmmanager.common.update"/>
       </c:otherwise>
       </c:choose>
        </td>
    </tr>
    <tr>
        <td width="200" class="formLabel"><fmt:message key="consolebase.common.groupName"/></td>
        <td class="formElement">
       <c:choose>
	   <c:when test="${add}"> 
	    <input type="hidden" name="action" value="add">
	    <input type="text" name="group" title='Submit' value="">
       </c:when>
       <c:otherwise>
	    <input type="hidden" name="action" value="update">
        <input type="hidden" name="group" value="${GroupName}">
        ${GroupName}
       </c:otherwise>
       </c:choose>       
        </td>
    </tr>   
    <tr>
        <td class="formLabel"><fmt:message key="consolebase.common.users"/></td>
        <td class="formElement">
        <c:choose>
        <c:when test="${users != null && fn:length(users) > 0}">
            <% 
            String[] users = ((String[])request.getAttribute("users"));
            %>
            <table>
            <tr>
                <td>
                <select name="availusers" title='<fmt:message key="consolebase.common.users"/>' size="4" multiple>
                <%
                for(int i = 0;i<users.length;i++){
                    String user = users[i];
                    Object grp = request.getAttribute("group");
                    String group = ((grp!=null)?grp.toString():"");
                %>    
                <%if(!SERealmGroupHelper.isGroupMember(group, user)){%>
                <option><%=user%></option>
                <%}%>
               <%}%> 
                </select>
                </td>
                <td align="center" valign="middle">
                <input type = "button" 
                    value="<fmt:message key="securityrealmmanager.common.add"/>&nbsp;&gt;&gt;"
                    onclick="<portlet:namespace/>move('<portlet:namespace/>addgroup','availusers','users');"
                    />
                    
                    <br/>
                <input type = "button" value="&lt;&lt;&nbsp;<fmt:message key="securityrealmmanager.common.remove"/>"
                    onclick="<portlet:namespace/>move('<portlet:namespace/>addgroup','users','availusers');"
                    />                
                </td> 
                <td>
                <select name="users" title='<fmt:message key="consolebase.common.users"/>' size="4" multiple>
                <%
                for(int i = 0;i<users.length;i++){
                    String user = users[i];
                    Object grp = request.getAttribute("group");
                    String group = ((grp!=null)?grp.toString():"");
                %>    
                <%if(SERealmGroupHelper.isGroupMember(group, user)){%>
                <option><%=user%></option>
                <%}%>
               <%}%> 
                </select>
                </td>
            </tr>   
            </table>
            </td>
             
        </c:when>
        <c:otherwise>
        <fmt:message key="securityrealmmanager.se.groups.addmaximized.noUsers" />
        </c:otherwise>
        </c:choose>       
        </td>
    </tr>
    <tr>   
       <td>&nbsp;</td><td align="left" class="formElement">
       <input type="submit" value="<fmt:message key="${Submit}"/>" 
            onclick="return <portlet:namespace/>validateForm() && <portlet:namespace/>selectAll('<portlet:namespace/>addgroup', 'users');">
       <input type="submit" name="cancel"  value="<fmt:message key="consolebase.common.cancel"/>">
       </td>
      </tr>
    </table>
</form>
