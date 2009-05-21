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
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<p><fmt:message key="dbwizard.list.summary"/></p>

<p><fmt:message key="dbwizard.list.hasDatabasePools"/></p>

<table width="100%" class="TableLine" summary="Database Pools - List">
  <tr>
    <th scope="col" class="DarkBackground" align="left"><fmt:message key="dbwizard.common.name"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="dbwizard.list.deployedAs"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="dbwizard.common.state"/></th>
    <th scope="col" class="DarkBackground" align="center"><fmt:message key="dbwizard.common.actions"/></th>
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
    <td class="${backgroundClass}">
      <c:choose>
        <c:when test="${empty pool.parentName}">
          <fmt:message key="dbwizard.list.serverWide" />
        </c:when>
        <c:otherwise>
          ${pool.parentName}  <%-- todo: make this a link to an application portlet --%>
        </c:otherwise>
      </c:choose>
    </td>
    <td class="${backgroundClass}">${pool.stateName}</td>
    <td class="${backgroundClass}">
         <%--<c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>--%>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="editExisting" />
        <portlet:param name="adapterAbstractName" value="${pool.adapterAbstractName}" />
        <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
      </portlet:actionURL>"><fmt:message key="dbwizard.common.edit"/></a>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="usage" />
        <portlet:param name="name" value="${pool.name}" />
        <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
      </portlet:actionURL>"><fmt:message key="dbwizard.list.usage"/></a>
       <a href="<portlet:actionURL portletMode="view">
         <portlet:param name="mode" value="delete" />
         <portlet:param name="name" value="${info.objectName}" />
         <portlet:param name="adapterAbstractName" value="${pool.adapterAbstractName}" />
         <portlet:param name="abstractName" value="${pool.factoryAbstractName}" />
       </portlet:actionURL>"><fmt:message key="dbwizard.common.delete"/></a>
    </td>
  </tr>
</c:forEach>
</table>

<p><fmt:message key="dbwizard.list.createPool"/>:</p>
<ul>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="rdbms" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.usingPoolWizard"/></a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="JBoss 4" />
              <portlet:param name="from" value=" '*-ds.xml' file from the 'jboss4/server/name/deploy' directory" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.importFromJBoss"/></a></li>
  <li><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="startImport" />
              <portlet:param name="importSource" value="WebLogic 8.1" />
              <portlet:param name="from" value="'config.xml' file from the WebLogic domain directory" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.importFromWebLogic"/></a></li>
</ul>
