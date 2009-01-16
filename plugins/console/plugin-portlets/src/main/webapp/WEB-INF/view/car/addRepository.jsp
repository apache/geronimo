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

<h2>
<fmt:message key="car.addRepository.title" />
</h2>

<c:if test="${!empty repositories}">
<p><fmt:message key="car.addRepository.currentlyPluginRepositories" />:</p>
<ul>
  <c:forEach var="repo" items="${repositories}">
    <li><tt>${repo}</tt></li>
  </c:forEach>
</ul>
</c:if>
<p>
<fmt:message key="car.addRepository.howToAddPluginRepositories" />
</p>


<p>
<fmt:message key="car.addRepository.downloadRunningCongfig" />
</p>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="addRepository-after" />
    <b><label for="<portlet:namespace/>newRepository"><fmt:message key="car.common.newRepository" /></label>:</b> <input type="text" name="newRepository" id="<portlet:namespace/>newRepository" size="40" maxlength="200" />
    <br/>
    <b><fmt:message key="car.index.optionalAuthentication"/>:</b>
    <label for="<portlet:namespace/>username"><fmt:message key="consolebase.common.user"/></label>: <input type="text" name="username" id="<portlet:namespace/>username" value="${repouser}" size="12" maxlength="200"/>
    <label for="<portlet:namespace/>password"><fmt:message key="consolebase.common.password"/></label>: <input type="password" name="password" id="<portlet:namespace/>password" value="${repopass}" size="12" maxlength="200"/>
    <br/>
    <c:if test="${!empty repoError}">
      <p><font color="red">${repoError}</font></p>
    </c:if>
    <br />
    <input type="submit" value='<fmt:message key="car.common.addRepository" />' />
</form>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
