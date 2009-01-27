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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>
<table border="0">
    <tr>
        <td><h1><fmt:message key="car.assemblyConfirm.successful" /></h1></td>
    </tr>
    <c:forEach var="plugin" items="${plugins}">
    <c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
    <tr>
        <th align="left" valign="top"><fmt:message key="car.assemblyConfirm.plugin" />:</th>
        <td>${plugin.name}</td>
    </tr>
    <tr>
        <th align="left" valign="top"><fmt:message key="car.assemblyConfirm.moduleId" />:</th>
        <td>${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</td>
    </tr>
    <tr>
        <th align="left" valign="top"><fmt:message key="car.assemblyConfirm.description" />:</th>
        <td>${plugin.description}</td>
    </tr>
    <br>
    </c:forEach>
</table>
<br>
<p>File Location: ${absoluteDeployedPath}</p>
<br>
<form method=POST">
<input type="submit" value='<fmt:message key="consolebase.common.done" />' onclick="history.go(-4); return false;" />
</form>

