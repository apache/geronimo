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
  var EXPERT_COOKIE = "org.apache.geronimo.assembly.expertmode";
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

    //this function searches the category column and display
    //rows within the table (identified by tid) that contain the searchterm
    function filterCategory(searchterm, tid) {
	    var term = searchterm.value.toLowerCase().split(" ");;
	    var table = document.getElementById(tid);
	    var element;
	    for (var i = 2; i < table.rows.length; i++) {
            //replace special characters with ""
	        element = table.rows[i].cells[3].innerHTML.replace(/<>/g,"");
            table.rows[i].style.display = 'none';

            for (var j = 0; j < term.length; j++) {
                if (element.toLowerCase().indexOf(term[j]) >= 0) {
                    //if search term is found, display
                    table.rows[i].style.display = '';
                    break;
                }
            } 
        }
    }

    //this function allows a user to search 2 tables
    function filterTables(searchterm, t1, t2) {
        if (document.frmlst.expertMode.checked) {
            filterCategory(searchterm, t1);
            filterCategory(searchterm, t2);
        } else {
            filterCategory(searchterm, t1);
            toggleExpertMode();
        }
    }

    //this function allows a user to search 3 tables
    function filterTables(searchterm, t1, t2, t3) {
        if (document.frmlst.expertMode.checked) {
            filterCategory(searchterm, t1);
            filterCategory(searchterm, t2);
            filterCategory(searchterm, t3);
        } else {
            filterCategory(searchterm, t1);
            filterCategory(searchterm, t2);
            toggleExpertMode();
        }
    }

    //Toggle expert mode on and off with onClick
    function toggleExpertMode() {
  	    var table = document.getElementById("sysPlugin");
        if (document.frmlst.expertMode.checked) {
            //  Set attribute/parameter to indicated expertMode is checked
            document.cookie=EXPERT_COOKIE+"=true";
    	    for (var i = 0; i < table.rows.length; i++) {
                table.rows[i].style.display = ''; 
            }
        }
        else {
            //  Set attribute/parameter to indicated expertMode is not checked
            document.cookie=EXPERT_COOKIE+"=false";
    	    for (var i = 0; i < table.rows.length; i++) {
                table.rows[i].style.display = 'none'; 
            }
        }
    }


    // get cookie utility routine
    function getCookie(name) {
        var result = "";
        var key = name + "=";
        if (document.cookie.length > 0) {
            start = document.cookie.indexOf(key);
            if (start != -1) { 
                start += key.length;
                end = document.cookie.indexOf(";", start);
                if (end == -1) end = document.cookie.length;
                result=document.cookie.substring(start, end);
            }
        }
        return result;
    }

    // initialization routine to set the initial display state for expert mode correctly
    function init() {
        if (getCookie(EXPERT_COOKIE) == 'true') {
            document.frmlst.expertMode.checked = true;
        } else {
            document.frmlst.expertMode.checked = false;
        }
        toggleExpertMode();
    }
</script>


<c:choose>
<c:when test="${fn:length(appPlugins) < 1 || fn:length(sysPlugins) < 1 }">
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

<h3>Select from plugins in current server:</h3>
<p><input type="checkbox" name="expertMode" id="<portlet:namespace/>expertMode" onClick="toggleExpertMode();" />&nbsp;
<label for="<portlet:namespace/>expertMode">Expert User (view all system plugins)</label>
</p>

<c:choose>
<c:when test="${type eq 'Application Centric'}">   
<p>Filter by category: <input name="filterbyca" onkeyup="filterTables(this, 'appPlugin', 'sysPlugin')" type="text"></p>
</c:when>
<c:otherwise>
<p>Filter by category: <input name="filterbyca" onkeyup="filterTables(this, 'groupPlugin', 'appPlugin', 'sysPlugin')" type="text"></p>
</c:otherwise>
</c:choose>

<table id="reqPlugin" border="0" cellpadding="3">
<tr>
  <td colspan="4"><h4><fmt:message key="car.list.requiredPlugin"/></h4></td>
</tr>
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${groupPlugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${artifact.artifactId eq 'framework'}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" CHECKED/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>' title="${plugin.description}">${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>
<br/>

<c:if test="${type eq 'Function Centric'}">   
<table id="groupPlugin" border="0" cellpadding="3">
<tr>
  <td colspan="4"><h4><fmt:message key="car.list.PluginGroup"/></h4></td>
</tr>
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${groupPlugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${artifact.artifactId ne 'framework'}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" 
           title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" 
           value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>' title="${plugin.description}">${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>
</c:if>    
</c:forEach>
</table>
<br/>
</c:if>

<table id="appPlugin" border="0" cellpadding="3">
<tr>
  <td colspan="4"><h4><fmt:message key="car.list.applicationPlugin"/></h4></td>
</tr>
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${appPlugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
    <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
    <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>' title="${plugin.description}">${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
</tr>   
</c:forEach>
</table>
<br/>

<c:choose>
<c:when test="${type eq 'Application Centric'}"> 
<!-- sysPlugin for application centric mode, which includes plugin groups -->
<table id="sysPlugin" border="0" cellpadding="3">
<tr>
  <td colspan="5"><h4><fmt:message key="car.list.systemPlugin"/></h4></td>
</tr>
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category,ConfigId">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${sysPlugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${artifact.artifactId ne 'framework'}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" 
           title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" 
           value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
      <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
      <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>' title="${plugin.description}">${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
  <td class="${style}">${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</td>
</tr>
</c:if>
</c:forEach>
</table>
</c:when>
<c:otherwise>
<!-- sysPlugin for function centric mode, which doesn't include plugin group -->
<table id="sysPlugin" border="0" cellpadding="3">
<tr>
  <td colspan="5"><h4><fmt:message key="car.list.systemPlugin"/></h4></td>
</tr>
<tr>
  <th class="DarkBackground">&nbsp;</th>
  <c:forEach var="column" items="Name,Version,Category,ConfigId">
  <th class="DarkBackground">${column}</th>
  </c:forEach>
</tr>
<c:forEach var="plugin" items="${sysPlugins}" varStatus="status">
<c:set var="style" value="${status.index % 2 == 0 ? 'MediumBackground' : 'LightBackground'}"/>
<c:set var="artifact" value="${plugin.pluginArtifact.moduleId}"/>
<c:if test="${!plugin.isPluginGroup}">
<tr>
  <td class="${style}">
    <input type="checkbox" name="plugin" 
           title="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}" 
           value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
  </td>
  <td class="${style}">
    <a href='<portlet:actionURL>
      <portlet:param name="configId" value="${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}"/>
      <portlet:param name="mode" value="assemblyView-before"/>
    </portlet:actionURL>' title="${plugin.description}">${plugin.name}</a>
  </td>
  <td class="${style}">${artifact.version}</td>
  <td class="${style}">${plugin.category}</td>
  <td class="${style}">${artifact.groupId}/${artifact.artifactId}/${artifact.version}/${artifact.type}</td>
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

<script>
// Call to set initial expert mode actions correctly
init();
</script>
