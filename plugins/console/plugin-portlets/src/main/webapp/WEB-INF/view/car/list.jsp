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

<h3><fmt:message key="car.list.pluginRepo" />&nbsp;<a href='${repository}'>${repository}</a></h3>

<c:choose>
<c:when test="${fn:length(plugins) < 1}">
  <fmt:message key="car.list.noPlugins" />
  <p>
  <form>
    <input type="submit" value="Cancel" onclick="history.go(-1); return false;" />
  </form>
</c:when>
<c:otherwise>
<form action="<portlet:actionURL/>">
<input type="hidden" name="repository" value="${repository}"/>
<input type="hidden" name="repo-user" value="${repouser}"/>
<input type="hidden" name="repo-pass" value="${repopass}"/>
<input id="mode" type="hidden" name="mode" value="viewForDownload-before"/>
<!--
<input id="viewButton" type="submit" value="View" disabled
onclick="document.getElementById('mode').value='viewForDownload-before'">
<input id="installButton" type="submit" value="Install" disabled
onclick="document.getElementById('mode').value='viewForDownload-after'">
-->
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category,Installable">
  <th class="DarkBackground"><a href='<portlet:actionURL>
  	                                   <portlet:param name="repository" value="${repository}"/>
	                                   <portlet:param name="repo-user" value="${repouser}"/>
	                                   <portlet:param name="repo-pass" value="${repopass}"/>
	                                   <portlet:param name="column" value="${column}"/>
	                                   <portlet:param name="mode" value="index-after"/>
	                                  </portlet:actionURL>'>${column}</a></th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<tr>
<!--
  <td class="${style}">
    <input type="radio" name="configId" 
    <c:choose>
      <c:when test="${plugin.installable}">
    onclick="document.getElementById('viewButton').disabled=false; document.getElementById('installButton').disabled=false;"
      </c:when>
      <c:otherwise>
    onclick="document.getElementById('viewButton').disabled=false; document.getElementById('installButton').disabled=true;"
      </c:otherwise>
    </c:choose>
    value='<c:out escapeXml="true" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>' />
  </td>
-->
    <td class="${style}">
        <input type="checkbox" name="plugin" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" ${plugin.installable ? "": "disabled='true'"}/>
    </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
      <portlet:param name="repository" value="${repository}"/>
      <portlet:param name="repo-user" value="${repouser}"/>
      <portlet:param name="repo-pass" value="${repopass}"/>
      <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
      <portlet:param name="mode" value="viewForDownload-before"/>
    </portlet:actionURL>'>${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
  <td align="center" class="${style}">
    ${plugin.installable ? "<img alt='check' src='/console/images/checkmark._16_green.png' />" : "<strong><font color='red'>X</font></strong>"}
  </td>
</tr>
</c:forEach>
</table>
    <p>
    <input type="submit" value="Install"/>
    <input type="submit" value="Cancel" onclick="history.go(-1); return false;" />
</form>
</c:otherwise>
</c:choose>
