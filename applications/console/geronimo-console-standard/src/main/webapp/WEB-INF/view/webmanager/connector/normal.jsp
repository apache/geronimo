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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<!-- Show existing connectors -->
<c:choose>
  <c:when test="${empty(containers)}">There are no Web Containers defined</c:when>
  <c:otherwise>
    <c:forEach var="container" items="${containers}">
      <c:if test="${fn:length(containers) > 1}"><p><b>Connectors for ${container.name}:</b></p></c:if>
        <c:choose>
          <c:when test="${empty(container.connectors)}"><p>There are no connectors defined for ${container.name}</p></c:when>
          <c:otherwise>
<table width="100%">
          <tr>
            <th class="DarkBackground" align="left">Name</th>
            <th class="DarkBackground" align="center">Protocol</th>
            <th class="DarkBackground" align="center">Port</th>
            <th class="DarkBackground" align="center">State</th>
            <th class="DarkBackground" align="center">Actions</th>
            <th class="DarkBackground" align="center">Type</th>
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
            <td class="${backgroundClass}">${info.displayName}</td>
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
                 <c:if test="${info.port eq serverPort}"> onClick="return confirm('Console application will not be available if ${info.displayName} is stopped.  Stop ${info.displayName}?');"</c:if>>
                 stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
                 <portlet:param name="managerURI" value="${container.managerURI}" />
                 <portlet:param name="containerURI" value="${container.containerURI}" />
               </portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${info.displayName}?');">delete</a>
            </td>
            <td class="${backgroundClass}">${info.description}</td>
          </tr>
</c:forEach>
</table>
          </c:otherwise>
        </c:choose>


<!-- Links to add new connectors -->
<c:forEach var="protocol" items="${container.protocols}">
<br />
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="protocol" value="${protocol}" />
           <portlet:param name="managerURI" value="${container.managerURI}" />
           <portlet:param name="containerURI" value="${container.containerURI}" />
           <portlet:param name="containerDisplayName" value="${container.name}" />
         </portlet:actionURL>">Add new ${protocol} listener for ${container.name}</a>
</c:forEach>

    </c:forEach>
  </c:otherwise>
</c:choose>
