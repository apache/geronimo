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

<table width="100%" class="TableLine" summary="Artifact Aliases - List">
  <tr>
    <th scope="col" class="DarkBackground" align="left"><fmt:message key="artifact.normal.name"/></th>
    <th scope="col" class="DarkBackground" align="left"><fmt:message key="artifact.normal.aliases"/></th>
    <th scope="col" colspan="2" class="DarkBackground" align="center"><fmt:message key="artifact.normal.actions"/></th>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="AliasesData" items="${AliasesDatas}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}">${AliasesData.name}</td>
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${empty AliasesData.aliases}">
          <fmt:message key="artifact.normal.aliases" />
        </c:when>
        <c:otherwise>
          ${AliasesData.aliases}
        </c:otherwise>
      </c:choose>
    </td>
    <td width="75" class="${backgroundClass}">
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="edit" />
        <portlet:param name="name" value="${AliasesData.name}" />
        <portlet:param name="aliases" value="${AliasesData.aliases}" />
      </portlet:actionURL>"><fmt:message key="artifact.actions.edit"/></a>
    </td>
    <td width="75" class="${backgroundClass}">
      <a href="<portlet:actionURL portletMode="view">
         <portlet:param name="mode" value="remove" />
         <portlet:param name="name" value="${AliasesData.name}" />
        <portlet:param name="aliases" value="${AliasesData.aliases}" />
      </portlet:actionURL>"><fmt:message key="artifact.actions.remove"/></a>
    </td>
  </tr>
</c:forEach>
</table>
