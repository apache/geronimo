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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects/>
<p><b>Apache mod_jk</b> -- Web App Selection</p>

<p>For each web application <i>currently running</i> in Geronimo, select:</p>
<dl>
  <dt><b>Through Apache</b></dt>
  <dd>Whether the web application should be exposed through Apache</dd>
  <dt><b>Static Content</b></dt>
  <dd>Whether Apache should serve static content for the web application (instead of all
    content being handled by Geronimo)</dd>
  <dt><b>Dynamic Paths</b></dt>
  <dd>If Apache is serving static content, which URL paths should be passed to Geronimo
      (e.g. <tt>/servlet/*</tt> or <tt>*.jsp</tt>)</dd>
</dl>

<!-- FORM TO COLLECT DATA FOR THIS PAGE -->
<form name="<portlet:namespace/>ApacheForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="webapp-after"/>
    <input type="hidden" name="os" value="${model.os}"/>
    <input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
    <input type="hidden" name="workersPath" value="${model.workersPath}"/>
    <input type="hidden" name="logFilePath" value="${model.logFilePath}"/>
<c:forEach var="webApp" items="${model.webApps}" varStatus="status">
    <input type="hidden" name="webapp.${status.index}.configId" value="${webApp.parentConfigId}"/>
    <input type="hidden" name="webapp.${status.index}.moduleBeanName" value="${webApp.moduleBeanName}"/>
    <input type="hidden" name="webapp.${status.index}.childName" value="${webApp.childName}"/>
    <input type="hidden" name="webapp.${status.index}.contextRoot" value="${webApp.contextRoot}"/>
    <input type="hidden" name="webapp.${status.index}.webAppDir" value="${webApp.webAppDir}"/>
</c:forEach>
    <table border="0">
        <tr>
            <th>Web Application</th>
            <th>Through Apache</th>
            <th>Static Content</th>
            <th>Dynamic Paths</th>
        </tr>
      <c:forEach var="web" items="${model.webApps}" varStatus="status">
      <c:if test="${web.running}">
        <tr>
            <td>${web.name}</td>
            <td align="center"><input type="checkbox" name="webapp.${status.index}.enabled"<c:if test="${model.webApps[status.index].enabled}"> checked="checked"</c:if> /></td>
            <td align="center"><input type="checkbox" name="webapp.${status.index}.serveStaticContent"<c:if test="${model.webApps[status.index].serveStaticContent}"> checked="checked"</c:if> /></td>
            <td><input type="text" name="webapp.${status.index}.dynamicPattern" size="20" maxlength="250"
                       value="${model.webApps[status.index].dynamicPattern}"/></td>
        </tr>
      </c:if>
      </c:forEach>

        <!-- SUBMIT BUTTON -->
        <tr>
            <td></td>
            <td colspan="3"><input type="submit" value="Finish"/></td>
        </tr>
    </table>
</form>
<!-- END OF FORM TO COLLECT DATA FOR THIS PAGE -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
