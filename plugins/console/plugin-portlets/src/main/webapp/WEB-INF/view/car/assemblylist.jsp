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

<script language="javascript" type="text/javascript">
  function checkAllVal(val) {
    val = document.getElementsByName(val);
    if(document.frmlst.artifactId.value == "") {
      alert("You must provide an Artifact Id Name.");
      return false;
    }
    for (i = 0; i < val.length; i++) {
      if(val[i].checked == true) {
        return true;
      }
    }
    alert("You must choose at least one plugin to install.");
    return false;
  }
</script>


<c:choose>
<c:when test="${fn:length(plugins) < 1}">
  <fmt:message key="car.list.noPlugins" />
  <p>
  <form>
    <input type="submit" value="Cancel" onclick="history.go(-1); return false;" />
  </form>
</c:when>
<c:otherwise>
<form name="frmlst" action="<portlet:actionURL/>">
    <h3><fmt:message key="car.list.nameServer" /></h3>
    <input id="mode" type="hidden" name="mode" value="assemblyView-before"/>
    
<table border="0" cellpadding="3">
<tr>
  <td><label for="<portlet:namespace/>groupId"><fmt:message key="car.list.assemblyGroupId"/></label></td>
  <td><input type="text" name="groupId" id="<portlet:namespace/>groupId" value="${groupId}"/></td>
</tr>
<tr>
  <td><label for="<portlet:namespace/>artifactId"><fmt:message key="car.list.assemblyArtifactId"/></label></td>
  <td><input type="text" name="artifactId" id="<portlet:namespace/>artifactId" value="${artifactId}"/></td>
</tr>
<tr>
  <td><label for="<portlet:namespace/>version"><fmt:message key="car.list.assemblyVersion"/></label></td>
  <td><input type="text" name="version" id="<portlet:namespace/>version" value="${version}"/></td>
</tr>
<tr>
  <td><label for="<portlet:namespace/>relativeServerPath"><fmt:message key="car.list.assemblyPath"/></label></td>
  <td><input type="text" name="relativeServerPath" id="<portlet:namespace/>relativeServerPath" value="${relativeServerPath}"/></td>
</tr>
<tr>
  <td><label for="<portlet:namespace/>format"><fmt:message key="car.list.assemblyFormat"/></label></td>
  <td><select name="format" id="<portlet:namespace/>format">
        <option <c:if test="${format ne 'zip'}">selected="true"</c:if>>tar.gz</option>
        <option <c:if test="${format eq 'zip'}">selected="true"</c:if>>zip</option>
      </select>
  </td>
</table>

<c:choose>
<c:when test="${type eq 'Application Centric'}">   

    <h3><fmt:message key="car.list.applicationPlugin"/></h3>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${!plugin.isSystemPlugin}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>'>${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>
</c:when>

<c:when test="${type eq 'Function Centric'}"> 
    <h3><fmt:message key="car.list.PluginGroup"/></h3>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${plugin.isPluginGroup}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>'>${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>


    <h3><fmt:message key="car.list.applicationPlugin"/></h3>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${!plugin.isSystemPlugin}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>'>${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>
</c:when>

<c:otherwise>

    <h3><fmt:message key="car.list.applicationPlugin"/></h3>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="ConfigId,Version,Category">
  <th class="DarkBackground"><a href='<portlet:actionURL>
	                                   <portlet:param name="column" value="${column}"/>
	                                   <portlet:param name="mode" value="index-after"/>
	                                  </portlet:actionURL>'>${column}</a></th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${!plugin.isSystemPlugin}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>'>${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>


    <h3><fmt:message key="car.list.systemPlugin"/></h3>
<table border="0" cellpadding="3">
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="ConfigId,Version,Category">
  <th class="DarkBackground"><a href='<portlet:actionURL>
	                                   <portlet:param name="column" value="${column}"/>
	                                   <portlet:param name="mode" value="index-after"/>
	                                  </portlet:actionURL>'>${column}</a></th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${plugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${plugin.isSystemPlugin}">
<tr>
    <td class="${style}">
        <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
      <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
      <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>'>${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>
</c:forEach>
</table>
</c:otherwise>
</c:choose>


    <input type="submit" value='<fmt:message key="car.common.assemble"/>' onclick="if(!checkAllVal('plugin')){return false;}else return true;"/>
    <input type="submit" value='<fmt:message key="consolebase.common.cancel"/>' onclick="history.go(-1); return false;" />

</form>
</c:otherwise>
</c:choose>
