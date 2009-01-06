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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<b><fmt:message key="internaldb.listDatabases.title"/>:</b>
<table width="100%" class="TableLine" summary="DB Viewer - DB List">
  <tr>
    <th scope="col" class="DarkBackground" colspan="1" align="center"><fmt:message key="internaldb.common.databases"/></th>
    <th scope="col" class="DarkBackground" colspan="2" align="center"><fmt:message key="internaldb.common.viewTables"/></th>
  </tr>
  <%-- Check if there are databases to display  --%>
  <c:choose>
    <c:when test="${fn:length(databases) == 0}">
      <tr>
        <td class="LightBackground" colspan="3" align="center">*** <fmt:message key="internaldb.listDatabases.nodatabases"/> ***</td>
      </tr>
    </c:when>
    <c:otherwise>
      <c:forEach var="db" items="${databases}" varStatus="status">
      <jsp:useBean type="javax.servlet.jsp.jstl.core.LoopTagStatus" id="status" />
      <tr>
        <c:choose>
          <c:when test="<%= status.getCount() % 2 == 1 %>">
            <c:set var="tdClass" scope="page" value="LightBackground" />
          </c:when>
            <c:otherwise>
              <c:set var="tdClass" scope="page" value="MediumBackground" />
            </c:otherwise>
          </c:choose>
        <td class="<c:out value='${tdClass}' />"><c:out value="${db}" /></td>
        <td class="<c:out value='${tdClass}' />" align="center">
          <a href="<portlet:actionURL portletMode="view">
                     <portlet:param name="action" value="listTables" />
                     <portlet:param name="db" value="${db}" />
                     <portlet:param name="viewTables" value="application" />
                   </portlet:actionURL>"><fmt:message key="internaldb.common.application"/></a>
        </td>
        <td class="<c:out value='${tdClass}' />" align="center">
          <a href="<portlet:actionURL portletMode="view">
                     <portlet:param name="action" value="listTables" />
                     <portlet:param name="db" value="${db}" />
                     <portlet:param name="viewTables" value="system" />
                   </portlet:actionURL>"><fmt:message key="internaldb.common.system"/></a>
        </td>
      </tr>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</table>
