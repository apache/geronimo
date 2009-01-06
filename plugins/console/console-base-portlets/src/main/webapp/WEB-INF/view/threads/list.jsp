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
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<p><fmt:message key="threads.list.title"/></p>

<table width="100%" class="TableLine" summary="Thread Pool Configuration List">
  <tr>
    <th scope="col" class="DarkBackground"><fmt:message key="consolebase.common.name"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.size"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.actions"/></th>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="pool" items="${pools}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}">${pool.name}</td>
    <td class="${backgroundClass}">${pool.poolSize}</td>
    <td class="${backgroundClass}">
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="monitor-before" />
        <portlet:param name="abstractName" value="${pool.abstractName}" />
      </portlet:actionURL>"><fmt:message key="consolebase.common.monitor"/></a>
    </td>
  </tr>
</c:forEach>
</table>
