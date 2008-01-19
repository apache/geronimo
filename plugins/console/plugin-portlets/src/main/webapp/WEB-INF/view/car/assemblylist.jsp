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

<h3><fmt:message key="car.list.pluginRepo" /> <a href='${repository}'>${repository}</a></h3>

<c:choose>
<c:when test="${fn:length(plugins) < 1}">
  <fmt:message key="car.list.noPlugins" />
</c:when>
<c:otherwise>
<form action="<portlet:actionURL/>">
<input id="mode" type="hidden" name="mode" value="assemblyView-before"/>

<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground"><a href='<portlet:actionURL>
	                                   <portlet:param name="column" value="${column}"/>
	                                   <portlet:param name="mode" value="listServer-after"/>
	                                  </portlet:actionURL>'>${column}</a></th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<tr>
    <td class="${style}">
        <input type="checkbox" name="plugin" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    </td>
  <td class="${style}">
    ${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:forEach>
</table>
    <input type="submit" value="Assemble"/>
</form>
</c:otherwise>
</c:choose>

<p><form>
<input type="submit" value="Cancel" onclick="history.go(-1); return false;" />
</form>
