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
<portlet:defineObjects/>

<p>This page lists the thread pools defined in the Geronimo server.  <i>Note: Currently
not all threads used by Geronimo come from one of these thread pools.  We're working
on migrating the different components of Geronimo toward these thread pools.</i></p>

<table width="100%">
  <tr>
    <th class="DarkBackground">Name</th>
    <th class="DarkBackground" align="center">Size</th>
    <th class="DarkBackground" align="center">Actions</th>
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
      </portlet:actionURL>">monitor</a>
    </td>
  </tr>
</c:forEach>
</table>
