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
    for (i = 0; i < val.length; i++) {
      if(val[i].checked == true) {
        return true;
      }
    }
    addErrorMessage("<portlet:namespace/>", '<fmt:message key="car.list.nothing"/>');
    return false;
  }

    //this function searches the category and name/descrpition columns and display
    //rows within the table (identified by tid) that contain the searchterm
    function filterCategoryAndName(searchterm, tid) {
        var term = searchterm.value.toLowerCase().split(" ");;
        var table = document.getElementById(tid);
        var nameElement;
        var categoryElement;
        for (var i = 2; i < table.rows.length; i++) {
            //replace special characters with ""
            nameElement = table.rows[i].cells[1].innerHTML.replace(/<>/g,"");           
            categoryElement = table.rows[i].cells[3].innerHTML.replace(/<>/g,"");

            // let's remove the long href within the nameElement by using everything after title
            var j = nameElement.indexOf("title");
            if (j > 0) {
            	nameElement = nameElement.substring(j + 5);
            }
            table.rows[i].style.display = 'none';

            for (var j = 0; j < term.length; j++) {  
                if (categoryElement.toLowerCase().indexOf(term[j]) >= 0 || nameElement.toLowerCase().indexOf(term[j]) >= 0) {
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
        	filterCategoryAndName(searchterm, t1);
        	filterCategoryAndName(searchterm, t2);
        } else {
        	filterCategoryAndName(searchterm, t1);
            toggleExpertMode();
        }
    }

    //this function allows a user to search 3 tables
    function filterTables(searchterm, t1, t2, t3) {
        if (document.frmlst.expertMode.checked) {
        	filterCategoryAndName(searchterm, t1);
        	filterCategoryAndName(searchterm, t2);
        	filterCategoryAndName(searchterm, t3);
        } else {
        	filterCategoryAndName(searchterm, t1);
        	filterCategoryAndName(searchterm, t2);
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

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<form name="frmlst" action="<portlet:actionURL/>" method="POST">

    <input id="mode" type="hidden" name="mode" value="assemblyView-before"/>

<h3><fmt:message key="car.list.selectPlugin"/></h3>
<p><input type="checkbox" name="expertMode" id="<portlet:namespace/>expertMode" onClick="toggleExpertMode();" />&nbsp;
<label for="<portlet:namespace/>expertMode"><fmt:message key="car.list.expertUser"/></label>
</p>

<c:choose>
<c:when test="${type eq 'Application Centric'}">   
<p><fmt:message key="car.list.filterByCategoryAndName"/><input name="filterbyca" onkeyup="filterTables(this, 'appPlugin', 'sysPlugin')" type="text"></p>
</c:when>
<c:otherwise>
<p><fmt:message key="car.list.filterByCategoryAndName"/><input name="filterbyca" onkeyup="filterTables(this, 'groupPlugin', 'appPlugin', 'sysPlugin')" type="text"></p>
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

    <input type="hidden" name="relativeServerPath" value="${relativeServerPath}"/>
    <input type="hidden" name="groupId" value="${groupId}"/>
    <input type="hidden" name="artifactId" value="${artifactId}"/>
    <input type="hidden" name="version" value="${version}"/>
    <input type="hidden" name="format" value="${format}"/>
    <input type="submit" value='<fmt:message key="car.common.assemble"/>' onclick="if(!checkAllVal('plugin')){return false;}else return true;"/>
    <input type="submit" value='<fmt:message key="consolebase.common.cancel"/>' onclick="history.go(-2); return false;" />

</form>

<script>
// Call to set initial expert mode actions correctly
init();
</script>
