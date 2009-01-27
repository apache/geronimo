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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>
<script language="javascript">
<!--
    function doSave(){
        document.datasource_form.action="<portlet:actionURL portletMode="view"/>";
        document.datasource_form.mode.value="save";
        return true;
    }
    function doCancel(){
        document.datasource_form.action="<portlet:actionURL portletMode="view"/>";
        document.datasource_form.mode.value="detail";
        return true;
    }
//-->
</script>
    
<form name="datasource_form" method="POST">
<input type="hidden" name="name" value="${ds.objectName}" />
<input type="hidden" name="mode" value="detail" />

<br>
<strong><fmt:message key="jmsmanager.activemq.common.connName" />:</strong>&nbsp;${ds.name}
<br><br>
<table width="100%">
    <tr><td><strong><label for="<portlet:namespace/>UserName"><fmt:message key="jmsmanager.activemq.config.userName" /></label></strong></td><td><input type="text" name="UserName" id="<portlet:namespace/>UserName" value="${ds.userName}" size="75" /></td></tr>
    <tr><td><strong><label for="<portlet:namespace/>password1"><fmt:message key="jmsmanager.common.psassword"/></label></strong></td><td><input type="password" name="password1" id="<portlet:namespace/>password1" size="75" /></td></tr>
    <tr><td><strong><label for="<portlet:namespace/>password2"><fmt:message key="jmsmanager.activemq.config.repeatPassword" /></label></strong></td><td><input type="password" name="password2" id="<portlet:namespace/>password2" size="75" /></td></tr>
<c:if test="${badPassword}"><tr><td colspan="2"><fmt:message key="jmsmanager.activemq.config.passwordsNotMatch"/></td></tr></c:if>
    <tr><td><strong><label for="<portlet:namespace/>ServerUrl"><fmt:message key="jmsmanager.activemq.config.serverUrl"/></label></strong></td><td><input type="text" name="ServerUrl" id="<portlet:namespace/>ServerUrl" value="${ds.serverUrl}" size="75" /></td></tr>
    <!--<tr><td><strong>Clientid</strong></td><td><input type="text" name="Clientid" value="${ds.clientid}" size="75" /></td></tr>


    <tr><td><strong>Partition Max Size</strong></td><td><input type="text" name="partitionMaxSize" value="${connectionManagerInfo.partitionMaxSize}" size="75" /></td></tr>
    <tr><td><strong>Partition Min Size</strong></td><td><input type="text" name="partitionMinSize" value="${connectionManagerInfo.partitionMinSize}" size="75" /></td></tr>
    <tr><td><strong>Blocking Timeout (Milliseconds)</strong></td><td><input type="text" name="blockingTimeoutMilliseconds" value="${connectionManagerInfo.blockingTimeoutMilliseconds}" size="75" /></td></tr>
    <tr><td><strong>Idle Timeout (Minutes)</strong></td><td><input type="text" name="idleTimeoutMinutes" value="${connectionManagerInfo.idleTimeoutMinutes}" size="75" /></td></tr>-->
    <tr><td colspan="2"><input type="submit" name="btnSave" value='<fmt:message key="jmsmanager.common.save"/>' onClick="doSave();"/><input type="submit" name="btnCancel" value="<fmt:message key="jmsmanager.common.cancel"/>" onClick="doCancel();"></td></tr>
</table>
</form>
