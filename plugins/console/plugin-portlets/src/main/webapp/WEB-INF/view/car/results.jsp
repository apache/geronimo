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
<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>
<p><b><fmt:message key="car.results.titles" />:</b></p>
<% 
    String[] configIds = request.getParameterValues("configId");
    for(int j=0;j<configIds.length;j++){
%>
        <%= configIds[j] %><br>
<%  }%>
    

<c:if test="${! empty dependencies}">
  <p><b><fmt:message key="car.results.filesProcessed" />:</b></p>
  <ul>
    <c:forEach var="dep" items="${dependencies}">
      <li>${dep.name} (${dep.action})</li>
    </c:forEach>
  </ul>
</c:if>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="index-before" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <%  
        for(int i=0;i<configIds.length;i++){
    %>
    <input type="hidden" name="configId" value="<%=configIds[i]%>" />
    <%
        }
    %>
    <input type="submit" value='<fmt:message key="consolebase.common.done"/>' />
</form>

