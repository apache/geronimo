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
The deployment plan you provided appears to be for a previous version of 
the application server.
A migrated version of your plan is provided below for your convenience.  Not all
deployment plans can be fully migrated so some manual editing may be required
before the migrated plan can be deployed.
<p/>
<div id="<portlet:namespace/>migratedPlan" style="display:inline">
Migrated plan:
<form method="POST" action="/console/forwards/plan-export">
    <textarea name="migratedPlan" rows=10 cols=80><c:out escapeXml="true" value="${migratedPlan}"/></textarea>
    <br/>
    <button onclick="<portlet:namespace/>toggleDisplay('originalPlan');<portlet:namespace/>toggleDisplay('migratedPlan');return false;">Show original plan</button>
    <input type="submit" value="Save this plan locally"/>
</form>
</div>
<div id="<portlet:namespace/>originalPlan" style="display:none">
Original plan:
<form>
    <textarea rows=10 cols=80><c:out escapeXml="true" value="${originalPlan}"/></textarea><br/>
    <button onclick="<portlet:namespace/>toggleDisplay('migratedPlan');<portlet:namespace/>toggleDisplay('originalPlan');return false;">Show Migrated plan</button>
</form>
</div>
<br/><hr/><br/>
</c:if>

<form enctype="multipart/form-data" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
<table>
  <tr><th align="right">Archive: </th><td><input type="file" name="module" /></td></tr>
  <tr><th align="right">Plan: </th><td><input type="file" name="plan" /></td></tr>
  <tr>
    <td></td>
    <td>
        <input name="startApp" type="checkbox" value="yes" checked>Start app after install <br />
        <input name="redeploy" type="checkbox" value="yes">Redeploy application <br />
    </td>
  </tr>
  <tr>
    <td></td>
    <td><input type="submit" value="Install" /></td>
  </tr>
</table>
</form>
