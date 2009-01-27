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
<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>" method="POST">
<table border="0">
<c:forEach var="plugin" items="${plugins}">
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<input type="hidden" name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" />

<tr>
    <td><h1>${plugin.name}</h1></td>
</tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.moduleId" />:</th>
    <td>${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.category" />:</th>
    <td>${plugin.category}</td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.description" />:</th>
    <td>${plugin.description}</td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.author" />:</th>
    <td>${plugin.author}</td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.website" />:</th>
    <td><a href="${plugin.url}">${plugin.url}</a></td>
  </tr>
  <c:forEach var="license" items="${plugin.license}">
      <tr>
        <th align="right" valign="top"><fmt:message key="car.viewForDownload.license" />:</th>
        <td>${license.value}&nbsp;
          <c:choose>
              <c:when test="${license.osiApproved}">(<fmt:message key="car.common.openSource" />)</c:when>
              <c:otherwise>(<fmt:message key="car.common.proprietary" />)</c:otherwise>
          </c:choose>
        </td>
      </tr>
  </c:forEach>
    <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.geronimoVersions" />:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.geronimoVersion}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="geronimoVersion" items="${plugin.geronimoVersion}">
            <b>${geronimoVersion}</b>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.jvmVersions" />:</th>
    <td>
      <c:choose>
          <c:when test="${empty plugin.jvmVersion}">
            <i>Any</i>
          </c:when>
          <c:otherwise>
            <c:forEach var="jvmVersion" items="${plugin.jvmVersion}">
              ${jvmVersion}<br/>
            </c:forEach>
          </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.dependencies" />:</th>
    <td>
      <c:forEach var="dependency" items="${plugin.dependency}">
        ${dependency.groupId}/${dependency.artifactId}/${dependency.version}/${dependency.type}<br />
      </c:forEach>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.prerequisites" />:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.prerequisite}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="prereq" items="${plugin.prerequisite}">
            <b>${prereq.id.groupId}/${prereq.id.artifactId}/${prereq.id.version}/${prereq.id.type}</b> (${prereq.resourceType})<br/>
            ${prereq.description}
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.obsoletes" />:</th>
    <td>
      <c:choose>
        <c:when test="${empty plugin.obsoletes}">
          <i>None</i>
        </c:when>
        <c:otherwise>
          <c:forEach var="module" items="${plugin.obsoletes}">
            ${module.groupId}/${module.artifactId}/${module.version}/${module.type}<br />
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <th align="right" valign="top"><fmt:message key="car.viewForDownload.installable" />:</th>
    <td>
    ${plugin.installable ? "<img alt='check' src='/console/images/checkmark._16_green.png' /> " : "<strong><font color='red'>X</font></strong> "}
    ${validation}
    </td>
  </tr>
  </c:forEach>


<tr>

<c:choose>
  <c:when test="${mode eq 'assemblyView-after'}">
    <c:if test="${empty clickedConfigId}">
      <td valign="top">
        <input type="submit" value='<fmt:message key="car.common.assemble" />'/>
        <input type="hidden" name="mode" value="${mode}" />
        <input type="hidden" name="relativeServerPath" value="${relativeServerPath}"/>
        <input type="hidden" name="groupId" value="${groupId}"/>
        <input type="hidden" name="artifactId" value="${artifactId}"/>
        <input type="hidden" name="version" value="${version}"/>
        <input type="hidden" name="format" value="${format}"/>
      <td>
    </c:if>
    </c:when>
  <c:otherwise>
    <c:if test="${allInstallable}">
      <td valign="top">
        <input type="submit" value='<fmt:message key="consolebase.common.install" />'/>
        <input type="hidden" name="mode" value="${mode}" />
        <input type="hidden" name="repository" value="${repository}" />
        <input type="hidden" name="repo-user" value="${repouser}" />
        <input type="hidden" name="repo-pass" value="${repopass}" />
      <td>
    </c:if>
  </c:otherwise>
</c:choose>

<td valign="top">
<input type="submit" value="<fmt:message key="consolebase.common.return" />" onclick="history.go(-1); return false;" />
</td>
</tr>
</table>
</form>
