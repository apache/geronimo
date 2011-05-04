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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<p><fmt:message key="jmsmanager.server.connector.normal.title" />:</p>

<!-- Show existing connectors -->
<c:if test="${!empty(connectors)}">
<table width="50%" class="TableLine" summary="JMS Network Listeners - Connectors">


          <tr>
            <th scope="col" class="DarkBackground"><fmt:message key="jmsmanager.common.name"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.broker" /></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.protocol" /></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.port" /></th>
            <%--<th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.state"/></th>--%>
            <%--<th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.actions"/></th>--%>
          </tr>
          <c:set var="backgroundClass" value='MediumBackground'/>
          <c:forEach var="info" items="${connectors}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr class="${backgroundClass}">
            <td>${info.connectorURI}</td>
            <td>${info.brokerName}</td>
            <td>${info.protocol}</td>
            <td>${info.port}</td>
            <%--<td class="${backgroundClass}">--%>
             <%--<c:choose>--%>
               <%--<c:when test="${info.connector.stateInstance.name eq 'running'}">--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="stop" />--%>
                 <%--<portlet:param name="brokerURI" value="${info.brokerURI}" />--%>
                 <%--<portlet:param name="connectorURI" value="${info.connectorURI}" />--%>
               <%--</portlet:actionURL>"><fmt:message key="jmsmanager.common.stop"/></a>--%>
               <%--</c:when>--%>
               <%--<c:otherwise>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="start" />--%>
                 <%--<portlet:param name="brokerURI" value="${info.brokerURI}" />--%>
                 <%--<portlet:param name="connectorURI" value="${info.connectorURI}" />--%>
               <%--</portlet:actionURL>"><fmt:message key="jmsmanager.common.start"/></a>--%>
               <%--</c:otherwise>--%>
             <%--</c:choose>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="edit" />--%>
                 <%--<portlet:param name="brokerURI" value="${info.brokerURI}" />--%>
                 <%--<portlet:param name="connectorURI" value="${info.connectorURI}" />--%>
               <%--</portlet:actionURL>"><fmt:message key="jmsmanager.common.edit"/></a>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="delete" />--%>
                 <%--<portlet:param name="brokerURI" value="${info.brokerURI}" />--%>
                 <%--<portlet:param name="connectorURI" value="${info.connectorURI}" />--%>
               <%--</portlet:actionURL>" onClick="return confirm('<fmt:message key="jmsmanager.server.connector.normal.confirmDelete"><fmt:param value="${info.connectorURI}"/></fmt:message>');"><fmt:message key="jmsmanager.common.delete"/></a>--%>
             <!--</td>-->
          </tr>
</c:forEach>
</table>
</c:if>

<!-- Links to add new connectors -->
<!--
<c:forEach var="info" items="${brokers}">
<p><fmt:message key="jmsmanager.server.connector.normal.addConnectorTo"><fmt:param value="${info.brokerName}"/></fmt:message>:</p>
<ul>
<c:forEach var="protocol" items="${protocols}">
<li><a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="brokerURI" value="${info.brokerURI}" />
           <portlet:param name="protocol" value="${protocol}" />
         </portlet:actionURL>"><fmt:message key="jmsmanager.server.connector.normal.addNewParaListener" >
         <fmt:param  value="${protocol}"/>
         </fmt:message></a></li>
</c:forEach>
</c:forEach>
-->
