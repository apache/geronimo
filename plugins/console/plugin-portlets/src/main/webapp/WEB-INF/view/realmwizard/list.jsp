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
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<p><fmt:message key="realmwizard.list.title" /></p>

<p><fmt:message key="realmwizard.list.seeExamples" /></p>

<table width="100%" class="TableLine" summary="Security Realms - List">
  <tr>
    <th scope="col" class="DarkBackground"><fmt:message key="consolebase.common.name"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.deployedAs"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.actions"/></th>
  </tr>
<c:forEach var="realm" items="${realms}">
  <tr>
    <td>${realm.name}</td>
    <td>
      <c:choose>
        <c:when test="${empty realm.parentName}">
          <fmt:message key="realmwizard.common.serverWide" />
        </c:when>
        <c:otherwise>
          ${realm.parentName}  <%-- todo: make this a link to an application portlet --%>
        </c:otherwise>
      </c:choose>
    </td>
    <td>
    <c:if test="${empty realm.parentName}">
         <%--<c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>"><fmt:message key="consolebase.common.stop"/></a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>"><fmt:message key="consolebase.common.start"/></a>
               </c:otherwise>
             </c:choose>--%>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="editExisting" />
        <portlet:param name="abstractName" value="${realm.abstractName}" />
      </portlet:actionURL>"><fmt:message key="consolebase.common.edit"/></a>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="usage" />
        <portlet:param name="name" value="${realm.name}" />
        <portlet:param name="abstractName" value="${realm.abstractName}" />
      </portlet:actionURL>"><fmt:message key="consolebase.common.usage"/></a>
           <%--<a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>"><fmt:message key="consolebase.common.delete"/></a>--%>
    </c:if>
    </td>
  </tr>
</c:forEach>
</table>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="type" />
            </portlet:actionURL>"><fmt:message key="realmwizard.common.addSecurityRealm" /></a></p>
