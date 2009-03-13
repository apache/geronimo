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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/>

<p><fmt:message key="dbwizard.importStatus.title"/></p>

<p><fmt:message key="dbwizard.importStatus.summary"/></p>

<table width="100%">
  <tr>
    <td class="DarkBackground"><fmt:message key="dbwizard.importStatus.originalJNDI"/></td>
    <td class="DarkBackground" align="center"><fmt:message key="dbwizard.importStatus.originalName"/></td>
    <td class="DarkBackground" align="center"><fmt:message key="dbwizard.importStatus.importStatus"/></td>
    <td class="DarkBackground" align="center"><fmt:message key="dbwizard.common.actions"/></td>
  </tr>
<c:forEach var="pool" items="${status.pools}" varStatus="loop" >
  <tr>
    <td>${pool.pool.name}</td>
    <td>${pool.pool.jndiName}</td>
    <td>${pool.status}</td>
    <td>
  <c:choose>
    <c:when test="${pool.skipped || pool.finished}">
    </c:when>
    <c:otherwise>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="importEdit" />
        <portlet:param name="importIndex" value="${loop.index}" />
      </portlet:actionURL>"><fmt:message key="dbwizard.common.confirmAndDeploy"/></a>
    </c:otherwise>
  </c:choose>
    </td>
  </tr>
</c:forEach>
  <tr>
    <td colspan="4" align="center">
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="importComplete" />
      </portlet:actionURL>"><c:choose><c:when test="${status.finished}"><fmt:message key="dbwizard.common.finish"/></c:when><c:otherwise><fmt:message key="dbwizard.common.skipRemainingPools"/></c:otherwise></c:choose></a>
    </td>
  </tr>
</table>

<hr />

<p><fmt:message key="dbwizard.importStatus.currentPools"/>:</p>
<ul>
<c:forEach var="pool" items="${pools}">
  <li>${pool.name}</li>
</c:forEach>
</ul>
