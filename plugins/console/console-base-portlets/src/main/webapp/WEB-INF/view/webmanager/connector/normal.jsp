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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/><br>

<!-- Show existing connectors -->
<c:forEach var="container" items="${containers}">
      <c:if test="${fn:length(containers) > 1}"><p><b>Connectors for ${container.name}:</b></p></c:if>
<table width="100%" class="TableLine" summary="Network Listeners">
          <tr>
            <th scope="col" class="DarkBackground" align="left"><fmt:message key="consolebase.common.name"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="webmanager.common.protocol"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="webmanager.common.port"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.state"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="consolebase.common.actions"/></th>
            <th class="DarkBackground" align="center"><fmt:message key="consolebase.common.type"/></th>
          </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="info" items="${container.connectors}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
          <tr>
            <td class="${backgroundClass}">${info.uniqueName}</td>
            <td class="${backgroundClass}">${info.protocol}</td>
            <td class="${backgroundClass}">${info.port}</td>
            <td class="${backgroundClass}">${info.stateName}</td>
            <td class="${backgroundClass}">
             <c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>"
                 <c:if test="${info.port eq serverPort}"> onClick="return confirm('Console application will not be available if ${info.uniqueName} is stopped.  Stop ${info.uniqueName}?');"</c:if>>
                 <fmt:message key="consolebase.common.stop"/></a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="restart" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>"
                 <c:if test="${info.port eq serverPort}"> onClick="return confirm('It is recommeded that you restart ${info.uniqueName} while accessing the Console application on a different port if possible. Console application may not be available temporarily on port ${serverPort}, typically 3 to 5 minutes, if ${info.uniqueName} is restarted. Restart ${info.uniqueName}?');"</c:if>>
                 <fmt:message key="consolebase.common.restart"/></a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>"><fmt:message key="consolebase.common.start"/></a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>"><fmt:message key="consolebase.common.edit"/></a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${info.uniqueName}?');"><fmt:message key="consolebase.common.delete"/></a>
            </td>
            <td class="${backgroundClass}">${info.description}</td>
          </tr>
</c:forEach>
</table>

<P><HR><P>
<!-- Links to add new connectors -->
<fmt:message key="webmanager.connector.normal.addNew"/>:
<ul>
<c:forEach var="connectorType" items="${container.connectorTypes}">
<li><a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="connectorType" value="${connectorType.description}" />
           <portlet:param name="managerURI" value="${container.managerURI}" />
           <portlet:param name="containerURI" value="${container.containerURI}" />
         </portlet:actionURL>">${connectorType.description}</a>
</c:forEach>
</ul>

<c:if test="${container.name eq 'Tomcat'}">
   <fmt:message key="webmanager.connector.normal.addNewNote"/>
</c:if>

</c:forEach>
