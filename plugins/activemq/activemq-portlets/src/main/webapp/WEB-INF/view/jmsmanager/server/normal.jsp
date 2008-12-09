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

<p><fmt:message key="jmsmanager.server.normal.title" />:</p>

<!-- Show existing connectors -->
<c:if test="${empty(brokers)}"><fmt:message key="jmsmanager.server.normal.noJMSBrokers"/></c:if>
<c:if test="${!empty(brokers)}">
<table width="50%" class="TableLine" summary="JMS Server Manager - Brokers">

          <tr>
            <th scope="col" class="DarkBackground"><fmt:message key="jmsmanager.common.name"/></th>
            <%--<th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.state"/></th>--%>
<!--
            <th class="DarkBackground" align="center">Actions</th>
-->
          </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="entry" items="${brokers}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">${entry.brokerName}</td>
            <%--<td class="${backgroundClass}">--%>
             <%--<c:choose>--%>
               <%--<c:when test="${entry.broker.stateInstance.name eq 'running'}">--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="stop" />--%>
                 <%--<portlet:param name="objectName" value="${entry.brokerURI}" />--%>
               <%--</portlet:actionURL>">stop</a>--%>
               <%--</c:when>--%>
               <%--<c:otherwise>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="start" />--%>
                 <%--<portlet:param name="objectName" value="${entry.brokerURI}" />--%>
               <%--</portlet:actionURL>">start</a>--%>
               <%--</c:otherwise>--%>
             <%--</c:choose>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="edit" />--%>
                 <%--<portlet:param name="objectName" value="${entry.brokerURI}" />--%>
               <%--</portlet:actionURL>">edit</a>--%>
               <%--<a href="<portlet:actionURL portletMode="view">--%>
                 <%--<portlet:param name="mode" value="delete" />--%>
                 <%--<portlet:param name="objectName" value="${entry.brokerURI}" />--%>
               <%--</portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${entry.brokerName}?');">delete</a>--%>
             <!--</td>-->

          </tr>
</c:forEach>
</table>
</c:if>
<!--
<br />
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
         </portlet:actionURL>"><fmt:message key="jmsmanager.server.normal.addJMSBroker"/></a>
-->
