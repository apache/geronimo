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

<script>
function trim(str){
    if(!str) return "";
    else if(str.charAt(0) == " "){       
        return trim(str.substring(1));
    }else if(str.charAt(str.length-1) == " "){
        return trim(str.substring(0,str.length-1));
    }else return str;    
}
function <portlet:namespace/>validateForm(){
    with(document.<portlet:namespace/>update){
        if(trim(logFile.value).length == ""){
            alert("Please input the log file name.");
            logFile.value="";
            logFile.focus();
            return false;
        }
        if(trim(layoutPattern.value) == ""){
            alert("Please input layout pattern.");
            layoutPattern.value="";
            layoutPattern.focus();
            return false;
        }
        
    }
    return true;
}
</script>

<fmt:message key="logmanager.common.title"/>
<p/>
<form name="<portlet:namespace/>update" action="<portlet:actionURL/>" onsubmit="return <portlet:namespace/>validateForm()" method="POST">
<input type="hidden" name="action" value="update"/>
<table width="680">
<tr>
<!--
        renderRequest.setAttribute("configuration", LogHelper.getConfiguration());
        renderRequest.setAttribute("logLevel", LogHelper.getLogLevel());
        renderRequest.setAttribute("refreshPeriod", LogHelper.getRefreshPeriod());

-->
    <td nowrap><fmt:message key="logmanager.common.configFile"/></td>
    <td><input type="text" name="configFile" value="${configFile}" size="45"/></td>
</tr>
<tr>
    <td nowrap><fmt:message key="logmanager.common.refreshPeriod"/></td>
    <td><input type="text" name="refreshPeriod" value="${refreshPeriod}" size="45"/></td>
</tr>
<tr>
    <td nowrap><fmt:message key="logmanager.common.logLevel"/></td>
    <td>
    <select name="logLevel">
        <option<c:if test="${logLevel eq 'ALL'}"> selected</c:if>>ALL</option>
        <option<c:if test="${logLevel eq 'TRACE'}"> selected</c:if>>TRACE</option>
        <option<c:if test="${logLevel eq 'DEBUG'}"> selected</c:if>>DEBUG</option>
        <option<c:if test="${logLevel eq 'INFO'}"> selected</c:if>>INFO</option>
        <option<c:if test="${logLevel eq 'WARN'}"> selected</c:if>>WARN</option>
        <option<c:if test="${logLevel eq 'ERROR'}"> selected</c:if>>ERROR</option>
        <option<c:if test="${logLevel eq 'FATAL'}"> selected</c:if>>FATAL</option>
        <option<c:if test="${logLevel eq 'OFF'}"> selected</c:if>>OFF</option>
    </select>
    </td>
</tr>
<tr>   
    <td colspan="2" align="center" class="formElement"><input type="submit" value='<fmt:message key="consolebase.common.update"/>' /> <input type="reset" value='<fmt:message key="consolebase.common.reset"/>' /></td>
</tr>
</table>
</form>
