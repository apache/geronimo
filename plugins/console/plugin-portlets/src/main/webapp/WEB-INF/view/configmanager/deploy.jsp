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
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<script>
// toggle the display state of an element
function <portlet:namespace/>toggleDisplay(id) {
  var element = document.getElementById("<portlet:namespace/>"+id);
  if (element.style.display == 'inline') {
      element.style.display='none';
  } else {
      element.style.display='inline';
  }
}
</script>

<!-- Abbreviated status message -->
<c:if test="${!(empty abbrStatusMessage)}">
    <div id="<portlet:namespace/>abbrStatusMessage" style="display:inline">
     ${abbrStatusMessage}<br/>
    <c:if test="${!(empty fullStatusMessage)}">
    <button onclick="<portlet:namespace/>toggleDisplay('fullStatusMessage');<portlet:namespace/>toggleDisplay('abbrStatusMessage');return false;">Show full details</button>
    </c:if>
    </div>
</c:if>
<!-- Full status message -->
<c:if test="${!(empty fullStatusMessage)}">
    <div id="<portlet:namespace/>fullStatusMessage" style="display:none">
    <pre>
<c:out escapeXml="true" value="${fullStatusMessage}"/>
    </pre>
    </div>
</c:if>

<P/>

<!-- Migrated Plan -->
<c:if test="${!(empty migratedPlan)}">
<hr/><br/>
<fmt:message key="configmanager.deploy.migratedPlanSummary" />

<p/>
<div id="<portlet:namespace/>migratedPlan" style="display:inline">
<fmt:message key="configmanager.deploy.migratedPlan" />:
<form method="POST" action="/console/forwards/plan-export">
    <textarea name="migratedPlan" rows=10 cols=80><c:out escapeXml="true" value="${migratedPlan}"/></textarea>
    <br/>
    <button onclick="<portlet:namespace/>toggleDisplay('originalPlan');<portlet:namespace/>toggleDisplay('migratedPlan');return false;"><fmt:message key="configmanager.deploy.showOriginalPlan" /></button>
    <input type="submit" value='<fmt:message key="configmanager.deploy.saveLocally" />' />
</form>
</div>
<div id="<portlet:namespace/>originalPlan" style="display:none">
<fmt:message key="configmanager.deploy.originalPlan" />:
<form method="POST">
    <textarea rows=10 cols=80><c:out escapeXml="true" value="${originalPlan}"/></textarea><br/>
    <button onclick="<portlet:namespace/>toggleDisplay('migratedPlan');<portlet:namespace/>toggleDisplay('originalPlan');return false;"><fmt:message key="configmanager.deploy.showMigratedPlan" /></button>
</form>
</div>
<br/><hr/><br/>
</c:if>

<form enctype="multipart/form-data" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
<table>
  <tr><th align="right"><fmt:message key="configmanager.common.archive" />: </th><td><input type="file" name="module" /></td></tr>
  <tr><th align="right"><fmt:message key="configmanager.common.plan" />: </th><td><input type="file" name="plan" /></td></tr>
  <tr>
    <td></td>
    <td>
        <input name="startApp" type="checkbox" value="yes" checked><fmt:message key="configmanager.common.startAppAfterInstall" /> <br />
        <input name="redeploy" type="checkbox" value="yes"><fmt:message key="configmanager.deploy.redeployApplication" /> <br />
    </td>
  </tr>
  <tr>
    <td></td>
    <td><input type="submit" value='<fmt:message key="consolebase.common.install"/>' /></td>
  </tr>
</table>
</form>
