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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<p>
<fmt:message key="car.download.processing" >
<fmt:param  value="${configId}"/>
</fmt:message>
</p>

<p>
<fmt:message key="car.download.foundDependencies" />
</p>


<ul>
<c:forEach var="dependency" items="${dependencies}">
    <li>${dependency.groupId}/${dependency.artifactId}/${dependency.type}/${dependency.version}</li>
</c:forEach>
</ul>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
<table><tr>
<td valign="top">
    <input type="submit" value="Install Plugin" />
    <input type="hidden" name="file" value="${file}" />
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="mode" value="download-after" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <input type="hidden" name="proceed" value="true" />
</td>
<td valign="top">
<input type="submit" value="<fmt:message key="consolebase.common.cancel"/>" onclick="history.go(-1); return false;" />
</td></tr></table>
</form>
