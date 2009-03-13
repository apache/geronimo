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
  function checkArtifactId() {
     if(document.frmlst.artifactId.value == "") {
      addErrorMessage("<portlet:namespace/>", '<fmt:message key="car.list.requireArtifactId"/>');
      return false;
    }
    return true;
  }
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<c:choose>
<c:when test="${!containsPlugin}">
  <fmt:message key="car.list.noPlugins" />
  <p>
  <form>
    <input type="submit" value="Cancel" onclick="history.go(-1); return false;" />
  </form>
</c:when>
<c:otherwise>
<form name="frmlst" action="<portlet:actionURL/>">

    <h3><fmt:message key="car.list.nameServer" /></h3>
    <input id="mode" type="hidden" name="mode" value="listServer-before"/>
    
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

    <input type="hidden" name="type" value="${type}"/>
    <input type="submit" value='<fmt:message key="consolebase.common.next"/>' onclick="if(!checkArtifactId()){return false;}else return true;"/>
    <input type="submit" value='<fmt:message key="consolebase.common.cancel"/>' onclick="history.go(-1); return false;" />

</form>
</c:otherwise>
</c:choose>

